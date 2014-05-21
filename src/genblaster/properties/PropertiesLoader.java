/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.properties;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.db.sluice.GenomeCrossBLAST_sluice;
import genblaster.db.sluice.GenomeCrossBLAST_sluice.distance_type;
import genblaster.properties.jaxb.ClusterSatellite;
import genblaster.properties.jaxb.GenBLASTerProperties;
import genblaster.properties.jaxb.Genome;
import genblaster.util.SystemUtil;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Alexander
 */
public class PropertiesLoader {

    private GenBLASTerProperties properties;
    private File path_to_properties_file;
    private File GenBLASTerHomeDir;
    private File GenBLASTerDB_Dir;
    private File BLAST_DB_folder;
    private File BLAST_RESULT_folder;
    private File BLAST_TASK_folder;
    private File ncbi_folder;
    private File ORF_Base_folder;
    private File CDS_Base_folder;
    private File matrix_folder;
    private File exectutables_containing_folder;
    private boolean isCore;
    private boolean useCluster;
    private double Evalue;
    private int numberOfSimultaniousBLASTs;
    private int minORF_Length;
    private int maxORF_Length;
    private int ORFPool;
    private int port;
    private String name;
    private String preferred_satellite_uri;
    private List<ClusterSatellite> clusterSatellites;
    private GenomeCrossBLAST_sluice.distance_type dt;
    private static final Logger log = Logger.getLogger(PropertiesLoader.class.getName());

    protected PropertiesLoader(File path_to_properties_file) {
        this.path_to_properties_file = path_to_properties_file;
    }

    public int getMinORF_Length() {
        return minORF_Length;
    }

    public int getMaxORF_Length() {
        return maxORF_Length;
    }

    public int getORFPool() {
        return ORFPool;
    }

    public int getNumberOfSimultaniousBLASTs() {
        return numberOfSimultaniousBLASTs;
    }

    public double getEvalue() {
        return Evalue;
    }

    public distance_type getDt() {
        return dt;
    }

    public File getMatrix_folder() {
        return matrix_folder;
    }

    public boolean getUseCluster() {
        return useCluster;
    }

    public boolean isThisInstanceRunsAsCore() {
        return isCore;
    }

    public List<ClusterSatellite> getClusterSatellites() {
        return this.clusterSatellites;
    }

    public String getName() {
        if (this.name != null) {
            return name;
        } else {
            return "Initialized as a CORE!";
        }
    }

    public int getPort() {
        return port;
    }

    public String getPreferred_satellite_uri() {
        return preferred_satellite_uri;
    }

