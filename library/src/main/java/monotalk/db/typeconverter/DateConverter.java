package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

public class DateConverter extends BaseTypeConverter<Date> {

    @Override
    public Date unpack(Cursor c, int index) {
        return new Date(c.getLong(index));
    }

    @Override
    public void pack(Date object, ContentValues cv, String name) {
        cv.put(name, object.getTime());
    }

    @Override
    public String toSql(Date object) {
        return String.valueOf(object.getTime());
    }

    @Override
    public String toBindSql(Date object) {
        return String.valueOf(object.getTime());
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.INTEGER;
    }

}
