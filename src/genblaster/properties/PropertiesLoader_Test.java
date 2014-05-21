/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.properties;

import java.io.File;

/**
 *
 * @author Alexander
 */
public class PropertiesLoader_Test {
    public static void main (String[]args){
        try{
        PropertiesLoader loader= PropertiesLoader.newInstance(new File("C:\\Users\\Alexander\\Documents\\NetBeansProjects\\GenBLASTer\\src\\genblaster\\properties\\driver.xml"));
        loader.loadAndCheckProperties();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
