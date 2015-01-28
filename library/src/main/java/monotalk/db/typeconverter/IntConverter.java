package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;

public class IntConverter extends BaseTypeConverter<Integer> {

    @Override
    public Integer unpack(Cursor c, int index) {
        return c.getInt(index);
    }

    @Override
    public void pack(Integer object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(Integer object) {
        return String.valueOf(object);
    }

    @Override
    public String toStringBindArg(Integer object) {
        return String.valueOf(object);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.INTEGER;
    }

    @Override
    public void bind(SQLiteProgram program, int index, Integer value) {
        program.bindLong(index, value);
    }
}
