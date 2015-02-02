/*******************************************************************************
 * Copyright (C) 2013-2015 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package monotalk.db.manager;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import java.util.List;

import monotalk.db.DatabaseConnectionSource;
import monotalk.db.DmlExecutor;
import monotalk.db.Entity;
import monotalk.db.TransactionManager;
import monotalk.db.query.DeleteOperationBuilder;
import monotalk.db.query.InsertOperationBuilder;
import monotalk.db.query.QueryCrudHandler;
import monotalk.db.query.UpdateOperationBuilder;
import monotalk.db.querydata.SelectQueryData;
import monotalk.db.querydata.TwoWayQueryData;

/**
 * EntityManagerの実装クラス
 * DatabaseOpenHelper(SqLiteOpenHelperの継承クラス)を介したDBアクセスを行う
 */
public class DBOpenHelperEntityManager extends BaseEntityManager implements QueryCrudHandler {
    /**
     * SQLiteOpenHelper クラス
     */
    private DmlExecutor mDmlExecutor = null;

    /**
     * TransactionManager クラス
     */
    private TransactionManager txManager = null;

    /**
     * Constructor
     *
     * @param connectionSource Database接続情報
     */
    private DBOpenHelperEntityManager(DatabaseConnectionSource connectionSource) {
        mDmlExecutor = new DmlExecutor(connectionSource);
        txManager = new TransactionManager(connectionSource.getDbHelper());
    }

    /**
     * インスタンスを生成する
     *
     * @param connectionSource
     * @return DBOpenHelperEntityManager
     */
    public static DBOpenHelperEntityManager newInstance(DatabaseConnectionSource connectionSource) {
        return new DBOpenHelperEntityManager(connectionSource);
    }

    @Override
    public void beginTransactionNonExclusive() {
        txManager.beginTransactionNonExclusive();
    }

    @Override
    public <T extends Entity> int bulkInsert(List<T> entities) {
        return mDmlExecutor.bulkInsert(entities);
    }

    @Override
    public <T extends Entity> int bulkUpdate(List<T> entities) {
        return mDmlExecutor.bulkUpdate(entities);
    }

    @Override
    public <T extends Entity> int bulkDelete(List<T> entities) {
        return mDmlExecutor.bulkDelete(entities);
    }

    @Override
    public ContentProviderOperation newUpdateByIdOperation(Class<? extends Entity> clazz, ContentValues value, long id) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public UpdateOperationBuilder newUpdateOperationBuilder(Class<? extends Entity> clazz) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public ContentProviderOperation newDeleteByIdOperation(Class<? extends Entity> clazz, long id) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public InsertOperationBuilder newInsertOperationBuilder(Class<? extends Entity> clazz) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public DeleteOperationBuilder newDeleteOperationBuilder(Class<? extends Entity> clazz) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public void endTransaction() {
        txManager.endTransaction();
    }

    @Override
    public Cursor selectCursorBySql(String sql, Object... selectionArgs) {
        return mDmlExecutor.selectCursorBySql(sql, selectionArgs);
    }

    @Override
    public void setTransactionSuccessful() {
        txManager.setTransactionSuccessful();
    }

    @Override
    public boolean yieldIfContendedSafely() {
        return txManager.yieldIfContendedSafely();
    }

    @Override
    public boolean yieldIfContendedSafely(int sleepAfterYieldDelay) {
        return txManager.yieldIfContendedSafely(sleepAfterYieldDelay);
    }

    @Override
    public int update(String tableName, ContentValues data, String selection, Object... selectionArgs) {
        return mDmlExecutor.update(tableName, data, selection, selectionArgs);
    }

    @Override
    public int delete(String tableName, String whereClause, Object... whereArgs) {
        return mDmlExecutor.delete(tableName, whereClause, whereArgs);
    }

    @Override
    public long insert(String tableName, ContentValues values) {
        return mDmlExecutor.insert(tableName, values, null);
    }

    @Override
    public CursorLoader buildLoader(SelectQueryData data) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public Cursor selectCursor(SelectQueryData data) {
        return mDmlExecutor.selectCursorBySql(data.toSql(), data.getSelectionArgs());
    }

    @Override
    public CursorLoader buildLoader(TwoWayQueryData data) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public Cursor selectCursorBySqlFile(TwoWayQueryData data) {
        return mDmlExecutor.selectCursorBySqlFile(data.getSqlFilePath(), data.getMapPmb());
    }

    @Override
    public Integer executeBySqlFile(TwoWayQueryData data) {
        return mDmlExecutor.executeBySqlFile(data.getSqlFilePath(), data.getMapPmb());
    }
}