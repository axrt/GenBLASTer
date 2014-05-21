/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice;

import genblaster.NCBI.BLAST.BLAST_Output;
import genblaster.NCBI.BLAST.BLAST_Task;
import genblaster.NCBI.BLAST.CrossBLASTHit;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Alexander
 */
public abstract class GenomeCrossBLAST_sluice {
    public static enum distance_type{
        average,
        median;
    }
    protected Connection connection;

    protected GenomeCrossBLAST_sluice(Connection connection) {
        this.connection = connection;
    }
    
    public abstract LinkedList<Long> assembleORF_ID_List_forAGenome(String genomeName) throws Exception;
    public abstract boolean databaseContainsResultsForORFOverGenome(long ORF_ID, String targetGenomeName)throws Exception;
    public abstract Set<Long> findOutWhichORFsHaveAllreadyBeenBLASTedAgainstTheTargerGenome(String queryGenomeName,String targetGenomeName)throws Exception;
    public abstract BLAST_Task assembleBLAST_Task(long ORF_ID, String targetGenomeName, String Evalue)throws Exception;
    public abstract List<BLAST_Output> assembleBLAST_OutputForA_PairOfGenomes(String queryGenomeName, String targetGenomeName)throws Exception;
    public abstract double calculateTaxonomicDistance(String queryGenomeName,String targetGenomeName,distance_type dt)throws Exception;
    public abstract double calculateTaxonomicDistance(List<CrossBLASTHit> crossBLAST_Hits,distance_type dt)throws Exception;
    public abstract List<CrossBLASTHit> calculateCrossBLAST_Hits(List<BLAST_Output> queryBLASToutputs,List<BLAST_Output> targetBLASToutputs)throws Exception;
    public abstract int saveTaxonomicDistance(String a_genome_name,String b_genome_name, double taxonomicDistance) throws Exception;
    public abstract long[] findOutWhichORFsNeedToBeBLASTedAgainstTheTargerGenome(String queryGenomeName, String targetGenomeName) throws Exception;
}
