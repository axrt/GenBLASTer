/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.io.aFileSaver;
import genblaster.util.SystemUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;

/**
 *
 * @author Alexander
 */
public class BLAST_Task implements Runnable,Serializable {

    private long queryID;
    private String name;
    private String queryAC;
    private String querySequence;
    private String queryGenomeName;
    private String targetGenomeName;
    private File preparedQueryFile;
    private String output;
    private BLAST_Output blast_output;
    private boolean accomplished;
    private String E_value;
    private aFileSaver fileSaver;
    private long timeout;

    protected BLAST_Task(long queryID, String queryAC, String querySequence,
            String queryGenomeName, String genomeName, String E_value) {
        this.queryID = queryID;
        this.queryGenomeName = queryGenomeName;
        this.E_value = E_value;
        this.queryAC = queryAC;
        this.querySequence = querySequence;
        this.targetGenomeName = genomeName;
        this.accomplished = false;
        this.fileSaver = aFileSaver.newInstance();
        this.name = '\"' + this.queryAC.substring(1) + " over " + this.targetGenomeName + '\"';
        this.timeout = Genblaster_Constants.blast_task_timeout;
    }

    private void prepareQueryFile() throws Exception {

        // log.log(Level.INFO, "Preparing BLAST Task {0}.", this.name);
        //Prepare the filename
        this.preparedQueryFile = new File(System.getProperty(Genblaster_Constants.BLAST_TASK_folder)
                + SystemUtil.SysFS + this.queryAC.substring(1) + Genblaster_Constants.dot_fasta);
        //Save the query file to disk
        this.fileSaver.SaveTextFile(this.queryAC + '\n' + this.querySequence, this.preparedQueryFile);
        //
        this.output = System.getProperty(Genblaster_Constants.BLAST_RESULT_folder) + SystemUtil.SysFS
                + this.queryAC.substring(1) + "_" + this.targetGenomeName + Genblaster_Constants.dot_blast;
    }

    private void BLAST() throws Exception {
        String blastp = System.getProperty(Genblaster_Constants.blastp_system_home);//TODO Should probably be faster as static
        String db = System.getProperty(Genblaster_Constants.BLAST_DB_folder)//TODO Should probably be faster as static
                + SystemUtil.SysFS + this.targetGenomeName + Genblaster_Constants.dot_db;

        String[] command = new String[19];
        command[0] = blastp;
        command[1] = "-outfmt";
        command[2] = "6 qseqid sseqid evalue slen qstart qend sstart send pident nident mismatch positive gapopen gaps";
        command[3] = "-query";
        command[4] = this.preparedQueryFile.getPath();
        command[5] = "-db";
        command[6] = db;
        command[7] = "-seg";
        command[8] = "yes";
        command[9] = "-evalue";
        command[10] = this.E_value;
        command[11] = "-word_size";
        command[12] = String.valueOf(2);
        command[13] = "-num_descriptions";
        command[14] = String.valueOf(1);
        command[15] = "-num_alignments";
        command[16] = String.valueOf(1);
        command[17] = "-out";
        command[18] = this.output;
//        for (String s:command){
//            System.out.println(s);
//        }
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        String s;
        //BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        //Read the output from the command
        //log.log(Level.INFO, "Standard output for {0} BLAST Task.", this.name);
//            while ((s = stdInput.readLine()) != null) {
//               // log.log(Level.INFO, "{0} on BLAST Task {1}.", new Object[]{s, this.name});
//            }
        //Read any errors from the attempted command
        //log.log(Level.INFO, "Error output for {0} BLAST Task.", this.name);
        while ((s = stdError.readLine()) != null) {
            if (s.contains("Cannot memory map file")) {
                //log.log(Level.INFO, "Stall reBLAST on {0}.", this.name);
                this.BLAST();
            }
            //log.log(Level.INFO, "{0} on BLAST Task {1}.", new Object[]{s, this.name});
        }
    }

    private void storeResults() throws Exception {
        this.blast_output = BLAST_Output.loadSelfFromFile(this.queryGenomeName, this.targetGenomeName,this.queryID,this.queryAC.substring(1), new File(this.output));
    }

    private void cleanup() {
        //Delete the input file
        this.preparedQueryFile.delete();
        //Delete the output file
        new File(this.output).delete();
    }

    @Override
    public void run() {
        try {
            this.prepareQueryFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.BLAST();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.storeResults();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.cleanup();
            this.accomplished = true;
            synchronized (this) {
                notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized BLAST_Output getBlast_output() {
        if (!this.accomplished) {
            try {
                //log.log(Level.INFO, "Awaiting BLAST Task {0} to finish.", this.name);
                wait(this.timeout);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            if (!this.accomplished) {
                return null;
            }
        }
        return blast_output;
    }

    public long getQueryID() {
        return queryID;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getQueryGenomeName() {
        return queryGenomeName;
    }

    public String getTargetGenomeName() {
        return targetGenomeName;
    }

    public static BLAST_Task newDefaultInstance(long queryID, String queryAC, String querySequence, String queryGenomeName,
            String targetGenomeName, String E_value) {
        BLAST_Task blast_Task = new BLAST_Task(queryID, queryAC, querySequence, queryGenomeName, targetGenomeName, E_value);
        return blast_Task;
    }
}
