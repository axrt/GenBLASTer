/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.genome;

import genblaster.ORF.GeneticTable;
import genblaster.io.aFileReader;
import java.io.File;
import java.io.Serializable;

/**
 *
 * @author Alexander
 */
public class DigestedGenome implements Serializable{
    private String name;
    private String directStrand;
    private String complementaryStrand;
    private GeneticTable geneticTable;
    private aFileReader fileReader;

    private DigestedGenome(String name) {
        this.fileReader= aFileReader.newInstance();
        this.name=name;
    }

    public String getDirectStrand() {
        return directStrand;
    }

    public String getComplementaryStrand() {
        return complementaryStrand;
    }

    public String getName() {
        return name;
    }

    public GeneticTable getGeneticTable() {
        return geneticTable;
    }
     
    private void deriveComplimentary() {
        char[]complementaryStrandBuilder=new char[this.directStrand.length()];
        int position=0;
        for(int i=this.directStrand.length()-1;i>-1;i--){
            complementaryStrandBuilder[position]=BasePairsTable.complimetaryNucleotide(this.directStrand.charAt(i));
            position++;
        }
        this.complementaryStrand=new String(complementaryStrandBuilder);
    }

    public static DigestedGenome newInstanceFromAGenomeFile(String name,File genomeFile, GeneticTable geneticTable) throws Exception {
        System.out.println( "Loading a genome from "+genomeFile+" file.");
        DigestedGenome digestedGenome = new DigestedGenome(name);
        String rawGenome = digestedGenome.fileReader.ReadTextFile(genomeFile);
        System.out.println( "Digesting "+name);
        rawGenome=GenomeDistiller.destileGenome(rawGenome);
        digestedGenome.geneticTable=geneticTable;
        digestedGenome.directStrand=rawGenome;
        digestedGenome.deriveComplimentary();
        System.out.println("Genome "+name+" digested.");
        return digestedGenome;
    }
}
