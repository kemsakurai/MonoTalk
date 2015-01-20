package monotalk.db.compat;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteTransactionListener;

/**
 * Compatibility interface to support various different versions of the Android API.
 *
 * @author graywatson
 */
public interface ApiCompatibility {

    /**
     * Perform a raw query on a database with an optional cancellation-hook.
     */
    public Cursor rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs, CancellationHook cancellationHook);

    /**
     * Return a cancellation hook object that will be passed to the
     * {@link #rawQuery(android.database.sqlite.SQLiteDatabase, String, String[], monotalk.db.compat.ApiCompatibility.CancellationHook)}. If not supported then this will return
     * null.
     */
    public CancellationHook createCancellationHook();

    /**
     * @param builder
     * @param projectionIn
     * @param selection
     * @param groupBy
     * @param having
     * @param sortOrder
     * @param limit
     * @return
     */
    public String buildQuery(SQLiteQueryBuilder builder, String[] projectionIn, String selection, String groupBy, String having, String sortOrder, String limit);

    /**
     * @param columnNames
     * @param initialCapacity
     * @return
     */
    public MatrixCursor newMatrixCursor(String[] columnNames, int initialCapacity);

    /**
     * @param db
     */
    public void beginTransactionNonExclusive(SQLiteDatabase db);

    /**
     * @param db
     * @param listener
     */
    public void beginTransactionWithListenerNonExclusive(SQLiteDatabase db, SQLiteTransactionListener listener);

    /**
     * @param db
     * @param statement
     * @return
     */
    public int executeUpdateDelete(SQLiteDatabase db, SQLiteStatement statement);

    /**
     * @param realStatement
     * @param bindArgs
     */
    public void bindAllArgsAsStrings(SQLiteStatement realStatement, String[] bindArgs);

    /**
     * Cancellation hook class returned by {@link monotalk.db.compat.ApiCompatibility#createCancellationHook()}.
     */
    public interface CancellationHook {
        /**
         * Cancel the associated query.
         */
        public void cancel();
    }
}
