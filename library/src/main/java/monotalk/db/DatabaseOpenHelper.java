/*******************************************************************************
 * Copyright (C) 2013 Kem
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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;
import android.util.SparseArray;

import java.util.List;

import monotalk.db.DBLog.LogLevel;

/**
 * DatabaseOpenHelper.java
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    /**
     * ログに使用するタグ名
     */
    private final static String TAG_NAME = DBLog.getTag(DatabaseOpenHelper.class);
    private List<Class<? extends Entity>> entityClasses;
    private SparseArray<Migration> migrations;

    public DatabaseOpenHelper(Context context, String name, int version, List<Class<? extends Entity>> tableClasses, SparseArray<Migration> migrations) {
        super(context, name, DBLog.isLoggable(LogLevel.VERBOSE) ? new LoggingCursorAdapter() : null, version);
        this.entityClasses = tableClasses;
        this.migrations = migrations;
    }

    /**
     * <p>
     * DB作成時に1度だけCallされるメソッド
     * </p>
     *
     * @param db SQLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Log出力
        DBLog.i(TAG_NAME, "onCreate() start databaseName = {%1$s}", db.getPath());
        executePragmasForeignKeysOn(db);
        if (entityClasses != null) {
            for (Class<? extends Entity> clazz : entityClasses) {
                DdlExecutor executor = new DdlExecutor(db);
                executor.executeCreateTableOrCreateView(clazz);
                executor.executeCreateIndex(clazz);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Log出力
        DBLog.i(TAG_NAME, "onUpgrade() start oldVersion = {%1$s} newVersion = {%2$s}", new Object[]{oldVersion, newVersion});
        executePragmasForeignKeysOn(db);
        if (migrations != null) {
            for (int i = oldVersion; i < newVersion; i++) {
                Migration migration = migrations.get(i);
                if (migration != null) {
                    migration.upgradeMigrate(db);
                }
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Log出力
        DBLog.i(TAG_NAME, "onDowngrade() start oldVersion = {%1$s} newVersion = {%2$s}", new Object[]{oldVersion, newVersion});
        executePragmasForeignKeysOn(db);
        if (migrations != null) {
            for (int i = oldVersion - 1; i >= newVersion; i--) {
                Migration migration = migrations.get(i);
                if (migration != null) {
                    migration.downgradeMigrate(db);
                }
            }
        }

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        executePragmasForeignKeysOn(db);
    }

    private void executePragmasForeignKeysOn(SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            DdlExecutor executor = new DdlExecutor(db);
            executor.executePragmasForeignKeysOn(db);
        }
    }

    protected static final class LoggingCursorAdapter implements CursorFactory {
        @SuppressWarnings("deprecation")
        @Override
        public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            Log.v(TAG_NAME, query.toString());
            return new SQLiteCursor(db, driver, editTable, query);
        }
    }
}