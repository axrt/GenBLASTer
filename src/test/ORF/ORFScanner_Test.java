/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.ORF;

import genblaster.ORF.GeneticTable;
import genblaster.ORF.ORFScanner;
import genblaster.db.derby.DerbyDBConnector;
import genblaster.genome.DigestedGenome;
import java.io.File;

/**
 *
 * @author Alexander
 */
public class ORFScanner_Test {

    public static void main(String[] args) {
        try {
            //Database
            DerbyDBConnector derbyDBConnector = DerbyDBConnector.defaultConnectorInstance();
            derbyDBConnector.setDerbyDBDefaultSystemDir();
            derbyDBConnector.connectToEmbeddedDatabase();
            derbyDBConnector.databasePassesChecks();

            System.out.println(System.getProperty("derby.system.home"));
            //Genome
            File genomFile = new File("C:\\Users\\Alexander\\Documents\\MFBT project\\GenBLASTer\\a_macrogynus.fasta");
            DigestedGenome digestedGenome = DigestedGenome.newInstanceFromAGenomeFile("test_genome", genomFile, GeneticTable.Standard());

            //ORF Scan
            ORFScanner orfScanner = ORFScanner.newInstance(digestedGenome, 150, 100000, 250, derbyDBConnector);
            orfScanner.startScan();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
