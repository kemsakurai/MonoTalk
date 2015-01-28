package monotalk.db.manager;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

import monotalk.db.Entity;
import monotalk.db.LazyList;
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
import monotalk.db.rowmapper.RowListMapper;
import monotalk.db.rowmapper.RowMapper;
import monotalk.db.utility.ConvertUtils;

import static monotalk.db.query.QueryBuilder.newQuery;
import static monotalk.db.query.QueryUtils.allColumns;
import static monotalk.db.query.QueryUtils.countRowIdAsCount;
import static monotalk.db.query.QueryUtils.excludesNullFrom;
import static monotalk.db.query.QueryUtils.idEquals;
import static monotalk.db.utility.AssertUtils.assertNotNull;

public abstract class BaseEntityManager implements EntityManager, QueryCrudHandler {
    BaseEntityManager() {
    }

    @Override
    public <T extends Entity> Delete.From<T> newDelete(Class<T> table) {
        return newQuery(this).newDelete().from(table);
    }

    @Override
    public <T extends Entity> Insert<T> newInsert(Class<T> table) {
        return newQuery(this).newInsert(table);
    }

    @Override
    public Select newSelect(Select.Column... columns) {
        return newQuery(this).newSelect(columns);
    }

    @Override
    public Select newSelect(String... columns) {
        return newQuery(this).newSelect(columns);
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
                .eq(entity.id)
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
        object.id = id;
        return id;
    }

    @Override
    public <T extends Entity> long insertExcludesNull(T object) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) object.getClass();
        long id = newQuery(this).newInsert(clazz).values(excludesNullFrom(object)).execute();
        object.id = id;
        return id;
    }

    @Override
    public <T extends Entity> long selectCount(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Cursor cursor = newQuery(this).newSelect(countRowIdAsCount()).from((Class<Entity>) clazz).selectCursor();
        return ConvertUtils.toScalarAndClose(Long.class, cursor);
    }

    @Override
    public <T extends Entity> long selectCountById(Class<T> clazz, long id) {
        @SuppressWarnings("unchecked")
        Cursor cursor = newQuery(this)
                .newSelect(countRowIdAsCount())
                .from((Class<Entity>) clazz)
                .where(idEquals(clazz, id))
                .selectCursor();
        return ConvertUtils.toScalarAndClose(Long.class, cursor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> Cursor selectCursorAll(Class<T> clazz) {
        return newQuery(this).newSelect(allColumns(clazz)).from((Class<Entity>) clazz).selectCursor();
    }

    @Override
    public <T extends Entity> Cursor selectCursorById(Class<T> clazz, long id) {
        return newQuery(this).newSelect(allColumns(clazz)).from(clazz).where(idEquals(clazz, id)).selectCursor();
    }

    @Override
    public <T extends Entity> List<T> selectListAll(Class<T> clazz) {
        Cursor cursor = selectCursorAll(clazz);
        return ConvertUtils.toEntityListAndClose(clazz, cursor, this);
    }

    @Override
    public <T extends Entity> LazyList<T> selectLazyListAll(Class<T> clazz) {
        Cursor cursor = selectCursorAll(clazz);
        return new LazyList<T>(cursor, clazz);
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
        if (object.id != null) {
            updateCount = update(object);
        }
        if (updateCount == 0) {
            id = insert(object);
        } else {
            id = object.id;
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
        return newQuery(this).newUpdate(clazz).values(excludesNullFrom(object)).where(idEquals(object)).execute();
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
        Cursor cursor = selectCursor(data);
        return ConvertUtils.toEntityListAndClose(type, cursor, this);
    }

    @Override
    public <T extends Entity> LazyList<T> selectLazyList(Class<T> clazz, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(clazz, "clazz is null");
        Cursor cursor = selectCursor(data);
        return new LazyList<T>(cursor, clazz);
    }

    @Override
    public <T extends Entity> T selectOne(Class<T> type, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(type, "type is null");
        Cursor cursor = selectCursor(data);
        return ConvertUtils.toEntityAndClose(type, cursor);
    }

    @Override
    public <T> T selectOne(RowMapper<T> mapper, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(mapper, "mapper is null");
        Cursor cursor = selectCursor(data);
        return ConvertUtils.toRowAndClose(mapper, cursor);
    }

    @Override
    public <T> List<T> selectList(RowListMapper<T> mapper, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(mapper, "mapper is null");
        Cursor cursor = selectCursor(data);
        return mapper.mapRowListAndClose(cursor);
    }

    @Override
    public <T> T selectScalar(Class<T> type, SelectQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(type, "type is null");
        Cursor cursor = selectCursor(data);
        return ConvertUtils.toScalarAndClose(type, cursor);
    }

    @Override
    public <T extends Entity> T selectOneBySqlFile(Class<T> type, TwoWayQueryData data) {
        Cursor cursor = selectCursorBySqlFile(data);
        return ConvertUtils.toEntityAndClose(type, cursor, this);
    }

    @Override
    public <T extends Entity> List<T> selectListBySqlFile(Class<T> type, TwoWayQueryData data) {
        Cursor cursor = selectCursorBySqlFile(data);
        return ConvertUtils.toEntityListAndClose(type, cursor, this);
    }

    @Override
    public <E> E selectOneBySqlFile(RowMapper<E> mapper, TwoWayQueryData data) {
        assertNotNull(data, "data is null");
        assertNotNull(mapper, "mapper is null");
        Cursor cursor = selectCursorBySqlFile(data);
        return ConvertUtils.toRowAndClose(mapper, cursor);
    }

    @Override
    public <T extends Entity> LazyList<T> selectLazyList(Class<T> type, TwoWayQueryData data) {
        Cursor cursor = selectCursorBySqlFile(data);
        return new LazyList<T>(cursor, type);
    }

    @Override
    public <E> List<E> selectListBySqlFile(RowListMapper<E> mapper, TwoWayQueryData data) {
        Cursor cursor = selectCursorBySqlFile(data);
        return mapper.mapRowListAndClose(cursor);
    }

    @Override
    public <E> E selectScalarBySqlFile(Class<E> clazz, TwoWayQueryData data) {
        Cursor cursor = selectCursorBySqlFile(data);
        return ConvertUtils.toScalarAndClose(clazz, cursor);
    }

    @Override
    public int delete(DeleteQueryData param) {
        return delete(param.getTableName(), param.getWhere(), param.getSelectionArgs());
    }
}
