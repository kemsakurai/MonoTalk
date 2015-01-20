package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;

public interface TypeConverter<T> {
    public T unpack(Cursor c, String name);

    public T unpack(Cursor c, int index);

    public void pack(T object, ContentValues cv, String name);

    public String toSql(T object);

    public String toBindSql(T object);

    public SQLiteType getSqlType();

    enum SQLiteType {
        INTEGER, REAL, TEXT, BLOB
    }
}
