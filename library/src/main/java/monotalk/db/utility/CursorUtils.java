package monotalk.db.utility;

import android.database.Cursor;

import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;

/**
 * <p>
 * TableDataConverter.java<br>
 * TABLEデータ⇔JAVAオブジェクトの変換を行うクラス
 * </p>
 *
 * @author Kem
 */
public class CursorUtils {

    public static Boolean getBoolean(Cursor cursor, int columnIndex) {
        TypeConverter<?> typeConverter = TypeConverterCache.getTypeConverterOrThrow(Boolean.class);
        Boolean value = (Boolean) typeConverter.unpack(cursor, columnIndex);
        return value;
    }

    public static Boolean getBoolean(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index <= 0) {
            return null;
        }
        return getBoolean(cursor, index);
    }

    public static Boolean getBooleanOrThrow(Cursor cursor, String columnName) {
        TypeConverter<?> typeConverter = TypeConverterCache.getTypeConverterOrThrow(Boolean.class);
        Boolean value = (Boolean) typeConverter.unpack(cursor, columnName);
        return value;
    }

    public static Long getLong(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index <= 0) {
            return null;
        }
        return cursor.getLong(index);
    }

    public static Long getLongOrThrow(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndexOrThrow(columnName);
        return cursor.getLong(index);
    }
}
