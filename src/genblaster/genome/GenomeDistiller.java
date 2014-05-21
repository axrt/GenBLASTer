/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.genome;

/**
 *
 * @author Alexander
 */
public class GenomeDistiller {

    public static String destileGenome(String genome) {
        //System.out.println(genome.length());
        genome = genome.replaceAll(" ", "");//Get ridd of all the spaces
        //System.out.println(genome.length());
        genome = genome.replaceAll(String.valueOf('\n'), "");//Get ridd of all the new lines
        //System.out.println(genome.length());
        genome = genome.replaceAll(String.valueOf('\t'), "");//Get ridd of all the tabs
        //System.out.println(genome.length());
        return genome;
    }
}
