/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.db.sluice.BLAST_Output_Saver_sluice;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 *
 * @author Alexander
 */
public class BLAST_Output_Saver implements Runnable {

    private BLAST_Task blast_task;
    private BLAST_Output_Saver_sluice output_saver_sluice;
    private GenomeCrossBLAST_Task genomeCrossBLAST_Task;
    private StackObjectPool<BLAST_Output_Saver> home;

    protected BLAST_Output_Saver(StackObjectPool<BLAST_Output_Saver> home, 
            BLAST_Task blast_task, BLAST_Output_Saver_sluice output_saver_sluice,
            GenomeCrossBLAST_Task genomeCrossBLAST_Task) {
        this.blast_task = blast_task;
        this.output_saver_sluice = output_saver_sluice;
        this.genomeCrossBLAST_Task = genomeCrossBLAST_Task;
        this.home = home;
    }

    protected BLAST_Output_Saver(StackObjectPool<BLAST_Output_Saver> home,
            BLAST_Output_Saver_sluice output_saver_sluice) {
        this.output_saver_sluice = output_saver_sluice;
        this.home=home;
    }

    public void setGenomeCrossBLAST_Task(GenomeCrossBLAST_Task genomeCrossBLAST_Task) {
        this.genomeCrossBLAST_Task = genomeCrossBLAST_Task;
    }

    public void setBlast_task(BLAST_Task blast_task) {
        this.blast_task = blast_task;
    }

    @Override
    public void run() {
        try {
            BLAST_Output blast_output = this.blast_task.getBlast_output();
            if (blast_output != null && !blast_output.getStatus().equals("empty")) {
                this.output_saver_sluice.saveBLAST_Output_to_DataBase(blast_output);
                this.genomeCrossBLAST_Task.reportTaskDone();
            } else if (blast_output.getStatus().equals("empty")) {
                //TODO simplify this 
                this.output_saver_sluice.saveBLAST_Output_to_DataBase(blast_output);
                this.genomeCrossBLAST_Task.reportTaskDone();
            } else {
                this.genomeCrossBLAST_Task.abandonTaskWithID(
                        this.blast_task.getQueryID());
            }
            this.returnHome();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void returnHome(){
        try{
        this.home.returnObject(this);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static BLAST_Output_Saver newInstanceFromBLAST_Task(StackObjectPool<BLAST_Output_Saver> home, BLAST_Task blast_task,
            BLAST_Output_Saver_sluice output_saver_sluice,
            GenomeCrossBLAST_Task genomeCrossBLAST_Task) {
        return new BLAST_Output_Saver(home, blast_task, output_saver_sluice, genomeCrossBLAST_Task);
    }

    public static BLAST_Output_Saver newInstanceFromSluiceOnly(StackObjectPool<BLAST_Output_Saver> home,
            BLAST_Output_Saver_sluice output_saver_sluice) {
        return new BLAST_Output_Saver(home, output_saver_sluice);
    }
}
