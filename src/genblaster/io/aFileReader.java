package genblaster.io;

/*
 * A standard file reader class. Can read both text files and binary serialized
 * java classes, returns them as objects. @author Alexander Tuzhikov
 */
import java.io.*;

public class aFileReader implements Serializable{

    protected aFileReader() {
    }

    /**
     * Read a given file and returns it as {@code String}
     *
     * @param pathToFile: a {@code File}, that contains text info
     * @return a {@code String} representation of the {@code pathToFile} file
     * @throws Exception
     */
    public String ReadTextFile(File pathToFile) throws Exception {
        //Provided a directory instead of a file - complain
        if (pathToFile.isDirectory()) {
            throw new IOException("Directory selected, please point the loader to a text file.");
        }
        //Prepares a new StringBuilder
        StringBuilder fileChunksRead = new StringBuilder();
        //log.log(Level.INFO, "Loading file: {0}", pathToFile.getAbsolutePath());
        FileReader fr = new FileReader(pathToFile);
        BufferedReader br = new BufferedReader(fr);

        //Reads the file
        String line;
        while ((line = br.readLine()) != null) {
            fileChunksRead.append(line);
            fileChunksRead.append('\n');
        }
        br.close();
        fr.close();
        return new String(fileChunksRead);
    }

    /**
     * Reads a serialized {@code Object} and returns it as is - Object \* which
     * probably should then get casted to a proper type.
     *
     * @param pathToFile: a {@code File}, that contains the serialized {@code Object}
     * @return {@code Object}
     * @throws Exception
     */
    public Object ReadObjectFile(File pathToFile) throws Exception {
        //Provided a directory instead of a file - complain
        if (pathToFile.isDirectory()) {
            throw new IOException("Directory selected, please point the loader to serialized Object file.");
        }
        //log.log(Level.INFO, "Loading Object: {0}", pathToFile.getAbsolutePath());
        //Generates a FileInputStream and an ObjectInputStream
        FileInputStream input = new FileInputStream(pathToFile);
        ObjectInputStream objetcInput = new ObjectInputStream(input);
        //Gets the Object from the ObjectInputStream
        Object object = objetcInput.readObject();
        return object;
    }

    public static aFileReader newInstance() {
        return new aFileReader();
    }
}
