/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice.derby;

import genblaster.db.DatabaseConstants;
import genblaster.db.sluice.Matrix_sluice;
import genblaster.output.FitchMatrix;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class DerbyMatrix_sluice extends Matrix_sluice {

    private static final Logger log = Logger.getLogger(DerbyMatrix_sluice.class.getName());

    protected DerbyMatrix_sluice(Connection connection) {
        super(connection);
    }

    @Override
    public FitchMatrix calculateFitchMatrix(List<Integer> distance_ids) throws Exception {
        log.log(Level.INFO, "Loading matrix from database.");
        Statement statement = this.connection.createStatement();
        ResultSet resultSet;
        List<String[]> databaseRows = new ArrayList<>(distance_ids.size());
        String[] row;
        for (Integer integer : distance_ids) {
            resultSet = statement.executeQuery(" SELECT "
                    + DatabaseConstants.A_GENOME_NAME + ","
                    + DatabaseConstants.B_GENOME_NAME + ","
                    + DatabaseConstants.DISTANCE
                    + " FROM "
                    + DatabaseConstants.GENBLASTER + DatabaseConstants.DISTANCE
                    + " WHERE "
                    + DatabaseConstants.ID + "=" + integer);
            if (resultSet.next()) {
                row = new String[3];
                row[0] = resultSet.getString(1);
                row[1] = resultSet.getString(2);
                row[2] = String.valueOf(String.format("%.4g%n", resultSet.getDouble(3)).trim());
                databaseRows.add(row);
            }
        }
        //Frist create a legend
        Set<String> legendNames = new HashSet<>(distance_ids.size() + 1);
        for (Object[] r : databaseRows) {
            legendNames.add((String) r[0]);
            legendNames.add((String) r[1]);
        }
        String[] legend_names = legendNames.toArray(new String[legendNames.size()]);
        HashMap<Integer, String> legend = new HashMap<>(legend_names.length);
        for (int i = 0; i < legend_names.length; i++) {
            legend.put(i,legend_names[i]);
        }

        //Now to the matrix
        String[][] matrix = new String[legend_names.length + 1][legend_names.length];
        for (int i = 0; i < matrix[0].length; i++) {
            matrix[0][i] = String.valueOf(i);
        }
        for(int j=1;j<matrix.length;j++){
            for(int i=0;i<matrix[0].length;i++){
                if(i==j-1){
                    matrix[j][i]="0.000";
                }else{
                    for(String[] strings:databaseRows){
//                            System.out.println(legend.get(i));
//                            System.out.println(legend.get(j-1));
//                            System.out.println("_____");
                        if(strings[0].equals(legend.get(i))&&strings[1].equals(legend.get(j-1))){  
                            matrix[j][i]=strings[2];
                            matrix[i+1][j-1]=strings[2];
                            break;
                        }
//                        if(strings[0].equals(legend.get(j-1))&&strings[1].equals(legend.get(i))){  
//                            matrix[j][i]=strings[2];
//                            //matrix[i+1][j]=strings[2];
//                            break;
//                        }
                    }
                }
            }
        }
        return FitchMatrix.newInstanceFromMatrixAndLegend(this, matrix, legend);
    }

    public static DerbyMatrix_sluice newInstance(Connection connection) {
        return new DerbyMatrix_sluice(connection);
    }
}
