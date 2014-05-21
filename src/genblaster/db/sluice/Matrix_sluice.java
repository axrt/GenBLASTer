/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genblaster.db.sluice;

import genblaster.output.FitchMatrix;
import genblaster.output.Matrix;
import java.sql.Connection;
import java.util.List;

/**
 *
 * @author Alexander
 */
public abstract class Matrix_sluice {
    protected Connection connection;

    protected Matrix_sluice(Connection connection) {
        this.connection = connection;
    }
    public abstract FitchMatrix calculateFitchMatrix(List<Integer> distance_ids) throws Exception;
}
