package monotalk.db.rowmapper;

import android.database.Cursor;
import android.text.TextUtils;

import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;
import monotalk.db.utility.AssertUtils;

public class ScalarRowMapper<T> implements RowMapper<T> {
    private String columnName;
    private int columnIndex = -1;
    @SuppressWarnings("rawtypes")
    private TypeConverter typeConverter;

    public ScalarRowMapper(Class<T> type, String columnName) {
        AssertUtils.assertNotEmpty(columnName, "columnName is Empty");
        this.columnName = columnName;
        typeConverter = TypeConverterCache.getTypeConverterOrThrow(type);
    }

    public ScalarRowMapper(Class<T> type, int columnIndex) {
        AssertUtils.assertArgument(columnIndex >= 0, "The columnIndex argument must be greater than or equal to 0");
        this.columnIndex = columnIndex;
        typeConverter = TypeConverterCache.getTypeConverterOrThrow(type);
    }

    @SuppressWarnings("unchecked")
    public T mapRow(Cursor cursor) {
        if (!TextUtils.isEmpty(columnName)) {
            return (T) typeConverter.unpack(cursor, columnName);
        }
        return (T) typeConverter.unpack(cursor, columnIndex);
    }
}
