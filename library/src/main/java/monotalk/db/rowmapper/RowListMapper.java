package monotalk.db.rowmapper;

import android.database.Cursor;

import java.util.List;

/**
 * Created by Kem on 2015/01/24.
 */
public interface RowListMapper<T> {
    public List<T> mapRowListAndClose(Cursor cursor);
}
