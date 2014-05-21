/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.var;

import genblaster.NCBI.BLAST_DB.Genblaster_Constants;
import genblaster.properties.PropertiesLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


/**
 *
 * @author Alexander
 */
public class Immediate {
    public static void main (String[]args){
        try{
//        System.out.println((int)(4/3));
//                System.out.println((int)(4/3)*3);
//        StringBuilder builder=new StringBuilder();
//        builder.append(1);
//         System.out.println(new String(builder));
        System.out.println(System.getProperty("os.name"));
        System.out.println(System.getProperty("sun.arch.data.model"));
        System.out.println(new File("C:\\"));
        
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
