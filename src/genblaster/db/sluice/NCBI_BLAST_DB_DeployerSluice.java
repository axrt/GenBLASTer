/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice;

import java.sql.Connection;

/**
 *
 * @author Alexander
 */
public abstract class NCBI_BLAST_DB_DeployerSluice {
    
    protected Connection connection;

    protected NCBI_BLAST_DB_DeployerSluice(Connection connection) {
        this.connection = connection;
    }
    public abstract String collectORFBaseForGenome(String genomeName) throws Exception;
    public abstract String collectCDSBaseForGenome(String genomeName) throws Exception;
}
