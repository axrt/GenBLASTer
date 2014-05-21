/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.db.sluice.GenomeCrossBLAST_sluice;
import genblaster.io.aFileSaver;
import genblaster.util.SystemUtil;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class GenomeCrossBLAST_Task {

    private enum genome_type {

        a_genome,
        b_genome;
    }
    private double taxonomicDistance;
    private boolean taxonomicDistanceProcessed;
    private boolean taxonomicDistanceSaved;
    private boolean needsCheck;
    private String a_genome_name;
    private String b_genome_name;
    private Queue<Long> a_genome_orf_list;
    private Queue<Long> b_genome_orf_list;
    private Queue<Long> ab_genome_orf_list;
    private HashMap<Long, genome_type> orf_id_genome_name_pairs;
    private GenomeCrossBLAST_sluice genomeCrossBLAST_sluice;
    private String Evalue;
    private int deligatedBLAST_Tasks;
    private int databaseDistanceID;
    private aFileSaver fileSaver;
    private static final Logger log = Logger.getLogger(GenomeCrossBLAST_Task.class.getName());

    protected GenomeCrossBLAST_Task(String a_genome_name, String b_genome_name,
            GenomeCrossBLAST_sluice genomeCrossBLAST_sluice, String Evalue, boolean needsCheck) {
        this.Evalue = Evalue;
        this.genomeCrossBLAST_sluice = genomeCrossBLAST_sluice;
        this.a_genome_name = a_genome_name;
        this.b_genome_name = b_genome_name;
        this.needsCheck = needsCheck;
        this.assembleBLAST_lists();
        this.deligatedBLAST_Tasks = 0;
        this.taxonomicDistanceProcessed = false;
        this.taxonomicDistanceSaved = false;
        this.fileSaver = aFileSaver.newInstance();
    }

    private void assembleBLAST_lists() {
        try {
            if (this.needsCheck) {
            this.a_genome_orf_list = this.genomeCrossBLAST_sluice.assembleORF_ID_List_forAGenome(this.a_genome_name);
            log.log(Level.INFO, "Genome {0} contains {1} ORF records.", new Object[]{this.a_genome_name, this.a_genome_orf_list.size()});
            Set<Long> a_already_BLASTed = this.genomeCrossBLAST_sluice.findOutWhichORFsHaveAllreadyBeenBLASTedAgainstTheTargerGenome(a_genome_name, b_genome_name);
            log.log(Level.INFO, "{0} ORFs have been BLASTed against {1}.", new Object[]{a_already_BLASTed.size(), this.b_genome_name});
            this.a_genome_orf_list.removeAll(a_already_BLASTed);
//                this.a_genome_orf_list = new LinkedList<>();
//                long[] a_genome_orf_list_ids = this.genomeCrossBLAST_sluice.findOutWhichORFsNeedToBeBLASTedAgainstTheTargerGenome(this.a_genome_name,this.b_genome_name);
//                log.log(Level.INFO, "{0} ORFs from genome {1} to BLAST against {2}.", new Object[]{a_genome_orf_list_ids.length,this.a_genome_name, this.b_genome_name});
//                for (long l : a_genome_orf_list_ids) {
//                    this.a_genome_orf_list.add(l);
//                }
                log.log(Level.INFO, "{0} ORFs left to BLAST against {1}.", new Object[]{this.a_genome_orf_list.size(), this.b_genome_name});
            this.b_genome_orf_list = this.genomeCrossBLAST_sluice.assembleORF_ID_List_forAGenome(this.b_genome_name);
            log.log(Level.INFO, "Genome {0} contains {1} ORF records.", new Object[]{this.b_genome_name, this.b_genome_orf_list.size()});
            Set<Long> b_already_BLASTed = this.genomeCrossBLAST_sluice.findOutWhichORFsHaveAllreadyBeenBLASTedAgainstTheTargerGenome(b_genome_name, a_genome_name);
            log.log(Level.INFO, "{0} ORFs have been BLASTed against {1}.", new Object[]{b_already_BLASTed.size(), this.a_genome_name});
            this.b_genome_orf_list.removeAll(b_already_BLASTed);

//                this.b_genome_orf_list = new LinkedList<>();
//                long[] b_genome_orf_list_ids = this.genomeCrossBLAST_sluice.findOutWhichORFsNeedToBeBLASTedAgainstTheTargerGenome(this.b_genome_name, this.a_genome_name);
//                log.log(Level.INFO, "{0} ORFs from genome {1} left to BLAST against {2}.", new Object[]{b_genome_orf_list_ids.length,this.b_genome_name, this.a_genome_name});
//                for (long l : b_genome_orf_list_ids) {
//                    this.b_genome_orf_list.add(l);
//                }
                log.log(Level.INFO, "{0} ORFs left to BLAST against {1}.", new Object[]{this.b_genome_orf_list.size(), this.a_genome_name});
                this.ab_genome_orf_list = new LinkedList<>();
                this.ab_genome_orf_list.addAll(this.a_genome_orf_list);
                this.ab_genome_orf_list.addAll(this.b_genome_orf_list);
                this.orf_id_genome_name_pairs = new HashMap<>();
                for (Long l : this.a_genome_orf_list) {
                    this.orf_id_genome_name_pairs.put(l, genome_type.b_genome);
                }
                for (Long l : this.b_genome_orf_list) {
                    this.orf_id_genome_name_pairs.put(l, genome_type.a_genome);
                }
            } else {
                this.ab_genome_orf_list = new LinkedList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized BLAST_Task pollNextBLAST_Task() throws Exception {
        while (!this.ab_genome_orf_list.isEmpty()) {//TODO change for 'if' case the presearch works
            long id = this.ab_genome_orf_list.poll();
            genome_type gt = this.orf_id_genome_name_pairs.get(id);
            String genometype = null;
            switch (gt) {
                case a_genome: {
                    genometype = this.a_genome_name;
                    break;
                }
                case b_genome: {
                    genometype = this.b_genome_name;
                    break;
                }
            }
//            if (!this.genomeCrossBLAST_sluice.databaseContainsResultsForORFOverGenome(id, genometype)) {
            this.deligatedBLAST_Tasks++;
            return this.genomeCrossBLAST_sluice.assembleBLAST_Task(id, genometype, this.Evalue);
//            }
        }
        notify();
        return null;
    }

    public synchronized void reportTaskDone() {
        this.deligatedBLAST_Tasks--;
        notify();
    }

    public synchronized void abandonTaskWithID(long orf_id) {
        this.ab_genome_orf_list.add(orf_id);
    }

    public synchronized boolean accomplished() {
        try {
            while (!this.ab_genome_orf_list.isEmpty() || this.deligatedBLAST_Tasks != 0) {
                //log.log(Level.INFO, "List of BLAST tasks is empty: {0}, the number of deligated tasks:{1}.",new Object[]{this.ab_genome_orf_list.isEmpty(),this.deligatedBLAST_Tasks});
                wait();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        return true;
    }

    public double calculateTaxonomicDistance(GenomeCrossBLAST_sluice.distance_type dt) throws Exception {
        //TODO input checks for another distance in the database with the same pair of genomes
        if (!this.taxonomicDistanceProcessed) {
            List<BLAST_Output> queryBLASToutputs = this.genomeCrossBLAST_sluice.assembleBLAST_OutputForA_PairOfGenomes(a_genome_name, b_genome_name);
            this.saveBLAST_OutputToAFile(queryBLASToutputs);
            List<BLAST_Output> targetBLASToutputs = this.genomeCrossBLAST_sluice.assembleBLAST_OutputForA_PairOfGenomes(b_genome_name, a_genome_name);
            this.saveBLAST_OutputToAFile(targetBLASToutputs);
            List<CrossBLASTHit> crossBLAST_Hits = this.genomeCrossBLAST_sluice.calculateCrossBLAST_Hits(queryBLASToutputs, targetBLASToutputs);
            this.saveCrossBLAST_OutputToAFile(crossBLAST_Hits);
            this.taxonomicDistance = this.genomeCrossBLAST_sluice.calculateTaxonomicDistance(
                    crossBLAST_Hits, dt);
            this.taxonomicDistanceProcessed = true;
        }
        return this.taxonomicDistance;
    }

    public int saveTaxonomicDistance() throws Exception {
        //TODO input checks for another distance in the database with the same pair of genomes
        if (!this.taxonomicDistanceSaved) {
            this.databaseDistanceID = this.genomeCrossBLAST_sluice.saveTaxonomicDistance(this.a_genome_name, this.b_genome_name, this.taxonomicDistance);
            this.taxonomicDistanceSaved = true;
        }
        return this.databaseDistanceID;
    }

    private void saveBLAST_OutputToAFile(List<BLAST_Output> BLASToutputs) {
        final String dir = System.getProperty(Genblaster_Constants.BLAST_RESULT_folder) + SystemUtil.SysFS;
        final String filename = BLASToutputs.get(0).getQeryGenomeName() + "_vs_"
                + BLASToutputs.get(0).getTargetGenomeName() + Genblaster_Constants.blast_result_file_ext;
        StringBuilder[] stringBuilders = new StringBuilder[BLASToutputs.size()];
        for (int i = 0; i < BLASToutputs.size(); i++) {
            stringBuilders[i] = new StringBuilder();
            stringBuilders[i].append(BLASToutputs.get(i).getQeryGenomeName());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getTargetGenomeName());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getQseqid());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getSseqid());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getEvalue());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getSlen());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getQstart());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getQend());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getSstart());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getSend());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getPident());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getNident());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getMismatch());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getPositive());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getGapopen());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(BLASToutputs.get(i).getGaps());
            stringBuilders[i].append('\t');
            stringBuilders[i].append('\n');
        }
        int length = 0;
        for (StringBuilder stringBuilder : stringBuilders) {
            length += stringBuilder.length();
        }
        StringBuilder superBuilder = new StringBuilder(length);
        for (StringBuilder stringBuilder : stringBuilders) {
            superBuilder.append(stringBuilder);
        }
        try {
            this.fileSaver.SaveTextFile(new String(superBuilder), new File(dir + filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCrossBLAST_OutputToAFile(List<CrossBLASTHit> crossBLAST_Hits) {
        final String dir = System.getProperty(Genblaster_Constants.BLAST_RESULT_folder) + SystemUtil.SysFS;
        final String filename = crossBLAST_Hits.get(0).getA_genomeName() + "_X_"
                + crossBLAST_Hits.get(0).getB_genomeName() + Genblaster_Constants.cross_blast_result_file_ext;
        StringBuilder[] stringBuilders = new StringBuilder[crossBLAST_Hits.size()];
        for (int i = 0; i < crossBLAST_Hits.size(); i++) {
            //TODO do smth to make the driver control the precision
            stringBuilders[i] = new StringBuilder();
            stringBuilders[i].append(crossBLAST_Hits.get(i).getA_genomeName());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(crossBLAST_Hits.get(i).getB_genomeName());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(crossBLAST_Hits.get(i).getQseqid());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(crossBLAST_Hits.get(i).getSseqid());
            stringBuilders[i].append('\t');
            stringBuilders[i].append(String.valueOf(String.format("%.7g%n", crossBLAST_Hits.get(i).getSlen()).trim()));
            stringBuilders[i].append('\t');
            stringBuilders[i].append(String.valueOf(String.format("%.7g%n", crossBLAST_Hits.get(i).getPident()).trim()));
            stringBuilders[i].append('\t');
            stringBuilders[i].append(String.valueOf(String.format("%.7g%n", crossBLAST_Hits.get(i).getNident()).trim()));
            stringBuilders[i].append('\t');
            stringBuilders[i].append(String.valueOf(String.format("%.7g%n", crossBLAST_Hits.get(i).getMismatch()).trim()));
            stringBuilders[i].append('\t');
            stringBuilders[i].append(String.valueOf(String.format("%.7g%n", crossBLAST_Hits.get(i).getPositive()).trim()));
            stringBuilders[i].append('\t');
            stringBuilders[i].append(String.valueOf(String.format("%.7g%n", crossBLAST_Hits.get(i).getGapopen()).trim()));
            stringBuilders[i].append('\t');
            stringBuilders[i].append(String.valueOf(String.format("%.7g%n", crossBLAST_Hits.get(i).getGaps()).trim()));
            stringBuilders[i].append('\t');
            stringBuilders[i].append(String.valueOf(String.format("%.7g%n", crossBLAST_Hits.get(i).getLength()).trim()));
            stringBuilders[i].append('\t');
            stringBuilders[i].append('\n');
        }
        int length = 0;
        for (StringBuilder stringBuilder : stringBuilders) {
            length += stringBuilder.length();
        }
        StringBuilder superBuilder = new StringBuilder(length);
        for (StringBuilder stringBuilder : stringBuilders) {
            superBuilder.append(stringBuilder);
        }
        try {
            this.fileSaver.SaveTextFile(new String(superBuilder), new File(dir + filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GenomeCrossBLAST_Task newDefaultInstance(String a_genome_name, String b_genome_name,
            GenomeCrossBLAST_sluice genomeCrossBLAST_sluice, String Evalue, boolean needsCheck) {
        //Correct this
        return new GenomeCrossBLAST_Task(a_genome_name, b_genome_name, genomeCrossBLAST_sluice, Evalue, needsCheck);
    }
}
