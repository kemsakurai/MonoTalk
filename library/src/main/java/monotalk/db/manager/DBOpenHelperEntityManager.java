/*******************************************************************************
 * Copyright (C) 2012-2013 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package monotalk.db.manager;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import org.seasar.dbflute.cbean.SimpleMapPmb;

import java.util.List;

import monotalk.db.DatabaseConnectionSource;
import monotalk.db.Entity;
import monotalk.db.SqlManager;
import monotalk.db.query.DeleteOperationBuilder;
import monotalk.db.query.InsertOperationBuilder;
import monotalk.db.query.QueryCrudHandler;
import monotalk.db.query.UpdateOperationBuilder;

/**
 * EntityManagerの実装クラス
 * DatabaseOpenHelper(SqLiteOpenHelperの継承クラス)を介したDBアクセスを行う
 */
public class DBOpenHelperEntityManager extends BaseEntityManager implements QueryCrudHandler {
    /**
     * SQLiteOpenHelper クラス
     */
    private SqlManager mSqlManager = null;

    /**
     * Constructor
     *
     * @param connectionSource Database接続情報
     */
    private DBOpenHelperEntityManager(DatabaseConnectionSource connectionSource) {
        super(connectionSource);
        mSqlManager = new SqlManager(connectionSource);
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
        mSqlManager.beginTransactionNonExclusive();
    }

    @Override
    public <T extends Entity> int bulkInsert(List<T> entities) {
        return mSqlManager.bulkInsert(entities, null);
    }

    @Override
    public <T extends Entity> int bulkUpdate(List<T> entities) {
        return mSqlManager.bulkUpdate(entities, null);
    }

    @Override
    public <T extends Entity> int bulkDelete(List<T> entities) {
        return mSqlManager.bulkDelete(entities, null);
    }

    @Override
    protected CursorLoader buildLoader(boolean distinct, String tableName, String[] columns, String where, String groupBy,
                                       String having, String orderBy, String limit, Object[] selectionArgs) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    protected CursorLoader buildLoader(String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public int delete(String tableName, String whereClause, Object... whereArgs) {
        return mSqlManager.delete(tableName, whereClause, null, whereArgs);
    }

    @Override
    public void endTransaction() {
        mSqlManager.endTransaction();
    }

    @Override
    public Cursor selectCursorBySql(String sql, Object... selectionArgs) {
        return mSqlManager.selectCursorBySql(sql, null, selectionArgs);
    }

    @Override
    protected Cursor selectCursor(boolean distinct, String tableName, String[] columns, String where, String groupBy, String having, String orderBy, String limit, Object[] selectionArgs) {
        return mSqlManager.selectCursor(distinct, tableName, columns, where, groupBy, having, orderBy, limit, selectionArgs, null);
    }

    @Override
    protected Cursor selectCursorBySqlFile(String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        return mSqlManager.selectCursorBySqlFile(sqlFilePath, mapPmb, null);
    }


    @Override
    public long insert(String tableName, ContentValues values) {
        return mSqlManager.insert(tableName, values, null);
    }

    @Override
    public void setTransactionSuccessful() {
        mSqlManager.setTransactionSuccessful();
    }

    @Override
    public int update(String tableName, ContentValues data, String selection, Object... selectionArgs) {
        return mSqlManager.update(tableName, data, selection, null, selectionArgs);
    }

    @Override
    public UpdateOperationBuilder newUpdateOperationBuilder(Class<? extends Entity> clazz) {
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
    public boolean yieldIfContendedSafely() {
        return mSqlManager.yieldIfContendedSafely();
    }

    @Override
    public boolean yieldIfContendedSafely(int sleepAfterYieldDelay) {
        return mSqlManager.yieldIfContendedSafely(sleepAfterYieldDelay);
    }
}