package monotalk.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;

import org.seasar.dbflute.cbean.SimpleMapPmb;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.twowaysql.context.CommandContextCreator;
import org.seasar.dbflute.twowaysql.node.Node;

import java.util.List;

import monotalk.db.compat.DatabaseCompat;

/**
 * Created by Kem on 2015/01/11.
 */
public class SqlManager {
    /**
     * DatabaseInfo
     */
    private DatabaseConnectionSource databaseConnectionSource = null;
    /**
     * SQLiteOpenHelper クラス
     */
    private DatabaseOpenHelper dbHelper = null;

    /**
     * Constructor
     *
     * @param databaseConnectionSource
     */
    public SqlManager(DatabaseConnectionSource databaseConnectionSource) {
        this.databaseConnectionSource = databaseConnectionSource;
        dbHelper = databaseConnectionSource.getDbHelper();
    }

    /**
     * トランザクションを開始する
     */
    public void beginTransactionWithListenerNonExclusive(SQLiteTransactionListener listener) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DatabaseCompat.beginTransactionWithListenerNonExclusive(db, listener);
    }

    /**
     * トランザクションを開始する
     */
    public void beginTransactionNonExclusive() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DatabaseCompat.beginTransactionNonExclusive(db);
    }

    /**
     * @param clazz
     * @param whereClause
     * @param listener
     * @param whereArgs
     * @return
     */
    public int deleteByWhereArrayStringArgs(Class<? extends Entity> clazz, final String whereClause, ModifyExecuteLister listener,
                                            final String... whereArgs) {
        String tableName = MonoTalk.getTableName(clazz);
        return deleteByWhereArrayStringArgs(tableName, whereClause, listener, whereArgs);
    }

    /**
     * @param table
     * @param whereClause
     * @param listener
     * @param whereArgs
     * @return
     */
    public int deleteByWhereArrayStringArgs(final String table, final String whereClause, ModifyExecuteLister listener,
                                            final String... whereArgs) {
        // 更新件数
        int updateCount;
        updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
            @Override
            public Integer doInTransaction(DmlExecutor statement) {
                return (Integer) statement.executeDeleteByWhereArrayStringArgs(table, whereClause, whereArgs);
            }
        }.execute();
        // 更新件数を返す
        return updateCount;
    }

    /**
     * @param table
     * @param whereClause
     * @param listener
     * @param whereArgs
     * @return
     */
    public int delete(final String table, final String whereClause, ModifyExecuteLister listener,
                      final Object... whereArgs) {
        // 更新件数
        int updateCount;
        updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
            @Override
            public Integer doInTransaction(DmlExecutor statement) {
                return (Integer) statement.executeDelete(table, whereClause, whereArgs);
            }
        }.execute();
        // 更新件数を返す
        return updateCount;
    }

    /**
     * @param tableName
     * @param data
     * @param listener
     * @return
     */
    public long insert(final String tableName, final ContentValues data, ModifyExecuteLister listener) {
        long id;
        id = new AbstractModifyStatementExecuter<Long>(dbHelper, listener) {
            @Override
            public Long doInTransaction(DmlExecutor statement) {
                return statement.executeInsert(tableName, data);
            }
        }.execute();
        return id;
    }

    /**
     * @param sql
     * @param listener
     * @param selectionArgs
     * @return
     */
    public Cursor selectCursorBySql(final String sql, SelectExecuteLister listener, final Object... selectionArgs) {
        Cursor cursor = new AbstractSelectStatementExecuter(dbHelper, listener) {
            @Override
            protected Cursor doInTransaction(DmlExecutor statement) {
                Cursor cursor = statement.executeSelectCursor(sql, selectionArgs);
                return cursor;
            }
        }.execute();
        return cursor;
    }

    /**
     * @param distinct
     * @param tableName
     * @param projection
     * @param selection
     * @param groupBy
     * @param having
     * @param sortOrder
     * @param limit
     * @param selectionArgs
     * @param listener
     * @return
     */
    public Cursor selectCursorByWhereArrayStringArgs(final boolean distinct, final String tableName, final String[] projection,
                                                     final String selection, final String groupBy, final String having, final String sortOrder,
                                                     final String limit, final String[] selectionArgs, SelectExecuteLister listener) {
        Cursor cursor = new AbstractSelectStatementExecuter(dbHelper, listener) {
            @Override
            protected Cursor doInTransaction(DmlExecutor statement) {
                Cursor cursor = statement.executeSelectCursorByWhereArrayStringArgs(
                        distinct,
                        tableName,
                        projection,
                        selection,
                        groupBy,
                        having,
                        sortOrder,
                        limit,
                        selectionArgs);
                return cursor;
            }
        }.execute();
        return cursor;
    }

    /**
     * @param distinct
     * @param tableName
     * @param projection
     * @param selection
     * @param groupBy
     * @param having
     * @param sortOrder
     * @param limit
     * @param selectionArgs
     * @param listener
     * @return
     */
    public Cursor selectCursor(final boolean distinct, final String tableName, final String[] projection,
                               final String selection, final String groupBy, final String having, final String sortOrder,
                               final String limit, final Object[] selectionArgs, SelectExecuteLister listener) {
        Cursor cursor = new AbstractSelectStatementExecuter(dbHelper, listener) {
            @Override
            protected Cursor doInTransaction(DmlExecutor statement) {
                Cursor cursor = statement.executeSelectCursor(
                        distinct,
                        tableName,
                        projection,
                        selection,
                        groupBy,
                        having,
                        sortOrder,
                        limit,
                        selectionArgs);
                return cursor;
            }
        }.execute();
        return cursor;
    }

    /**
     * @param clazz
     * @param values
     * @param selection
     * @param listener
     * @param selectionArgs
     * @return
     */
    public int updateByWhereArrayStringArgs(Class<? extends Entity> clazz, final ContentValues values, final String selection, ModifyExecuteLister listener, final String... selectionArgs) {
        String tableName = MonoTalk.getTableName(clazz);
        return updateByWhereArrayStringArgs(tableName, values, selection, listener, selectionArgs);
    }

    /**
     * @param tableName
     * @param values
     * @param selection
     * @param listener
     * @param selectionArgs
     * @return
     */
    public int updateByWhereArrayStringArgs(final String tableName, final ContentValues values, final String selection, ModifyExecuteLister listener, final String... selectionArgs) {
        int updateCount = 0;
        updateCount = (int) new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
            @Override
            public Integer doInTransaction(DmlExecutor statement) {
                return (Integer) statement.executeUpdateByWhereArrayStringArgs(tableName, values, selection, selectionArgs);
            }
        }.execute();
        return updateCount;
    }

    /**
     * @param tableName
     * @param values
     * @param selection
     * @param listener
     * @param selectionArgs
     * @return
     */
    public int update(final String tableName, final ContentValues values, final String selection, ModifyExecuteLister listener, final Object... selectionArgs) {
        int updateCount = 0;
        updateCount = (int) new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
            @Override
            public Integer doInTransaction(DmlExecutor statement) {
                return (Integer) statement.executeUpdate(tableName, values, selection, selectionArgs);
            }
        }.execute();
        return updateCount;
    }

    /**
     * 指定されたSQLを実行しデータを取得する
     *
     * @param sqlFilePath
     * @param mapPmb
     * @param listener
     * @return
     */
    public Cursor selectCursorBySqlFile(final String sqlFilePath, final SimpleMapPmb<Object> mapPmb,
                                        SelectExecuteLister listener) {
        // Node create
        Node node = databaseConnectionSource.getNode(sqlFilePath);
        final CommandContext context;
        if (mapPmb != null) {
            context = createCommandContext(mapPmb);
        } else {
            context = createCommandContext(null, null, null);
        }
        node.accept(context);

        // Cast selectionArgs to Strings
        final Object[] args = context.getBindVariables();
        Cursor cursor = new AbstractSelectStatementExecuter(dbHelper, listener) {
            @Override
            protected Cursor doInTransaction(DmlExecutor statement) {
                Cursor cursor = statement.executeSelectCursor(context.getSql(), args);
                return cursor;
            }
        }.execute();
        return cursor;
    }

    public void endTransaction() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.endTransaction();
    }

    /**
     * Idをキーにデータを削除する
     *
     * @param entity
     * @param id
     * @param listener
     * @return
     */
    public int deleteById(Class<? extends Entity> entity, final long id, ModifyExecuteLister listener) {
        final TableStatement.TableDeleteStatement deleteStatement =
                databaseConnectionSource.getTableDeleteStatement(entity);
        // 更新件数
        int updateCount;
        updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
            @Override
            public Integer doInTransaction(DmlExecutor statement) {
                return deleteStatement.executeDelete(id);
            }
        }.execute();
        // 更新件数を返す
        return updateCount;
    }

    /**
     * @param clazz
     * @param values
     * @param listener
     * @return
     */
    public long insert(Class<? extends Entity> clazz, final ContentValues values, ModifyExecuteLister listener) {
        final TableStatement.TableInsertStatement insertStatement = databaseConnectionSource.getTableInsertStatement(clazz);
        long id = new AbstractModifyStatementExecuter<Long>(dbHelper, listener) {
            @Override
            public Long doInTransaction(DmlExecutor statement) {
                return insertStatement.executeInsert(values);
            }
        }.execute();
        return id;
    }

    /**
     * @param clazz
     * @param id
     * @return
     */
    public Cursor selectCursorById(Class<? extends Entity> clazz, final long id, SelectExecuteLister listener) {
        final TableStatement.TableSelectStatement selectStatement = databaseConnectionSource.getTableSelectStatement(clazz);
        Cursor cursor = new AbstractSelectStatementExecuter(dbHelper, listener) {
            @Override
            protected Cursor doInTransaction(DmlExecutor statement) {
                return selectStatement.rawQery(id);
            }
        }.execute();
        return cursor;

    }

    /**
     * @param distinct
     * @param clazz
     * @param projection
     * @param selection
     * @param groupBy
     * @param having
     * @param sortOrder
     * @param limit
     * @param selectionArgs
     * @param listner
     * @return
     */
    public Cursor selectCursorByWhereArrayStringArgs(boolean distinct, Class<? extends Entity> clazz, String[] projection, String selection, String groupBy, String having, String sortOrder, String limit, String[] selectionArgs, SelectExecuteLister listner) {
        String tableName = MonoTalk.getTableName(clazz);
        return selectCursorByWhereArrayStringArgs(
                distinct,
                tableName,
                projection,
                selection,
                groupBy,
                having,
                sortOrder,
                limit,
                selectionArgs,
                listner);
    }

    public int updateById(Class<? extends Entity> clazz, final ContentValues values, final long id, ModifyExecuteLister listener) {
        final TableStatement.TableUpdateStatement updateStatement = databaseConnectionSource.getTableUpdateStatement(clazz);
        int updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
            @Override
            public Integer doInTransaction(DmlExecutor statement) {
                return updateStatement.executeUpdate(values, id);
            }
        }.execute();
        return updateCount;
    }

    /**
     * @param entities
     * @param listener
     * @return
     */
    public int bulkInsert(final List<? extends Entity> entities, final ModifyExecuteLister listener) {
        int updateCount = 0;
        if (!entities.isEmpty()) {
            Class<? extends Entity> type = (Class<? extends Entity>) entities.get(0).getClass();
            final TableStatement.TableInsertStatement tableInsertStatement = databaseConnectionSource.getTableInsertStatement(type);
            updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
                @Override
                public Integer doInTransaction(DmlExecutor statement) {
                    int numValues = 0;
                    for (Entity entity : entities) {
                        tableInsertStatement.executeInsert(entity);
                        numValues++;
                    }
                    return numValues;
                }
            }.execute();
        }
        return updateCount;
    }

    /**
     * 一括登録する
     *
     * @param clazz
     * @param values
     * @param listener
     * @return 更新件数
     */
    public int bulkInsert(final Class<? extends Entity> clazz, final ContentValues[] values, final ModifyExecuteLister listener) {
        int updateCount;
        final TableStatement.TableInsertStatement insertStatement = databaseConnectionSource.getTableInsertStatement(clazz);
        updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {

            @Override
            public Integer doInTransaction(DmlExecutor statement) {
                int numValues = 0;
                for (ContentValues data : values) {
                    insertStatement.executeInsert(data);
                    numValues++;
                }
                return numValues;
            }
        }.execute();
        return updateCount;
    }

    /**
     * 一括更新する
     *
     * @param entities entityのList
     * @param listener 更新系Listener
     * @param <T>
     * @return 更新件数
     */
    public <T extends Entity> int bulkUpdate(final List<T> entities, final ModifyExecuteLister listener) {
        int updateCount = 0;
        if (!entities.isEmpty()) {
            final Class<T> type = (Class<T>) entities.get(0).getClass();
            final TableStatement.TableUpdateStatement updateStatement = databaseConnectionSource.getTableUpdateStatement(type);
            updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
                @Override
                public Integer doInTransaction(DmlExecutor statement) {
                    int numValues = 0;
                    for (T entity : entities) {
                        numValues += updateStatement.executeUpdate(entity);
                    }
                    return numValues;
                }
            }.execute();
        }
        return updateCount;
    }

    /**
     * 一括削除する
     *
     * @param entities entityのList
     * @param listener 更新系Listener
     * @param <T>
     * @return 更新件数
     */
    public <T extends Entity> int bulkDelete(final List<T> entities, final ModifyExecuteLister listener) {
        int updateCount = 0;
        if (!entities.isEmpty()) {
            final Class<T> type = (Class<T>) entities.get(0).getClass();
            final TableStatement.TableDeleteStatement deleteStatement =
                    databaseConnectionSource.getTableDeleteStatement(type);
            updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
                @Override
                public Integer doInTransaction(DmlExecutor statement) {
                    int numValues = 0;
                    for (T entity : entities) {
                        numValues += deleteStatement.executeDelete(entity);
                    }
                    return numValues;
                }
            }.execute();
        }
        return updateCount;
    }


    /**
     * Inner Class
     */
    abstract class AbstractSelectStatementExecuter {

        private SQLiteOpenHelper helper;
        private SelectExecuteLister listener = new DefaultSelectExecuteListener();

        /**
         * コンストラクター
         *
         * @param helper
         */
        public AbstractSelectStatementExecuter(final SQLiteOpenHelper helper, SelectExecuteLister listener) {
            this.helper = helper;
            this.listener = listener;
        }

        /**
         * 実行メソッド
         */
        public Cursor execute() {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = null;
            boolean doAsTransaction = !db.inTransaction();
            try {
                DmlExecutor statement = new DmlExecutor(db);
                if (doAsTransaction) {
                    // Transaction内でない(単独実行クエリの場合)
                    DatabaseCompat.beginTransactionWithListenerNonExclusive(db, listener);
                }
                onPreExecuteQuery();
                cursor = doInTransaction(statement);
                onPostExecuteQeury(cursor);

                if (doAsTransaction) {
                    db.setTransactionSuccessful();
                }
            } catch (SQLiteException e) {
                // エラー処理
                onExecuteError(cursor, e);
                throw e;
            } finally {
                if (db != null && doAsTransaction) {
                    db.endTransaction();
                }
            }
            // 戻り値を返す
            return cursor;
        }

        /**
         * 抽象メソッド
         *
         * @return
         */
        protected abstract Cursor doInTransaction(DmlExecutor statement);

        private void onPreExecuteQuery() {
            if (listener != null) {
                listener.onPreExecuteQuery();
            }
        }

        private void onPostExecuteQeury(Cursor cursor) {
            if (listener != null) {
                listener.onPostExecuteQeury(cursor);
            }
        }

        private void onExecuteError(Cursor cursor, RuntimeException e) {
            if (listener != null) {
                listener.onExecuteError(cursor, e);
            }
        }
    }

    /**
     * インナークラス
     *
     * @author Kem
     */
    private abstract class AbstractModifyStatementExecuter<T extends Number> {
        private SQLiteOpenHelper helper;
        private ModifyExecuteLister listener = new DefaultModifyExecuteListener();

        /**
         * コンストラクター
         *
         * @param helper
         */
        public AbstractModifyStatementExecuter(final SQLiteOpenHelper helper, final ModifyExecuteLister listener) {
            this.helper = helper;
            this.listener = listener;
        }

        /**
         * 実行メソッド
         *
         * @return
         */
        public T execute() {
            T result = null;
            SQLiteDatabase db = helper.getWritableDatabase();
            boolean doAsTransaction = !db.inTransaction();

            try {
                DmlExecutor statement = new DmlExecutor(db);
                if (doAsTransaction) {
                    // Transaction内でない(単独実行クエリの場合)
                    DatabaseCompat.beginTransactionWithListenerNonExclusive(db, listener);
                }
                onPreExecuteQeury();
                result = doInTransaction(statement);
                onPostExecuteQeury();

                if (doAsTransaction) {
                    db.setTransactionSuccessful();
                }
            } catch (SQLiteException e) {
                // エラー処理
                onExecuteError(e);
                throw e;
            } finally {
                if (db != null && doAsTransaction) {
                    db.endTransaction();
                }
            }
            // 戻り値を返す
            return result;
        }

        /**
         * 抽象メソッド
         *
         * @return
         */
        protected abstract T doInTransaction(DmlExecutor statement);

        private void onPreExecuteQeury() {
            if (listener != null) {
                listener.onPreExecuteQuery();
            }
        }

        private void onPostExecuteQeury() {
            if (listener != null) {
                listener.onPostExecuteQeury();
            }
        }

        private void onExecuteError(RuntimeException e) {
            if (listener != null) {
                listener.onExecuteError();
            }
        }
    }

    private CommandContext createCommandContext(Object pmb) {
        return createCommandContext(new Object[]{pmb}, new String[]{"pmb"}, new Class<?>[]{pmb.getClass()});
    }

    private CommandContext createCommandContext(Object[] args, String[] argNames, Class<?>[] argTypes) {
        return new CommandContextCreator(argNames, argTypes).createCommandContext(args);
    }

    /**
     * setTransactionSuccessfulを実行する
     */
    public void setTransactionSuccessful() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.setTransactionSuccessful();
    }

    /**
     * yieldIfContendedSafelyを実行する
     *
     * @return
     */
    public boolean yieldIfContendedSafely() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.yieldIfContendedSafely();
    }

    /**
     * yieldIfContendedSafelyを実行する
     *
     * @param sleepAfterYieldDelay
     * @return
     */
    public boolean yieldIfContendedSafely(int sleepAfterYieldDelay) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.yieldIfContendedSafely(sleepAfterYieldDelay);
    }

    public static interface ModifyExecuteLister extends SQLiteTransactionListener {
        public void onExecuteError();

        public void onPreExecuteQuery();

        public void onPostExecuteQeury();
    }

    public static interface SelectExecuteLister extends SQLiteTransactionListener {
        public void onExecuteError(Cursor cursor, RuntimeException e);

        public void onPreExecuteQuery();

        public void onPostExecuteQeury(Cursor cursor);
    }

    public static class DefaultModifyExecuteListener implements ModifyExecuteLister {
        @Override
        public void onExecuteError() {

        }

        @Override
        public void onPreExecuteQuery() {

        }

        @Override
        public void onPostExecuteQeury() {

        }

        @Override
        public void onBegin() {

        }

        @Override
        public void onCommit() {

        }

        @Override
        public void onRollback() {

        }
    }

    public static class DefaultSelectExecuteListener implements SelectExecuteLister {
        @Override
        public void onExecuteError(Cursor cursor, RuntimeException e) {

        }

        @Override
        public void onPreExecuteQuery() {

        }

        @Override
        public void onPostExecuteQeury(Cursor cursor) {

        }

        @Override
        public void onBegin() {

        }

        @Override
        public void onCommit() {

        }

        @Override
        public void onRollback() {

        }
    }
}
