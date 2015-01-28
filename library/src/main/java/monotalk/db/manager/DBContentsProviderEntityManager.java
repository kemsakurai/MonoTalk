package monotalk.db.manager;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import java.util.ArrayList;
import java.util.List;

import monotalk.db.DatabaseProviderConnectionSource;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.UriUtils;
import monotalk.db.query.DeleteOperationBuilder;
import monotalk.db.query.InsertOperationBuilder;
import monotalk.db.query.QueryUtils;
import monotalk.db.query.UpdateOperationBuilder;
import monotalk.db.querydata.SelectQueryData;
import monotalk.db.querydata.TwoWayQueryData;
import monotalk.db.utility.ConvertUtils;
import monotalk.db.valuesmapper.EntityValuesMapper;

import static monotalk.db.query.QueryUtils.arrayFrom;
import static monotalk.db.utility.AssertUtils.assertArgument;
import static monotalk.db.utility.AssertUtils.assertNotNull;

/**
 * ContentsProviderを介してDBアクセスを行う
 */
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
        ContentValues[] valuesArray = arrayFrom(entities);
        return crudHandler.bulkInsert(modelUri, valuesArray);
    }

    @Override
    public <T extends Entity> int bulkUpdate(List<T> entities) {
        assertNotNull(entities, "entities is null");
        if (!entities.isEmpty()) {
            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
            Class<T> type = (Class<T>) entities.get(0).getClass();

            EntityValuesMapper<T> mapper = new EntityValuesMapper.Builder(type)
                    .includesNull()
                    .create();

            for (T entity : entities) {
                ContentProviderOperation operation = newUpdateOperationBuilder(type, entity.id)
                        .values(mapper.mapValues(entity)).build();
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
        assertNotNull(entities, "entities is null");
        if (!entities.isEmpty()) {
            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
            Class<T> type = (Class<T>) entities.get(0).getClass();
            for (T entity : entities) {
                ContentProviderOperation operation = newDeleteByIdOperation(type, entity.id);
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
    public ContentProviderOperation newUpdateByIdOperation(Class<? extends Entity> clazz, ContentValues value, long id) {
        assertNotNull(clazz, "clazz is null");
        assertNotNull(value, "value is null");
        return new UpdateOperationBuilder(mAuthorityUri, clazz, id).values(value).build();
    }

    @Override
    public UpdateOperationBuilder newUpdateOperationBuilder(Class<? extends Entity> clazz) {
        assertNotNull(clazz, "clazz is null");
        return new UpdateOperationBuilder(mAuthorityUri, clazz);
    }

    @Override
    public ContentProviderOperation newDeleteByIdOperation(Class<? extends Entity> clazz, long id) {
        return new DeleteOperationBuilder(mAuthorityUri, clazz, id).build();
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
    public CursorLoader buildLoader(SelectQueryData data) {
        CursorLoader loader = new CursorLoader(mContext);
        loader.setProjection(data.getColumns());
        loader.setSelection(data.getWhere());
        loader.setSelectionArgs(data.getStringSelectionArgs());
        loader.setSortOrder(data.getOrderBy());
        loader.setUri(data.buildQueryUri(mAuthorityUri));
        return loader;
    }

    @Override
    public Cursor selectCursor(SelectQueryData data) {
        Uri uri = data.buildQueryUri(mAuthorityUri);
        return crudHandler.query(uri,
                data.getColumns(),
                data.getWhere(),
                data.getStringSelectionArgs(),
                data.getOrderBy());
    }

    @Override
    public CursorLoader buildLoader(TwoWayQueryData data) {
        CursorLoader loader = new CursorLoader(mContext);
        Uri uri = data.buildTwoWaySqlUri(mAuthorityUri);
        loader.setUri(uri);
        return loader;
    }

    @Override
    public Cursor selectCursorBySqlFile(TwoWayQueryData data) {
        assertNotNull(data, "data is Null");
        Uri uri = data.buildTwoWaySqlUri(mAuthorityUri);
        return crudHandler.query(uri, null, null, null, null);
    }
}
