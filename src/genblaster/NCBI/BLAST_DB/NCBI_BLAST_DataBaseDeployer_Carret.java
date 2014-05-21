/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST_DB;

import genblaster.db.sluice.NCBI_BLAST_DB_DeployerSluice;
import genblaster.io.aFileSaver;
import genblaster.util.SystemUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander Tuzhikov
 */
public class NCBI_BLAST_DataBaseDeployer_Carret implements Runnable {

    private NCBI_BLAST_DB_DeployerSluice ncbi_blast_DB_DeployerSluice;
    private String genomeName;
    private aFileSaver fileSaver;
    private static final Logger log = Logger.getLogger(NCBI_BLAST_DataBaseDeployer_Carret.class.getName());

    protected NCBI_BLAST_DataBaseDeployer_Carret(String genomeName, NCBI_BLAST_DB_DeployerSluice ncbi_blast_DB_DeployerSluice) {
        this.fileSaver=aFileSaver.newInstance();
        this.genomeName = genomeName;
        this.ncbi_blast_DB_DeployerSluice = ncbi_blast_DB_DeployerSluice;
    }

    @Override
    public void run() {
        //First check whether the database has already been deployed
        String db_folder = System.getProperty(Genblaster_Constants.BLAST_DB_folder)+ SystemUtil.SysFS;
        String orf_folder = System.getProperty(Genblaster_Constants.ORF_Base_folder)+ SystemUtil.SysFS;
        String cds_folder = System.getProperty(Genblaster_Constants.CDS_Base_folder)+ SystemUtil.SysFS;
        String makeblastdb_path=System.getProperty(Genblaster_Constants.makeblastdb_system_home);
        if (!new File(db_folder + SystemUtil.SysFS + this.genomeName + ".db.phr").exists()) {
            try {
                log.log(Level.INFO, "Deploying BLAST database for {0}.", this.genomeName);
                //First get ofr-base file from the database
                String orf_base = this.ncbi_blast_DB_DeployerSluice.collectORFBaseForGenome(this.genomeName);
                File orf_base_out_file = new File(orf_folder + this.genomeName + Genblaster_Constants.dot_fasta);
                this.fileSaver.SaveTextFile(orf_base, orf_base_out_file);
                log.log(Level.INFO, "ORF base for the {0} genome extracted from the database.", this.genomeName);
                //Then save the cds-base file from database
                String cds_base = this.ncbi_blast_DB_DeployerSluice.collectCDSBaseForGenome(this.genomeName);
                File cds_base_out_file = new File(cds_folder + this.genomeName + Genblaster_Constants.dot_fasta);
                this.fileSaver.SaveTextFile(cds_base, cds_base_out_file);
                log.log(Level.INFO, "CDS base for the {0} genome extracted from the database.", this.genomeName);
                log.log(Level.INFO, "Processing BLAST database for the {0} genome.", this.genomeName);
                //Start the deployment process
                Process p = Runtime.getRuntime().exec(makeblastdb_path+" -in "
                        +orf_base_out_file.getPath()+ " -out "
                        + db_folder+this.genomeName+Genblaster_Constants.dot_db + " -parse_seqids -dbtype prot");
                p.waitFor();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String s;
                // Read the output from the command
                log.log(Level.INFO, "Standard output for {0} genome BLAST database.", this.genomeName);
                while ((s = stdInput.readLine()) != null) {
                    log.log(Level.INFO, "{0}", s);
                }
                //Read any errors from the attempted command
                log.log(Level.INFO, "Error output for {0} genome BLAST database.", this.genomeName);
                while ((s = stdError.readLine()) != null) {
                    log.log(Level.INFO, "{0}", s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.log(Level.INFO, "The database for {0} has already been deployed.", this.genomeName);
        }

    }

    public static NCBI_BLAST_DataBaseDeployer_Carret newInstanceForGenome(String genomeName,
            NCBI_BLAST_DB_DeployerSluice ncbi_blast_DB_DeployerSluice) {
        return new NCBI_BLAST_DataBaseDeployer_Carret(genomeName, ncbi_blast_DB_DeployerSluice);
    }
}
