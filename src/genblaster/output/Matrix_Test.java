/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.output;

import genblaster.db.derby.DerbyDBConnector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexander
 */
public class Matrix_Test {
    
    public static void main (String[]agrs){
        try{
            //Database
            DerbyDBConnector derbyDBConnector = DerbyDBConnector.defaultConnectorInstance();
            derbyDBConnector.setDataBaseWorkingDirectory(new File("/home/alext/.genblasterDB"));
            derbyDBConnector.connectToEmbeddedDatabase();
            System.out.println(System.getProperty("derby.system.home"));
            
            //Matrix
            List<Integer> distance_ids=new ArrayList<>();
            for(int i=227;i<282;i++){
                distance_ids.add(i);
            }
            FitchMatrix fitchMatrix = FitchMatrix.newInstanceFromDistanceIDs(
                    derbyDBConnector.newMatrix_sluice(), distance_ids);
            System.out.println(fitchMatrix.formText());
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
