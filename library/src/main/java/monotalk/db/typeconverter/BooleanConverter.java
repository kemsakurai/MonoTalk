package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;

public class BooleanConverter extends BaseTypeConverter<Boolean> {

    @Override
    public void pack(Boolean object, ContentValues cv, String name) {
        cv.put(name, object ? 1 : 0);
    }

    @Override
    public String toSql(Boolean object) {
        return object ? "1" : "0";
    }

    @Override
    public String toStringBindArg(Boolean object) {
        return object ? "1" : "0";
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.INTEGER;
    }

    @Override
    public void bind(SQLiteProgram program, int index, Boolean value) {
        program.bindLong(index, Long.parseLong(value ? "1" : "0"));
    }
    
    @Override
    public Boolean unpack(Cursor c, int index) {
        return c.getLong(index) > 0;
    }
}

