/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.genome;

import genblaster.genome.DigestedGenome;
import java.io.File;

/**
 *
 * @author Alexander
 */
public class DigestedGenome_Test {
    public static final File testGenome=new File("C:\\Users\\Alexander\\Documents\\MFBT project\\GenBLASTer\\testGenome1.txt");
    public static void main(String[] args) {
        try{
    DigestedGenome digestedGenome= DigestedGenome.newInstanceFromAGenomeFile("test", testGenome,null);
    System.out.println(digestedGenome.getName());
    System.out.println(digestedGenome.getDirectStrand());
    System.out.println(digestedGenome.getComplementaryStrand());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
