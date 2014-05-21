/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.db.DatabaseConnector;
import genblaster.io.aFileReader;
import genblaster.properties.jaxb.ClusterSatellite;
import genblaster.rmi.RemoteBLASTHelper;
import genblaster.util.SystemUtil;
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 *
 * @author Alexander
 */
public class BLAST_TaskExternalDelegator implements Runnable {

    private DatabaseConnector databaseConnector;
    private ExecutorService BLAST_monitors;
    private GenomeCrossBLAST_Task_Charger charger;
    private RemoteBLASTHelper helper;
    private ClusterSatellite clusterSatellite;
    private List<BLAST_Task> task_batch;
    private int batchSize;
    private aFileReader fileReader;
    private StackObjectPool<BLAST_Output_Saver> saverStackObjectPool;
    private static final Logger log = Logger.getLogger(BLAST_TaskExternalDelegator.class.getName());

    private BLAST_TaskExternalDelegator(GenomeCrossBLAST_Task_Charger charger, ClusterSatellite clusterSatellite, DatabaseConnector databaseConnector) {
        this.batchSize = Integer.valueOf(clusterSatellite.getBatchSize());
        this.BLAST_monitors = Executors.newFixedThreadPool(this.batchSize);
        this.databaseConnector = databaseConnector;
        this.charger = charger;
        this.clusterSatellite = clusterSatellite;
        this.task_batch = new ArrayList<>(this.batchSize);
        BLAST_Output_Saver_factory saver_factory = BLAST_Output_Saver_factory.newInstance(this.databaseConnector);
        this.saverStackObjectPool = new StackObjectPool<>(saver_factory);
        saver_factory.setSavers_home(this.saverStackObjectPool);
        this.stationSatellite();
        this.fileReader = aFileReader.newInstance();
    }

    @Override
    public void run() {
        List<Future<?>> futures = new ArrayList<>();
        GenomeCrossBLAST_Task genomeCrossBLAST_Task = this.charger.getTopTask();
        try {
            BLAST_Output_Saver saver;
            BLAST_Task blast_task;
            while ((genomeCrossBLAST_Task = this.charger.getTopTask()) != null) {
                if ((blast_task = genomeCrossBLAST_Task.pollNextBLAST_Task()) != null && this.task_batch.size() < this.batchSize) {
                    this.task_batch.add(blast_task);
                } else {
                    this.checkIfTheSatelliteHasTheNessessaryORFBaseForGenome(task_batch.get(0).getQueryGenomeName());
                    this.checkIfTheSatelliteHasTheNessessaryORFBaseForGenome(task_batch.get(0).getTargetGenomeName());
                    this.task_batch = this.helper.processBLAST_RequestBatch(this.task_batch);
                    log.log(Level.INFO, "BLASTtasks returned from satellite {0}", this.clusterSatellite.getName());
                    for (BLAST_Task bt : this.task_batch) {
                        saver = this.saverStackObjectPool.borrowObject();
                        saver.setBlast_task(bt);
                        saver.setGenomeCrossBLAST_Task(genomeCrossBLAST_Task);
//                            saver = BLAST_Output_Saver.newInstanceFromBLAST_Task(
//                                    bt, this.databaseConnector.newBLAST_Output_Saver_sluice(),
//                                    genomeCrossBLAST_Task);
                        futures.add(this.BLAST_monitors.submit(saver));
                    }
                    this.task_batch.clear();
                    futures.clear();
//                    System.out.println("External BLASTER::number of savers in pool=> active: "
//                            + this.saverStackObjectPool.getNumActive() + " idle: " + this.saverStackObjectPool.getNumIdle());
                }
            }
            this.task_batch = this.helper.processBLAST_RequestBatch(this.task_batch);
            log.log(Level.INFO, "BLASTtasks returned from satellite {0}", this.clusterSatellite.getName());
            for (BLAST_Task bt : this.task_batch) {
                saver = this.saverStackObjectPool.borrowObject();
                saver.setBlast_task(bt);
                saver.setGenomeCrossBLAST_Task(genomeCrossBLAST_Task);
//                            saver = BLAST_Output_Saver.newInstanceFromBLAST_Task(
//                                    bt, this.databaseConnector.newBLAST_Output_Saver_sluice(),
//                                    genomeCrossBLAST_Task);
                futures.add(this.BLAST_monitors.submit(saver));
            }
            this.task_batch.clear();
            for (Future future : futures) {
                future.get();
            }
            futures.clear();
            log.log(Level.INFO, "No more BLAST tasks for this satellite {0}", this.clusterSatellite.getName());
        } catch (RemoteException re) {
            re.printStackTrace();
            for (BLAST_Task blast_task : this.task_batch) {
                genomeCrossBLAST_Task.abandonTaskWithID(blast_task.getQueryID());
            }
        } catch (Exception e) {
            e.printStackTrace();
            //TODO think of smth more adequate like reconnect attempts
        }
    }

    private void checkIfTheSatelliteHasTheNessessaryORFBaseForGenome(String genomeName) {
        try {
            if (!this.helper.satelliteHasORFSetForGenome(genomeName)) {
                String orf_folder = System.getProperty(Genblaster_Constants.ORF_Base_folder) + SystemUtil.SysFS;
                File orf_base_input_file = new File(orf_folder + genomeName + Genblaster_Constants.dot_fasta);
                this.helper.transferORFSetToSatellite(this.fileReader.ReadTextFile(orf_base_input_file), genomeName);
            } else {
                //log.log(Level.INFO, "Satellite {0} already has ORF Base for {1}.",new Object[]{
                    //this.clusterSatellite.getName(), genomeName});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stationSatellite() {
        try {
            int satellitePort = Integer.valueOf(this.clusterSatellite.getPort());
            Registry registry = LocateRegistry.getRegistry(this.clusterSatellite.getUri(), satellitePort);
            this.helper = (RemoteBLASTHelper) registry.lookup(this.clusterSatellite.getName());
            log.log(Level.INFO, "Connected to Satellite {0} at {1} on {2}", new Object[]{this.clusterSatellite.getName(),
                        this.clusterSatellite.getUri(), this.clusterSatellite.getPort()});
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            //TODO think of smth more adequate like reconnect attempts
        }
    }

    public static BLAST_TaskExternalDelegator newInstance(GenomeCrossBLAST_Task_Charger charger,
            ClusterSatellite clusterSatellite, DatabaseConnector databaseConnector) {
        return new BLAST_TaskExternalDelegator(charger, clusterSatellite, databaseConnector);
    }
}
