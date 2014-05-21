/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice;

import genblaster.ORF.ORF;
import java.sql.Connection;

/**
 *
 * @author Alexander
 */
public abstract class ORFScannerSluice {
    
    protected Connection connection;

    protected ORFScannerSluice(Connection connection) {
        this.connection = connection;
    }
    
    public abstract int saveAnORF_ToDataBase(ORF orf) throws Exception;
    public abstract void saveORFs_ToDataBase(ORF[] orfs) throws Exception;
    public abstract boolean ORF_ScansForGenomeAlreadyExist(String genomeName) throws Exception;
}
