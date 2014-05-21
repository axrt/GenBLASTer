/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.db.derby.DerbyDBConnector;

/**
 *
 * @author Alexander
 */
public class GenomeCrossBLAST_Task_Test {

    public static void main(String[] args) {
        try {
            //Database
            DerbyDBConnector derbyDBConnector = DerbyDBConnector.defaultConnectorInstance();
            derbyDBConnector.connectToEmbeddedDatabase();
            System.out.println(System.getProperty("derby.system.home"));

            //GenomeCrossBLAST_Task
            GenomeCrossBLAST_Task genomeCrossBLAST_Task =
                    GenomeCrossBLAST_Task.newDefaultInstance("test_genome", "test_genome",
                    derbyDBConnector.newGenomeCrossBLAST_sluice(), "0.01", true);
            while (genomeCrossBLAST_Task.pollNextBLAST_Task() != null) {
                genomeCrossBLAST_Task.pollNextBLAST_Task();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
