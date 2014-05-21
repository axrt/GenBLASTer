/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice.derby;

import genblaster.NCBI.BLAST.BLAST_Output;
import genblaster.NCBI.BLAST.BLAST_Task;
import genblaster.NCBI.BLAST.CrossBLASTHit;
import genblaster.db.DatabaseConstants;
import genblaster.db.sluice.GenomeCrossBLAST_sluice;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.StatUtils;


/**
 *
 * @author Alexander
 */
public class DerbyGenomeCrossBLAST_sluice extends GenomeCrossBLAST_sluice {

    private static final Logger log = Logger.getLogger(DerbyGenomeCrossBLAST_sluice.class.getName());

    protected DerbyGenomeCrossBLAST_sluice(Connection connection) {
        super(connection);
    }

    @Override
    public LinkedList<Long> assembleORF_ID_List_forAGenome(String genomeName) throws Exception {
        //log.log(Level.INFO, "Loading a list of ORF IDs for {0} genome.", genomeName);
        //Prepare a statement 
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT("
                + DatabaseConstants.ID + ") FROM "
                + DatabaseConstants.GENBLASTER
                + DatabaseConstants.ORF + " WHERE "
                + DatabaseConstants.GENOME_NAME + "=\'" + genomeName + "\'");
        int numberOfORF_Records = 0;
        if (resultSet.next()) {
            numberOfORF_Records = resultSet.getInt(1);
        }
        //Having ensured of the number of ORFs in the database - create an array of a desired capacity
        //log.log(Level.INFO, "The database contains {0} "
        //        + "ORF records for the genome with id {1}.", new Object[]{numberOfORF_Records, genomeName});
        resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.ID
                + " FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF
                + " WHERE " + DatabaseConstants.GENOME_NAME
                + "=\'" + genomeName + "\'"
                + " ORDER BY " + DatabaseConstants.FRAME);
        List<Long> id_list = new ArrayList<>(numberOfORF_Records);
        while (resultSet.next()) {
            id_list.add(resultSet.getLong(1));
        }
        statement.close();
        return new LinkedList<>(id_list);
    }

    @Override
    public boolean databaseContainsResultsForORFOverGenome(long ORF_ID, String targetGenomeName) throws Exception {
        //log.log(Level.INFO, "Getting AC for the ORF ID {0}.", ORF_ID);
        Statement statement = this.connection.createStatement();
        String ORF_AC = "";
        ResultSet resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.AC + ","
                + DatabaseConstants.FRAME + ","
                + DatabaseConstants.GENOME_NAME
                + " FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF
                + " WHERE " + DatabaseConstants.ID + "="
                + ORF_ID);
        if (resultSet.next()) {
            ORF_AC = DatabaseConstants.ORF_perfix + String.valueOf(resultSet.getInt(1) + "_" + String.valueOf(resultSet.getShort(2) + "_" + resultSet.getString(3)));
        }
        //log.log(Level.INFO, "Checking whether ORF {0} has already been BLASTed over genome {1}.", new Object[]{ORF_AC, targetGenomeName});
        resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.ID
                + " FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST
                + " WHERE " + DatabaseConstants.TARGET_GENOME_NAME
                + "=\'" + targetGenomeName + "\'"
                + " AND " + DatabaseConstants.QSEQID
                + "=\'" + ORF_AC.substring(1) + "\'");
        //System.out.println(ORF_AC);
        if (resultSet.next()) {
            statement.close();
            log.log(Level.INFO, "ORF {0} has already been BLASTed over genome {1}.", new Object[]{ORF_AC, targetGenomeName});
            return true;
        } else {
            statement.close();
            return false;
        }
    }

    @Override
    public Set<Long> findOutWhichORFsHaveAllreadyBeenBLASTedAgainstTheTargerGenome(String queryGenomeName, String targetGenomeName) throws Exception {
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT("
                + DatabaseConstants.ID + ") FROM "
                + DatabaseConstants.GENBLASTER
                + DatabaseConstants.BLAST + " WHERE "
                + DatabaseConstants.QUERY_GENOME_NAME + "=\'" + queryGenomeName + "\'"
                + " AND "
                + DatabaseConstants.TARGET_GENOME_NAME + "=\'" + targetGenomeName + "\'");
        int numberOfORF_Records = 0;
        if (resultSet.next()) {
            numberOfORF_Records = resultSet.getInt(1);
        }
        log.log(Level.INFO, "The DataBase contains {0} records for the ORFs from {1} that have been BLASTed agains genome {2}",
                new Object[]{numberOfORF_Records, queryGenomeName, targetGenomeName});
        if (numberOfORF_Records > 0) {
            Set<Long> ORF_IDs = new HashSet<>(numberOfORF_Records);
            resultSet = statement.executeQuery("SELECT "
                    + DatabaseConstants.QUERY_ORF_ID + " FROM "
                    + DatabaseConstants.GENBLASTER
                    + DatabaseConstants.BLAST + " WHERE "
                    + DatabaseConstants.QUERY_GENOME_NAME + "=\'" + queryGenomeName + "\'"
                    + " AND "
                    + DatabaseConstants.TARGET_GENOME_NAME + "=\'" + targetGenomeName + "\'");
            while (resultSet.next()) {
                ORF_IDs.add(resultSet.getLong(1));
            }
            statement.close();
            return ORF_IDs;
        } else {
            statement.close();
            return new HashSet<>(); //Probably unnessessary, could have returned null and input a check on the other side
        }
    }

    @Override //TODO this is a total piece of crap, delete it later
    public long[] findOutWhichORFsNeedToBeBLASTedAgainstTheTargerGenome(String queryGenomeName, String targetGenomeName) throws Exception {
        log.log(Level.INFO, "Searching for BLAST results for {0} over {1}.",
                new Object[]{queryGenomeName, targetGenomeName});
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT("
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF+'.'
                + DatabaseConstants.ID + ") FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST
                + " RIGHT OUTER JOIN "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF
                + " ON "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF +'.'+ DatabaseConstants.ID
                + " = "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST +'.'+ DatabaseConstants.QUERY_ORF_ID
                + " WHERE "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST +'.'+ DatabaseConstants.ID + " IS NULL"
                + " AND "
                + "\'"+queryGenomeName+"\'"
                + " = " 
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST +'.'+ DatabaseConstants.QUERY_GENOME_NAME
                + " AND "
                + "\'"+targetGenomeName+"\'"
                + " = " 
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST +'.'+ DatabaseConstants.TARGET_GENOME_NAME
                );
        int numberOfORF_Records = 0;
        if (resultSet.next()) {
            numberOfORF_Records = resultSet.getInt(1);
        }
        log.log(Level.INFO, "The DataBase contains {0} records for the ORFs from {1} that have been BLASTed agains genome {2}",
                new Object[]{numberOfORF_Records, queryGenomeName, targetGenomeName});
        if (numberOfORF_Records > 0) {
            long [] ids_to_BLAST=new long[numberOfORF_Records];
            resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF+'.'
                + DatabaseConstants.ID + " FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST
                + " RIGHT OUTER JOIN "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF
                + " ON "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF +'.'+ DatabaseConstants.ID
                + " = "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST +'.'+ DatabaseConstants.QUERY_ORF_ID
                + " WHERE "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST +'.'+ DatabaseConstants.ID + " IS NULL"
                + " AND "
                + "\'"+queryGenomeName+"\'"
                + " = " 
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST +'.'+ DatabaseConstants.QUERY_GENOME_NAME
                + " AND "
                + "\'"+targetGenomeName+"\'"
                + " = " 
                + DatabaseConstants.GENBLASTER + DatabaseConstants.BLAST +'.'+ DatabaseConstants.TARGET_GENOME_NAME
                );
            for (int i=0;resultSet.next();i++) {
                ids_to_BLAST[i]=resultSet.getLong(1);
            }
            statement.close();
            return ids_to_BLAST;
        } else {
            statement.close();
            return new long[0]; //Probably unnessessary, could have returned null and input a check on the other side
        }
    }

    @Override
    public BLAST_Task assembleBLAST_Task(long ORF_ID, String targetGenomeName, String Evalue) throws Exception {
        //log.log(Level.INFO, "Loading a new task: BLAST ORF with ID {0} over {1}.", new Object[]{ORF_ID,targetGenomeName});
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.AC + ","
                + DatabaseConstants.FRAME + ","
                + DatabaseConstants.GENOME_NAME + ","
                + DatabaseConstants.AASEQ
                + " FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF
                + " WHERE " + DatabaseConstants.ID + "="
                + ORF_ID);
        BLAST_Task blast_task = null;
        if (resultSet.next()) {
            String AC = DatabaseConstants.ORF_perfix + String.valueOf(resultSet.getInt(1) + "_" + String.valueOf(resultSet.getShort(2) + "_" + resultSet.getString(3)));
            blast_task = BLAST_Task.newDefaultInstance(
                    ORF_ID, AC,
                    resultSet.getString(4), resultSet.getString(3), targetGenomeName, Evalue);
        }
        statement.close();
        return blast_task;
    }

    public static DerbyGenomeCrossBLAST_sluice newInstance(Connection connection) {
        return new DerbyGenomeCrossBLAST_sluice(connection);
    }

    @Override
    public List<BLAST_Output> assembleBLAST_OutputForA_PairOfGenomes(String queryGenomeName, String targetGenomeName) throws Exception {
        log.log(Level.INFO, "Loading BLAST results for genome {0} over {1}.", new Object[]{queryGenomeName,targetGenomeName});
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT("
                + DatabaseConstants.ID + ") FROM "
                + DatabaseConstants.GENBLASTER
                + DatabaseConstants.BLAST + " WHERE "
                + DatabaseConstants.QUERY_GENOME_NAME + "=\'" + queryGenomeName + "\'"
                + " AND "
                + DatabaseConstants.TARGET_GENOME_NAME + "=\'" + targetGenomeName + "\'"
                + " AND "
                + DatabaseConstants.SSEQID + " IS NOT NULL");
        int numberOfORF_queryRecords = 0;
        if (resultSet.next()) {
            numberOfORF_queryRecords = resultSet.getInt(1);
        }
        List<BLAST_Output> queryBLASToutputs = new ArrayList<>(numberOfORF_queryRecords);
        resultSet = statement.executeQuery("SELECT *"
                + " FROM "
                + DatabaseConstants.GENBLASTER
                + DatabaseConstants.BLAST + " WHERE "
                + DatabaseConstants.QUERY_GENOME_NAME + "=\'" + queryGenomeName + "\'"
                + " AND "
                + DatabaseConstants.TARGET_GENOME_NAME + "=\'" + targetGenomeName + "\'"
                + " AND "
                + DatabaseConstants.SSEQID + " IS NOT NULL");
        BLAST_Output blast_output;
        while (resultSet.next()) {
            blast_output = BLAST_Output.loadSelfFromParameters(
                    resultSet.getString(2), resultSet.getString(3), resultSet.getLong(5), resultSet.getString(4),
                    resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9),
                    resultSet.getString(10), resultSet.getString(11), resultSet.getString(12), resultSet.getString(13),
                    resultSet.getString(14), resultSet.getString(15), resultSet.getString(16),
                    resultSet.getString(17), resultSet.getString(18));
            queryBLASToutputs.add(blast_output);
        }
        log.log(Level.INFO, "BLAST results for genome {0} over {1} loaded from database.", new Object[]{queryGenomeName,targetGenomeName});
        return queryBLASToutputs;
    }

    @Override
    public List<CrossBLASTHit> calculateCrossBLAST_Hits(List<BLAST_Output> queryBLASToutputs, List<BLAST_Output> targetBLASToutputs) throws Exception {
        List<CrossBLASTHit> crossBLAST_Hits = new ArrayList<>();
//        HashMap<String, BLAST_Output> queryHits = new HashMap<>(queryBLASToutputs.size());
//        for (BLAST_Output bo : queryBLASToutputs) {
//            queryHits.put(bo.getQseqid(), bo);
//        }
        HashMap<String, BLAST_Output> targetHits = new HashMap<>(targetBLASToutputs.size());
        for (BLAST_Output bo : targetBLASToutputs) {
            targetHits.put(bo.getQseqid(), bo);
        }
        ////
        BLAST_Output blast_output;
        log.log(Level.INFO, "Browsing from crossBLAST Hits within {0} <-> {1} BLAST results.", new Object[]{queryBLASToutputs.get(0).getQeryGenomeName(), targetBLASToutputs.get(0).getQeryGenomeName()});
        for (BLAST_Output bo : queryBLASToutputs) {
            if (targetHits.containsKey(bo.getSseqid())) {
                blast_output = targetHits.get(bo.getSseqid());
                if (blast_output.getSseqid().equals(bo.getQseqid())) {
                    crossBLAST_Hits.add(
                            CrossBLASTHit.newInstanceFromParameters(
                            bo.getQeryGenomeName(), blast_output.getQeryGenomeName(), bo.getQseqid(),
                            blast_output.getQseqid(),
                            (Double.parseDouble(bo.getSlen()) + Double.parseDouble(blast_output.getSlen())) / 2,
                            (Double.parseDouble(bo.getQstart()) + Double.parseDouble(blast_output.getQstart())) / 2,
                            (Double.parseDouble(bo.getQend()) + Double.parseDouble(blast_output.getQend())) / 2,
                            (Double.parseDouble(bo.getSstart()) + Double.parseDouble(blast_output.getSstart())) / 2,
                            (Double.parseDouble(bo.getSend()) + Double.parseDouble(blast_output.getSend())) / 2,
                            (Double.parseDouble(bo.getPident()) + Double.parseDouble(blast_output.getPident())) / 2,
                            (Double.parseDouble(bo.getNident()) + Double.parseDouble(blast_output.getNident())) / 2,
                            (Double.parseDouble(bo.getMismatch()) + Double.parseDouble(blast_output.getMismatch())) / 2,
                            (Double.parseDouble(bo.getPositive()) + Double.parseDouble(blast_output.getPositive())) / 2,
                            (Double.parseDouble(bo.getGapopen()) + Integer.parseInt(blast_output.getGapopen())) / 2,
                            (Double.parseDouble(bo.getGaps()) + Integer.parseInt(blast_output.getGaps())) / 2));
                }
            }
        }
        return crossBLAST_Hits;
    }

    @Override
    public double calculateTaxonomicDistance(List<CrossBLASTHit> crossBLAST_Hits, distance_type dt) throws Exception {
        log.log(Level.INFO, "{0} <-> {1} pair has {2} crossBLASTs.", new Object[]{crossBLAST_Hits.get(0).getA_genomeName(), crossBLAST_Hits.get(0).getB_genomeName(), crossBLAST_Hits.size()});
        double distance;
        switch (dt) {
            default:
            case average: {
                double totalMomentus = 0;
                //double totalLenght = 0;
                double totalMismatch = 0;
                double totalGapopen = 0;
                for (CrossBLASTHit cbh : crossBLAST_Hits) {
                    //totalLenght += cbh.getLength();
                    totalGapopen += cbh.getGapopen();
                    totalMismatch += cbh.getMismatch();
                    totalMomentus += cbh.getPident() * cbh.getLength() / 100;
                }
                distance = (1 - (totalMomentus / (totalMomentus + totalGapopen + totalMismatch))) * 100;
                log.log(Level.INFO, "{0} <-> {1} pair has average distance of {2}.", new Object[]{crossBLAST_Hits.get(0).getA_genomeName(), crossBLAST_Hits.get(0).getB_genomeName(), distance});
                break;
            }
            case median: {
                double[] medianSubset = new double[crossBLAST_Hits.size()];
                int i = 0;
                double momentus = 0;
                //double length = 0;
                double mismatch = 0;
                double gapopen = 0;
                for (CrossBLASTHit cbh : crossBLAST_Hits) {
                    //length = cbh.getLength();
                    gapopen = cbh.getGapopen();
                    mismatch = cbh.getMismatch();
                    momentus = cbh.getPident() * cbh.getLength() / 100;
                    medianSubset[i] = (1 - (momentus / (momentus + gapopen + mismatch))) * 100;
                    i++;
                }
                distance = StatUtils.percentile(medianSubset, 50);
                log.log(Level.INFO, "{0} <-> {1} pair has median distance of {2}.", new Object[]{crossBLAST_Hits.get(0).getA_genomeName(), crossBLAST_Hits.get(0).getB_genomeName(), distance});
                break;
            }
        }
        return distance;
    }

    @Override
    public double calculateTaxonomicDistance(String queryGenomeName, String targetGenomeName, distance_type dt) throws Exception {

        List<BLAST_Output> queryBLASToutputs = this.assembleBLAST_OutputForA_PairOfGenomes(queryGenomeName, targetGenomeName);
        List<BLAST_Output> targetBLASToutputs = this.assembleBLAST_OutputForA_PairOfGenomes(targetGenomeName, queryGenomeName);
        List<CrossBLASTHit> crossBLAST_Hits = this.calculateCrossBLAST_Hits(queryBLASToutputs, targetBLASToutputs);
        ////
        return this.calculateTaxonomicDistance(crossBLAST_Hits, dt);
    }

    @Override
    public int saveTaxonomicDistance(String a_genome_name, String b_genome_name, double taxonomicDistance) throws Exception {
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.ID + " FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.DISTANCE
                + " WHERE "
                + DatabaseConstants.A_GENOME_NAME + "=\'" + a_genome_name + "\'"
                + " AND "
                + DatabaseConstants.B_GENOME_NAME + "=\'" + b_genome_name + "\'");
        int DIST_ID = 0;
        if (resultSet.next()) {
            DIST_ID = resultSet.getInt(1);
            statement.execute("UPDATE "
                    + DatabaseConstants.GENBLASTER + DatabaseConstants.DISTANCE
                    + " SET "
                    + DatabaseConstants.DISTANCE + "=" + taxonomicDistance
                    + " WHERE "
                    + DatabaseConstants.A_GENOME_NAME + "=\'" + a_genome_name + "\'"
                    + " AND "
                    + DatabaseConstants.B_GENOME_NAME + "=\'" + b_genome_name + "\'");//TODO get ridd of the result set here
            //Not to forget to close the statement
            statement.close();
        } else {
            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "INSERT INTO "
                    + DatabaseConstants.GENBLASTER
                    + DatabaseConstants.DISTANCE + "("
                    + DatabaseConstants.A_GENOME_NAME + ","
                    + DatabaseConstants.B_GENOME_NAME + ","
                    + DatabaseConstants.DISTANCE + ")"
                    + "VALUES(?,?,?)", new String[]{DatabaseConstants.ID});
            preparedStatement.setString(1, a_genome_name);
            preparedStatement.setString(2, b_genome_name);
            preparedStatement.setDouble(3, taxonomicDistance);
            // ResultSet resultSet;
            preparedStatement.execute();
            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                DIST_ID = resultSet.getInt(1);
            }
            //Not to forget to close the statement
            preparedStatement.close();
        }
        // log.log(Level.INFO, "Distance saved successfully.");
        return DIST_ID;
    }
}
