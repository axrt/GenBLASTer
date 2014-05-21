/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.output;

import genblaster.db.sluice.Matrix_sluice;
import genblaster.util.SystemUtil;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Alexander
 */
public class FitchMatrix extends Matrix {

    private FitchMatrix(Matrix_sluice matrix_sluice) {
        super(matrix_sluice);
    }

    @Override
    public String formText() {
        if (this.text_matrix == null) {
            StringBuilder stringBuilder = new StringBuilder();//TODO try to figure out capacity
            stringBuilder.append("    ");
            stringBuilder.append(this.matrix[0].length);
            stringBuilder.append('\n');
            for (int i = 0; i < this.matrix[0].length; i++) {
                for (int j = 0; j < this.matrix.length; j++) {
                    if (j == 0) {
                        stringBuilder.append(this.matrix[j][i]);
                        for (int k = this.matrix[j][i].length(); k < 11; k++) {
                            stringBuilder.append(' ');
                        }
                    } else {
                        stringBuilder.append(this.matrix[j][i]);
                    }
                    if (j + 1 != this.matrix.length) {
                        stringBuilder.append(' ');
                    }
                }
                if (i + 1 != this.matrix[0].length) {
                    stringBuilder.append('\n');
                }
            }
            this.text_matrix = new String(stringBuilder);
        }
        return this.text_matrix;
    }

    @Override
    public String formLegend() {
        if(this.text_legend==null){
            StringBuilder stringBuilder = new StringBuilder();//TODO try to figure out capacity
            for(int i=0;i<this.legend.size();i++){
                stringBuilder.append(i);
                stringBuilder.append('\t');
                stringBuilder.append(this.legend.get(i));
                if(i+1!=this.legend.size()){
                    stringBuilder.append('\n');
                }
            }
            this.text_legend=new String(stringBuilder);
        }
        return this.text_legend;
    }
    

    @Override
    public void saveMatrixToFile(File directory) throws Exception {
        String matrixPath= directory.getPath()+SystemUtil.SysFS+Matrix.matrixFileName+".mtx";
        this.fileSaver.SaveTextFile(this.text_matrix, new File(matrixPath));
        String legendPath= directory.getPath()+SystemUtil.SysFS+Matrix.legendFileName+".lgn";
        this.fileSaver.SaveTextFile(this.text_legend, new File(legendPath));
    }

    public static FitchMatrix newInstanceFromDistanceIDs(Matrix_sluice matrix_sluice, List<Integer> distance_ids) throws Exception {
        FitchMatrix fitchMatrix = matrix_sluice.calculateFitchMatrix(distance_ids);
        fitchMatrix.distance_ids = distance_ids;
        return fitchMatrix;
    }

    public static FitchMatrix newInstanceFromMatrixAndLegend(Matrix_sluice matrix_sluice, String[][] matrix, HashMap<Integer, String> legend) throws Exception {
        FitchMatrix fitchMatrix = new FitchMatrix(matrix_sluice);
        fitchMatrix.matrix = matrix;
        fitchMatrix.legend = legend;
        return fitchMatrix;
    }
}
