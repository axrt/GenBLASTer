/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.db.sluice.GenomeCrossBLAST_sluice;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class GenomeCrossBLAST_Task_Charger implements Runnable {

    private Queue<GenomeCrossBLAST_Task> genomeCrossBLAST_Tasks;
    private ExecutorService executor;
    private List<GenomeCrossBLAST_Task> shotOFF_Genomes;
    private List<Integer> crossBLAST_Distance_ids;
    private static final Logger log = Logger.getLogger(GenomeCrossBLAST_Task_Charger.class.getName());

    protected GenomeCrossBLAST_Task_Charger(Queue<GenomeCrossBLAST_Task> genomeCrossBLAST_Tasks) {
        this.genomeCrossBLAST_Tasks = genomeCrossBLAST_Tasks;
        this.executor = Executors.newSingleThreadExecutor();
        this.shotOFF_Genomes = new ArrayList<>(this.genomeCrossBLAST_Tasks.size());
        this.crossBLAST_Distance_ids = new ArrayList<>(this.genomeCrossBLAST_Tasks.size());
    }

    public List<Integer> getCrossBLAST_Distance_ids() {
        return crossBLAST_Distance_ids;
    }

    public void launch() {
        this.executor.execute(this);
    }

    @Override
    public void run() {
        while (this.genomeCrossBLAST_Tasks.peek() != null && this.genomeCrossBLAST_Tasks.peek().accomplished()) {
            synchronized (this) {
                this.shotOFF_Genomes.add(this.genomeCrossBLAST_Tasks.poll());
                log.log(Level.INFO, "CrossBLAST task changed.");
            }
        }
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public void calculateTaxonomicDistances(GenomeCrossBLAST_sluice.distance_type dt) {
        for (GenomeCrossBLAST_Task gct : this.shotOFF_Genomes) {
            try {
                gct.calculateTaxonomicDistance(dt);
                this.crossBLAST_Distance_ids.add(gct.saveTaxonomicDistance());
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public synchronized GenomeCrossBLAST_Task getTopTask() {
        return this.genomeCrossBLAST_Tasks.peek();
    }

    public static GenomeCrossBLAST_Task_Charger newInstance(Queue<GenomeCrossBLAST_Task> genomeCrossBLAST_Tasks) {
        return new GenomeCrossBLAST_Task_Charger(genomeCrossBLAST_Tasks);
    }
}
