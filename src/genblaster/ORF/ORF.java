/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.ORF;

/**
 *
 * @author Alexander
 */
public class ORF {
    
    private String genomeName;
    private int frame;
    private int AC;
    private String AASEQ;
    private String NUSEQ;
    public ORF(String genomeName, int frame, int AC, String AASEQ, String NUSEQ) {
        this.genomeName = genomeName;
        this.frame = frame;
        this.AC = AC;
        this.AASEQ = AASEQ;
        this.NUSEQ=NUSEQ;
    }

    public int getAC() {
        return AC;
    }

    public String getAASEQ() {
        return AASEQ;
    }

    public String getNUSEQ() {
        return NUSEQ;
    }

    public int getFrame() {
        return frame;
    }

    public String getGenomeName() {
        return genomeName;
    }
}
