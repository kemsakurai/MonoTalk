package monotalk.db.utility;

import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.List;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.FieldInfo;
import monotalk.db.MonoTalk;
import monotalk.db.compat.DatabaseCompat;
import monotalk.db.manager.EntityManager;
import monotalk.db.rowmapper.EntityRowListMapper;
import monotalk.db.rowmapper.EntityRowMapper;
import monotalk.db.rowmapper.RowMapper;
import monotalk.db.rowmapper.ScalarRowMapper;

public class ConvertUtils {

    private static final String TAG_NAME = DBLog.getTag(ConvertUtils.class);

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
            List<FieldInfo> infos = MonoTalk.getTableInfo(entity.getClass()).getFieldInfos();
            for (FieldInfo info : infos) {
                String name = info.getColumnName();
                if (columnName.equals(name)) {
                    dist[count] = ReflectUtils.readField(info.getField(), entity);
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
     * @param entityManager
     * @param <T>
     * @return
     */
    public static <T extends Entity> T toEntity(Class<T> clazz, Cursor cursor, EntityManager entityManager) {
        EntityRowMapper<T> mapper = new EntityRowMapper<T>(clazz, entityManager);
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
     * @param entityManager
     * @return
     */
    public static <T extends Entity> T toEntityAndClose(Class<T> clazz, Cursor cursor, EntityManager entityManager) {
        T modelObject = null;
        if (cursor.moveToFirst()) {
            EntityRowMapper<T> mapper = new EntityRowMapper<T>(clazz, entityManager);
            modelObject = mapper.mapRow(cursor);
        }
        cursor.close();
        return modelObject;
    }

    /**
     * CursorをBeanListに変換して返します。
     * 変換前にカーソルを1行目に移動させ、変換後にCursorは閉じます。
     *
     * @param clazz  TABLE行クラス
     * @param cursor カーソル
     * @return
     */
    public static <T extends Entity> List<T> toEntityListAndClose(Class<T> clazz, Cursor cursor,
                                                                  EntityManager entityManager) {
        EntityRowListMapper<T> mapper = new EntityRowListMapper<T>(clazz, entityManager);
        return mapper.mapRowListAndClose(cursor);
    }

    /**
     * カーソル1行をオブジェクトにMapする
     */
    public static <T> T toRowAndClose(RowMapper<T> rowMapper, Cursor cursor) {
        T result = null;
        if (cursor.moveToNext()) {
            result = rowMapper.mapRow(cursor);
        }
        cursor.close();
        return result;
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
