package monotalk.db.utility;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.compat.DatabaseCompat;
import monotalk.db.manager.EntityCache;
import monotalk.db.rowmapper.EntityRowMapper;
import monotalk.db.rowmapper.RowMapper;
import monotalk.db.rowmapper.ScalarRowMapper;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;

public class ConvertUtils {

    private static final String TAG_NAME = DBLog.getTag(ConvertUtils.class);

    /**
     * ContentValuesに値を追加する
     *
     * @param fieldName
     * @param value
     * @param values
     */
    public static void addValue(String fieldName, Object value, ContentValues values) {
        if (value == null) {
            values.putNull(fieldName);
            return;
        } else {
            Class<?> fieldType = value.getClass();
            @SuppressWarnings("rawtypes")
            final TypeConverter typeConverter = TypeConverterCache.getTypeConverter(fieldType);
            if (typeConverter != null) {
                // serialize data
                typeConverter.pack(value, values, fieldName);
            } else {
                // TODO: Find a smarter way to do this? This if block is
                // necessary because we
                // can't know the type until runtime.
                if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
                    values.put(fieldName, (Byte) value);
                } else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
                    values.put(fieldName, (Short) value);
                } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                    values.put(fieldName, (Integer) value);
                } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                    values.put(fieldName, (Long) value);
                } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                    values.put(fieldName, (Float) value);
                } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                    values.put(fieldName, (Double) value);
                } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                    values.put(fieldName, (Boolean) value);
                } else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
                    values.put(fieldName, value.toString());
                } else if (fieldType.equals(String.class)) {
                    values.put(fieldName, value.toString());
                } else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
                    values.put(fieldName, (byte[]) value);
                } else if (ReflectUtils.isEntity(fieldType)) {
                    Entity model = (Entity) value;
                    if (model.getId() == null) {
                        values.putNull(fieldName);
                    } else {
                        long idValue = model.getId();
                        values.put(fieldName, idValue);
                    }
                } else if (ReflectUtils.isSubclassOf(fieldType, Enum.class)) {
                    values.put(fieldName, ((Enum<?>) value).name());
                } else {
                    DBLog.w(TAG_NAME, "fieldType=[ " + fieldType.getName() + "] can not convert...");
                }
            }
        }
    }

    /**
     * <p>
     * カーソルのコピーを作成する
     * </p>
     *
     * @param cursor
     * @return
     */
    public static MatrixCursor toMatrixCursor(Cursor cursor) {
        // カーソルをコピーする
        MatrixCursor matrixCursor = DatabaseCompat.newMatrixCursor(cursor.getColumnNames(), cursor.getCount());
        // カラム要素数を取得
        int columnCount = cursor.getColumnCount();
        if (cursor.moveToFirst()) {
            do {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = cursor.getString(i);
                }
                matrixCursor.addRow(row);
            } while (cursor.moveToNext());
        }
        // コピー元のカーソルはCLOSEする
        cursor.close();
        return matrixCursor;
    }

    /**
     * <p>
     * TABLE行オブジェクトを行情報(Object配列)に変換する
     * </p>
     *
     * @param columnNames
     * @param entity
     * @return
     */
    public static Object[] toMatrixCursorRow(String[] columnNames, Entity entity) {
        // Object配列を確保
        Object[] dist = new Object[columnNames.length];
        // カウント用変数
        int count = 0;
        // カラム数分繰り返し
        for (String columnName : columnNames) {
            Map<Field, String> map = MonoTalk.getTableInfo(entity.getClass()).getColumnNamesMap();
            for (Entry<Field, String> e : map.entrySet()) {
                String name = e.getValue();
                if (columnName.equals(name)) {
                    dist[count] = ReflectUtils.readField(e.getKey(), entity, true);
                    break;
                }
            }
            count++;
        }
        return dist;
    }

    /**
     * @param clazz
     * @param cursor
     * @return
     */
    public static <T extends Entity> T toEntity(Class<T> clazz, Cursor cursor) {
        T object = toEntity(clazz, cursor, null);
        return object;
    }

    /**
     * @param clazz
     * @param cursor
     * @param entityCache
     * @param <T>
     * @return
     */
    public static <T extends Entity> T toEntity(Class<T> clazz, Cursor cursor, EntityCache entityCache) {
        EntityRowMapper<T> mapper = new EntityRowMapper<T>(clazz, entityCache);
        T modelObject = mapper.mapRow(cursor);
        return modelObject;
    }

    /**
     * @param clazz
     * @param cursor
     * @return
     */
    public static <T extends Entity> T toEntityAndClose(Class<T> clazz, Cursor cursor) {
        T object = toEntityAndClose(clazz, cursor, null);
        return object;
    }

    /**
     * @param clazz
     * @param cursor
     * @param entityCache
     * @return
     */
    public static <T extends Entity> T toEntityAndClose(Class<T> clazz, Cursor cursor, EntityCache entityCache) {
        EntityRowMapper<T> mapper = new EntityRowMapper<T>(clazz, entityCache);
        T modelObject = null;
        try {
            if (cursor.moveToFirst()) {
                modelObject = mapper.mapRow(cursor);
            }
        } finally {
            cursor.close();
        }
        return modelObject;
    }

    /**
     * CursorをBeanListに変換して返す<br>
     * 変換後にCursorは閉じる
     *
     * @param clazz  TABLE行クラス
     * @param cursor カーソル
     * @return
     */
    public static <T extends Entity> List<T> toEntityListAndClose(Class<T> clazz, Cursor cursor,
                                                                  EntityCache entityCache) {
        EntityRowMapper<T> mapper = new EntityRowMapper<T>(clazz, entityCache);
        List<T> values = toRowListAndClose(mapper, cursor);
        entityCache.evictAllEntity();
        return values;
    }

    /**
     * カーソル1行をオブジェクトにMapする
     */
    public static <T> T toRowAndClose(RowMapper<T> rowMapper, Cursor cursor) {
        try {
            if (cursor.moveToFirst()) {
                // カーソルのデータの件数が0であれば、そのままNullを返却する
                return rowMapper.mapRow(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /**
     * カーソルをオブジェクトにMapする
     */
    public static <T> List<T> toRowList(RowMapper<T> rowMapper, Cursor cursor) {
        // 戻り値のリストを確保
        List<T> list = new ArrayList<T>(cursor.getCount());
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(rowMapper.mapRow(cursor));
                } while (cursor.moveToNext());
            }
        } catch (IllegalStateException e) {
            // REMENBER エラー発生時は、エラーは通知しない。
            DBLog.w(TAG_NAME, "Raise IllegalStateException errorMessage [" + e.getMessage() + "]");
            // 設定できたデータを返却する
        }
        return list;
    }

    /**
     * カーソルをオブジェクトにMapする
     */
    public static <T> List<T> toRowListAndClose(RowMapper<T> rowMapper, Cursor cursor) {
        List<T> list = null;
        try {
            list = toRowList(rowMapper, cursor);
        } finally {
            cursor.close();
        }
        return list;
    }

    public static <T> T toScalar(Class<T> clazz, Cursor cursor) {
        return toScalar(clazz, cursor, 0);
    }

    public static <T> T toScalar(Class<T> clazz, Cursor cursor, int index) {
        ScalarRowMapper<T> mapper = new ScalarRowMapper<T>(clazz, index);
        return mapper.mapRow(cursor);
    }

    public static <T> T toScalarAndClose(Class<T> clazz, Cursor cursor) {
        return toScalarAndClose(clazz, cursor, 0);
    }

    public static <T> T toScalarAndClose(Class<T> clazz, Cursor cursor, int index) {
        T result = null;
        try {
            ScalarRowMapper<T> mapper = new ScalarRowMapper<T>(clazz, index);
            // カーソルのデータの件数が0であれば、そのままNullを返却する
            if (cursor.moveToFirst()) {
                return mapper.mapRow(cursor);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * 指定したカラムの値をlong型の配列を返す
     *
     * @param cursor     カーソル
     * @param columnName カラム名
     * @return
     */
    public static long[] toSingelColumLongArray(final Cursor cursor, String columnName) {
        // 戻り値のリストを確保
        final long[] values = new long[cursor.getCount()];
        final RowMapper<Long> rowMapper = new ScalarRowMapper<Long>(Long.class, columnName);
        new PrimitiveArrayCreater(cursor) {
            @Override
            void setElement(int index, Cursor cursor2) {
                values[index] = rowMapper.mapRow(cursor);
            }
        }.execute();
        return values;
    }

    // =================================================================
    // Inner Class
    // =================================================================
    abstract static class PrimitiveArrayCreater {
        private Cursor cursor;

        public PrimitiveArrayCreater(Cursor cursor) {
            this.cursor = cursor;
        }

        public void execute() {
            try {
                int index = 0;
                if (cursor.moveToFirst()) {
                    do {
                        setElement(index, cursor);
                        index++;
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                // REMENBER エラー発生時は、エラーは通知しない。
                DBLog.e(TAG_NAME, "Raise Exception errorMessage [" + e.getMessage() + "]", e);
                // 設定できたデータを返却する
            }
        }

        abstract void setElement(int index, Cursor cursor2);
    }
}
