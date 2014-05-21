/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.genome;

import genblaster.NCBI.BLAST.GenomeCrossBLAST_Task;
import genblaster.NCBI.BLAST.GenomeCrossBLAST_Task_Charger;
import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.NCBI.BLAST_DB.NCBI_BLAST_DataBaseDeployer;
import genblaster.ORF.GeneticTable;
import genblaster.ORF.ORFScanner;
import genblaster.db.DatabaseConnector;
import genblaster.io.aFileReader;
import genblaster.io.aFileSaver;
import genblaster.properties.PropertiesLoader;
import genblaster.util.SystemUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import genblaster.properties.jaxb.Additive;
import genblaster.properties.jaxb.AlterCodon;
import genblaster.properties.jaxb.Genome;
import java.util.*;

/**
 *
 * @author Alexander
 */
public class GenomeDigester {

    private List<Genome> raw_genomes;
    private List<String> genomeNames;
    private Set<String> uncheckedGenomes;
    private PropertiesLoader propertiesLoader;
    private DatabaseConnector databaseConnector;
    private NCBI_BLAST_DataBaseDeployer deployer;
    private static final Logger log = Logger.getLogger(GenomeDigester.class.getName());
    private final aFileSaver fileSaver;
    private final aFileReader fileReader;

    protected GenomeDigester(PropertiesLoader propertiesLoader, DatabaseConnector databaseConnector) {
        this.propertiesLoader = propertiesLoader;
        this.databaseConnector = databaseConnector;
        this.raw_genomes = this.propertiesLoader.getGenomeSet();
        this.genomeNames = new ArrayList<>(raw_genomes.size());
        this.fileSaver = aFileSaver.newInstance();
        this.fileReader = aFileReader.newInstance();
        this.uncheckedGenomes = new HashSet<>();
    }

