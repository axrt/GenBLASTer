/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.genome;

import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class BasePairsTable {
    private static final Logger log = Logger.getLogger(BasePairsTable.class.getName());
    private static final char a = 'a';
    private static final char t = 't';
    private static final char g = 'g';
    private static final char c = 'c';
    private static final char n = 'n';
    private static final char A = 'A';
    private static final char T = 'T';
    private static final char G = 'G';
    private static final char C = 'C';
    private static final char N = 'N';

    public static char complimetaryNucleotide(char nucleotide) {
        switch (nucleotide) {
            case a: {
                return t;
            }
            case t: {
                return a;
            }
            case c: {
                return g;
            }
            case g: {
                return c;
            }
            case A: {
                return T;
            }
            case T: {
                return A;
            }
            case C: {
                return G;
            }
            case G: {
                return C;
            }
            default:
                return nucleotide;
        }
    }
}
