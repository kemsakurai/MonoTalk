package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;

public class FloatConverter extends BaseTypeConverter<Float> {

    @Override
    public Float unpack(Cursor c, int index) {
        return c.getFloat(index);
    }

    @Override
    public void pack(Float object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(Float object) {
        return String.valueOf(object);
    }

    @Override
    public String toStringBindArg(Float object) {
        return String.valueOf(object);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.REAL;
    }

    @Override
    public void bind(SQLiteProgram program, int index, Float value) {
        program.bindDouble(index, value);
    }
}
