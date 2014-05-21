/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class ServerConnection implements Runnable {

    private static final Logger log = Logger.getLogger(ServerConnection.class.getName());
    private boolean running;
    private int activeSatellites;
    private Socket satelliteSocket;
    private ExecutorService saverExecutors;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    protected ServerConnection(Socket satelliteSocket) {
        this.satelliteSocket = satelliteSocket;
        this.running = false;
        this.activeSatellites = 0;
        try {
            this.satelliteSocket.setKeepAlive(true);
        } catch (SocketException se) {
            se.printStackTrace();
        }
        this.saverExecutors = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            this.objectInputStream=new ObjectInputStream(this.satelliteSocket.getInputStream());
            this.objectOutputStream=new ObjectOutputStream(this.satelliteSocket.getOutputStream());
            while (this.running){
                this.objectOutputStream.writeObject(this.processRequest(this.objectInputStream.readObject()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Object processRequest(Object request){
        return null;
    }
}
