package monotalk.db.manager;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import org.seasar.dbflute.cbean.SimpleMapPmb;

import java.util.ArrayList;
import java.util.List;

import monotalk.db.DatabaseProviderConnectionSource;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.UriUtils;
import monotalk.db.query.DeleteOperationBuilder;
import monotalk.db.query.InsertOperationBuilder;
import monotalk.db.query.QueryBuilder;
import monotalk.db.query.QueryUtils;
import monotalk.db.query.UpdateOperationBuilder;
import monotalk.db.utility.ConvertUtils;
import monotalk.db.valuesmapper.EntityValuesMapper;

import static monotalk.db.utility.AssertUtils.assertArgument;
import static monotalk.db.utility.AssertUtils.assertNotNull;

public class DBContentsProviderEntityManager extends BaseEntityManager {
    protected Uri mAuthorityUri;
    protected Context mContext;
    protected ProviderCrudHandler crudHandler;

    /**
     * Constructor
     *
     * @param connectionSource
     * @param context
     */
    DBContentsProviderEntityManager(DatabaseProviderConnectionSource connectionSource, Context context) {
        super(connectionSource.getConnectionSource());
        mAuthorityUri = connectionSource.getAuthorityUri();
        mContext = context;
        crudHandler = newCrudHandler();
    }

    /**
     * CrudHandlerを返す
     */
    protected ProviderCrudHandler newCrudHandler() {
        return new ContentResolverCrudHandler(mContext.getContentResolver());
    }

    public static DBContentsProviderEntityManager newInstance(DatabaseProviderConnectionSource connectionSource, Context context) {
        return new DBContentsProviderEntityManager(connectionSource, context);
    }

    @Override
    public void beginTransactionNonExclusive() {
        Uri uri = UriUtils.buildBeginTransactionUri(mAuthorityUri);
        crudHandler.query(uri, null, null, null, null);
    }

    @Override
    public <T extends Entity> int bulkInsert(List<T> entities) {
        assertNotNull(entities, "entities is null");
        String tableName = MonoTalk.getTableName(entities.get(0).getClass());
        Uri modelUri = UriUtils.buildEntityUri(mAuthorityUri, tableName);
        ContentValues[] valuesArray = QueryBuilder.toValuesArray(entities);
        return crudHandler.bulkInsert(modelUri, valuesArray);
    }

    @Override
    public <T extends Entity> int bulkUpdate(List<T> entities) {
        if (!entities.isEmpty()) {
            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
            Class<T> type = (Class<T>) entities.get(0).getClass();

            EntityValuesMapper<T> mapper = new EntityValuesMapper.Builder(type)
                    .includesNull()
                    .create();

            for (T entity : entities) {
                ContentProviderOperation operation = newUpdateOperationBuilder(type, entity.getId())
                        .withValues(mapper.mapValues(entity)).build();
                operations.add(operation);
            }
            ContentProviderResult[] results = crudHandler.applyBatch(mAuthorityUri.getAuthority(), operations);

            assertArgument((results != null && results.length > 0), "results size should be greater than 0");
            int updateCount = 0;
            for (ContentProviderResult result : results) {
                updateCount += result.count;
            }
            return updateCount;
        }
        return 0;
    }

    @Override
    public <T extends Entity> int bulkDelete(List<T> entities) {
        if (!entities.isEmpty()) {
            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
            Class<T> type = (Class<T>) entities.get(0).getClass();
            for (T entity : entities) {
                ContentProviderOperation operation = newDeleteOperationBuilder(type, entity.getId()).build();
                operations.add(operation);
            }
            ContentProviderResult[] results = crudHandler.applyBatch(mAuthorityUri.getAuthority(), operations);
            assertArgument((results != null && results.length > 0), "results size should be greater than 0");

            int updateCount = 0;
            for (ContentProviderResult result : results) {
                updateCount += result.count;
            }
            return updateCount;
        }
        return 0;
    }

    private DeleteOperationBuilder newDeleteOperationBuilder(Class<? extends Entity> clazz, Long id) {
        return new DeleteOperationBuilder(mAuthorityUri, clazz, id);
    }

    @Override
    public UpdateOperationBuilder newUpdateOperationBuilder(Class<? extends Entity> clazz) {
        assertNotNull(clazz, "clazz is null");
        return new UpdateOperationBuilder(mAuthorityUri, clazz);
    }

    @Override
    public InsertOperationBuilder newInsertOperationBuilder(Class<? extends Entity> clazz) {
        assertNotNull(clazz, "clazz is null");
        return new InsertOperationBuilder(mAuthorityUri, clazz);
    }

