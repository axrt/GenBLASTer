/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

/**
 *
 * @author Alexander
 */
public class CrossBLASTHit {

    private String a_genomeName;
    private String b_genomeName;
    private String qseqid;
    private String sseqid;
    private double slen;
    
    private double qstart;
    private double qend;
    private double sstart;
    private double send;
    
    private double pident;
    private double nident;
    private double mismatch;
    private double positive;
    private double gapopen;
    private double gaps;
    private double length;

    protected CrossBLASTHit(
            String a_genomeName, String b_genomeName,
            String qseqid, String sseqid, double slen,
            double qstart,double qend,double sstart,double send,
            double pident, double nident, double mismatch,
            double positive, double gapopen, double gaps) {
        this.a_genomeName = a_genomeName;
        this.b_genomeName = b_genomeName;
        this.qseqid = qseqid;
        this.sseqid = sseqid;
        this.slen = slen;
        this.pident = pident;
        this.nident = nident;
        this.mismatch = mismatch;
        this.positive = positive;
        this.gapopen = gapopen;
        this.gaps = gaps;
        this.qstart=qstart;
        this.qend=qend;
        this.sstart=sstart;
        this.send=send;
        this.length = (this.qend - this.qstart) + 1 + ((this.send - this.sstart) - (this.qend - this.qstart) + this.gaps) / 2;
    }
    
    public double getLength() {
        return length;
    }

    public String getA_genomeName() {
        return a_genomeName;
    }

    public String getB_genomeName() {
        return b_genomeName;
    }

    public double getGapopen() {
        return gapopen;
    }

    public CrossBLASTHit() {
    }

    public double getGaps() {
        return gaps;
    }

    public double getMismatch() {
        return mismatch;
    }

    public double getNident() {
        return nident;
    }

    public double getPident() {
        return pident;
    }

    public double getPositive() {
        return positive;
    }

    public String getQseqid() {
        return qseqid;
    }

    public double getSlen() {
        return slen;
    }

    public String getSseqid() {
        return sseqid;
    }

    public static CrossBLASTHit newInstanceFromParameters(
            String a_genomeName, String b_genomeName,
            String qseqid, String sseqid, double slen,
            double qstart,double qend,double sstart,double send,
            double pident, double nident, double mismatch,
            double positive, double gapopen, double gaps) {
        return new CrossBLASTHit(a_genomeName, b_genomeName, 
                qseqid, sseqid, slen, qstart, qend, sstart, 
                send, pident, nident, mismatch, positive, 
                gapopen, gaps);
    }
}
