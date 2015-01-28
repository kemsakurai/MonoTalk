package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;

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
    public String toStringBindArg(Long object) {
        return String.valueOf(object);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.INTEGER;
    }

    @Override
    public void bind(SQLiteProgram program, int index, Long value) {
        program.bindLong(index, value);
    }
}
