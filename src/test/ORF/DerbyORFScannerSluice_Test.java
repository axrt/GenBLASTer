/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.ORF;

import genblaster.ORF.ORF;
import genblaster.db.derby.DerbyDBConnector;
import genblaster.db.sluice.ORFScannerSluice;

/**
 *
 * @author Alexander
 */
public class DerbyORFScannerSluice_Test {
    public static void main (String[]args){
        DerbyDBConnector derbyDBConnector= DerbyDBConnector.defaultConnectorInstance();
        ORFScannerSluice derbyORFScannerSluice;
        ORF orf;
        try{
        derbyDBConnector.connectToEmbeddedDatabase();
        orf = new ORF("test", 1, 111, "AAGG"," ");
        derbyORFScannerSluice=derbyDBConnector.newORFScannerSluice();
        System.out.println(derbyORFScannerSluice.saveAnORF_ToDataBase(orf));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
