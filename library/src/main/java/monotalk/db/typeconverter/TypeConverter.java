package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;

public interface TypeConverter<T> {
    public T unpack(Cursor c, String name);

    public T unpack(Cursor c, int index);

    public void pack(T object, ContentValues cv, String name);

    public String toSql(T object);

    public String toStringBindArg(T object);

    public SQLiteType getSqlType();

    public void bind(SQLiteProgram program, int index, T value);

    enum SQLiteType {
        INTEGER, REAL, TEXT, BLOB
    }
}
