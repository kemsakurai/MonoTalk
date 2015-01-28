package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteProgram;

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
    public String toStringBindArg(String object) {
        return object;
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.TEXT;
    }

    @Override
    public void bind(SQLiteProgram program, int index, String value) {
        program.bindString(index, value);
    }
}
