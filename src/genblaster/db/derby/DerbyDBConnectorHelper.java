/*
 * This class just handles the driver loading procees.
 * 
 */
package genblaster.db.derby;

/**
 *
 * @author Alexander Tuzhikov
 */
public class DerbyDBConnectorHelper {
    
    /**
     * A private constructor to ensure noninstantiability
     */
    private DerbyDBConnectorHelper() {
        throw new AssertionError();
    }

    /**
     * The embedded driver, org.apache.derby.jdbc.EmbeddedDriver
     */
    private static final String derbyEmbeddedDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    /**
     * Loads a driver for the embedded derby database
     */
    protected static boolean loadDerbyDriver() {
        try {
            Class.forName(DerbyDBConnectorHelper.derbyEmbeddedDriver);
            return  true;
        } catch (ClassNotFoundException ex) {
            //Do smth about the exception, however, there is not much to do, probably exit(1)
            ex.printStackTrace();
            return false;
        }
    }
}
