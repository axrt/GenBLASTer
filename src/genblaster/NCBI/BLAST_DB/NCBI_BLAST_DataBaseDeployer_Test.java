/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST_DB;

import genblaster.db.derby.DerbyDBConnector;

/**
 *
 * @author Alexander
 */
public class NCBI_BLAST_DataBaseDeployer_Test {

    public static void main(String[] args) {
        try {
            //Database
            DerbyDBConnector derbyDBConnector = DerbyDBConnector.defaultConnectorInstance();
            derbyDBConnector.connectToEmbeddedDatabase();
            System.out.println(System.getProperty("derby.system.home"));
            
            //
            NCBI_BLAST_DataBaseDeployer deployer= NCBI_BLAST_DataBaseDeployer.defaultInstance(2, derbyDBConnector);
            deployer.deployBLAST_DataBaseForGenome("test_genome");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
