package monotalk.db.query;

import android.content.ContentValues;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.utility.AssertUtils;
import monotalk.db.valuesmapper.EntityValuesMapper;

import static monotalk.db.utility.AssertUtils.assertNotNull;

public class QueryBuilder {

    private static final String TAG_NAME = DBLog.getTag(QueryBuilder.class);
    private QueryCrudHandler queryCrudHandler;

    /**
     * コンストラクター
     *
     * @param queryCrudHandler
     */
    private QueryBuilder(QueryCrudHandler queryCrudHandler) {
        this.queryCrudHandler = queryCrudHandler;
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
     * QueryBuilderを返します。
     *
     * @param queryCrudHandler
     * @return
     */
    public static QueryBuilder newQuery(QueryCrudHandler queryCrudHandler) {
        return new QueryBuilder(queryCrudHandler);
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
        Long id = object.getId();
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
    public static <T extends Entity> ContentValues[] toValuesArray(List<T> entities) {
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
    public static <T extends Entity> ContentValues toValues(T object) {
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
    public static <T extends Entity> ContentValues toValues(T object, String... includeColums) {
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
    public static <T extends Entity> ContentValues toValuesExcludesColumns(T object, String... excludeColums) {
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
    public static <T extends Entity> ContentValues toValuesExcludesNull(T object) {
        EntityValuesMapper<T> mapper = new EntityValuesMapper.Builder((Class<T>) object.getClass())
                .excludesNull()
                .create();
        return mapper.mapValues(object);

    }

    /**
     * Deleteクエリオブジェクトを生成する
     *
     * @return
     */
    public Delete newDelete() {
        return new Delete(queryCrudHandler);
    }

    /**
     * Insertクエリオブジェクトを生成する
     *
     * @return
     */
    public <T extends Entity> Insert<T> newInsert(Class<T> table) {
        return new Insert<T>(table, queryCrudHandler);
    }

    /**
     * Selectクエリオブジェクトを生成する
     *
     * @param columns
     * @return
     */
    public Select newSelectColumns(Select.Column... columns) {
        return new Select(queryCrudHandler, columns);
    }

    /**
     * Selectクエリオブジェクトを生成する
     *
     * @param columns
     * @return
     */
    public Select newSelectColumns(String... columns) {
        return new Select(queryCrudHandler, columns);
    }

    /**
     * TwoWayQuerySelectクエリオブジェクトを生成する
     *
     * @param filePath
     * @param table
     * @param <T>
     * @return
     */
    public <T extends Entity> TwoWayQuerySelect<T> newSelectBySqlFile(String filePath, Class<T> table) {
        return new TwoWayQuerySelect<T>(queryCrudHandler, table, filePath);
    }

    /**
     * Updateクエリオブジェクトを生成する
     *
     * @param table
     * @param <T>
     * @return
     */
    public <T extends Entity> Update<T> newUpdate(Class<T> table) {
        return new Update<T>(table, queryCrudHandler);
    }
}
