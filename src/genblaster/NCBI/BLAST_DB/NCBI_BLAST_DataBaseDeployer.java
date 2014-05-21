/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST_DB;

import genblaster.db.DatabaseConnector;
import genblaster.util.SystemUtil;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class NCBI_BLAST_DataBaseDeployer {

    private static final Logger log = Logger.getLogger(NCBI_BLAST_DataBaseDeployer.class.getName());
    private File makeblastdb;
    private File genblasterfolder;
    private int max_number_of_simult_deploys;
    private ExecutorService executor;
    private DatabaseConnector databaseConnector;

    protected NCBI_BLAST_DataBaseDeployer(int max_number_of_simult_deploys, DatabaseConnector databaseConnector) {
        this.max_number_of_simult_deploys = max_number_of_simult_deploys;
        this.databaseConnector = databaseConnector;
        this.executor = Executors.newFixedThreadPool(this.max_number_of_simult_deploys);
    }

    public void deployBLAST_DataBaseForGenome(String genomeName) {
        try {
            NCBI_BLAST_DataBaseDeployer_Carret carret =
                    NCBI_BLAST_DataBaseDeployer_Carret.newInstanceForGenome(
                    genomeName, this.databaseConnector.newNCBI_BLAST_DB_DeployerSluice());
            this.executor.execute(carret);
            this.executor.shutdown();
            this.executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deployBLAST_DataBaseForGenomes(List<String> genomeNames) {
        try {
            for (String genomeName : genomeNames) {
                NCBI_BLAST_DataBaseDeployer_Carret carret =
                        NCBI_BLAST_DataBaseDeployer_Carret.newInstanceForGenome(
                        genomeName, this.databaseConnector.newNCBI_BLAST_DB_DeployerSluice());
                this.executor.execute(carret);
            }
            this.executor.shutdown();
            this.executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDefaultSystemDirs() throws Exception {
        String genblasterSystemDir = SystemUtil.userHomeDir
                + SystemUtil.SysFS
                + Genblaster_Constants.genblaster_folder_name;
        genblasterfolder = new File(genblasterSystemDir);
        Genblaster_Constants.platform platform = Genblaster_Constants.ensurePlatform();

        String makeblastdbSystemDir = null;
        switch (platform) {
            case linux: {
                makeblastdbSystemDir = genblasterSystemDir
                        + SystemUtil.SysFS + Genblaster_Constants.ncbi_folder_name
                        + SystemUtil.SysFS + Genblaster_Constants.linux_folder_name
                        + SystemUtil.SysFS + Genblaster_Constants.makeblastdb;
                break;
            }
            case windows: {
                makeblastdbSystemDir = genblasterSystemDir
                        + SystemUtil.SysFS + Genblaster_Constants.ncbi_folder_name
                        + SystemUtil.SysFS + Genblaster_Constants.windows_folder_name
                        + SystemUtil.SysFS + Genblaster_Constants.makeblastdb;
                break;
            }
            case macos: {
                makeblastdbSystemDir = genblasterSystemDir
                        + SystemUtil.SysFS + Genblaster_Constants.ncbi_folder_name
                        + SystemUtil.SysFS + Genblaster_Constants.linux_folder_name
                        + SystemUtil.SysFS + Genblaster_Constants.makeblastdb;
                break;
            }
            case other: {
                throw new Exception("Unknown platform."); //TODO think of smth better than this
            }
        }
        this.makeblastdb = new File(makeblastdbSystemDir);
        System.setProperty(Genblaster_Constants.makeblastdb_system_home, makeblastdbSystemDir);
        System.setProperty(Genblaster_Constants.genblaster_system_home, genblasterSystemDir);
        System.setProperty(Genblaster_Constants.blast_db_system_home, genblasterSystemDir
                + SystemUtil.SysFS + Genblaster_Constants.blast_db_folder_name);
        System.setProperty(Genblaster_Constants.orf_system_home, genblasterSystemDir
                + SystemUtil.SysFS + Genblaster_Constants.orf_folder_name);
    }

    private void setSystemDirsFromProperties() {
        String makeblastdbSystemDir = System.getProperty(Genblaster_Constants.ncbi_execs_system_home)
                + SystemUtil.SysFS + Genblaster_Constants.makeblastdb;
        this.makeblastdb = new File(makeblastdbSystemDir);
    }

    public static NCBI_BLAST_DataBaseDeployer defaultInstance(
            int max_number_of_simult_deploys, DatabaseConnector databaseConnector) throws Exception {
        NCBI_BLAST_DataBaseDeployer ncbi_blast_DataBaseDeployer =
                new NCBI_BLAST_DataBaseDeployer(max_number_of_simult_deploys, databaseConnector);
        ncbi_blast_DataBaseDeployer.setDefaultSystemDirs();
        return ncbi_blast_DataBaseDeployer;
    }

    public static NCBI_BLAST_DataBaseDeployer newInstanceFromProperties(
            int max_number_of_simult_deploys, DatabaseConnector databaseConnector) throws Exception {
        NCBI_BLAST_DataBaseDeployer ncbi_blast_DataBaseDeployer =
                new NCBI_BLAST_DataBaseDeployer(max_number_of_simult_deploys, databaseConnector);
        ncbi_blast_DataBaseDeployer.setSystemDirsFromProperties();
        return ncbi_blast_DataBaseDeployer;
    }
    //TODO implement an option to input different folders
}
