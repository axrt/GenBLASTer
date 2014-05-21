/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.output;

import genblaster.db.sluice.Matrix_sluice;
import genblaster.io.aFileSaver;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Alexander
 */
public abstract class Matrix {
    
    protected static final String matrixFileName="matrix";
    protected static final String legendFileName="legend";
    protected String matrixFileNameExtension;
    protected String legendFileNameExtension;
    protected String[][] matrix;
    protected String text_matrix;
    protected String text_legend;
    protected HashMap<Integer,String> legend;
    protected aFileSaver fileSaver;
    protected Matrix_sluice matrix_sluice;
    protected List<Integer> distance_ids;

    protected Matrix(Matrix_sluice matrix_sluice) {
        this.matrix_sluice = matrix_sluice;
        this.fileSaver=aFileSaver.newInstance();
    }
    public abstract String formText();
    public abstract String formLegend();
    public abstract void saveMatrixToFile(File directory) throws Exception;
}
