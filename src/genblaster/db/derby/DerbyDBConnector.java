/*
 * A class to handle all the database interactions
 * of the microblaster tool
 * 
 */
package genblaster.db.derby;

import genblaster.db.DatabaseConnector;
import genblaster.db.DatabaseConstants;
import genblaster.db.sluice.*;
import genblaster.db.sluice.derby.*;
import genblaster.genome.GenomeDigester;
import genblaster.util.SystemUtil;
import java.io.File;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander Tuzhikov
 */
public enum DerbyDBConnector implements DatabaseConnector {

    /**
     * A Singleton-instance
     */
    Instance;
    /**
     * Properties to connect the database
     */
    private Properties derbyConnectionProperties;
    /**
     * A folder that contains the derby database folder
     */
    private File derbyDataBaseFolder;
    /**
     * The derby url, jdbc:derby:microblasterDB by default
     */
    private final String derbyURL;
    /**
     * User, used as a username when connecting the derby database
     */
    private final String user;
    /**
     * Password, used as a password when connecting the derby database
     */
    private final String password;
    /**
     * Create, used to set the create database (if db not exists yet) 
     * when connecting the derby database
     */
    private final String create;
    /**
     * Database connection
     */
    private Connection connection;
    
    private static final Logger log = Logger.getLogger(GenomeDigester.class.getName());
    /**
     * Private constructor to use with static factories
     */
    private DerbyDBConnector() {
        //Inicializing the database URL
        this.derbyURL = "jdbc:derby:genblasterDB";
        //Inicializing default user
        this.user = "genblaster";
        //Inicializing default password
        this.password = "genblaster";
        //Inicializing default create
        this.create = "true";
        //Inicializing and assembling properties
        this.derbyConnectionProperties = new Properties();
        this.derbyConnectionProperties.put("user", this.user);
        this.derbyConnectionProperties.put("password", this.password);
        this.derbyConnectionProperties.put("create", this.create);
        //Set the default sysetm directory for the database
    }
    
    /**
     * Attempts to connect to the embedded derby database
     * @return {@code true} if connected
     * @throws Exception 
     */
    @Override
    public boolean connectToEmbeddedDatabase() throws Exception {
        if (this.connectToEmbeddedDerbyDatabase()) {
            return true;
        } else {
            throw new Exception("Could not connect to the derby database!");
        }
    }

    /**
     * Connects to the embedded derby database
     * @return {@code true} if connected, {@code false} if smth went wrong
     * @throws SQLException 
     */
    private boolean connectToEmbeddedDerbyDatabase() throws SQLException {
        log.log(Level.INFO, "Connecting to DataBase.");
        if (DerbyDBConnectorHelper.loadDerbyDriver()) {
            this.connection = DriverManager.getConnection(this.derbyURL, this.derbyConnectionProperties);
            return true;
        } else {
            return false;
        }
    }
    
    
    @Override
    public boolean databasePassesChecks() throws Exception {//Might need to move this to the DerbyDatabaseDeployer
        Statement statement = this.connection.createStatement();
        //Checking ORF table
        if(!this.dataBaseExists(this.getConnection(), DatabaseConstants.ORF)){
            log.log(Level.INFO, "Creating {0} table.",DatabaseConstants.ORF);
            statement.addBatch("CREATE TABLE "
                    + DatabaseConstants.GENBLASTER+DatabaseConstants.ORF
                    +" (ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1), "
                    + DatabaseConstants.GENOME_NAME+" VARCHAR(50)NOT NULL, "
                    + DatabaseConstants.FRAME+" SMALLINT NOT NULL, "
                    + DatabaseConstants.AC+" INTEGER NOT NULL, "
                    + DatabaseConstants.AASEQ+" LONG VARCHAR NOT NULL, "
                    + DatabaseConstants.NUSEQ+" LONG VARCHAR NOT NULL)");
        }
        //Checking BLAST table
        if(!this.dataBaseExists(this.getConnection(), DatabaseConstants.BLAST)){
            log.log(Level.INFO, "Creating {0} table.", DatabaseConstants.BLAST);
            statement.addBatch("CREATE TABLE "
                    + DatabaseConstants.GENBLASTER+DatabaseConstants.BLAST
                    + "(ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1), "
                    + DatabaseConstants.QUERY_GENOME_NAME+" VARCHAR(50) NOT NULL, "
                    + DatabaseConstants.TARGET_GENOME_NAME+" VARCHAR(50) NOT NULL, "
                    + DatabaseConstants.QSEQID+" VARCHAR(50) NOT NULL, "
                    + DatabaseConstants.QUERY_ORF_ID+" BIGINT NOT NULL, "
                    + DatabaseConstants.SSEQID+" VARCHAR(50), "
                    + DatabaseConstants.EVALUE+" DOUBLE PRECISION, "
                    + DatabaseConstants.SLEN+" SMALLINT, "
                    + DatabaseConstants.QSTART+" SMALLINT, "
                    + DatabaseConstants.QEND+" SMALLINT, "
                    + DatabaseConstants.SSTART+" SMALLINT, "
                    + DatabaseConstants.SEND+" SMALLINT, "
                    + DatabaseConstants.PIDENT+" DOUBLE PRECISION, "
                    + DatabaseConstants.NIDENT+" DOUBLE PRECISION, "
                    + DatabaseConstants.MISMATCH+" DOUBLE PRECISION, "
                    + DatabaseConstants.POSITIVE+" DOUBLE PRECISION, "
                    + DatabaseConstants.GAPOPEN+" SMALLINT, "
                    + DatabaseConstants.GAPS+" SMALLINT)");
        }
        //Checking Distance table
        if(!this.dataBaseExists(this.getConnection(), DatabaseConstants.DISTANCE)){
            log.log(Level.INFO, "Creating {0} table.",DatabaseConstants.DISTANCE);
            statement.addBatch("CREATE TABLE "
                    + DatabaseConstants.GENBLASTER+DatabaseConstants.DISTANCE
                    +" (ID INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1), "
                    + DatabaseConstants.A_GENOME_NAME+" VARCHAR(50)NOT NULL, "
                    + DatabaseConstants.B_GENOME_NAME+" VARCHAR(50)NOT NULL, "
                    + DatabaseConstants.DISTANCE+" DOUBLE PRECISION)");
        }
        statement.executeBatch();
        return true;
    }
    private boolean dataBaseExists(Connection connection, String databaseName) throws Exception {
        //Prepares a statement to query the database
        Statement statement = connection.createStatement();
        //Asks whether the system folder has the NODES table
        ResultSet resultSet = statement.executeQuery("SELECT "
                + DatabaseConstants.TABLENAME+" FROM "
                + DatabaseConstants.SYS+DatabaseConstants.SYSTABLES+" WHERE "
                + DatabaseConstants.TABLENAME+"='"
                + databaseName
                + "'");
        if (resultSet.next()
                && resultSet.getString(DatabaseConstants.TABLENAME).equals(databaseName)) {
            log.log(Level.INFO, "Database table {0} checked.", databaseName);
            return true;
        } else {
            log.log(Level.INFO, "Database table {0} does not exist yet.", databaseName);
            return false;
        }
    }
    @Override
    public void setDataBaseWorkingDirectory(File workDir) throws Exception {
        this.setDerbyDBToFolderDirectory(workDir);
    }
    

