/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice.derby;

import genblaster.ORF.ORF;
import genblaster.db.DatabaseConstants;
import genblaster.db.sluice.ORFScannerSluice;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class DerbyORF_ScannerSluice extends ORFScannerSluice {

    private static final Logger log = Logger.getLogger(DerbyORF_ScannerSluice.class.getName());

    protected DerbyORF_ScannerSluice(Connection connection) {
        super(connection);
    }

    @Override
    public int saveAnORF_ToDataBase(ORF orf) throws Exception {
        //TODO should not be an int, but a long. However, does not seem to be usefull at all
       // log.log(Level.INFO, "Saving {0} ORF.", orf.getAC());
        PreparedStatement preparedStatement = this.connection.prepareStatement(
                "INSERT INTO "
                + DatabaseConstants.GENBLASTER
                + DatabaseConstants.ORF + "("
                + DatabaseConstants.GENOME_NAME + ","
                + DatabaseConstants.FRAME + ","
                + DatabaseConstants.AC + ","
                + DatabaseConstants.AASEQ + ","
                + DatabaseConstants.NUSEQ + ")"
                + "VALUES(?,?,?,?,?)", new String[]{DatabaseConstants.ID});
        preparedStatement.setString(1, orf.getGenomeName());
        preparedStatement.setInt(2, orf.getFrame());
        preparedStatement.setInt(3, orf.getAC());
        preparedStatement.setString(4, orf.getAASEQ());
        preparedStatement.setString(5, orf.getNUSEQ());
        ResultSet resultSet;
        int ORF_ID = 0;
        preparedStatement.execute();
        resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()) {
            ORF_ID = resultSet.getInt(1);
        }
        //Not to forget to close the statement
        preparedStatement.close();
        // log.log(Level.INFO, "ORF saved successfully.");
        return ORF_ID;
    }

    @Override
    public boolean ORF_ScansForGenomeAlreadyExist(String genomeName) throws Exception {
        Statement statement = this.connection.createStatement();
        ResultSet resultSet=statement.executeQuery("SELECT "
                + DatabaseConstants.ID+" FROM "
                + DatabaseConstants.GENBLASTER+DatabaseConstants.ORF
                + " WHERE "+ DatabaseConstants.GENOME_NAME
                +"=\'"+genomeName+"\'");
        if(resultSet.next()){
            statement.close();
            return true;
        }else return false;
    }

    @Override
    public void saveORFs_ToDataBase(ORF[] orfs) throws Exception {
        PreparedStatement preparedStatement = this.connection.prepareStatement(
                "INSERT INTO "
                + DatabaseConstants.GENBLASTER
                + DatabaseConstants.ORF + "("
                + DatabaseConstants.GENOME_NAME + ","
                + DatabaseConstants.FRAME + ","
                + DatabaseConstants.AC + ","
                + DatabaseConstants.AASEQ + ","
                + DatabaseConstants.NUSEQ + ")"
                + "VALUES(?,?,?,?,?)", new String[]{DatabaseConstants.ID});
        for (ORF orf : orfs) {
            //log.log(Level.INFO, "Saving {0} ORF.", orf.getAC());
            preparedStatement.setString(1, orf.getGenomeName());
            preparedStatement.setInt(2, orf.getFrame());
            preparedStatement.setInt(3, orf.getAC());
            preparedStatement.setString(4, orf.getAASEQ());
            preparedStatement.setString(5, orf.getNUSEQ());
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
        // log.log(Level.INFO, "ORFs saved successfully.");
    }

    public static DerbyORF_ScannerSluice newInstance(Connection connection) {
        return new DerbyORF_ScannerSluice(connection);
    }
}
