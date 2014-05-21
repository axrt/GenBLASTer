/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice.derby;

import genblaster.db.DatabaseConstants;
import genblaster.db.sluice.NCBI_BLAST_DB_DeployerSluice;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class DerbyNCBI_BLAST_DB_DeployerSluice extends NCBI_BLAST_DB_DeployerSluice {

    private static final Logger log = Logger.getLogger(DerbyNCBI_BLAST_DB_DeployerSluice.class.getName());

    protected DerbyNCBI_BLAST_DB_DeployerSluice(Connection connection) {
        super(connection);
    }

    @Override
    public String collectORFBaseForGenome(String genomeName) throws Exception {
        //log.log(Level.INFO, "Loading ORF-base for {0} genome.", genomeName);
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
        //log.log(Level.INFO, "The database contains {0} "
        //        + "ORF records for the genome with id {1}.", new Object[]{numberOfORF_Records,genomeName});
        resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.AC + ","
                + DatabaseConstants.FRAME + ","
                + DatabaseConstants.AASEQ
                + " FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF
                + " WHERE " + DatabaseConstants.GENOME_NAME
                + "=\'" + genomeName + "\'"
                + " ORDER BY " + DatabaseConstants.FRAME);
        StringBuilder[] ORFrecordBuilders = new StringBuilder[numberOfORF_Records];
        int ORFrecordBuilders_counter = 0;
        while (resultSet.next()) {
            ORFrecordBuilders[ORFrecordBuilders_counter] = new StringBuilder();
            ORFrecordBuilders[ORFrecordBuilders_counter].append(DatabaseConstants.ORF_perfix);
            ORFrecordBuilders[ORFrecordBuilders_counter].append(resultSet.getInt(1));
            ORFrecordBuilders[ORFrecordBuilders_counter].append('_');
            ORFrecordBuilders[ORFrecordBuilders_counter].append(resultSet.getInt(2));
            ORFrecordBuilders[ORFrecordBuilders_counter].append('_');
            ORFrecordBuilders[ORFrecordBuilders_counter].append(genomeName);
            ORFrecordBuilders[ORFrecordBuilders_counter].append('\n');
            ORFrecordBuilders[ORFrecordBuilders_counter].append(resultSet.getString(3));
            ORFrecordBuilders[ORFrecordBuilders_counter].append('\n');
            ORFrecordBuilders_counter++;
        }
        StringBuilder baseBuilder = new StringBuilder();
        for (StringBuilder stringBuilder : ORFrecordBuilders) {
            baseBuilder.append(stringBuilder);
        }
        statement.close();
        return new String(baseBuilder);
    }

    @Override
    public String collectCDSBaseForGenome(String genomeName) throws Exception {
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
        resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.AC + ","
                + DatabaseConstants.FRAME + ","
                + DatabaseConstants.NUSEQ
                + " FROM "
                + DatabaseConstants.GENBLASTER + DatabaseConstants.ORF
                + " WHERE " + DatabaseConstants.GENOME_NAME
                + "=\'" + genomeName + "\'"
                + " ORDER BY " + DatabaseConstants.FRAME);
        StringBuilder[] ORFrecordBuilders = new StringBuilder[numberOfORF_Records];
        int ORFrecordBuilders_counter = 0;
        while (resultSet.next()) {
            ORFrecordBuilders[ORFrecordBuilders_counter] = new StringBuilder();
            ORFrecordBuilders[ORFrecordBuilders_counter].append(DatabaseConstants.ORF_perfix);
            ORFrecordBuilders[ORFrecordBuilders_counter].append(resultSet.getInt(1));
            ORFrecordBuilders[ORFrecordBuilders_counter].append('_');
            ORFrecordBuilders[ORFrecordBuilders_counter].append(resultSet.getInt(2));
            ORFrecordBuilders[ORFrecordBuilders_counter].append('_');
            ORFrecordBuilders[ORFrecordBuilders_counter].append(genomeName);
            ORFrecordBuilders[ORFrecordBuilders_counter].append('\n');
            ORFrecordBuilders[ORFrecordBuilders_counter].append(resultSet.getString(3));
            ORFrecordBuilders[ORFrecordBuilders_counter].append('\n');
            ORFrecordBuilders_counter++;
        }
        StringBuilder baseBuilder = new StringBuilder();
        for (StringBuilder stringBuilder : ORFrecordBuilders) {
            baseBuilder.append(stringBuilder);
        }
        statement.close();
        return new String(baseBuilder);
    }

    public static DerbyNCBI_BLAST_DB_DeployerSluice newInstance(Connection connection) {
        return new DerbyNCBI_BLAST_DB_DeployerSluice(connection);
    }
}
