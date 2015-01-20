package monotalk.db.valuesmapper;

import android.content.ContentValues;

/**
 * Parameterized version similar to Spring's RowMapper which converts a result
 * row into an object.
 *
 * @param <T> Type that the mapValues returns.
 * @author Kem
 */
public interface ValuesMapper<T> {

    /**
     * Used to convert a results row to an object.
     *
     * @param results Results object we are mapping.
     * @return The created object with allColumns of the fields values from the results;
     */
    public ContentValues mapValues(T object);
}