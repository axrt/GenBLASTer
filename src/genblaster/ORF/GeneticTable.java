/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.ORF;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Alexander
 */
public class GeneticTable implements Serializable{
    
    private String name;
    private HashMap<String, Character> table;

    private GeneticTable(String name) {
        this.name=name;
    }
    
    public void alter(String codon, char aminoacid){
        if(this.table.containsKey(codon)){
            this.table.remove(codon);
        }
        this.table.put(codon, aminoacid);
    }
    
    public char translateCodon(String codon){
        if(this.table.containsKey(codon)){
            return this.table.get(codon);
        }else{
            return '*';
        }
    }

    @Override
    protected GeneticTable clone() throws CloneNotSupportedException {
        GeneticTable geneticTable=new GeneticTable(this.name);
        geneticTable.table=new HashMap<>(this.table);
        return geneticTable;
    }
    
    public static GeneticTable Standard(){
        GeneticTable geneticTable=new GeneticTable("Standard");
        geneticTable.table= new HashMap<>();
        geneticTable.table.put("GCA", 'A');
        geneticTable.table.put("GCT", 'A');
        geneticTable.table.put("GCC", 'A');
        geneticTable.table.put("GCG", 'A');
        geneticTable.table.put("ACT", 'T');
        geneticTable.table.put("ACC", 'T');
        geneticTable.table.put("ACA", 'T');
        geneticTable.table.put("ACG", 'T');
        geneticTable.table.put("CCT", 'P');
        geneticTable.table.put("CCC", 'P');
        geneticTable.table.put("CCA", 'P');
        geneticTable.table.put("CCG", 'P');
        geneticTable.table.put("TCT", 'S');
        geneticTable.table.put("TCC", 'S');
        geneticTable.table.put("TCA", 'S');
        geneticTable.table.put("TCG", 'S');
        geneticTable.table.put("AGT", 'S');
        geneticTable.table.put("AGC", 'S');
        geneticTable.table.put("GTT", 'V');
        geneticTable.table.put("GTC", 'V');
        geneticTable.table.put("GTA", 'V');
        geneticTable.table.put("GTG", 'V');
        geneticTable.table.put("ATG", 'M');
        geneticTable.table.put("ATT", 'I');
        geneticTable.table.put("ATC", 'I');
        geneticTable.table.put("ATA", 'I');
        geneticTable.table.put("TTA", 'L');
        geneticTable.table.put("CTT", 'L');
        geneticTable.table.put("TTG", 'L');
        geneticTable.table.put("CTC", 'L');
        geneticTable.table.put("CTA", 'L');
        geneticTable.table.put("CTG", 'L');
        geneticTable.table.put("TTT", 'F');
        geneticTable.table.put("TTC", 'F');
        geneticTable.table.put("GAT", 'D');
        geneticTable.table.put("GAC", 'D');
        geneticTable.table.put("GAA", 'E');
        geneticTable.table.put("GAG", 'E');
        geneticTable.table.put("AAA", 'K');
        geneticTable.table.put("AAG", 'K');
        geneticTable.table.put("AAT", 'N');
        geneticTable.table.put("AAC", 'N');
        geneticTable.table.put("CAA", 'Q');
        geneticTable.table.put("CAG", 'Q');
        geneticTable.table.put("CAT", 'H');
        geneticTable.table.put("CAC", 'H');
        geneticTable.table.put("TAA", '*');
        geneticTable.table.put("TAG", '*');
        geneticTable.table.put("TGA", '*');
        geneticTable.table.put("TAT", 'Y');
        geneticTable.table.put("TAC", 'Y');
        geneticTable.table.put("GGT", 'G');
        geneticTable.table.put("GGC", 'G');
        geneticTable.table.put("GGA", 'G');
        geneticTable.table.put("GGG", 'G');
        geneticTable.table.put("AGA", 'R');
        geneticTable.table.put("AGG", 'R');
        geneticTable.table.put("CGT", 'R');
        geneticTable.table.put("CGC", 'R');
        geneticTable.table.put("CGA", 'R');
        geneticTable.table.put("CGG", 'R');
        geneticTable.table.put("TGG", 'W');
        geneticTable.table.put("TGT", 'C');
        geneticTable.table.put("TGC", 'C');
        return geneticTable;
    }
}
