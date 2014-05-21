/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.io;

/**
 *
 * @author Alexander Tuzhikov
 */
import java.io.*;

public class aFileSaver implements Serializable{

    protected aFileSaver() {
    }

    public void SaveTextFile(String textToSave, File targetFile) throws Exception {
        //Input checks here
        try {
            //Writing the file here
            FileWriter outFile = new FileWriter(targetFile);
            PrintWriter out = new PrintWriter(outFile);
            out.print(textToSave);
            out.close();
        } catch (IOException e) {
            System.out.println("Error during file write..");//TODO redo for a log
            e.printStackTrace();
            //Input what to do in case there is no file. There should be one)))
        }//This is crap, I should think of a way to save with a better way.
    }

    public void SaveObjectFile(Object object, File pathToFile) throws Exception {//TODO redo path to a "File"
        //Input checks here
        try {
            FileOutputStream fileOut = new FileOutputStream(pathToFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public static aFileSaver newInstance(){
        return new aFileSaver();
    }
}
