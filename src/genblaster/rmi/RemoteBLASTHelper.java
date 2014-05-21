/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.rmi;

import genblaster.NCBI.BLAST.BLAST_Task;
import java.rmi.Remote;
import java.util.List;

/**
 *
 * @author Alexander
 */
public interface RemoteBLASTHelper extends Remote {

    public BLAST_Task processBLAST_Request(BLAST_Task blast_task) throws Exception;

    public List<BLAST_Task> processBLAST_RequestBatch(List<BLAST_Task> blast_tasks) throws Exception;

    public boolean satelliteHasORFSetForGenome(String genomeName) throws Exception;
    
    public void transferORFSetToSatellite(String ORFBase,String genomeName) throws Exception;
}
