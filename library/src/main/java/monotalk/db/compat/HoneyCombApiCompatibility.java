package monotalk.db.compat;

import android.annotation.TargetApi;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;

/**
 * Basic class which provides no-op methods for allColumns Android version.
 * <p/>
 * <p>
 * <b>NOTE:</b> Will show as in error if compiled with previous Android versions.
 * </p>
 *
 * @author graywatson
 */
public class HoneyCombApiCompatibility extends BasicApiCompatibility {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public String buildQuery(SQLiteQueryBuilder builder, String[] projectionIn, String selection, String groupBy, String having, String sortOrder, String limit) {
        return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void beginTransactionNonExclusive(SQLiteDatabase db) {
        db.beginTransactionNonExclusive();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public int executeUpdateDelete(SQLiteDatabase db, SQLiteStatement statement) {
        int updateCount = 0;
        updateCount = statement.executeUpdateDelete();
        return updateCount;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void bindAllArgsAsStrings(SQLiteStatement realStatement, String[] bindArgs) {
        realStatement.bindAllArgsAsStrings(bindArgs);
    }
}