    public void loadAndCheckProperties() {
        //Loading file
        InputStream inputStream = null;
        try {
            log.log(Level.INFO, "Loading properties from {0}.", this.path_to_properties_file.getPath());
            inputStream = new FileInputStream(this.path_to_properties_file);
        } catch (FileNotFoundException fnfe) {
            log.log(Level.INFO, "Error upon loading properties from {0}. Plaes see the reason below:", this.path_to_properties_file.getPath());
            fnfe.printStackTrace();
            System.exit(1);
        }
        //Loading from JAXB-compiled entities
        try {
            log.log(Level.INFO, "Parsing properties from {0}.", this.path_to_properties_file.getPath());
            this.properties = this.loadPropertiesFromXML(inputStream);
        } catch (Exception e) {
            log.log(Level.INFO, "Error upon parsing properties from {0}. Please see the reason below:", this.path_to_properties_file.getPath());
            e.printStackTrace();
            System.exit(1);
        }
        //Checking properties
        try {
            //If does not exist yet - create one
            this.GenBLASTerHomeDir = new File(this.properties.getGenBLASTerHomeDir().getDirectory());
            this.createGenBLASTerHomeDir();
            this.createGenBLASTerDB_Dir();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            if (!this.properties.getRoleInCluster().getRole().equals("satellite")
                    && !this.properties.getRoleInCluster().getRole().equals("core")) {
                throw new Exception("Undefined role in cluster: is it supposed to be a satellite or the core?");
            } else if (this.properties.getRoleInCluster().getRole().equals("satellite")) {
                this.isCore = false;
                log.log(Level.INFO, "Running in SATELLITE mode.");
            } else {
                this.isCore = true;
                log.log(Level.INFO, "Running in CORE mode.");
            }
        } catch (Exception e) {
            log.log(Level.INFO, "Error upon checking \"RoleInCluster\" property.");
            e.printStackTrace();
            System.exit(1);
        }
        //Checking ClusterProperties
        try {
            if (this.properties.getClusterProperties().getUseCluster().getUse().equals("yes")) {
                this.useCluster = true;
            } else if (this.properties.getClusterProperties().getUseCluster().getUse().equals("no")) {
                this.useCluster = false;
            } else {
                throw new Exception("Could not understand what to do about the cluster.");
            }
        } catch (Exception e) {
            log.log(Level.INFO, "Error upon checking \"UseCluster\" property.");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            this.numberOfSimultaniousBLASTs = Integer.valueOf(this.properties.getBLASTProperties().getSimultaniousBLASTs().getNumber());
        } catch (Exception e) {
            log.log(Level.INFO, "Error upon checking \"SimultaniousBLASTs\" property.");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            this.Evalue = Double.valueOf(this.properties.getBLASTProperties().getEvalue().getValue());
        } catch (Exception e) {
            log.log(Level.INFO, "Error upon checking \"Evalue\" property.");
            e.printStackTrace();
            System.exit(1);
        }
        if (this.isCore) {
            try {
                if (this.properties.getDistanceMatrixType().getType().equals("average")) {
                    this.dt = GenomeCrossBLAST_sluice.distance_type.average;
                } else if (this.properties.getDistanceMatrixType().getType().equals("median")) {
                    this.dt = GenomeCrossBLAST_sluice.distance_type.median;
                } else {
                    throw new Exception("Could not understand what to do about the matrix.");
                }
            } catch (Exception e) {
                log.log(Level.INFO, "Error upon checking \"DistanceMatrixType\" property.");
                e.printStackTrace();
                System.exit(1);
            }
            try {
                this.minORF_Length = Integer.valueOf(this.properties.getBLASTProperties().getMinORFLength().getValue());
            } catch (Exception e) {
                log.log(Level.INFO, "Error upon checking \"MinORFLength\" property.");
                e.printStackTrace();
                System.exit(1);
            }
            try {
                this.maxORF_Length = Integer.valueOf(this.properties.getBLASTProperties().getMaxORFLength().getValue());
            } catch (Exception e) {
                log.log(Level.INFO, "Error upon checking \"MaxORFLength\" property.");
                e.printStackTrace();
                System.exit(1);
            }
            try {
                this.ORFPool = Integer.valueOf(this.properties.getBLASTProperties().getORFPool().getValue());
            } catch (Exception e) {
                log.log(Level.INFO, "Error upon checking \"ORFPool\" property.");
                e.printStackTrace();
                System.exit(1);
            }
            if (this.useCluster) {
                try {
                    this.clusterSatellites=new ArrayList<>(this.properties.getClusterProperties().getClusterSatellites().getClusterSatellite().size());
                    for (ClusterSatellite clusterSatellite : this.properties.getClusterProperties().getClusterSatellites().getClusterSatellite()) {
                        if (clusterSatellite.getBatchSize().length()!=0
                                && clusterSatellite.getName().length()!=0
                                && clusterSatellite.getPort().length()!=0
                                && clusterSatellite.getUri().length()!=0) {
                            if (!clusterSatellite.getSummon().equals("yes")
                                    && !clusterSatellite.getSummon().equals("no")) {
                                throw new Exception("So, is it allowed to summon the the cluster " + clusterSatellite.getName() + "? (yes/no).");
                            } else {
                                char[] chars = clusterSatellite.getBatchSize().toCharArray();
                                for (char c : chars) {
                                    if (!Character.isDigit(c)) {
                                        throw new Exception("Batch number can not contain characters >> err in " + clusterSatellite.getName() + ".");
                                    }
                                }
                                chars = clusterSatellite.getPort().toCharArray();
                                for (char c : chars) {
                                    if (!Character.isDigit(c)) {
                                        throw new Exception("Port number can not contain characters >> err in " + clusterSatellite.getName() + ".");
                                    }
                                }
                                this.clusterSatellites.add(clusterSatellite);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.log(Level.INFO, "Error upon checking \"ClusterSatellite\" checks. {0}", e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        } else {
            try {
                if (this.properties.getRoleInCluster().getSatelliteProperties().getName().length()!=0) {
                    this.name = this.properties.getRoleInCluster().getSatelliteProperties().getName();
                }else{
                    throw new Exception("Name on a satellite can not be empty as long as it is needed for the RMI.");
                }
                if (this.properties.getRoleInCluster().getSatelliteProperties().getUri().length()!=0) {
                    this.preferred_satellite_uri = this.properties.getRoleInCluster().getSatelliteProperties().getUri();
                }else{
                    throw new Exception("URI on a satellite can not be empty as long as it is needed for the RMI.");
                }
                char[] chars = this.properties.getRoleInCluster().getSatelliteProperties().getPort().toCharArray();
                for (char c : chars) {
                    if (!Character.isDigit(c)) {
                        throw new Exception("Satellite port number can not contain characters >> err is " + c + ".");
                    }
                }
                this.port=Integer.valueOf(this.properties.getRoleInCluster().getSatelliteProperties().getPort());
            } catch (Exception e) {
                log.log(Level.INFO, "Error upon checking \"ORFPool\" property.");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public List<Genome> getGenomeSet() {
        return this.properties.getGenomes().getGenome();
    }

    private void createGenBLASTerDB_Dir() {
        if (!(this.GenBLASTerDB_Dir = new File(this.properties.getGenBLASTerDbDir().getDirectory())).exists()) {
            log.log(Level.INFO, "Creatring home folder for the DataBase {0}.", this.GenBLASTerDB_Dir.getPath());
            this.GenBLASTerDB_Dir.mkdir();
        }
//        System.setProperty(Genblaster_Constants., this.GenBLASTerDB_Dir.getPath());
        //
        System.setProperty("derby.system.home", this.GenBLASTerDB_Dir.getPath());
        //
    }

    private void createGenBLASTerHomeDir() throws Exception {
        String platform = null;
        switch (Genblaster_Constants.ensurePlatform()) {
            case windows: {
                platform = Genblaster_Constants.windows_folder_name;
                break;
            }
            case linux: {
                platform = Genblaster_Constants.linux_folder_name;
                break;
            }
            case macos: {
                platform = Genblaster_Constants.macos_folder_name;
                break;
            }
            case other: {
                log.log(Level.INFO, "You seem to be running an unknown platfrom, sorry.");
                System.exit(1);
            }
        }
        this.ncbi_folder = new File(this.GenBLASTerHomeDir.getPath() + SystemUtil.SysFS + Genblaster_Constants.ncbi_folder);
        System.setProperty(Genblaster_Constants.ncbi_system_home, this.ncbi_folder.getPath());
        this.exectutables_containing_folder = new File(this.ncbi_folder.getPath() + SystemUtil.SysFS + platform);
        System.setProperty(Genblaster_Constants.ncbi_execs_system_home, this.exectutables_containing_folder.getPath());
        //Create the file system tree
        if (!this.GenBLASTerHomeDir.exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.GenBLASTerHomeDir.getPath());
            this.GenBLASTerHomeDir.mkdir();
        }
        System.setProperty(Genblaster_Constants.genblaster_system_home, this.GenBLASTerHomeDir.getPath());
        if (!(this.BLAST_DB_folder = new File(this.GenBLASTerHomeDir.getPath() + SystemUtil.SysFS + Genblaster_Constants.BLAST_DB_folder)).exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.BLAST_DB_folder.getPath());
            this.BLAST_DB_folder.mkdir();
        }
        System.setProperty(Genblaster_Constants.BLAST_DB_folder, this.BLAST_DB_folder.getPath());
        if (!(this.BLAST_RESULT_folder = new File(this.GenBLASTerHomeDir.getPath() + SystemUtil.SysFS + Genblaster_Constants.BLAST_RESULT_folder)).exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.BLAST_RESULT_folder.getPath());
            this.BLAST_RESULT_folder.mkdir();
        }
        System.setProperty(Genblaster_Constants.BLAST_RESULT_folder, this.BLAST_RESULT_folder.getPath());
        if (!(this.BLAST_TASK_folder = new File(this.GenBLASTerHomeDir.getPath() + SystemUtil.SysFS + Genblaster_Constants.BLAST_TASK_folder)).exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.BLAST_TASK_folder.getPath());
            this.BLAST_TASK_folder.mkdir();
        }
        System.setProperty(Genblaster_Constants.BLAST_TASK_folder, this.BLAST_TASK_folder.getPath());
        if (!(this.matrix_folder = new File(this.GenBLASTerHomeDir.getPath() + SystemUtil.SysFS + Genblaster_Constants.matrix_folder)).exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.matrix_folder.getPath());
            this.matrix_folder.mkdir();
        }
        System.setProperty(Genblaster_Constants.matrix_folder, this.matrix_folder.getPath());
        if (!this.ncbi_folder.exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.ncbi_folder.getPath());
            this.ncbi_folder.mkdir();
            //Now check out what's the platform
            this.exectutables_containing_folder.mkdir();
            //Now download a sutable BLASTP executable from NCBI
            this.updateBLASTGear();
        }
        if (!this.exectutables_containing_folder.exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.exectutables_containing_folder.getPath());
            this.exectutables_containing_folder.mkdir();
            this.updateBLASTGear();
        }
        System.setProperty(Genblaster_Constants.makeblastdb_system_home, this.exectutables_containing_folder.getPath()
                + SystemUtil.SysFS + Genblaster_Constants.makeblastdb);
        System.setProperty(Genblaster_Constants.blastp_system_home, this.exectutables_containing_folder.getPath()
                + SystemUtil.SysFS + Genblaster_Constants.blastp);
        if (!(this.ORF_Base_folder = new File(this.GenBLASTerHomeDir.getPath() + SystemUtil.SysFS + Genblaster_Constants.ORF_Base_folder)).exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.ORF_Base_folder.getPath());
            this.ORF_Base_folder.mkdir();
        }
        System.setProperty(Genblaster_Constants.ORF_Base_folder, this.ORF_Base_folder.getPath());
        if (!(this.CDS_Base_folder = new File(this.GenBLASTerHomeDir.getPath() + SystemUtil.SysFS + Genblaster_Constants.CDS_Base_folder)).exists()) {
            log.log(Level.INFO, "Creatring home folder {0}.", this.CDS_Base_folder.getPath());
            this.CDS_Base_folder.mkdir();
        }
        System.setProperty(Genblaster_Constants.CDS_Base_folder, this.CDS_Base_folder.getPath());
    }

    private void updateBLASTGear() {
        String platform_selector = null;
        if (SystemUtil.capacity.equals("64")) {
            switch (Genblaster_Constants.ensurePlatform()) {
                case windows: {
                    platform_selector = Genblaster_Constants.NCBI_FTP_win64;
                    break;
                }
                case linux: {
                    platform_selector = Genblaster_Constants.NCBI_FTP_linux64;
                    break;
                }
                case macos: {
                    platform_selector = Genblaster_Constants.NCBI_FTP_macos;
                    break;
                }
                case other: {
                    log.log(Level.INFO, "You seem to be running an unknown platfrom, sorry.");
                    System.exit(1);
                }
            }
        } else {
            switch (Genblaster_Constants.ensurePlatform()) {
                case windows: {
                    platform_selector = Genblaster_Constants.NCBI_FTP_win32;
                    break;
                }
                case linux: {
                    platform_selector = Genblaster_Constants.NCBI_FTP_linux32;
                    break;
                }
                case macos: {
                    platform_selector = Genblaster_Constants.NCBI_FTP_macos;
                    break;
                }
                case other: {
                    log.log(Level.INFO, "You seem to be running an unknown platfrom, sorry.");
                    System.exit(1);
                }
            }
        }

        try {
            //Select which one you need for the GenBLASTer
            FTPClient client = new FTPClient();
            FileOutputStream fos = null;
            File NCBI_BLAST_Gear_archive = null;
            try {
                client.connect(Genblaster_Constants.NCBI_FTP);
                client.login(Genblaster_Constants.anonymous,
                        Genblaster_Constants.anonymous);
                client.setFileType(FTP.BINARY_FILE_TYPE);
                client.cwd(Genblaster_Constants.NCBI_FTP_BLAST_EXECS);
                FTPFile[] ftpFiles = client.listFiles();
                for (FTPFile ftpFile : ftpFiles) {
                    if (ftpFile.getType() == FTPFile.FILE_TYPE) {
                        if (ftpFile.getName().contains(platform_selector)) {
                            log.log(Level.INFO, "Downloading NCBI BLAST gear: {0}.", ftpFile.getName());
                            fos = new FileOutputStream((NCBI_BLAST_Gear_archive = new File(this.exectutables_containing_folder
                                    + SystemUtil.SysFS + ftpFile.getName())));
                            client.retrieveFile(ftpFile.getName(), fos);
                            log.log(Level.INFO, "NCBI BLAST gear ({0}) downloaded successfully.", ftpFile.getName());
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                client.disconnect();
                e.printStackTrace();
            } finally {
                try {
                    client.logout();
                    client.disconnect();
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    log.log(Level.INFO, "Downloading NCBI BLAST gear failed, could not procede..");
                    System.exit(1);
                    e.printStackTrace();
                }
            }
            try {
                log.log(Level.INFO, "Extracting NCBI BLAST gear.");
                InputStream input = new FileInputStream(NCBI_BLAST_Gear_archive);
                input = new GZIPInputStream(input);
                TarInputStream tar = new TarInputStream(input);
                TarEntry entry;
                while (null != (entry = tar.getNextEntry())) {
                    int bytesRead;
                    if (entry.isDirectory()) {
                        continue;
                    }
                    byte[] buf = new byte[1024];
                    fos = new FileOutputStream(this.exectutables_containing_folder.getPath()
                            + SystemUtil.SysFS + entry.getName().substring(entry.getName().lastIndexOf("/") + 1));
                    while ((bytesRead = tar.read(buf, 0, 1024)) > -1) {
                        fos.write(buf, 0, bytesRead);
                    }
                    try {
                        if (null != fos) {
                            fos.close();
                        }
                    } catch (Exception e) {
                    }
                    log.log(Level.INFO, "{0} from NCBI BLAST gear extracted.", entry.getName());
                }
            } catch (Exception e) {
                log.log(Level.INFO, "Failed to extract NCBI BLAST gear..");
                e.printStackTrace();
                System.exit(1);
            }
        } catch (Exception e) {
            log.log(Level.INFO, "Error..");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private GenBLASTerProperties loadPropertiesFromXML(InputStream in) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(GenBLASTerProperties.class);
        Unmarshaller u = jc.createUnmarshaller();
        XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        xmlreader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        xmlreader.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                String file = null;
                if (systemId.contains("properties.dtd")) {
                    file = "properties.dtd";
                }
                return new InputSource(GenBLASTerProperties.class.getResourceAsStream(file));
            }
        });
        InputSource input = new InputSource(in);
        Source source = new SAXSource(xmlreader, input);
        return (GenBLASTerProperties) u.unmarshal(source);
    }

    public static PropertiesLoader newInstance(File path_to_properties_file) {
        return new PropertiesLoader(path_to_properties_file);
    }
}
