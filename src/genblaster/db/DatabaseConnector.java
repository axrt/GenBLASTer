/*
 * An interface that allows to communicate with the database
 * 
 */
package genblaster.db;

import genblaster.db.sluice.*;
import java.io.File;

/**
 *
 * @author Alexander Tuzhikov
 */
public interface DatabaseConnector {

    /**
     * Connect to a database of type specific to for the implementing class
     * @return: {@code true} if connection succeeded, {@code false} if fails
     */
    public boolean connectToEmbeddedDatabase() throws Exception;

    public ORFScannerSluice newORFScannerSluice()throws Exception;
    
    public NCBI_BLAST_DB_DeployerSluice newNCBI_BLAST_DB_DeployerSluice() throws Exception;
    
    public BLAST_Output_Saver_sluice newBLAST_Output_Saver_sluice()  throws Exception;
    
    public GenomeCrossBLAST_sluice newGenomeCrossBLAST_sluice()  throws Exception;
    
    public Matrix_sluice newMatrix_sluice()throws Exception;
    
    public boolean databasePassesChecks() throws Exception;
    
    /**
     * Should be able to set the given directory as a working directory 
     * @param workDir:{@code File} working directory
     * @throws Exception 
     */
    public void setDataBaseWorkingDirectory(File workDir) throws Exception;
}
