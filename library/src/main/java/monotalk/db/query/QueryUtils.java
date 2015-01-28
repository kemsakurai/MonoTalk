package monotalk.db.query;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.TableInfo;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;
import monotalk.db.utility.AssertUtils;
import monotalk.db.valuesmapper.EntityValuesMapper;

import static monotalk.db.utility.AssertUtils.assertNotNull;

/**
 * Created by Kem on 2015/01/10.
 */
public class QueryUtils {

    private static final String TAG_NAME = DBLog.getTag(QueryBuilder.class);

    public static String[] toStirngArrayArgs(Object... args) {
        String[] stringArgs = null;
        if (args != null) {
            stringArgs = new String[args.length];
            int index = 0;
            for (Object arg : args) {
                TypeConverter converter = TypeConverterCache.getTypeConverter(arg.getClass());
                stringArgs[index] = converter.toStringBindArg(arg);
                index++;
            }
        }
        return stringArgs;
    }

    public static String toInsertSql(String tableName, String[] data) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(" INTO ");
        sql.append(tableName.toLowerCase());
        sql.append('(');
        sql.append(TextUtils.join(",", data));
        sql.append(')');
        sql.append(" VALUES (");
        for (int i = 0; i < data.length; i++) {
            sql.append((i > 0) ? ",?" : "?");
        }
        sql.append(')');
        return sql.toString();

    }

    public static String toInsertSql(Class<? extends Entity> type) {
        TableInfo info = MonoTalk.getTableInfo(type);
        String[] columnNames = info.getColumnNames();
        String tableName = info.getTableName();
        return toInsertSql(tableName, columnNames);
    }

    public static String toUpdateSql(Class<? extends Entity> type) {
        StringBuilder sql = new StringBuilder(120);
        TableInfo info = MonoTalk.getTableInfo(type);
        sql.append("UPDATE ");
        sql.append(info.getTableName().toLowerCase());
        sql.append(" SET ");
        // move allColumns bind args to one array
        String[] columnNames = info.getColumnNames();
        sql.append(TextUtils.join("= ? ,", columnNames));
        sql.append("= ?");
        sql.append(" WHERE ");
        sql.append(info.getIdName());
        sql.append("= ?");
        return sql.toString();
    }

    public static String toSelectSql(Class<? extends Entity> type) {
        TableInfo info = MonoTalk.getTableInfo(type);
        StringBuilder sql = new StringBuilder(120);
        sql.append("SELECT ");
        String[] columnNames = info.getColumnNames();
        sql.append(TextUtils.join(",", columnNames));
        sql.append("FROM ");
        sql.append(info.getTableName().toLowerCase());
        sql.append(" WHERE ");
        sql.append(info.getIdName());
        sql.append("= ?");
        return sql.toString();
    }

    public static String toDeleteSql(Class<? extends Entity> type) {
        TableInfo info = MonoTalk.getTableInfo(type);
        StringBuilder sql = new StringBuilder(120);
        sql.append("DELETE ");
        sql.append("FROM ");
        sql.append(info.getTableName().toLowerCase());
        sql.append(" WHERE ");
        sql.append(info.getIdName());
        sql.append("= ?");
        return sql.toString();
    }

    public static String toDeleteSql(String tableName, String whereClause) {
        StringBuilder sb = new StringBuilder(120);
        sb.append("DELETE ");
        sb.append("FROM ");
        sb.append(tableName.toLowerCase());
        if (!TextUtils.isEmpty(whereClause)) {
            sb.append(" WHERE ");
            sb.append(whereClause);
        }
        return sb.toString();
    }

    public static String toUpdateSql(String tableName, String[] columNames, String whereClause) {
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(tableName.toLowerCase());
        sql.append(" SET ");
        sql.append(TextUtils.join("= ? ,", columNames));
        sql.append("= ?");
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
        }
        return sql.toString();
    }

    /**
     * TABLE行クラスから全てのカラム名を取得し、返します。
     *
     * @param clazz TABLE行クラス
     * @return カラム名の配列
     */
    public static <T extends Entity> String[] allColumns(Class<T> clazz) {
        return excludesColumns(clazz, new String[0]);
    }

    /**
     * ROWIDをカウントするSelectカラム名を返します。
     *
     * @return
     */
    public static Select.Column countRowIdAsCount() {
        return new Select.Column("count(ROWID)", "count");
    }

    /**
     * TABLE行クラスから指定したカラムを以外のカラム名を取得します。
     *
     * @param clazz   TABLE行クラス
     * @param exclude 除外カラム名
     * @return カラム名の配列
     */
    public static <T extends Entity> String[] excludesColumns(Class<T> clazz, String... exclude) {
        List<String> list = new ArrayList<String>();
        List<String> excludeList = Arrays.asList(exclude);
        String[] columnNames = MonoTalk.getTableInfo(clazz).getColumnNames();
        if (columnNames.length == 0) {
            Log.w(TAG_NAME, "ColumInfo is Null ClassName = [" + clazz.getName() + "]");
            return null;
        }

        // アノテーション修飾されたFieldsを取得
        for (String columnName : columnNames) {
            if (excludeList != null && excludeList.contains(columnName)) {
                continue;
            }
            list.add(columnName);
        }
        return (String[]) list.toArray(new String[0]);
    }

    /**
     * ModelクラスのIDを一致することを条件とするWhere句を返す
     *
     * @param clazz
     * @param id
     * @return
     */
    public static <T extends Entity> Selection idEquals(Class<T> clazz, long id) {
        Selection selection = new Selection();
        String columnName = MonoTalk.getTableInfo(clazz).getIdName();
        selection.where(columnName).eq(id);
        return selection;
    }

    /**
     * ModelクラスのIDを一致することを条件とするWhere句を返す
     *
     * @param object
     * @return
     */
    public static <T extends Entity> Selection idEquals(T object) {
        AssertUtils.assertNotNull(object, "object is null");
        Selection selection = new Selection();
        String columnName = MonoTalk.getTableInfo(object.getClass()).getIdName();
        Long id = object.id;
        Expressions<Selection> exp = selection.where(columnName);
        if (id != null) {
            selection = exp.eq(id);
        } else {
            selection = exp.isNull();
        }
        return selection;
    }

    /**
     * EntityをContentsValueに変換する 変換時にNullも含める
     *
     * @param entities
     * @return
     */
    public static <T extends Entity> ContentValues[] arrayFrom(List<T> entities) {
        assertNotNull(entities, "entities is null");
        if (entities.isEmpty()) {
            return new ContentValues[0];
        }
        Class<T> type = (Class<T>) entities.get(0).getClass();
        EntityValuesMapper<T> mapper = new EntityValuesMapper.Builder(type)
                .includesNull()
                .create();
        int size = entities.size();
        ContentValues[] valuesArray = new ContentValues[entities.size()];
        for (int i = 0; i < size; i++) {
            valuesArray[i] = mapper.mapValues(entities.get(i));
        }
        return valuesArray;
    }

    /**
     * ModelオブジェクトをContentsValueに変換する 変換時にNullも含める
     *
     * @param object
     * @return
     */
    public static <T extends Entity> ContentValues from(T object) {
        EntityValuesMapper<T> mapper = new EntityValuesMapper.Builder((Class<T>) object.getClass())
                .includesNull()
                .create();
        return mapper.mapValues(object);
    }

    /**
     * ModelオブジェクトをContentsValueに変換する
     *
     * @param object
     * @return
     */
    public static <T extends Entity> ContentValues from(T object, String... includeColums) {
        EntityValuesMapper<T> mapper = new EntityValuesMapper.Builder((Class<T>) object.getClass())
                .includesNull()
                .includesColumns(includeColums)
                .create();
        return mapper.mapValues(object);
    }

    /**
     * ModelオブジェクトをContentsValueに変換する
     *
     * @param object
     * @return
     */
    public static <T extends Entity> ContentValues excludesFrom(T object, String... excludeColums) {
        EntityValuesMapper<T> mapper = new EntityValuesMapper.Builder((Class<T>) object.getClass())
                .includesNull()
                .excludesColumns(excludeColums)
                .create();
        return mapper.mapValues(object);

    }

    /**
     * EntityをContentsValueに変換する 変換時にNullは含めない
     *
     * @param object
     * @return
     */
    public static <T extends Entity> ContentValues excludesNullFrom(T object) {
        EntityValuesMapper<T> mapper = new EntityValuesMapper.Builder((Class<T>) object.getClass())
                .excludesNull()
                .create();
        return mapper.mapValues(object);

    }
}