    public void processGenomes() {
        try {
            for (Genome genome : this.raw_genomes) {
                if (!this.genomePassesChecks(genome)) {
                    throw new Exception("Genome description malformed.. " + genome.getName());
                } else {
                    this.genomeNames.add(genome.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            for (Genome genome : this.raw_genomes) {
                if (genome.getNeedsCheck().equals("yes")) {
                    this.scanGenome(genome);
                    this.uncheckedGenomes.add(genome.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            this.deployer = NCBI_BLAST_DataBaseDeployer.newInstanceFromProperties(
                    this.propertiesLoader.getNumberOfSimultaniousBLASTs(), this.databaseConnector);
            this.deployBLAST_DBs();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public GenomeCrossBLAST_Task_Charger chargeCrossBLASTs() throws Exception {
        Queue<GenomeCrossBLAST_Task> genomeCrossBLAST_Tasks = new LinkedList<>();
        GenomeCrossBLAST_Task genomeCrossBLAST_Task;
        for (int i = 0; i < this.genomeNames.size(); i++) {
            for (int j = i; j < this.genomeNames.size(); j++) {
                if (i != j) {
                    if (this.uncheckedGenomes.contains(this.genomeNames.get(i))
                            || this.uncheckedGenomes.contains(this.genomeNames.get(j))) {
                        genomeCrossBLAST_Task = GenomeCrossBLAST_Task.newDefaultInstance(
                                this.genomeNames.get(i),
                                this.genomeNames.get(j),
                                this.databaseConnector.newGenomeCrossBLAST_sluice(),
                                String.valueOf(this.propertiesLoader.getEvalue()), true);
                    } else {
                        genomeCrossBLAST_Task = GenomeCrossBLAST_Task.newDefaultInstance(
                                this.genomeNames.get(i),
                                this.genomeNames.get(j),
                                this.databaseConnector.newGenomeCrossBLAST_sluice(),
                                String.valueOf(this.propertiesLoader.getEvalue()), false);
                    }
                    genomeCrossBLAST_Tasks.add(genomeCrossBLAST_Task);
                }
            }
        }
        return GenomeCrossBLAST_Task_Charger.newInstance(genomeCrossBLAST_Tasks);
    }

    private void scanGenome(Genome genome) throws Exception {
        log.log(Level.INFO, "Preparing a genetic table for genome {0}.", genome.getName());
        GeneticTable geneticTable = GeneticTable.Standard();
        for (AlterCodon alterCodon : genome.getGeneticTable().getAlterCodon()) {
            if (genome.getGeneticTable().getType().equals("altered")) {
                log.log(Level.INFO, "Altering genetic table for {0}.", genome.getName());
                geneticTable.alter(alterCodon.getCodon().toUpperCase(), alterCodon.getAminoacid().toUpperCase().charAt(0));
            }
        }
        log.log(Level.INFO, "Digesting genome {0}.", genome.getName());
        File digestedGenomeFile;
        DigestedGenome digestedGenome;
        if (!(digestedGenomeFile = new File(new File(genome.getPath()).getParent()
                + SystemUtil.SysFS + genome.getName() + Genblaster_Constants.digested_genome_ext)).exists()) {
            digestedGenome =
                    DigestedGenome.newInstanceFromAGenomeFile(
                    genome.getName(), new File(genome.getPath()), geneticTable);
            log.log(Level.INFO, "Saving digested genome {0} to {1}.", new Object[]{genome.getName(), digestedGenomeFile.getPath()});
            this.fileSaver.SaveObjectFile(digestedGenome, digestedGenomeFile);
            log.log(Level.INFO, "Scanning genome {0}.", genome.getName());
            ORFScanner orfScanner = ORFScanner.newInstance(digestedGenome,
                    this.propertiesLoader.getMinORF_Length(),
                    this.propertiesLoader.getMaxORF_Length(),
                    this.propertiesLoader.getORFPool(),
                    this.databaseConnector);
            orfScanner.startScan();
        } else {
            log.log(Level.INFO, "Genome {0} has already been digested, loading.", genome.getName());
            digestedGenome = (DigestedGenome) this.fileReader.ReadObjectFile(digestedGenomeFile);
            ORFScanner orfScanner = ORFScanner.newInstance(digestedGenome,
                    this.propertiesLoader.getMinORF_Length(),
                    this.propertiesLoader.getMaxORF_Length(),
                    this.propertiesLoader.getORFPool(),
                    this.databaseConnector);
            orfScanner.startScan();
        }
    }

    private void deployBLAST_DBs() throws Exception {
        log.log(Level.INFO, "Deploying a BLAST DataBases.");
        this.deployer.deployBLAST_DataBaseForGenomes(this.genomeNames);
    }

    private boolean genomePassesChecks(Genome genome) {
        log.log(Level.INFO, "Checking genome {0}.", genome.getName());
        if (genome.getName().length() < 1) {
            return false;
        }
        if (!new File(genome.getPath()).exists()) {
            return false;
        }
        if (!genome.getGeneticTable().getType().equals("standard")
                && !genome.getGeneticTable().getType().equals("altered")) {
            return false;
        }
        if (!genome.getNeedsCheck().equals("yes") && !genome.getNeedsCheck().equals("no")) {
            return false;
        }
        if (genome.getGeneticTable().getType().equals("altered")) {
            for (AlterCodon alterCodon : genome.getGeneticTable().getAlterCodon()) {
                if (alterCodon.getCodon().length() != 3) {
                    return false;
                }
                if (alterCodon.getAminoacid().length() != 1) {
                    return false;
                }
                //TODO input checks for allowed aminoacids
            }
        }
        for (Additive additive : genome.getAdditives().getAdditive()) {
            if (additive.getName().equals("") || additive.getName().equals(" ")) {//TODO Any other bright ideas?
                return false;
            }
            if (!new File(additive.getPath()).exists()) {
                return false;
            }
        }
        return true;
    }

    public static GenomeDigester newInstance(PropertiesLoader propertiesLoader, DatabaseConnector databaseConnector) {
        return new GenomeDigester(propertiesLoader, databaseConnector);
    }
}
