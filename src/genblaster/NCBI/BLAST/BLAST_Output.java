/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.io.aFileReader;
import java.io.File;
import java.io.Serializable;

/**
 *
 * @author Alexander
 */
public class BLAST_Output implements Serializable{

    private String status;
    private String qeryGenomeName;
    private String targetGenomeName;
    private long queryORF_ID;
    private String qseqid;
    private String sseqid;
    private String evalue;
    private String slen;
    private String qstart;
    private String qend;
    private String sstart;
    private String send;
    private String pident;
    private String nident;
    private String mismatch;
    private String positive;
    private String gapopen;
    private String gaps;
    private aFileReader fileReader;

    protected BLAST_Output(String qeryGenomeName, String targetGenomeName, long queryORF_ID, String qseqid, String sseqid, String evalue, String slen, String qstart, String qend, String sstart, String send, String pident, String nident, String mismatch, String positive, String gapopen, String gaps) {
        this.qeryGenomeName = qeryGenomeName;
        this.targetGenomeName = targetGenomeName;
        this.queryORF_ID=queryORF_ID;
        this.qseqid = qseqid;
        this.sseqid = sseqid;
        this.evalue = evalue;
        this.slen = slen;
        this.qstart = qstart;
        this.qend = qend;
        this.sstart = sstart;
        this.send = send;
        this.pident = pident;
        this.nident = nident;
        this.mismatch = mismatch;
        this.positive = positive;
        this.gapopen = gapopen;
        this.gaps = gaps;
        this.fileReader=  aFileReader.newInstance();
    }

    protected BLAST_Output() {
        this.fileReader=  aFileReader.newInstance();
    }

    public String getQseqid() {
        return qseqid;
    }

    public String getSseqid() {
        return sseqid;
    }

    public String getEvalue() {
        return evalue;
    }

    public String getGapopen() {
        return gapopen;
    }

    public String getGaps() {
        return gaps;
    }

    public String getMismatch() {
        return mismatch;
    }

    public String getNident() {
        return nident;
    }

    public String getPident() {
        return pident;
    }

    public String getPositive() {
        return positive;
    }

    public String getQend() {
        return qend;
    }

    public String getQeryGenomeName() {
        return qeryGenomeName;
    }

    public String getQstart() {
        return qstart;
    }

    public String getSend() {
        return send;
    }

    public String getSlen() {
        return slen;
    }

    public String getSstart() {
        return sstart;
    }

    public String getTargetGenomeName() {
        return targetGenomeName;
    }

    public String getStatus() {
        return status;
    }

    public long getQueryORF_ID() {
        return queryORF_ID;
    }

    public static BLAST_Output loadSelfFromFile(String qeryGenomeName, String targetGenomeName,long queryORF_ID, String QSEQID, File file) throws Exception {
        BLAST_Output blast_output = new BLAST_Output();
        String output = blast_output.fileReader.ReadTextFile(file);
        if (output.length() > 0) {
            String[] splitter = output.split("\t");
            blast_output.qeryGenomeName = qeryGenomeName;
            blast_output.targetGenomeName = targetGenomeName;
            blast_output.queryORF_ID=queryORF_ID;
            blast_output.qseqid = splitter[0];
            blast_output.sseqid = splitter[1];
            blast_output.evalue = splitter[2];
            blast_output.slen = splitter[3];
            blast_output.qstart = splitter[4];
            blast_output.qend = splitter[5];
            blast_output.sstart = splitter[6];
            blast_output.send = splitter[7];
            blast_output.pident = splitter[8];
            blast_output.nident = splitter[9];
            blast_output.mismatch = splitter[10];
            blast_output.positive = splitter[11];
            blast_output.gapopen = splitter[12];
            blast_output.gaps = splitter[13];
            blast_output.status = "fine";
        } else {
            blast_output.qeryGenomeName = qeryGenomeName;
            blast_output.targetGenomeName = targetGenomeName;
            blast_output.qseqid = QSEQID;
            blast_output.queryORF_ID=queryORF_ID;
            blast_output.sseqid = Genblaster_Constants.empty_plug;
            blast_output.evalue = Genblaster_Constants.empty_plug;
            blast_output.slen = Genblaster_Constants.empty_plug;
            blast_output.qstart = Genblaster_Constants.empty_plug;
            blast_output.qend = Genblaster_Constants.empty_plug;
            blast_output.sstart = Genblaster_Constants.empty_plug;
            blast_output.send = Genblaster_Constants.empty_plug;
            blast_output.pident = Genblaster_Constants.empty_plug;
            blast_output.nident = Genblaster_Constants.empty_plug;
            blast_output.mismatch = Genblaster_Constants.empty_plug;
            blast_output.positive = Genblaster_Constants.empty_plug;
            blast_output.gapopen = Genblaster_Constants.empty_plug;
            blast_output.gaps = Genblaster_Constants.empty_plug;
            blast_output.status = "empty";
        }
        return blast_output;
    }
    public static BLAST_Output loadSelfFromParameters(
            String qeryGenomeName, String targetGenomeName, 
            long queryORF_ID, String qseqid, String sseqid,
            String evalue, String slen, String qstart, 
            String qend, String sstart, String send, 
            String pident, String nident, String mismatch, 
            String positive, String gapopen, String gaps){
        return new BLAST_Output(qeryGenomeName, targetGenomeName, 
                queryORF_ID, qseqid, sseqid, evalue, slen, 
                qstart, qend, sstart, send, pident, nident, 
                mismatch, positive, gapopen, gaps);
    }
}
