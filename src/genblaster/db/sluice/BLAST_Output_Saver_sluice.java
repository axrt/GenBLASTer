/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice;

import genblaster.NCBI.BLAST.BLAST_Output;
import java.sql.Connection;

/**
 *
 * @author Alexander
 */
public abstract class BLAST_Output_Saver_sluice {

    protected Connection connection;

    protected BLAST_Output_Saver_sluice(Connection connection) {
        this.connection = connection;
    }
    
    public abstract void saveBLAST_Output_to_DataBase(BLAST_Output blast_output) throws Exception;
}
