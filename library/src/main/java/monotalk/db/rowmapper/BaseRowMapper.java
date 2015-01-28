package monotalk.db.rowmapper;

import android.database.Cursor;

/**
 * Created by Kem on 2015/01/24.
 */
abstract class BaseRowMapper<T> implements RowMapper<T> {
    @Override
    public T mapRowAndClose(Cursor cursor) {
        T reuslt = null;
        if (cursor.moveToFirst()) {
            reuslt = mapRow(cursor);
        }
        cursor.close();
        return reuslt;
    }
}
