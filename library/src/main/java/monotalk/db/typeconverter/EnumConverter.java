package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteProgram;

/**
 * Created by Kem on 2015/01/24.
 */
public class EnumConverter<T extends Enum<T>> extends BaseTypeConverter<T> {

    Class<T> enumType = null;

    public EnumConverter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public T unpack(Cursor c, int index) {
        T value = Enum.valueOf(enumType, c.getString(index));
        return value;
    }

    @Override
    public void pack(T object, ContentValues cv, String name) {
        cv.put(name, object.name());
    }

    @Override
    public String toSql(T object) {
        return DatabaseUtils.sqlEscapeString(object.name());
    }

    @Override
    public String toStringBindArg(T object) {
        return object.name();
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.TEXT;
    }

    @Override
    public void bind(SQLiteProgram program, int index, T value) {
        program.bindString(index, value.name());
    }
}
