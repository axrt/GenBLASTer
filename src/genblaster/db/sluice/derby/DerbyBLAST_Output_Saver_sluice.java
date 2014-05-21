/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice.derby;

import genblaster.NCBI.BLAST.BLAST_Output;
import genblaster.db.DatabaseConstants;
import genblaster.db.sluice.BLAST_Output_Saver_sluice;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class DerbyBLAST_Output_Saver_sluice extends BLAST_Output_Saver_sluice {

    private static final Logger log = Logger.getLogger(DerbyBLAST_Output_Saver_sluice.class.getName());
    private static long number_of_saved_null_BLAST_outputs=0;
    private static long number_of_saved_nonull_BLAST_outputs=0;
    private static  long total_number_of_BLAST_results_saved=0;

    protected DerbyBLAST_Output_Saver_sluice(Connection connection) {
        super(connection);
    }

    @Override
    public void saveBLAST_Output_to_DataBase(BLAST_Output blast_output) throws Exception {
        //TODO substitute the 100.. 1000 etc with a centrally referenced smth
        if(!blast_output.getStatus().equals("empty")){
            DerbyBLAST_Output_Saver_sluice.number_of_saved_nonull_BLAST_outputs++;
            if(DerbyBLAST_Output_Saver_sluice.number_of_saved_nonull_BLAST_outputs%100==0){
                log.log(Level.INFO, "Another 100 non-null BLAST results saved.");
                log.log(Level.INFO, "Total number of non-null results for this session is {0}.",DerbyBLAST_Output_Saver_sluice.number_of_saved_nonull_BLAST_outputs);
            }
            //log.log(Level.INFO, "Saving BLAST result {0} ORF.", blast_output.getQseqid() + "_" + blast_output.getSseqid());
        }else{
            DerbyBLAST_Output_Saver_sluice.number_of_saved_null_BLAST_outputs++;
            if(DerbyBLAST_Output_Saver_sluice.number_of_saved_null_BLAST_outputs%1000==0){
                log.log(Level.INFO, "Another 1000 null BLAST results saved.");
                log.log(Level.INFO, "Total number of null results for this session is {0}.",DerbyBLAST_Output_Saver_sluice.number_of_saved_null_BLAST_outputs);
            }
        }
        DerbyBLAST_Output_Saver_sluice.total_number_of_BLAST_results_saved++;
        if(DerbyBLAST_Output_Saver_sluice.total_number_of_BLAST_results_saved%5000==0){
            log.log(Level.INFO, ">>> Total number of BLAST results saved in this session is {0}.",DerbyBLAST_Output_Saver_sluice.total_number_of_BLAST_results_saved);
        }
       // 
        PreparedStatement preparedStatement = this.connection.prepareStatement(
                "INSERT INTO "
                + DatabaseConstants.GENBLASTER
                + DatabaseConstants.BLAST + "("
                + DatabaseConstants.QUERY_GENOME_NAME + ","
                + DatabaseConstants.TARGET_GENOME_NAME + ","
                + DatabaseConstants.QUERY_ORF_ID + ","
                + DatabaseConstants.QSEQID + ","
                + DatabaseConstants.SSEQID + ","
                + DatabaseConstants.EVALUE + ","
                + DatabaseConstants.SLEN + ","
                + DatabaseConstants.QSTART + ","
                + DatabaseConstants.QEND + ","
                + DatabaseConstants.SSTART + ","
                + DatabaseConstants.SEND + ","
                + DatabaseConstants.PIDENT + ","
                + DatabaseConstants.NIDENT + ","
                + DatabaseConstants.MISMATCH + ","
                + DatabaseConstants.POSITIVE + ","
                + DatabaseConstants.GAPOPEN + ","
                + DatabaseConstants.GAPS + ")"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new String[]{DatabaseConstants.ID});
        preparedStatement.setString(1, blast_output.getQeryGenomeName());
        preparedStatement.setString(2, blast_output.getTargetGenomeName());
        preparedStatement.setLong(3, blast_output.getQueryORF_ID());
        preparedStatement.setString(4, blast_output.getQseqid());
        preparedStatement.setString(5, blast_output.getSseqid());
        preparedStatement.setString(6, blast_output.getEvalue());
        preparedStatement.setString(7, blast_output.getSlen());
        preparedStatement.setString(8, blast_output.getQstart());
        preparedStatement.setString(9, blast_output.getQend());
        preparedStatement.setString(10, blast_output.getSstart());
        preparedStatement.setString(11, blast_output.getSend());
        preparedStatement.setString(12, blast_output.getPident());
        preparedStatement.setString(13, blast_output.getNident());
        preparedStatement.setString(14, blast_output.getMismatch());
        preparedStatement.setString(15, blast_output.getPositive());
        preparedStatement.setString(16, blast_output.getGapopen());
        preparedStatement.setString(17, blast_output.getGaps());
        preparedStatement.execute();
        //log.log(Level.INFO, "BLAST result {0} ORF saved successfully.", blast_output.getQseqid() + "_" + blast_output.getSseqid());
        preparedStatement.close();
    }

    public static DerbyBLAST_Output_Saver_sluice newInstance(Connection connection) {
        return new DerbyBLAST_Output_Saver_sluice(connection);
    }
}
