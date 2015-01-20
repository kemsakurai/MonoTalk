package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

public class StringConverter extends BaseTypeConverter<String> {

    @Override
    public String unpack(Cursor c, int index) {
        return c.getString(index);
    }

    @Override
    public void pack(String object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(String object) {
        return DatabaseUtils.sqlEscapeString(object);
    }

    @Override
    public String toBindSql(String object) {
        return object;
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.TEXT;
    }

}
