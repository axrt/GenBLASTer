/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.NCBI.BLAST;

import genblaster.db.DatabaseConnector;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 *
 * @author Alexander
 */
public class BLAST_Output_Saver_factory extends BasePoolableObjectFactory<BLAST_Output_Saver> {

    private DatabaseConnector databaseConnector;
    private StackObjectPool<BLAST_Output_Saver> savers_home;

    private BLAST_Output_Saver_factory(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    @Override
    public BLAST_Output_Saver makeObject() throws Exception {
        return BLAST_Output_Saver.newInstanceFromSluiceOnly(this.savers_home, this.databaseConnector.newBLAST_Output_Saver_sluice());
    }

    public void setSavers_home(StackObjectPool<BLAST_Output_Saver> savers_home) {
        this.savers_home = savers_home;
    }
    
    public static BLAST_Output_Saver_factory newInstance(DatabaseConnector databaseConnector) {
        BLAST_Output_Saver_factory saver_factory=new BLAST_Output_Saver_factory(databaseConnector);
        return saver_factory;
    }
}
