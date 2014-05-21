/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.ORF;

import genblaster.db.DatabaseConnector;
import genblaster.db.sluice.ORFScannerSluice;
import genblaster.genome.DigestedGenome;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class ORFScanner {
    private static final Logger log = Logger.getLogger(ORFScanner.class.getName());
    private ExecutorService executor;
    private ORFScannerCarret[] scannerCarrets;
    private DigestedGenome digestedGenome;
    private final DatabaseConnector databaseConnector;
    private final int minORFLength;
    private final int maxORFLength;
    private final int ORFPool;
    private ORFScannerSluice orfScannerSluice;

    protected ORFScanner(DigestedGenome digestedGenome, int minORFLength, int maxORFLength, int ORFPool, DatabaseConnector databaseConnector) {
        this.digestedGenome = digestedGenome;
        this.scannerCarrets = new ORFScannerCarret[6];
        this.executor = Executors.newFixedThreadPool(this.scannerCarrets.length);
        this.minORFLength = minORFLength;
        this.maxORFLength = maxORFLength;
        this.ORFPool = ORFPool;
        this.databaseConnector = databaseConnector;
    }

    public void startScan() {
        try {
            log.log(Level.INFO, "Starting ORF Scan.");
            this.orfScannerSluice=databaseConnector.newORFScannerSluice();
            List<Future<?>> futures=new ArrayList<>();
            if(!this.orfScannerSluice.ORF_ScansForGenomeAlreadyExist(this.digestedGenome.getName())){
            for (int i = 0; i < 3; i++) {
                this.scannerCarrets[i] = ORFScannerCarret.newPreparedInstance(this.minORFLength, this.maxORFLength, this.ORFPool, i+1,
                        this.digestedGenome.getDirectStrand().substring(i), this.digestedGenome.getGeneticTable().clone(),
                        ORFSaver.newInstance(this.databaseConnector.newORFScannerSluice()), this.digestedGenome.getName());
            }
            log.log(Level.INFO, "Direct strand prepared for the ORF Scan.");
            for (int i = 3; i < 6; i++) {
                this.scannerCarrets[i] = ORFScannerCarret.newPreparedInstance(this.minORFLength, this.maxORFLength, this.ORFPool, 2-i,
                        this.digestedGenome.getComplementaryStrand().substring(i), this.digestedGenome.getGeneticTable().clone(),
                        ORFSaver.newInstance(this.databaseConnector.newORFScannerSluice()), this.digestedGenome.getName());
            }
            log.log(Level.INFO, "Reverse strand prepared for the ORF Scan.");
            for (ORFScannerCarret scannerCarret : this.scannerCarrets) {
                //log.log(Level.INFO, "Launching scanning carret {0}", scannerCarret.getFrame());
                futures.add(this.executor.submit(scannerCarret));
            }
//            for(Future f:futures){
//                f.get();
//            }
            this.executor.shutdown();
            this.executor.awaitTermination(1, TimeUnit.HOURS);
            }else{
                 log.log(Level.INFO, "Genome {0} Has already been scanned for ORFs.", this.digestedGenome.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ORFScanner newInstance(
            DigestedGenome digestedGenome, int minORFLength, int maxORFLength,
            int ORFPool, DatabaseConnector databaseConnector) {
        return new ORFScanner(digestedGenome, minORFLength, maxORFLength, ORFPool, databaseConnector);
    }
}
