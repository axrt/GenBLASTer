/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.genome;

import genblaster.genome.BasePairsTable;

/**
 *
 * @author Alexander
 */
public class BasePairsTable_Test {
        public static final String testNucleotide1 = "AAATGGGCF";
        public static final String testNucleotide2 = testNucleotide1.toLowerCase();
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(testNucleotide1);
        sb.append('\n');
        for (char a : testNucleotide1.toCharArray()) {
            sb.append(BasePairsTable.complimetaryNucleotide(a));
        }
        sb.append('\n');
        sb.append(testNucleotide1);
        sb.append('\n');
        for (char a : testNucleotide2.toCharArray()) {
            sb.append(BasePairsTable.complimetaryNucleotide(a));
        }
        System.out.println(new String(sb));
    }
}
