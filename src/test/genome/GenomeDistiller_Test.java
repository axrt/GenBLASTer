/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.genome;

import genblaster.genome.GenomeDistiller;

/**
 *
 * @author Alexander
 */
public class GenomeDistiller_Test {
    public static final String testNucleotide3=BasePairsTable_Test.testNucleotide1+"\n \tnothing";
    public static void main (String[]args){
         System.out.println(BasePairsTable_Test.testNucleotide1);
         System.out.println(testNucleotide3);
         System.out.println(GenomeDistiller.destileGenome(testNucleotide3));
    }
    
}
