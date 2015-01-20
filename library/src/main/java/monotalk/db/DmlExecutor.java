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
package monotalk.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.Arrays;

import monotalk.db.DBLog.LogLevel;
import monotalk.db.compat.DatabaseCompat;
import monotalk.db.query.QueryUtils;

public class DmlExecutor {

    private final static String TAG_NAME = DBLog.getTag(DmlExecutor.class);

    private SQLiteDatabase db = null;

    /**
     * コンストラクター
     *
     * @param db SQLiteDatabase
     */
    public DmlExecutor(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * データを削除する
     *
     * @param tableName
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int executeDelete(String tableName, String whereClause, Object[] whereArgs) {
        String[] stringArrayArgs = QueryUtils.toStirngArrayArgs(whereArgs);
        return executeDeleteByWhereArrayStringArgs(tableName, whereClause, stringArrayArgs);
    }

    /**
     * データを削除する
     *
     * @param tableName
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public <T extends Entity> int executeDeleteByWhereArrayStringArgs(String tableName, String whereClause, String[] whereArgs) {
        int updateCount = -1;
        try {
            if (DBLog.isLoggable(LogLevel.DEBUG)) {
                notifyStartParameterDebug();
                DBLog.d(TAG_NAME, "tableName=" + tableName);
                DBLog.d(TAG_NAME, "whereClause=" + whereClause);
                DBLog.d(TAG_NAME, "whereArgs=" + Arrays.toString(whereArgs));
                notifyEndParameterDebug();
            }
            updateCount = db.delete(tableName, whereClause, whereArgs);
        } catch (RuntimeException e) {
            ModifyStatementErrorMsgBuilder builder = new ModifyStatementErrorMsgBuilder();
            builder.setTableName(tableName).setSelection(whereClause).setSelectionArgs(whereArgs);
            // エラーログ出力
            DBLog.e(TAG_NAME, "Raise RuntimeException" + builder.buildMsg());
            throw e;
        }
        return updateCount;
    }

    /**
     * データを登録する
     *
     * @param tableName
     * @param data
     * @return
     */
    public long executeInsert(String tableName, ContentValues data) {
        long id;
        try {
            if (DBLog.isLoggable(LogLevel.DEBUG)) {
                notifyStartParameterDebug();
                DBLog.d(TAG_NAME, "tableName=" + tableName);
                DBLog.d(TAG_NAME, "data=" + data.toString());
                notifyEndParameterDebug();
            }
            id = (int) db.insertOrThrow(tableName, null, data);
        } catch (RuntimeException e) {
            ModifyStatementErrorMsgBuilder builder = new ModifyStatementErrorMsgBuilder();
            builder.setTableName(tableName).setValues(data);
            DBLog.e(TAG_NAME, "Raise SQLiteException " + builder.buildMsg());
            throw e;
        }
        return id;
    }

    /**
     * Cursorを返す
     *
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
    public Cursor executeSelectCursorByWhereArrayStringArgs(boolean distinct, final String tableName, final String[] projection,
                                                            final String selection, final String groupBy, final String having, final String sortOrder,
                                                            final String limit, final String[] selectionArgs) {
        Cursor cursor;
        try {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setDistinct(distinct);
            builder.setTables(tableName);
            String sql = DatabaseCompat.buildQuery(builder, projection, selection, groupBy, having, sortOrder, limit);
            if (DBLog.isLoggable(LogLevel.DEBUG)) {
                notifyStartParameterDebug();
                DBLog.d(TAG_NAME, "sql=" + sql);
                DBLog.d(TAG_NAME, "selectionArgs=" + Arrays.toString(selectionArgs));
                notifyEndParameterDebug();
            }
            cursor = db.rawQuery(sql, selectionArgs);

            if (DBLog.isLoggable(LogLevel.DEBUG)) {
                DBLog.d(TAG_NAME, cursor);
            }

        } catch (RuntimeException e) {
            SelectStatementErrorMsgBuilder builder = new SelectStatementErrorMsgBuilder();
            builder
                    .setTableName(tableName)
                    .setProjection(projection)
                    .setSelection(selection)
                    .setGroupBy(groupBy)
                    .setHaving(having)
                    .setSortOrder(sortOrder)
                    .setLimit(limit)
                    .setSelectionArgs(selectionArgs);
            DBLog.e(TAG_NAME, "Raise SQLiteException " + builder.buildMsg());
            throw e;
        }
        return cursor;
    }

    /**
     * Cursorを返す
     *
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
    public Cursor executeSelectCursor(boolean distinct, final String tableName, final String[] projection,
                                      final String selection, final String groupBy, final String having, final String sortOrder,
                                      final String limit, final Object[] selectionArgs) {
        String[] stringArrayArgs = QueryUtils.toStirngArrayArgs(selectionArgs);
        return executeSelectCursorByWhereArrayStringArgs(distinct, tableName, projection, selection, groupBy, having, sortOrder, limit, stringArrayArgs);
    }

    private void notifyStartParameterDebug() {
        DBLog.d(TAG_NAME, "DEBUG method Parameters >>>>>>>>>>>>>>>>>>>>>>>>");
    }

    /**
     * SQLを元にクエリを実行する
     *
     * @param sql
     * @param selectionArgs
     * @return
     */
    public Cursor executeSelectCursor(String sql, Object[] selectionArgs) {
        Cursor cursor;
        String[] stringArrayArgs = QueryUtils.toStirngArrayArgs(selectionArgs);
        try {
            if (DBLog.isLoggable(LogLevel.DEBUG)) {
                notifyStartParameterDebug();
                DBLog.d(TAG_NAME, "sql=" + sql);
                DBLog.d(TAG_NAME, "selectionArgs=" + Arrays.toString(stringArrayArgs));
                notifyEndParameterDebug();
            }
            cursor = db.rawQuery(sql, stringArrayArgs);

            if (DBLog.isLoggable(LogLevel.DEBUG)) {
                DBLog.d(TAG_NAME, cursor);
            }

        } catch (RuntimeException e) {
            SelectStatementErrorMsgBuilder builder = new SelectStatementErrorMsgBuilder();
            builder.setSql(sql).setSelectionArgs(stringArrayArgs);
            DBLog.e(TAG_NAME, "Raise SQLiteException " + builder.buildMsg());
            throw e;
        }
        return cursor;
    }

    /**
     * データを更新する
     *
     * @param tableName
     * @param data
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public <T extends Entity> int executeUpdateByWhereArrayStringArgs(String tableName, ContentValues data, String whereClause,
                                                                      String[] whereArgs) {
        // Tableアノテーションを取得
        int updateCount = -1;
        try {
            if (DBLog.isLoggable(LogLevel.DEBUG)) {
                notifyStartParameterDebug();
                DBLog.d(TAG_NAME, "tableName=" + tableName);
                DBLog.d(TAG_NAME, "data=" + data);
                DBLog.d(TAG_NAME, "whereClause=" + whereClause);
                DBLog.d(TAG_NAME, "whereArgs=" + Arrays.toString(whereArgs));
                notifyEndParameterDebug();
            }
            updateCount = db.update(tableName, data, whereClause, whereArgs);
        } catch (RuntimeException e) {
            ModifyStatementErrorMsgBuilder builder = new ModifyStatementErrorMsgBuilder();
            builder.setTableName(tableName).setValues(data).setSelection(whereClause).setSelectionArgs(whereArgs);
            DBLog.e(TAG_NAME, "Raise RuntimeException " + builder.buildMsg());
            throw e;
        }
        return updateCount;
    }

    /**
     * データを更新する
     *
     * @param tableName
     * @param data
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public <T extends Entity> int executeUpdate(String tableName, ContentValues data, String whereClause,
                                                Object[] whereArgs) {
        String[] stringArrayArgs = QueryUtils.toStirngArrayArgs(whereArgs);
        return executeUpdateByWhereArrayStringArgs(tableName, data, whereClause, stringArrayArgs);
    }

    private void notifyEndParameterDebug() {
        DBLog.d(TAG_NAME, "DEBUG method Parameters <<<<<<<<<<<<<<<<<<<<<<<<");
    }

    // ========================================================================
    // Builder Class
    // ========================================================================
    private static class ModifyStatementErrorMsgBuilder {

        private String selection = null;
        private String[] selectionArgs = null;
        private String sql = null;
        private String tableName = null;
        private ContentValues values = null;

        public ModifyStatementErrorMsgBuilder setSelection(String selection) {
            this.selection = selection;
            return this;
        }

        public ModifyStatementErrorMsgBuilder setSelectionArgs(String[] selectionArgs) {
            this.selectionArgs = selectionArgs;
            return this;
        }

        public ModifyStatementErrorMsgBuilder setSql(String sql) {
            this.sql = sql;
            return this;
        }

        public ModifyStatementErrorMsgBuilder setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public ModifyStatementErrorMsgBuilder setValues(ContentValues values) {
            this.values = values;
            return this;
        }

        public String buildMsg() {
            StringBuilder builder = new StringBuilder();
            builder.append("ModifyStatementErrorMsgBuilder [");
            if (selection != null) {
                builder.append("selection=").append(selection).append(", ");
            }
            if (selectionArgs != null) {
                builder.append("selectionArgs=").append(Arrays.toString(selectionArgs)).append(", ");
            }
            if (sql != null) {
                builder.append("sql=").append(sql).append(", ");
            }
            if (tableName != null) {
                builder.append("tableName=").append(tableName).append(", ");
            }
            if (values != null) {
                builder.append("values=").append(values);
            }
            builder.append("]");
            return builder.toString();
        }
    }

    // ========================================================================
    // Builder Class
    // ========================================================================
    private static class SelectStatementErrorMsgBuilder {
        private String groupBy = null;
        private String having = null;
        private String limit = null;
        private String[] projection = null;
        private String selection = null;
        private String[] selectionArgs = null;
        private String sortOrder = null;
        private String sql = null;
        private String tableName = null;

        public String buildMsg() {
            StringBuilder builder = new StringBuilder();
            builder.append("SelectStatementErrorMsgBuilder [");
            if (groupBy != null) {
                builder.append("groupBy=").append(groupBy).append(", ");
            }
            if (having != null) {
                builder.append("having=").append(having).append(", ");
            }
            if (limit != null) {
                builder.append("limit=").append(limit).append(", ");
            }
            if (projection != null) {
                builder.append("projection=").append(Arrays.toString(projection)).append(", ");
            }
            if (selection != null) {
                builder.append("selection=").append(selection).append(", ");
            }
            if (selectionArgs != null) {
                builder.append("selectionArgs=").append(Arrays.toString(selectionArgs)).append(", ");
            }
            if (sortOrder != null) {
                builder.append("sortOrder=").append(sortOrder).append(", ");
            }
            if (sql != null) {
                builder.append("sql=").append(sql).append(", ");
            }
            if (tableName != null) {
                builder.append("tableName=").append(tableName);
            }
            builder.append("]");
            return builder.toString();
        }

        public SelectStatementErrorMsgBuilder setGroupBy(String groupBy) {
            this.groupBy = groupBy;
            return this;
        }

        public SelectStatementErrorMsgBuilder setHaving(String having) {
            this.having = having;
            return this;
        }

        public SelectStatementErrorMsgBuilder setLimit(String limit) {
            this.limit = limit;
            return this;
        }

        public SelectStatementErrorMsgBuilder setProjection(String[] projection) {
            this.projection = projection;
            return this;
        }

        public SelectStatementErrorMsgBuilder setSelection(String selection) {
            this.selection = selection;
            return this;
        }

        public SelectStatementErrorMsgBuilder setSelectionArgs(String[] selectionArgs) {
            this.selectionArgs = selectionArgs;
            return this;
        }

        public SelectStatementErrorMsgBuilder setSortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public SelectStatementErrorMsgBuilder setSql(String sql) {
            this.sql = sql;
            return this;
        }

        public SelectStatementErrorMsgBuilder setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
    }
}
