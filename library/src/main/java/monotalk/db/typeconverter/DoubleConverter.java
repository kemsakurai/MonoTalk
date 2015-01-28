package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;

public class DoubleConverter extends BaseTypeConverter<Double> {

    @Override
    public Double unpack(Cursor c, int index) {
        return c.getDouble(index);
    }

    @Override
    public void pack(Double object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(Double object) {
        return String.valueOf(object);
    }

    @Override
    public String toStringBindArg(Double object) {
        return String.valueOf(object);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.REAL;
    }

    @Override
    public void bind(SQLiteProgram program, int index, Double value) {
        program.bindDouble(index, value);
    }
}