    @Override
    public DeleteOperationBuilder newDeleteOperationBuilder(Class<? extends Entity> clazz) {
        assertNotNull(clazz, "clazz is null");
        return new DeleteOperationBuilder(mAuthorityUri, clazz);
    }

    @Override
    public void endTransaction() {
        Uri uri = UriUtils.buildEndTransaction(mAuthorityUri);
        crudHandler.query(uri, null, null, null, null);
    }

    @Override
    public Cursor selectCursorBySql(String sql, Object... selectionArgs) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public void setTransactionSuccessful() {
        Uri uri = UriUtils.buildSetTransactionSuccessfulUri(mAuthorityUri);
        crudHandler.query(uri, null, null, null, null);
    }

    @Override
    public boolean yieldIfContendedSafely() {
        Uri uri = UriUtils.buildYieldIfContendedUri(mAuthorityUri);
        Cursor cursor = crudHandler.query(uri, null, null, null, null);
        return ConvertUtils.toScalarAndClose(Boolean.class, cursor);
    }

    @Override
    public boolean yieldIfContendedSafely(int sleepAfterYieldDelay) {
        Uri uri = UriUtils.buildYieldIfContendedUri(mAuthorityUri, sleepAfterYieldDelay);
        Cursor cursor = crudHandler.query(uri, null, null, null, null);
        return ConvertUtils.toScalarAndClose(Boolean.class, cursor);
    }

    @Override
    public int update(String tableName, ContentValues data, String selection, Object... selectionArgs) {
        Uri baseUri = UriUtils.buildEntityUri(mAuthorityUri, tableName);
        String[] stringArrayArgs = QueryUtils.toStirngArrayArgs(selectionArgs);
        return crudHandler.update(baseUri, data, selection, stringArrayArgs);
    }

    @Override
    public int delete(String tableName, String whereClause, Object... whereArgs) {
        assertNotNull(tableName, "tableName is null");
        Uri modelUri = UriUtils.buildEntityUri(mAuthorityUri, tableName);
        String[] args = QueryUtils.toStirngArrayArgs(whereArgs);
        int updateCount = crudHandler.delete(modelUri, whereClause, args);
        return updateCount;
    }

    @Override
    public long insert(String tableName, ContentValues data) {
        assertNotNull(tableName, "tableName is null");
        assertNotNull(data, "data is null");
        Uri modelUri = UriUtils.buildEntityUri(mAuthorityUri, tableName);
        return ContentUris.parseId(crudHandler.insert(modelUri, data));
    }

    private UpdateOperationBuilder newUpdateOperationBuilder(Class<? extends Entity> clazz, long id) {
        return new UpdateOperationBuilder(mAuthorityUri, clazz, id);
    }

    @Override
    protected Cursor selectCursor(boolean distinct, String tableName, String[] projection, String selection,
                                  String groupBy, String having, String sortOrder, String limit, Object[] selectionArgs) {
        Uri uri = UriUtils.buildQueryUri(mAuthorityUri, tableName, limit, having, groupBy, distinct);
        String[] stringArrayArgs = QueryUtils.toStirngArrayArgs(selectionArgs);
        return crudHandler.query(uri, projection, selection, stringArrayArgs, sortOrder);
    }

    @Override
    protected CursorLoader buildLoader(boolean distinct, String tableName, String[] columns, String where, String groupBy,
                                       String having, String orderBy, String limit, Object[] selectionArgs) {
        CursorLoader loader = new CursorLoader(mContext);
        loader.setProjection(columns);
        loader.setSelection(where);
        String[] stringArrayArgs = QueryUtils.toStirngArrayArgs(selectionArgs);
        loader.setSelectionArgs(stringArrayArgs);
        loader.setSortOrder(orderBy);
        Uri uri = UriUtils.buildQueryUri(mAuthorityUri, tableName, limit, having, groupBy, distinct);
        loader.setUri(uri);
        return loader;
    }

    @Override
    protected CursorLoader buildLoader(String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        CursorLoader loader = new CursorLoader(mContext);
        Uri uri = UriUtils.buildTwoWaySqlUri(mAuthorityUri, entityPath, sqlFilePath, mapPmb);
        loader.setUri(uri);
        return loader;
    }

    @Override
    protected Cursor selectCursorBySqlFile(String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        assertNotNull(entityPath, "entityPath is Null");
        assertNotNull(sqlFilePath, "sqlFilePath is Null");
        Uri uri = UriUtils.buildTwoWaySqlUri(mAuthorityUri, entityPath, sqlFilePath, mapPmb);
        return crudHandler.query(uri, null, null, null, null);
    }
}
