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
package monotalk.db;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import monotalk.db.compat.DatabaseCompat;
import monotalk.db.query.QueryUtils;

/**
 * Created by Kem on 2015/01/18.
 */
public class TableStatement {
    Class<? extends Entity> type;
    ThreadLocal<TableInsertStatement> insertAllValuesStatement = new ThreadLocal<TableInsertStatement>();
    ThreadLocal<TableUpdateStatement> updateAllValuesByIdStatement = new ThreadLocal<TableUpdateStatement>();
    ThreadLocal<TableDeleteStatement> deleteByIdStatement = new ThreadLocal<TableDeleteStatement>();
    ThreadLocal<TableSelectStatement> tableSelectStatement = new ThreadLocal<TableSelectStatement>();

    public TableStatement(Class<? extends Entity> clazz) {
        type = clazz;
    }

    abstract class TableBaseStetement {
        protected SQLiteStatement statement;
        protected int index = 1;

        protected void clearBindings() {
            statement.clearBindings();
            index = 1;
        }

        protected void bindAllArgs(Entity entity) {
            for (FieldInfo fieldinfo : MonoTalk.getTableInfo(type).getFieldInfos()) {
                Object value = null;
                try {
                    value = fieldinfo.getField().get(entity);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
                DatabaseUtils.bindObjectToProgram(statement, index, value);
                index++;
            }
        }

        protected void bindId(long id) {
            statement.bindLong(index, id);
            index++;
        }

        protected void bindWhereArgs(String args[]) {
            for (String arg : args) {
                statement.bindString(index, arg);
                index++;
            }
        }

        protected void bindWhereIdArgs(Entity entity) {
            statement.bindLong(index, entity.id);
            index++;
        }

        protected void bindAllArgs(ContentValues values) {
            for (String columnName : MonoTalk.getTableInfo(type).getColumnNames()) {
                Object value = values.get(columnName);
                DatabaseUtils.bindObjectToProgram(statement, index, value);
                index++;
            }
        }
    }

    class TableInsertStatement extends TableBaseStetement {

        public TableInsertStatement(SQLiteDatabase db) {
            String insertSql = QueryUtils.toInsertSql(type);
            statement = db.compileStatement(insertSql);
        }

        public long executeInsert(ContentValues values) {
            clearBindings();
            bindAllArgs(values);
            try {
                return statement.executeInsert();
            } catch (RuntimeException e) {
                Log.e(DBLog.getTag(TableUpdateStatement.class), "insert error statement=" + statement.toString(), e);
                throw e;
            }
        }

        public long executeInsert(Entity entity) {
            clearBindings();
            bindAllArgs(entity);
            try {
                return statement.executeInsert();
            } catch (RuntimeException e) {
                Log.e(DBLog.getTag(TableUpdateStatement.class), "insert error statement=" + statement.toString(), e);
                throw e;
            }
        }
    }

    class TableUpdateStatement extends TableBaseStetement {

        private SQLiteDatabase db;

        public TableUpdateStatement(SQLiteDatabase db) {
            String updateSql = QueryUtils.toUpdateSql(type);
            statement = db.compileStatement(updateSql);
            this.db = db;
        }

        public int executeUpdate(ContentValues values, long id) {
            int updateCount;
            try {
                clearBindings();
                bindAllArgs(values);
                bindId(id);
                updateCount = DatabaseCompat.executeUpdateDelete(db, statement);
            } catch (RuntimeException e) {
                Log.e(DBLog.getTag(TableUpdateStatement.class), "update error statement=" + statement.toString(), e);
                throw e;
            }
            return updateCount;
        }

        public int executeUpdate(Entity entity) {
            int updateCount;
            try {
                clearBindings();
                bindAllArgs(entity);
                bindWhereIdArgs(entity);
                updateCount = DatabaseCompat.executeUpdateDelete(db, statement);
            } catch (RuntimeException e) {
                Log.e(DBLog.getTag(TableUpdateStatement.class), "update error statement=" + statement.toString(), e);
                throw e;
            }
            return updateCount;
        }
    }

    class TableDeleteStatement extends TableBaseStetement {

        private SQLiteDatabase db;

        public TableDeleteStatement(SQLiteDatabase db) {
            String buildSql = QueryUtils.toDeleteSql(type);
            statement = db.compileStatement(buildSql);
            this.db = db;
        }

        public int executeDelete(long id) {
            int updateCount;
            try {
                clearBindings();
                bindId(id);
                updateCount = DatabaseCompat.executeUpdateDelete(db, statement);
            } catch (RuntimeException e) {
                Log.e(DBLog.getTag(TableUpdateStatement.class), "delete error statement=" + statement.toString(), e);
                throw e;
            }
            return updateCount;
        }

        public int executeDelete(Entity entity) {
            int updateCount;
            try {
                clearBindings();
                bindWhereIdArgs(entity);
                updateCount = DatabaseCompat.executeUpdateDelete(db, statement);
            } catch (RuntimeException e) {
                Log.e(DBLog.getTag(TableUpdateStatement.class), "delete error statement=" + statement.toString(), e);
                throw e;
            }
            return updateCount;
        }
    }

    class TableSelectStatement {
        private SQLiteDatabase db;
        private String buildSql;

        public TableSelectStatement(SQLiteDatabase db) {
            buildSql = QueryUtils.toSelectSql(type);
            this.db = db;
        }

        public android.database.Cursor rawQery(Entity entity) {
            long id = entity.id;
            return db.rawQuery(buildSql, new String[]{String.valueOf(id)});
        }

        public android.database.Cursor rawQery(long id) {
            return db.rawQuery(buildSql, new String[]{String.valueOf(id)});
        }
    }

    public TableInsertStatement getInsertStatement(SQLiteDatabase db) {
        TableInsertStatement statement = insertAllValuesStatement.get();
        if (statement != null) {
            return statement;
        }
        statement = new TableInsertStatement(db);
        insertAllValuesStatement.set(statement);
        return statement;
    }

    public TableUpdateStatement getUpdateStatement(SQLiteDatabase db) {
        TableUpdateStatement statement = updateAllValuesByIdStatement.get();
        if (statement != null) {
            return statement;
        }
        statement = new TableUpdateStatement(db);
        updateAllValuesByIdStatement.set(statement);
        return statement;

    }

    public TableDeleteStatement getDeleteStatement(SQLiteDatabase db) {
        TableDeleteStatement statement = deleteByIdStatement.get();
        if (statement != null) {
            return statement;
        }

        statement = new TableDeleteStatement(db);
        deleteByIdStatement.set(statement);
        return statement;
    }

    public TableSelectStatement getSelectStatement(SQLiteDatabase db) {
        TableSelectStatement statement = tableSelectStatement.get();
        if (statement != null) {
            return statement;
        }
        statement = new TableSelectStatement(db);
        tableSelectStatement.set(statement);
        return statement;
    }
}

