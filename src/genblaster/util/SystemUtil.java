/*
 * A class to contain static System helper units
 * 
 */
package genblaster.util;

import genblaster.GenBLASTer;
import java.io.File;

/**
 *
 * @author Alexander Tuzhikov
 */
public class SystemUtil {
    /**
     * System file system path separator
     */
    public static final String SysFS=System.getProperty("file.separator");
    /**
     * User home directory
     */
    public static String userHomeDir = System.getProperty("user.home", ".");
    public static String platform=System.getProperty("os.name");
    public static final String capacity=System.getProperty("sun.arch.data.model");
    public static final String jarPath = new File(GenBLASTer.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParent();
    
}
