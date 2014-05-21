/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.ORF;

/**
 *
 * @author Alexander
 */
public class ORF_DB_Record extends ORF{
    
    private int DB_ID;

    public ORF_DB_Record(int DB_ID, String genomeName, int frame, int AC, String AASEQ,String NUSEQ) {
        super(genomeName, frame, AC, AASEQ, NUSEQ);
        this.DB_ID = DB_ID;
    }
    
}
