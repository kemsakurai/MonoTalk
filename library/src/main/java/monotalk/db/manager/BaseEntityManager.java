package monotalk.db.manager;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import org.seasar.dbflute.cbean.SimpleMapPmb;

import java.util.List;

import monotalk.db.DatabaseConnectionSource;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.TableInfo;
import monotalk.db.query.Delete;
import monotalk.db.query.Insert;
import monotalk.db.query.QueryCrudHandler;
import monotalk.db.query.Select;
import monotalk.db.query.TwoWayQuerySelect;
import monotalk.db.query.Update;
import monotalk.db.querydata.DeleteQueryData;
import monotalk.db.querydata.InsertQueryData;
import monotalk.db.querydata.SelectQueryData;
import monotalk.db.querydata.TwoWayQueryData;
import monotalk.db.querydata.UpdateQueryData;
import monotalk.db.rowmapper.RowMapper;
import monotalk.db.utility.ConvertUtils;

import static monotalk.db.query.QueryBuilder.allColumns;
import static monotalk.db.query.QueryBuilder.countRowIdAsCount;
import static monotalk.db.query.QueryBuilder.idEquals;
import static monotalk.db.query.QueryBuilder.newQuery;
import static monotalk.db.query.QueryBuilder.toValuesExcludesNull;
import static monotalk.db.utility.AssertUtils.assertNotNull;

public abstract class BaseEntityManager implements EntityManager, QueryCrudHandler, EntityCache {
    private DatabaseConnectionSource connectionSource;

