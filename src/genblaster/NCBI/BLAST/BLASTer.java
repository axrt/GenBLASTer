/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.db.DatabaseConnector;
import genblaster.properties.jaxb.ClusterSatellite;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.pool.BaseObjectPool;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 *
 * @author Alexander
 */
public class BLASTer {

    private ExecutorService BLAST_launchpads;
    private ExecutorService BLAST_satellites;
    private int numberOfSimultaniousBLASTs;
    private ExecutorService BLAST_monitors;
    private DatabaseConnector databaseConnector;
    private GenomeCrossBLAST_Task_Charger charger;
    private boolean accomplished;
    private int blast_task_buffer_size;
    private Queue<BLAST_Task> blast_task_buffer;
    private static final Logger log = Logger.getLogger(BLASTer.class.getName());
    private List<ClusterSatellite> clusterSatellites;
    private List<BLAST_TaskExternalDelegator> BLAST_TaskExternalDelegator_s;
    private StackObjectPool<BLAST_Output_Saver> saverStackObjectPool;

    protected BLASTer(DatabaseConnector databaseConnector, GenomeCrossBLAST_Task_Charger charger,
            int numberOfSimultaniousBLASTs, List<ClusterSatellite> clusterSatellites) {
        this.accomplished = false;
        this.charger = charger;
        this.numberOfSimultaniousBLASTs = numberOfSimultaniousBLASTs;
        this.databaseConnector = databaseConnector;
        this.BLAST_launchpads = Executors.newFixedThreadPool(numberOfSimultaniousBLASTs);
        //TODO this is creepy, should input a normal overloaded constructor
        if (clusterSatellites != null) {
            this.BLAST_satellites = Executors.newFixedThreadPool(clusterSatellites.size());
            this.BLAST_TaskExternalDelegator_s = new ArrayList<>(clusterSatellites.size());
            this.clusterSatellites = clusterSatellites;
        } else {
            this.BLAST_TaskExternalDelegator_s = new ArrayList<>();
            this.clusterSatellites = new ArrayList<>();
        }
        this.blast_task_buffer = new LinkedList<>();
        this.blast_task_buffer_size = this.numberOfSimultaniousBLASTs * 3;
        this.BLAST_monitors = Executors.newFixedThreadPool(this.blast_task_buffer_size);
        BLAST_Output_Saver_factory saver_factory = BLAST_Output_Saver_factory.newInstance(this.databaseConnector);
        this.saverStackObjectPool = new StackObjectPool<>(saver_factory);
        saver_factory.setSavers_home(this.saverStackObjectPool);
    }

    private synchronized void processTaskBuffer() {
        try {
            //System.out.println("Processing BLAST buffer");
            //log.log(Level.INFO, "Processing BLAST buffer.");
            //BLAST_Task blast_task;
            List<Future<?>> futures = new ArrayList<>();
            BLAST_Task blast_task;
            while (!this.blast_task_buffer.isEmpty()) {
                //System.out.println("Queuing a new BLAST");
                if ((blast_task = this.blast_task_buffer.poll()) != null) {
                    futures.add(this.BLAST_launchpads.submit(blast_task));
                } else {
                    break;
                }
            }
            int getter = futures.size() - this.numberOfSimultaniousBLASTs;
            Future f;
            if (getter > 0) {
                for (int i = 0; i < getter; i++) {
                    f = futures.get(i);
                    f.get();
                }
                //log.log(Level.INFO, "BLAST task buffer drained successfully.");
            } else {
                log.log(Level.INFO, "Waiting for the remaining BLAST tasks, {0} tasks remaining.", futures.size());
                for (int i = 0; i < futures.size(); i++) {
                    f = futures.get(i);
                    f.get();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launch() {
        try {
            List<Future<?>> satelliteFutures = new ArrayList<>();
            for (ClusterSatellite clusterSatellite : this.clusterSatellites) {
                this.BLAST_TaskExternalDelegator_s.add(BLAST_TaskExternalDelegator.newInstance(
                        this.charger, clusterSatellite, this.databaseConnector));
            }
            for (BLAST_TaskExternalDelegator aBLAST_TaskExternalDelegator : this.BLAST_TaskExternalDelegator_s) {
                satelliteFutures.add(this.BLAST_satellites.submit(aBLAST_TaskExternalDelegator));
            }
            List<Future<?>> futures = new ArrayList<>();
            GenomeCrossBLAST_Task genomeCrossBLAST_Task;
//             this.BLAST_monitors=Executors.newCachedThreadPool(
//                     BLAST_Output_Saver_factory.newInstance(databaseConnector, genomeCrossBLAST_Task));
            while (!this.accomplished) {
                //log.log(Level.INFO, "Filling in buffer.");
                if ((genomeCrossBLAST_Task = this.charger.getTopTask()) != null) {
                    if (this.blast_task_buffer.size() <= this.blast_task_buffer_size) {
                        BLAST_Task blast_task;
                        BLAST_Output_Saver saver;
                        if ((blast_task = genomeCrossBLAST_Task.pollNextBLAST_Task()) != null) {

                            this.blast_task_buffer.add(blast_task);
                            //TODO think of a way of how to reuse the savers
                            saver = this.saverStackObjectPool.borrowObject();
                            saver.setBlast_task(blast_task);
                            saver.setGenomeCrossBLAST_Task(genomeCrossBLAST_Task);
//                            saver = BLAST_Output_Saver.newInstanceFromBLAST_Task(
//                                    blast_task, this.databaseConnector.newBLAST_Output_Saver_sluice(),
//                                    genomeCrossBLAST_Task);

                            futures.add(this.BLAST_monitors.submit(saver));
                        } else {
                            log.log(Level.INFO, "Waiting for the remaining BLAST result savers, {0} tasks remaining.", futures.size());
                            this.processTaskBuffer();
                            for (Future f : futures) {
                                f.get();
                            }
                            futures.clear();
                        }
                    } else {
                        this.processTaskBuffer();
                        futures.clear();
                        //System.out.println("BLASTER::number of savers in pool=> active: "
                                //+ this.saverStackObjectPool.getNumActive() + " idle: " + this.saverStackObjectPool.getNumIdle());
                    }
                } else {
                    this.processTaskBuffer();
                    for (Future future : satelliteFutures) {
                        future.get();
                    }
                    this.accomplished = true;
                    this.BLAST_launchpads.shutdown();
                    this.BLAST_launchpads.awaitTermination(1, TimeUnit.HOURS);
                    this.BLAST_monitors.shutdown();
                    this.BLAST_monitors.awaitTermination(1, TimeUnit.HOURS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BLASTer newInstance(DatabaseConnector databaseConnector, GenomeCrossBLAST_Task_Charger charger,
            int numberOfSimultaniousBLASTs, List<ClusterSatellite> clusterSatellites) {
        return new BLASTer(databaseConnector, charger, numberOfSimultaniousBLASTs, clusterSatellites);
    }
}
