/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice;

import genblaster.db.derby.DerbyDBConnector;
import genblaster.db.sluice.derby.DerbyNCBI_BLAST_DB_DeployerSluice;

/**
 *
 * @author Alexander
 */
public class DerbyNCBI_BLAST_DB_DeployerSluice_Test {
    public static void main(String[]args){
        try{
            //Database
            DerbyDBConnector derbyDBConnector = DerbyDBConnector.defaultConnectorInstance();
            derbyDBConnector.connectToEmbeddedDatabase();
            
            //Test
            NCBI_BLAST_DB_DeployerSluice sluice=derbyDBConnector.newNCBI_BLAST_DB_DeployerSluice();
            System.out.println("conllection started");
            String s=sluice.collectORFBaseForGenome("test_genome");
            System.out.println(s.substring(0, 500));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
