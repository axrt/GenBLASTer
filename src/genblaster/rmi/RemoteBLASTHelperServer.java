/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.rmi;

import genblaster.NCBI.BLAST.BLAST_Task;
import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.io.aFileSaver;
import genblaster.util.SystemUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class RemoteBLASTHelperServer implements RemoteBLASTHelper {

    private ExecutorService executor;
    private int number_of_simultaneous_blasts;
    private String name;
    private String uri;
    private int port;
    private aFileSaver fileSaver;
    private static final Logger log = Logger.getLogger(RemoteBLASTHelperServer.class.getName());

    private RemoteBLASTHelperServer(int number_of_simultaneous_blasts, String name, String uri, int port) {
        this.number_of_simultaneous_blasts = number_of_simultaneous_blasts;
        this.executor = Executors.newFixedThreadPool(this.number_of_simultaneous_blasts);
        this.name = name;
        this.port = port;
        this.uri = uri;
        this.fileSaver = aFileSaver.newInstance();
    }

    @Override
    public synchronized BLAST_Task processBLAST_Request(BLAST_Task blast_task) throws Exception {
        Future future = this.executor.submit(blast_task);
        future.get();
        return blast_task;
    }

    @Override
    public synchronized List<BLAST_Task> processBLAST_RequestBatch(List<BLAST_Task> blast_tasks) throws Exception {
        log.log(Level.INFO, "A help request from CORE has been received, volume: {0}, top genomes are {1} and {2}.",
                new Object[]{blast_tasks.size(), blast_tasks.get(0).getQueryGenomeName(), blast_tasks.get(0).getTargetGenomeName()});
        List<Future> futures = new ArrayList<>(blast_tasks.size());
        for (BLAST_Task blast_task : blast_tasks) {
            futures.add(this.executor.submit(blast_task));
        }
        for (Future future : futures) {
            future.get();
        }
        return blast_tasks;
    }

    public void selfDeploy() {
        try {
            System.setProperty("java.rmi.server.hostname", this.uri);
            RemoteBLASTHelper stub = (RemoteBLASTHelper) UnicastRemoteObject.exportObject(this, 9531);
            Registry registry = LocateRegistry.createRegistry(this.port);
            registry.rebind(this.name, stub);
            log.log(Level.INFO, "RemoteBLASTHelper bound. {0}", System.getProperty("java.rmi.server.hostname"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public boolean satelliteHasORFSetForGenome(String genomeName) throws Exception {
        String db_folder = System.getProperty(Genblaster_Constants.BLAST_DB_folder) + SystemUtil.SysFS;
        if (new File(db_folder + SystemUtil.SysFS + genomeName + ".db.phr").exists()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void transferORFSetToSatellite(String ORFBase, String genomeName) throws Exception {
        String makeblastdb_path = System.getProperty(Genblaster_Constants.makeblastdb_system_home);
        String db_folder = System.getProperty(Genblaster_Constants.BLAST_DB_folder) + SystemUtil.SysFS;
        String orf_folder = System.getProperty(Genblaster_Constants.ORF_Base_folder) + SystemUtil.SysFS;
        File orf_base_out_file = new File(orf_folder + genomeName + Genblaster_Constants.dot_fasta);
        this.fileSaver.SaveTextFile(ORFBase, orf_base_out_file);
        log.log(Level.INFO, "CDS base for the {0} genome extracted from the database.", genomeName);
        log.log(Level.INFO, "Processing BLAST database for the {0} genome.", genomeName);
        //Start the deployment process
        Process p = Runtime.getRuntime().exec(makeblastdb_path + " -in "
                + orf_base_out_file.getPath() + " -out "
                + db_folder + genomeName + Genblaster_Constants.dot_db + " -parse_seqids -dbtype prot");
        p.waitFor();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        // Read the output from the command
        log.log(Level.INFO, "Standard output for {0} genome BLAST database.", genomeName);
        while ((s = stdInput.readLine()) != null) {
            log.log(Level.INFO, "{0}", s);
        }
        //Read any errors from the attempted command
        log.log(Level.INFO, "Error output for {0} genome BLAST database.", genomeName);
        while ((s = stdError.readLine()) != null) {
            log.log(Level.INFO, "{0}", s);
        }
    }

    public static RemoteBLASTHelperServer newInstance(int number_of_simultaneous_blasts, String name, String uri, int port) {
        return new RemoteBLASTHelperServer(number_of_simultaneous_blasts, name, uri, port);
    }
}
