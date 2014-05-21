/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.db.derby.DerbyDBConnector;
import genblaster.util.SystemUtil;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Alexander
 */
public class BLASTER_Test {

    public static void main(String[] args) {

        try {
            //System properties
            String genblasterSystemDir = SystemUtil.userHomeDir
                    + SystemUtil.SysFS
                    + Genblaster_Constants.genblaster_folder_name;
            System.setProperty(Genblaster_Constants.blastp_task_system_home,
                    genblasterSystemDir + SystemUtil.SysFS + Genblaster_Constants.blastp_tasks_folder_name);
            System.setProperty(Genblaster_Constants.blastp_result_system_home,
                    genblasterSystemDir + SystemUtil.SysFS + Genblaster_Constants.blastp_results_folder_name);
            System.setProperty(Genblaster_Constants.blastp_system_home,
                    genblasterSystemDir + SystemUtil.SysFS + Genblaster_Constants.ncbi_folder_name
                    + SystemUtil.SysFS + "win" + SystemUtil.SysFS + Genblaster_Constants.blastp);
            System.setProperty(Genblaster_Constants.blast_db_system_home,
                    genblasterSystemDir + SystemUtil.SysFS + Genblaster_Constants.blast_db_folder_name);
            System.out.println(System.getProperty(Genblaster_Constants.blastp_task_system_home));
            System.out.println(System.getProperty(Genblaster_Constants.blastp_result_system_home));
            System.out.println(System.getProperty(Genblaster_Constants.blastp_system_home));
            System.out.println(System.getProperty(Genblaster_Constants.blast_db_system_home));

            //Database
            DerbyDBConnector derbyDBConnector = DerbyDBConnector.defaultConnectorInstance();
            derbyDBConnector.connectToEmbeddedDatabase();
            System.out.println(System.getProperty("derby.system.home"));

            //GenomeCrossBLAST_Task
            GenomeCrossBLAST_Task genomeCrossBLAST_Task =
                    GenomeCrossBLAST_Task.newDefaultInstance("test_genome", "test_genome",
                    derbyDBConnector.newGenomeCrossBLAST_sluice(), "0.01", true);

            //List of single GenomeCrossBLAST_Task
            Queue<GenomeCrossBLAST_Task> cross_blast_tasks = new LinkedList<>();
            cross_blast_tasks.add(genomeCrossBLAST_Task);

            //Charger
            GenomeCrossBLAST_Task_Charger charger = GenomeCrossBLAST_Task_Charger.newInstance(cross_blast_tasks);
            charger.launch();

            //BLASter
            BLASTer blaster = BLASTer.newInstance(derbyDBConnector, charger, 2,null);
            blaster.launch();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
