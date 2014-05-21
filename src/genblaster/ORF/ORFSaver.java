/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.ORF;

import genblaster.db.sluice.ORFScannerSluice;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class ORFSaver implements Runnable {

    private static final Logger log = Logger.getLogger(ORFSaver.class.getName());
    private ExecutorService executor;
    private ORFScannerSluice orfScannerSluice;
    private ORF[] orfs;
    private boolean saving = false;

    public synchronized void saveORFs(ORF[] orfs) throws Exception {
        if (saving) {
            log.log(Level.INFO, "Waiting for the ORFSaver.");
            wait();
        }
        this.orfs =new ORF[orfs.length];
        System.arraycopy(orfs, 0, this.orfs, 0, this.orfs.length);
        this.saving = true;
        this.executor.execute(this);
    }
    public synchronized void switchOFF()throws Exception{
        if(saving){
            log.log(Level.INFO, "Waiting for the ORFSaver to terminate.");
            wait();
        }
        this.executor.shutdown();
    }

    protected ORFSaver(ORFScannerSluice orfScannerSluice) {
        this.orfScannerSluice = orfScannerSluice;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        try {
            this.orfScannerSluice.saveORFs_ToDataBase(this.orfs);
            this.saving = false;
            synchronized (this) {
                notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ORFSaver newInstance(ORFScannerSluice orfScannerSluice) {
        return new ORFSaver(orfScannerSluice);
    }
}