package monotalk.db.rowmapper;

import android.database.Cursor;

/**
 * Parameterized version similar to Spring's RowMapper which converts a result
 * row into an object.
 *
 * @param <T> Type that the mapRow returns.
 * @author Kem
 */
public interface RowMapper<T> {

    /**
     * Used to convert a results row to an object.
     *
     * @param results Results object we are mapping.
     * @return The created object with allColumns of the fields values from the results;
     */
    public T mapRow(Cursor cursor);
}