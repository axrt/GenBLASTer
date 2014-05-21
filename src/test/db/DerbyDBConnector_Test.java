/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.db;

import genblaster.db.derby.DerbyDBConnector;

/**
 *
 * @author Alexander
 */
public class DerbyDBConnector_Test {
    public static void main (String[]args){
        DerbyDBConnector derbyDBConnector= DerbyDBConnector.Instance;
        try{
        derbyDBConnector.connectToEmbeddedDatabase();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
