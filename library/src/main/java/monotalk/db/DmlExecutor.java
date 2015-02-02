package monotalk.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.seasar.dbflute.cbean.SimpleMapPmb;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.twowaysql.context.CommandContextCreator;
import org.seasar.dbflute.twowaysql.node.Node;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import monotalk.db.compat.DatabaseCompat;
import monotalk.db.query.QueryUtils;

import static monotalk.db.TransactionManager.AbstractModifyStatementExecuter;
import static monotalk.db.TransactionManager.AbstractSelectStatementExecuter;
import static monotalk.db.TransactionManager.ModifyExecuteLister;
import static monotalk.db.TransactionManager.SelectExecuteLister;

/**
 * Created by Kem on 2015/01/11.
 */
public class DmlExecutor {
    private final static String TAG_NAME = DBLog.getTag(DmlExecutor.class);
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
    public DmlExecutor(DatabaseConnectionSource databaseConnectionSource) {
        this.databaseConnectionSource = databaseConnectionSource;
        dbHelper = databaseConnectionSource.getDbHelper();
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
            public Integer doInTransaction(SQLiteDatabase db) {
                int updateCount1 = -1;
                try {
                    if (DBLog.isLoggable(DBLog.LogLevel.DEBUG)) {
                        notifyStartParameterDebug("delete");
                        DBLog.d(TAG_NAME, "tableName=" + table);
                        DBLog.d(TAG_NAME, "whereClause=" + whereClause);
                        DBLog.d(TAG_NAME, "whereArgs=" + Arrays.toString(whereArgs));
                        notifyEndParameterDebug();
                    }
                    updateCount1 = db.delete(table, whereClause, whereArgs);
                } catch (RuntimeException e) {
                    ModifyStatementErrorMsgBuilder builder = new ModifyStatementErrorMsgBuilder();
                    builder.setTableName(table).setSelection(whereClause).setSelectionArgs(whereArgs);
                    // エラーログ出力
                    DBLog.e(TAG_NAME, "Raise RuntimeException" + builder.buildMsg());
                    throw e;
                }
                return (Integer) updateCount1;
            }
        }.execute();
        // 更新件数を返す
        return updateCount;
    }

    private void notifyStartParameterDebug(String methodName) {
        DBLog.d(TAG_NAME, "DEBUG method [" + methodName + "] Parameters >>>>>>>>>>>>>>>>>>>>>>>>");
    }

    private void notifyEndParameterDebug() {
        DBLog.d(TAG_NAME, "DEBUG method Parameters <<<<<<<<<<<<<<<<<<<<<<<<");
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
            public Long doInTransaction(SQLiteDatabase db) {
                long id1;
                try {
                    if (DBLog.isLoggable(DBLog.LogLevel.DEBUG)) {
                        notifyStartParameterDebug("executeInsertOrThrow");
                        DBLog.d(TAG_NAME, "tableName=" + tableName);
                        DBLog.d(TAG_NAME, "data=" + data.toString());
                        notifyEndParameterDebug();
                    }
                    id1 = (int) db.insertOrThrow(tableName, null, data);
                } catch (RuntimeException e) {
                    ModifyStatementErrorMsgBuilder builder = new ModifyStatementErrorMsgBuilder();
                    builder.setTableName(tableName).setValues(data);
                    DBLog.e(TAG_NAME, "Raise SQLiteException " + builder.buildMsg());
                    throw e;
                }
                return id1;
            }
        }.execute();
        return id;
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
            public Integer doInTransaction(SQLiteDatabase db) {
                // Tableアノテーションを取得
                int updateCount1 = -1;
                try {
                    if (DBLog.isLoggable(DBLog.LogLevel.DEBUG)) {
                        notifyStartParameterDebug("executeUpdate");
                        DBLog.d(TAG_NAME, "tableName=" + tableName);
                        DBLog.d(TAG_NAME, "data=" + values);
                        DBLog.d(TAG_NAME, "whereClause=" + selection);
                        DBLog.d(TAG_NAME, "whereArgs=" + Arrays.toString(selectionArgs));
                        notifyEndParameterDebug();
                    }
                    updateCount1 = db.update(tableName, values, selection, selectionArgs);
                } catch (RuntimeException e) {
                    ModifyStatementErrorMsgBuilder builder = new ModifyStatementErrorMsgBuilder();
                    builder.setTableName(tableName).setValues(values).setSelection(selection).setSelectionArgs(selectionArgs);
                    DBLog.e(TAG_NAME, "Raise RuntimeException " + builder.buildMsg());
                    throw e;
                }
                return (Integer) updateCount1;
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
            public Integer doInTransaction(SQLiteDatabase db) {
                // Tableアノテーションを取得
                int updateCount1 = -1;
                try {
                    if (DBLog.isLoggable(DBLog.LogLevel.DEBUG)) {
                        notifyStartParameterDebug("executeUpdate");
                        DBLog.d(TAG_NAME, "tableName=" + tableName);
                        DBLog.d(TAG_NAME, "data=" + values);
                        DBLog.d(TAG_NAME, "whereClause=" + selection);
                        DBLog.d(TAG_NAME, "whereArgs=" + Arrays.toString(selectionArgs));
                        notifyEndParameterDebug();
                    }
                    int size = values.size();
                    String[] columnNames = new String[size];
                    int index = 0;
                    for (Map.Entry<String, Object> entry : values.valueSet()) {
                        columnNames[index] = entry.getKey();
                        index++;
                    }
                    String updateSql = QueryUtils.toUpdateSql(tableName, columnNames, selection);
                    SQLiteStatement statement = db.compileStatement(updateSql);
                    int bindIndex = 1;
                    for (String columnName : columnNames) {
                        DatabaseUtils.bindObjectToProgram(statement, bindIndex, values.get(columnName));
                        bindIndex++;
                    }
                    for (Object arg : selectionArgs) {
                        DatabaseUtils.bindObjectToProgram(statement, bindIndex, arg);
                        bindIndex++;
                    }
                    updateCount1 = DatabaseCompat.executeUpdateDelete(db, statement);

                } catch (RuntimeException e) {
                    ModifyStatementErrorMsgBuilder builder = new ModifyStatementErrorMsgBuilder();
                    builder.setTableName(tableName).setValues(values).setSelection(selection).setSelectionArgs(selectionArgs);
                    DBLog.e(TAG_NAME, "Raise RuntimeException " + builder.buildMsg());
                    throw e;
                }
                return (Integer) updateCount1;
            }
        }.execute();
        return updateCount;
    }

    /**
     * 指定されたSQLを実行しデータを取得する
     *
     * @param sqlFilePath
     * @param mapPmb
     * @return
     */
    public Cursor selectCursorBySqlFile(final String sqlFilePath, final SimpleMapPmb<Object> mapPmb) {
        return selectCursorBySqlFile(sqlFilePath, mapPmb);
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
        if (mapPmb != null && !mapPmb.isEmpty()) {
            context = createCommandContext(mapPmb);
        } else {
            context = createCommandContext(null, null, null);
        }
        node.accept(context);

        // Cast selectionArgs to Strings
        final Object[] args = context.getBindVariables();
        Cursor cursor = null;
        try {
            if (DBLog.isLoggable(DBLog.LogLevel.DEBUG)) {
                notifyStartParameterDebug("selectCursorBySqlFile");
                DBLog.d(TAG_NAME, "sqlFilePath=" + sqlFilePath);
                DBLog.d(TAG_NAME, "sql=" + context.getSql());
                DBLog.d(TAG_NAME, "args=" + Arrays.toString(args));
                notifyEndParameterDebug();
            }
            cursor = new AbstractSelectStatementExecuter(dbHelper, listener) {
                @Override
                protected Cursor doInTransaction(SQLiteDatabase statement) {
                    String[] strginArgs = QueryUtils.toStirngArrayArgs(args);
                    Cursor cursor = statement.rawQuery(context.getSql(), strginArgs);
                    return cursor;
                }
            }.execute();
        } catch (RuntimeException e) {
            SelectStatementErrorMsgBuilder builder = new SelectStatementErrorMsgBuilder();
            builder.setSql(context.getSql());
            builder.setSelectionArgs(QueryUtils.toStirngArrayArgs(args));
            // エラーログ出力
            DBLog.e(TAG_NAME, "Raise RuntimeException" + builder.buildMsg());
            throw e;
        }
        return cursor;
    }

    private CommandContext createCommandContext(Object pmb) {
        return createCommandContext(new Object[]{pmb}, new String[]{"pmb"}, new Class<?>[]{pmb.getClass()});
    }

    private CommandContext createCommandContext(Object[] args, String[] argNames, Class<?>[] argTypes) {
        return new CommandContextCreator(argNames, argTypes).createCommandContext(args);
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
            public Integer doInTransaction(SQLiteDatabase db) {
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
            public Long doInTransaction(SQLiteDatabase db) {
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
            protected Cursor doInTransaction(SQLiteDatabase db) {
                return selectStatement.rawQery(id);
            }
        }.execute();
        return cursor;

    }

    public int updateById(Class<? extends Entity> clazz, final ContentValues values, final long id, ModifyExecuteLister listener) {
        final TableStatement.TableUpdateStatement updateStatement = databaseConnectionSource.getTableUpdateStatement(clazz);
        int updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
            @Override
            public Integer doInTransaction(SQLiteDatabase db) {
                return updateStatement.executeUpdate(values, id);
            }
        }.execute();
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
            public Integer doInTransaction(SQLiteDatabase db) {
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
     * データを削除する
     *
     * @param tableName
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int delete(String tableName, String whereClause, Object... whereArgs) {
        return delete(tableName, whereClause, null, whereArgs);
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
            public Integer doInTransaction(SQLiteDatabase db) {
                int updateCount1 = -1;
                try {
                    if (DBLog.isLoggable(DBLog.LogLevel.DEBUG)) {
                        notifyStartParameterDebug("delete");
                        DBLog.d(TAG_NAME, "tableName=" + table);
                        DBLog.d(TAG_NAME, "whereClause=" + whereClause);
                        DBLog.d(TAG_NAME, "whereArgs=" + Arrays.toString(whereArgs));
                        notifyEndParameterDebug();
                    }
                    String deleteSql = QueryUtils.toDeleteSql(table, whereClause);
                    SQLiteStatement statement = db.compileStatement(deleteSql);
                    int size = whereArgs.length;
                    for (int i = 0; i < size; i++) {
                        DatabaseUtils.bindObjectToProgram(statement, i + 1, whereArgs[i]);
                    }
                    updateCount1 = DatabaseCompat.executeUpdateDelete(db, statement);
                } catch (RuntimeException e) {
                    ModifyStatementErrorMsgBuilder builder = new ModifyStatementErrorMsgBuilder();
                    builder.setTableName(table).setSelection(whereClause).setSelectionArgs(whereArgs);
                    // エラーログ出力
                    DBLog.e(TAG_NAME, "Raise RuntimeException" + builder.buildMsg());
                    throw e;
                }
                return (Integer) updateCount1;
            }
        }.execute();
        // 更新件数を返す
        return updateCount;
    }

    public <T extends Entity> int bulkDelete(List<T> entities) {
        return bulkDelete(entities, null);
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
                public Integer doInTransaction(SQLiteDatabase sqLiteDatabase) {
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
     * データを更新します
     *
     * @param tableName     Table名
     * @param data          ContentValues
     * @param selection     Where句
     * @param selectionArgs Bindパラメータ
     * @return
     */
    public int update(String tableName, ContentValues data, String selection, Object[] selectionArgs) {
        return update(tableName, data, selection, null, selectionArgs);
    }

    /**
     * 一括登録する
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T extends Entity> int bulkInsert(List<T> entities) {
        return bulkInsert(entities, null);
    }

    /**
     * 一括登録する
     *
     * @param entities
     * @param listener
     * @return
     */
    public int bulkInsert(final List<? extends Entity> entities, final TransactionManager.ModifyExecuteLister listener) {
        int updateCount = 0;
        if (!entities.isEmpty()) {
            Class<? extends Entity> type = (Class<? extends Entity>) entities.get(0).getClass();
            final TableStatement.TableInsertStatement tableInsertStatement = databaseConnectionSource.getTableInsertStatement(type);
            updateCount = new AbstractModifyStatementExecuter<Integer>(dbHelper, listener) {
                @Override
                public Integer doInTransaction(SQLiteDatabase db) {
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
     * 一括更新する
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T extends Entity> int bulkUpdate(List<T> entities) {
        return bulkUpdate(entities, null);
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
                public Integer doInTransaction(SQLiteDatabase sqLiteDatabase) {
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

    public Cursor selectCursorBySql(String s, Object[] selectionArgs) {
        return selectCursorBySql(s, null, selectionArgs);
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
            protected Cursor doInTransaction(SQLiteDatabase db) {
                String[] args = QueryUtils.toStirngArrayArgs(selectionArgs);
                Cursor cursor = db.rawQuery(sql, args);
                return cursor;
            }
        }.execute();
        return cursor;
    }

    public Integer executeBySqlFile(String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
            return null;
    }
}
