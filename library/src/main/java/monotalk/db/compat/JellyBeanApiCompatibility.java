package monotalk.db.compat;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.CancellationSignal;

/**
 * Basic class which provides no-op methods for allColumns Android version.
 * <p/>
 * <p>
 * <b>NOTE:</b> Will show as in error if compiled with previous Android versions.
 * </p>
 *
 * @author graywatson
 */
public class JellyBeanApiCompatibility extends BasicApiCompatibility {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Cursor rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs, CancellationHook cancellationHook) {
        if (cancellationHook == null) {
            return db.rawQuery(sql, selectionArgs);
        } else
            return db.rawQuery(sql, selectionArgs, ((JellyBeanCancellationHook) cancellationHook).cancellationSignal);
    }

    @Override
    public CancellationHook createCancellationHook() {
        return new JellyBeanCancellationHook();
    }

    @Override
    public MatrixCursor newMatrixCursor(String[] columnNames, int initialCapacity) {
        return new MatrixCursor(columnNames, initialCapacity);
    }

    /**
     * Hook object that supports canceling a running query.
     */
    protected static class JellyBeanCancellationHook implements CancellationHook {
        private final CancellationSignal cancellationSignal;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public JellyBeanCancellationHook() {
            this.cancellationSignal = new CancellationSignal();
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void cancel() {
            cancellationSignal.cancel();
        }
    }
}