    BaseEntityManager(DatabaseConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    @Override
    public <T extends Entity> T getEntityOrSelect(Class<T> entityType, long entityId) {
        String key = MonoTalk.getTableName(entityType) + entityId;
        T entity = (T) connectionSource.getEntity(key);
        if (entity == null) {
            entity = selectOneById(entityType, entityId);
            if (entity != null) {
                connectionSource.putEntity(key, entity);
            }
        }
        return entity;
    }

    @Override
    public void evictAllEntity() {
        connectionSource.evictAllEntity();
    }

    @Override
    public <T extends Entity> Delete.From<T> newDeleteFrom(Class<T> table) {
        return newQuery(this).newDelete().from(table);
    }

    @Override
    public <T extends Entity> Insert<T> newInsertInto(Class<T> table) {
        return newQuery(this).newInsert(table);
    }

    @Override
    public Select newSelect(Select.Column... columns) {
        return newQuery(this).newSelectColumns(columns);
    }

    @Override
    public Select newSelect(String... columns) {
        return newQuery(this).newSelectColumns(columns);
    }

    @Override
    public <T extends Entity> TwoWayQuerySelect<T> newSelectBySqlFile(Class<T> table, String filePath) {
        return newQuery(this).newSelectBySqlFile(filePath, table);
    }

    @Override
    public <T extends Entity> Update<T> newUpdate(Class<T> table) {
        return newQuery(this).newUpdate(table);
    }

    @Override
    public <T extends Entity> int delete(Class<T> clazz, String whereClause, Object... whereArgs) {
        String tableName = MonoTalk.getTableName(clazz);
        int updateCount = delete(tableName, whereClause, whereArgs);
        return updateCount;
    }

    @Override
    public <T extends Entity> int delete(T entity) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) entity.getClass();
        TableInfo tableInfo = MonoTalk.getTableInfo(type);
        return newQuery(this)
                .newDelete()
                .from(type)
                .where(tableInfo.getIdName())
                .eq(entity.getId())
                .execute();
    }

    @Override
    public <T extends Entity> int deleteAll(Class<T> clazz) {
        return newQuery(this).newDelete().from(clazz).execute();
    }

    @Override
    public <T extends Entity> int deleteById(Class<T> clazz, long id) {
        return newQuery(this).newDelete().from(clazz).where(idEquals(clazz, id)).execute();
    }

    @Override
    public <T extends Entity> int updateById(Class<T> clazz, ContentValues values, long id) {
        return newQuery(this).newUpdate(clazz).values(values).where(idEquals(clazz, id)).execute();
    }

    @Override
    public <T extends Entity> long insert(Class<T> clazz, ContentValues values) {
        return newQuery(this).newInsert(clazz).values(values).execute();
    }

    @Override
    public <T extends Entity> long insert(T object) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) object.getClass();
        long id = newQuery(this).newInsert(clazz).values(object).execute();
        object.setId(id);
        return id;
    }

    @Override
    public <T extends Entity> long insertExcludesNull(T object) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) object.getClass();
        long id = newQuery(this).newInsert(clazz).values(toValuesExcludesNull(object)).execute();
        object.setId(id);
        return id;
    }

    @Override
    public <T extends Entity> long selectCount(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Cursor cursor = newQuery(this).newSelectColumns(countRowIdAsCount()).from((Class<Entity>) clazz).selectCursor();
        return ConvertUtils.toScalarAndClose(Long.class, cursor);
    }

    @Override
    public <T extends Entity> long selectCountById(Class<T> clazz, long id) {
        @SuppressWarnings("unchecked")
        Cursor cursor = newQuery(this)
                .newSelectColumns(countRowIdAsCount())
                .from((Class<Entity>) clazz)
                .where(idEquals(clazz, id))
                .selectCursor();
        return ConvertUtils.toScalarAndClose(Long.class, cursor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> Cursor selectCursorAll(Class<T> clazz) {
        return newQuery(this).newSelectColumns(allColumns(clazz)).from((Class<Entity>) clazz).selectCursor();
    }

    @Override
    public <T extends Entity> Cursor selectCursorById(Class<T> clazz, long id) {
        return newQuery(this).newSelectColumns(allColumns(clazz)).from(clazz).where(idEquals(clazz, id)).selectCursor();
    }

    @Override
    public <T extends Entity> List<T> selectListAll(Class<T> clazz) {
        Cursor cursor = selectCursorAll(clazz);
        return ConvertUtils.toEntityListAndClose(clazz, cursor, this);
    }

    @Override
    public <T extends Entity> T selectOneById(Class<T> clazz, long id) {
        Cursor cursor = selectCursorById(clazz, id);
        return ConvertUtils.toEntityAndClose(clazz, cursor, this);
    }

    @Override
    public <T extends Entity> T selectOneBySql(Class<T> clazz, String sql, Object... selectionArgs) {
        Cursor cursor = selectCursorBySql(sql, selectionArgs);
        return ConvertUtils.toEntityAndClose(clazz, cursor, this);
    }

    @Override
    public <T extends Entity> long store(T object) {
        long id;
        int updateCount = 0;
        if (object.getId() != null) {
            updateCount = update(object);
        }
        if (updateCount == 0) {
            id = insert(object);
        } else {
            id = object.getId();
        }
        return id;
    }

    @Override
    public <T extends Entity> int update(final Class<T> clazz, final ContentValues data, final String whereClause,
                                         final Object... whereArgs) {
        String tableName = MonoTalk.getTableName(clazz);
        return update(tableName, data, whereClause, whereArgs);
    }

    @Override
    public <T extends Entity> int update(T object) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) object.getClass();
        return newQuery(this).newUpdate(clazz).values(object).where(idEquals(object)).execute();
    }

    @Override
    public <T extends Entity> int updateExcludesNull(T object) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) object.getClass();
        return newQuery(this).newUpdate(clazz).values(toValuesExcludesNull(object)).where(idEquals(object)).execute();
    }

    @Override
    public void release() {
        // Do Nothing...
    }

    @Override
    public int update(UpdateQueryData data) {
        assertNotNull(data, "data is null");
        return update(
                data.getTableName(),
                data.getValues(),
                data.getWhere(),
                data.getSelectionArgs());
    }

    @Override
    public Long insert(InsertQueryData data) {
        return insert(data.getTableName(), data.getValues());
    }

    @Override
    public <T extends Entity> List<T> selectList(Class<T> type, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(type, "type is null");
        return selectList(
                type,
                data.isDistinct(),
                data.getTableAndJoin(),
                data.getColumns(),
                data.getWhere(),
                data.getGroupBy(),
                data.getHaving(),
                data.getOrderBy(),
                data.getLimit(),
                data.getSelectionArgs());
    }

    /**
     * @param clazz
     * @param distinct
     * @param tableName
     * @param columns
     * @param where
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param selectionArgs
     * @return
     */
    protected <T extends Entity> List<T> selectList(Class<T> clazz, boolean distinct, String tableName, String[] columns, String where, String groupBy, String having, String orderBy, String limit, Object[] selectionArgs) {
        Cursor cursor = selectCursor(distinct, tableName, columns, where, groupBy, having, orderBy, limit, selectionArgs);
        return ConvertUtils.toEntityListAndClose(clazz, cursor, this);
    }

    /**
     * 引数を元にSQLを生成、実行しCursorを返す
     *
     * @param distinct
     * @param tableName
     * @param columns
     * @param where
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param selectionArgs
     * @return
     */
    protected abstract Cursor selectCursor(boolean distinct, String tableName, String[] columns, String where,
                                           String groupBy, String having, String orderBy, String limit, Object[] selectionArgs);

    @Override
    public CursorLoader buildLoader(SelectQueryData data) {
        assertNotNull(data, "data is null");
        return buildLoader(
                data.isDistinct(),
                data.getTableAndJoin(),
                data.getColumns(),
                data.getWhere(),
                data.getGroupBy(),
                data.getHaving(),
                data.getOrderBy(),
                data.getLimit(),
                data.getSelectionArgs());
    }

    /**
     * Loaderを作成します。
     *
     * @param distinct
     * @param tableName
     * @param columns
     * @param where
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param selectionArgs
     * @return
     */
    protected abstract CursorLoader buildLoader(boolean distinct, String tableName, String[] columns, String where,
                                                String groupBy, String having, String orderBy, String limit, Object[] selectionArgs);

    @Override
    public <T extends Entity> T selectOne(Class<T> type, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(type, "type is null");
        T object = selectOne(
                type,
                data.isDistinct(),
                data.getTableAndJoin(),
                data.getColumns(),
                data.getWhere(),
                data.getGroupBy(),
                data.getHaving(),
                data.getOrderBy(),
                data.getLimit(),
                data.getSelectionArgs());
        return object;
    }

    /**
     * 引数を元にSQLを生成、実行しModelを返す
     *
     * @param distinct
     * @param tableName
     * @param columns
     * @param where
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param selectionArgs
     * @return
     */
    protected <T extends Entity> T selectOne(Class<T> clazz, boolean distinct, String tableName, String[] columns, String where, String groupBy, String having, String orderBy, String limit, Object[] selectionArgs) {
        Cursor cursor = selectCursor(distinct, tableName, columns, where, groupBy, having, orderBy, limit, selectionArgs);
        return ConvertUtils.toEntityAndClose(clazz, cursor, this);
    }

    @Override
    public Cursor selectCursor(SelectQueryData data) {
        assertNotNull(data, "data is null");
        return selectCursor(
                data.isDistinct(),
                data.getTableAndJoin(),
                data.getColumns(),
                data.getWhere(),
                data.getGroupBy(),
                data.getHaving(),
                data.getOrderBy(),
                data.getLimit(),
                data.getSelectionArgs());
    }

    @Override
    public <T> T selectOne(RowMapper<T> mapper, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(mapper, "mapper is null");
        return selectOne(
                mapper,
                data.isDistinct(),
                data.getTableAndJoin(),
                data.getColumns(),
                data.getWhere(),
                data.getGroupBy(),
                data.getHaving(),
                data.getOrderBy(),
                data.getLimit(),
                data.getSelectionArgs());
    }

    /**
     * データを1件取得する
     *
     * @param mapper
     * @param distinct
     * @param tableName
     * @param projection
     * @param selection
     * @param groupBy
     * @param having
     * @param sortOrder
     * @param limit
     * @param selectionArgs
     * @return
     */
    public <T> T selectOne(RowMapper<T> mapper, boolean distinct, String tableName, String[] projection, String selection, String groupBy, String having, String sortOrder, String limit, Object[] selectionArgs) {
        Cursor cursor = selectCursor(distinct, tableName, projection, selection, groupBy, having, sortOrder, limit, selectionArgs);
        return ConvertUtils.toRowAndClose(mapper, cursor);
    }

    @Override
    public <T> List<T> selectList(RowMapper<T> mapper, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(mapper, "mapper is null");
        return selectList(
                mapper,
                data.isDistinct(),
                data.getTableAndJoin(),
                data.getColumns(),
                data.getWhere(),
                data.getGroupBy(),
                data.getHaving(),
                data.getOrderBy(),
                data.getLimit(),
                data.getSelectionArgs());
    }

    /**
     * MapperクラスをもとにList形式のデータを取得する
     *
     * @param mapper
     * @param distinct
     * @param tableName
     * @param columns
     * @param where
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param selectionArgs
     * @return
     */
    protected <T> List<T> selectList(RowMapper<T> mapper, boolean distinct, String tableName, String[] columns, String where, String groupBy, String having, String orderBy, String limit, Object[] selectionArgs) {
        Cursor cursor = selectCursor(distinct, tableName, columns, where, groupBy, having, orderBy, limit, selectionArgs);
        return ConvertUtils.toRowListAndClose(mapper, cursor);
    }

    @Override
    public <T> T selectScalar(Class<T> type, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(type, "type is null");
        return selectScalar(
                type,
                data.isDistinct(),
                data.getTableAndJoin(),
                data.getColumns(),
                data.getWhere(),
                data.getGroupBy(),
                data.getHaving(),
                data.getOrderBy(),
                data.getLimit(),
                data.getSelectionArgs());
    }

    /**
     * スカラー値を取得する
     *
     * @param clazz
     * @param distinct
     * @param tableName
     * @param projection
     * @param selection
     * @param groupBy
     * @param having
     * @param sortOrder
     * @param limit
     * @param selectionArgs
     * @return
     */
    protected <T> T selectScalar(Class<T> clazz, boolean distinct, String tableName, String[] projection, String selection, String groupBy, String having, String sortOrder, String limit, Object[] selectionArgs) {
        Cursor cursor = selectCursor(distinct, tableName, projection, selection, groupBy, having, sortOrder, limit, selectionArgs);
        return ConvertUtils.toScalarAndClose(clazz, cursor);
    }

    @Override
    public CursorLoader buildLoader(TwoWayQueryData data) {
        return buildLoader(data.getTableName(), data.getSqlFilePath(), data.getMapPmb());
    }

    /**
     * Loaderを作成します。
     *
     * @param sqlFilePath
     * @param mapPmb
     * @return
     */
    protected abstract CursorLoader buildLoader(String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb);

    @Override
    public Cursor selectCursorBySqlFile(TwoWayQueryData data) {
        return selectCursorBySqlFile(data.getTableName(), data.getSqlFilePath(), data.getMapPmb());
    }

    /**
     * SELECT_SQL指定して、実行します。
     *
     * @param sqlFilePath
     * @param mapPmb
     * @return
     */
    protected abstract Cursor selectCursorBySqlFile(String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb);

    public <T extends Entity> T selectOneBySqlFile(Class<T> type, TwoWayQueryData data) {
        return selectOneBySqlFile(type, data.getTableName(), data.getSqlFilePath(), data.getMapPmb());
    }

    protected <T extends Entity> T selectOneBySqlFile(Class<T> clazz, String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        Cursor cursor = selectCursorBySqlFile(entityPath, sqlFilePath, mapPmb);
        return ConvertUtils.toEntityAndClose(clazz, cursor, this);
    }

    @Override
    public <T extends Entity> List<T> selectListBySqlFile(Class<T> type, TwoWayQueryData data) {
        return selectListBySqlFile(type, data.getTableName(), data.getSqlFilePath(), data.getMapPmb());
    }

    public <T extends Entity> List<T> selectListBySqlFile(Class<T> clazz, String entityPath, String sqlFilePath,
                                                          SimpleMapPmb<Object> mapPmb) {
        Cursor cursor = selectCursorBySqlFile(entityPath, sqlFilePath, mapPmb);
        List<T> objects = ConvertUtils.toEntityListAndClose(clazz, cursor, this);
        return objects;
    }

    @Override
    public <E> E selectOneBySqlFile(RowMapper<E> mapper, TwoWayQueryData data) {
        return selectOneBySqlFile(mapper, data.getSqlFilePath(), data.getSqlFilePath(), data.getMapPmb());
    }

    protected <T> T selectOneBySqlFile(RowMapper<T> mapper, String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        Cursor cursor = selectCursorBySqlFile(entityPath, sqlFilePath, mapPmb);
        T object = ConvertUtils.toRowAndClose(mapper, cursor);
        return object;
    }

    @Override
    public <E> List<E> selectListBySqlFile(RowMapper<E> mapper, TwoWayQueryData data) {
        return selectListBySqlFile(mapper, data.getTableName(), data.getSqlFilePath(), data.getMapPmb());
    }

    protected <T> List<T> selectListBySqlFile(RowMapper<T> mapper, String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        Cursor cursor = selectCursorBySqlFile(entityPath, sqlFilePath, mapPmb);
        List<T> objects = ConvertUtils.toRowListAndClose(mapper, cursor);
        return objects;
    }

    @Override
    public <E> E selectScalarBySqlFile(Class<E> clazz, TwoWayQueryData data) {
        return selectScalarBySqlFile(clazz, data.getTableName(), data.getSqlFilePath(), data.getMapPmb());
    }

    protected <T> T selectScalarBySqlFile(Class<T> clazz, String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        Cursor cursor = selectCursorBySqlFile(entityPath, sqlFilePath, mapPmb);
        return ConvertUtils.toScalarAndClose(clazz, cursor);
    }

    @Override
    public int delete(DeleteQueryData param) {
        return delete(param.getTableName(), param.getWhere(), param.getSelectionArgs());
    }
}
