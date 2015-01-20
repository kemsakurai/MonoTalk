package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;

public class LongConverter extends BaseTypeConverter<Long> {

    @Override
    public Long unpack(Cursor c, int index) {
        return c.getLong(index);
    }

    @Override
    public void pack(Long object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(Long object) {
        return String.valueOf(object);
    }

    @Override
    public String toBindSql(Long object) {
        return String.valueOf(object);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.INTEGER;
    }

}
