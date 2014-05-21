/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST.server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class BLAST_Server implements Runnable {

    private static final Logger log = Logger.getLogger(BLAST_Server.class.getName());
    private ExecutorService selfExecutor;
    private ExecutorService satelliteExecutors;
    private boolean running;
    private ServerSocket serverSocket;
    private int port;

    protected BLAST_Server(int port) {
        this.port = port;
        this.selfExecutor = Executors.newSingleThreadExecutor();

        this.satelliteExecutors = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            while (this.running) {
                this.satelliteExecutors.execute(new ServerConnection(this.serverSocket.accept()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        this.running = true;
        this.selfExecutor.execute(this);
    }

    public void stopServer() throws InterruptedException {
        this.running = false;
        this.selfExecutor.shutdown();
        this.selfExecutor.awaitTermination(1, TimeUnit.HOURS);
    }
}
