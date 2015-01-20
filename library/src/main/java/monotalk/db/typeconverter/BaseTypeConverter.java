package monotalk.db.typeconverter;

import android.database.Cursor;

public abstract class BaseTypeConverter<T> implements TypeConverter<T> {

    @Override
    public T unpack(Cursor c, String name) {
        return unpack(c, c.getColumnIndexOrThrow(name));
    }
}