    /**
     * A connection getter, synchronized to make sure that only one
     * Thread receives access to the database at a time
     * @return 
     */
    public synchronized Connection getConnection() {
        return connection;
    }

    /**
     * Sets the derby database system directory to 
     * user's home directory + ".microblasterDB"
     */
    public void setDerbyDBDefaultSystemDir() {//TODO protect this
        String systemDir = SystemUtil.userHomeDir
                + SystemUtil.SysFS
                + ".genblasterDB";
        System.setProperty("derby.system.home", systemDir);

    }

    /**
     * Sets the derby database system directory to a given directory
     * @param derbyDataBaseFolder: {@code String} directory 
     */
    private void setDerbyDBToFolderDirectory(File derbyDataBaseFolder) {
        this.derbyDataBaseFolder = derbyDataBaseFolder;
        System.setProperty("derby.system.home", this.derbyDataBaseFolder.getPath());
    }
    
    @Override
    public synchronized ORFScannerSluice newORFScannerSluice() throws Exception{
        return DerbyORF_ScannerSluice.newInstance(
                DriverManager.getConnection(this.derbyURL, this.derbyConnectionProperties));
    }
    @Override
    public NCBI_BLAST_DB_DeployerSluice newNCBI_BLAST_DB_DeployerSluice() throws Exception{
        return DerbyNCBI_BLAST_DB_DeployerSluice.newInstance(
                DriverManager.getConnection(this.derbyURL, this.derbyConnectionProperties)); 
    }
    @Override
    public BLAST_Output_Saver_sluice newBLAST_Output_Saver_sluice()  throws Exception{
        return DerbyBLAST_Output_Saver_sluice.newInstance(
                DriverManager.getConnection(this.derbyURL, this.derbyConnectionProperties));
    }
    @Override
    public GenomeCrossBLAST_sluice newGenomeCrossBLAST_sluice()  throws Exception{
        return DerbyGenomeCrossBLAST_sluice.newInstance(
                DriverManager.getConnection(this.derbyURL, this.derbyConnectionProperties));
    }
    @Override
    public Matrix_sluice newMatrix_sluice()throws Exception{
        return DerbyMatrix_sluice.newInstance(
                DriverManager.getConnection(this.derbyURL, this.derbyConnectionProperties));
    }
    
    /**
     * A static factory that returns a default Connector
     * @return {@code Connector} default
     */
    public static DerbyDBConnector defaultConnectorInstance() {
        return Instance;
    }
}
