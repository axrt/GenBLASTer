/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.ORF;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class ORFScannerCarret implements Runnable {

    private static final Logger log = Logger.getLogger(ORFScannerCarret.class.getName());
    private String sequenceToScan;
    private boolean scanning;
    private ORFSaver orfSaver;
    private GeneticTable geneticTable;
    private final int minORFLength;
    private final int maxORFLength;
    private final int ORFPool;
    private final int frame;
    private final String genomeName;

    protected ORFScannerCarret(int minORFLength, int maxORFLength, int ORFPool, int frame, String genomeName) {
        this.minORFLength = minORFLength;
        this.maxORFLength = maxORFLength;
        this.ORFPool = ORFPool;
        this.frame = frame;
        this.genomeName = genomeName;
    }

    public int getFrame() {
        return frame;
    }

    public synchronized void prepareToScan(String sequenceToScan, GeneticTable geneticTable, ORFSaver orfSaver) throws Exception {
        if (this.scanning) {
            wait();
        }
        this.sequenceToScan = sequenceToScan;
        this.geneticTable = geneticTable;
        this.orfSaver = orfSaver;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Launching ORF Scan on carret {0}.", this.frame);
        ORF[] orfs = new ORF[this.ORFPool];
        String current_codon;
        StringBuilder AASEQ_Builder = new StringBuilder(this.maxORFLength);
        StringBuilder NUSEQ_Builder = new StringBuilder(this.maxORFLength * 3);
        int length = (this.sequenceToScan.length() / 3 * 3) - 3;
        int current_orf = 0;
        int orfNumber = 0;
        for (int i = 0; i < length; i += 3) {
            current_codon = this.sequenceToScan.substring(i, i + 3);
            //System.out.println(this.sequenceToScan.substring(i, i + 3));
            char translatedCodon = this.geneticTable.translateCodon(current_codon);
            if (translatedCodon != '*') {
                //System.out.println(translatedCodon);
                AASEQ_Builder.append(translatedCodon);
                NUSEQ_Builder.append(current_codon);
            } else {
//                System.out.println(stringBuilder.length());
//                System.out.println(new String(stringBuilder));
                if (AASEQ_Builder.length() >= this.minORFLength) {
//               System.out.println("+");
                    if (AASEQ_Builder.length() <= this.maxORFLength) {
//                        System.out.println("++");
                        orfs[current_orf] = new ORF(this.genomeName, this.frame, orfNumber,
                                new String(AASEQ_Builder), new String(NUSEQ_Builder));
                        orfNumber++;
                        current_orf++;
                        if (current_orf >= this.ORFPool) {
                            log.log(Level.INFO, "Saving ORFs form pool.");
                            try {
                                this.orfSaver.saveORFs(orfs);
                                current_orf = 0;
                                orfs=new ORF[this.ORFPool];
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                AASEQ_Builder.delete(0, AASEQ_Builder.length());
                NUSEQ_Builder.delete(0, NUSEQ_Builder.length());
            }
        }
        //Save the last of it
        try {
            //log.log(Level.INFO, "Saving ORFs form pool.");
            int numberOfNonNullOrfs = 0;
            for (ORF orf : orfs) {
                if (orf != null) {
                    numberOfNonNullOrfs++;
                }
            }
            ORF[] leftOverORFs = new ORF[numberOfNonNullOrfs];
            int i = 0;
            for (ORF orf : orfs) {
                if (orf != null) {
                    leftOverORFs[i] = orf;
                }
                i++;
            }
            this.orfSaver.saveORFs(leftOverORFs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.orfSaver.switchOFF();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ORFScannerCarret newEmptyInstance(int minORFLength, int maxORFLength, int ORFPool, int frame, String genomeName) {
        return new ORFScannerCarret(minORFLength, maxORFLength, ORFPool, frame, genomeName);
    }

    public static ORFScannerCarret newPreparedInstance(int minORFLength, int maxORFLength, int ORFPool, int frame, String sequenceToScan, GeneticTable geneticTable, ORFSaver orfSaver, String genomeName) {
        ORFScannerCarret orfScannerCarret = new ORFScannerCarret(minORFLength, maxORFLength, ORFPool, frame, genomeName);
        try {
            orfScannerCarret.prepareToScan(sequenceToScan, geneticTable, orfSaver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orfScannerCarret;
    }
}
