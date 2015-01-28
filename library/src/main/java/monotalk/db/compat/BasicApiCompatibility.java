package monotalk.db.compat;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteTransactionListener;

/**
 * Basic class which provides no-op methods for allColumns Android version.
 *
 * @author graywatson
 */
public class BasicApiCompatibility implements ApiCompatibility {

    public Cursor rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs, CancellationHook cancellationHook) {
        // NOTE: cancellationHook will always be null
        return db.rawQuery(sql, selectionArgs);
    }

    public CancellationHook createCancellationHook() {
        return null;
    }

    @Override
    public String buildQuery(SQLiteQueryBuilder builder, String[] projectionIn, String selection, String groupBy, String having, String sortOrder, String limit) {
        return builder.buildQuery(projectionIn, selection, null, groupBy, having, sortOrder, limit);
    }

    @Override
    public MatrixCursor newMatrixCursor(String[] columnNames, int initialCapacity) {
        return new MatrixCursorCompat(columnNames, initialCapacity);
    }

    @Override
    public void beginTransactionNonExclusive(SQLiteDatabase db) {
        db.beginTransaction();
    }

    @Override
    public void beginTransactionWithListenerNonExclusive(SQLiteDatabase db, SQLiteTransactionListener listener) {
        db.beginTransactionWithListener(listener);
    }

    @Override
    public int executeUpdateDelete(SQLiteDatabase db, SQLiteStatement statement) {
        int updateCount = 0;
        // UPDATE実行
        statement.execute();
        SQLiteStatement selectStatement = null;
        try {
            selectStatement = db.compileStatement("select changes()");
            updateCount = (int) selectStatement.simpleQueryForLong();
        } catch (android.database.SQLException e) {
            // ignore the exception and just return 1
            updateCount = 1;
        } finally {
            if (selectStatement != null) {
                selectStatement.close();
            }
        }
        return updateCount;
    }

    @Override
    public void bindAllArgsAsStrings(SQLiteStatement realStatement, String[] bindArgs) {
        if (bindArgs != null) {
            for (int i = bindArgs.length; i != 0; i--) {
                realStatement.bindString(i, bindArgs[i - 1]);
            }
        }
    }
}
