package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;

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
    public String toBindSql(Float object) {
        return String.valueOf(object);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.REAL;
    }

}
