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

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Field;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.ConflictClause;
import monotalk.db.annotation.ForeignKey;
import monotalk.db.annotation.Id;
import monotalk.db.annotation.Index;
import monotalk.db.annotation.Indexies;
import monotalk.db.annotation.NotNull;
import monotalk.db.annotation.Table;
import monotalk.db.annotation.Unique;
import monotalk.db.annotation.Uniques;
import monotalk.db.annotation.View;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;
import monotalk.db.utility.ReflectUtils;
import monotalk.db.utility.ResourceUtis;

import static monotalk.db.utility.AssertUtils.assertNotNull;

/**
 * DdlStatement.java
 * DDLステートメントを実行する
 *
 * @author Kem
 */
public class DdlExecutor {

    private static final String ON = "ON";
    private static final String CREATE_INDEX_IF_NOT_EXISTS = "CREATE INDEX IF NOT EXISTS";
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS";
    private static final String CREATE_VIEW_IF_NOT_EXISTS = "CREATE VIEW IF NOT EXISTS";
    private static final String PRIMARY_KEY = "PRIMARY KEY";
    private static final String AUTOINCREMENT = "AUTOINCREMENT";
    private static final String NOT_NULL = "NOT NULL";
    private static final String UNIQUE = "UNIQUE";
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS";
    private static final String DROP_VIEW_IF_EXISTS = "DROP VIEW IF EXISTS";
    private static final String DROP_INDEX = "DROP INDEX";
    private static final String DEFAULT = "DEFAULT";
    private static final String ON_CONFLICT = "ON CONFLICT";

    /**
     * ログに使用するタグ名
     */
    private final static String TAG_NAME = DBLog.getTag(DdlExecutor.class);

    private SQLiteDatabase db = null;

    /**
     * コンストラクター
     *
     * @param db
     */
    public DdlExecutor(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * "PRAGMA foreign_keys=ON;"
     *
     * @param db
     */
    public void executePragmasForeignKeysOn(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * SQL:Create文を実行する
     *
     * @param clazz
     */
    public void executeCreateTableOrCreateView(Class<? extends Entity> clazz) {
        assertNotNull(clazz, "clazz is null");

        if (clazz.isAnnotationPresent(Table.class)) {
            String sql = buildCreateTableSql(clazz);
            DBLog.d(TAG_NAME, sql);
            db.execSQL(sql);
        } else if (clazz.isAnnotationPresent(View.class)) {
            String sql = buildCreateViewSql(clazz);
            DBLog.d(TAG_NAME, sql);
            db.execSQL(sql);
        } else {
            DBLog.i(TAG_NAME, "Create entity skipped.. Table Annotation Not Present className [%1$s].", clazz.getName());
        }
    }

    private String buildCreateViewSql(Class<? extends Entity> clazz) {
        View view = clazz.getAnnotation(View.class);
        String sqlFileName = view.sqlFileName();
        String sql = ResourceUtis.getSQL(MonoTalk.getContext(), clazz, sqlFileName);
        StringBuilder sb = new StringBuilder();
        sb.append(CREATE_VIEW_IF_NOT_EXISTS);
        sb.append(" ");
        sb.append(view.name());
        sb.append(" ");
        sb.append("AS");
        sb.append(" ");
        sb.append(sql);
        sb.append(";");
        return sb.toString();
    }

    // ## Make Create Table
    private String buildCreateTableSql(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        // SQL文を生成する
        StringBuilder columnDef = new StringBuilder();
        columnDef.append(CREATE_TABLE_IF_NOT_EXISTS);
        columnDef.append(" ");
        columnDef.append(table.name());
        columnDef.append(" (");
        Field idField = ReflectUtils.getIdField();
        appendColumSqlParts(columnDef, idField);
        for (Field field : clazz.getDeclaredFields()) {
            appendColumSqlParts(columnDef, field);
        }

        // ## Unique constraint >>>UNIQUE(Column1, Column2, ...)
        Uniques uniques = clazz.getAnnotation(Uniques.class);
        if (uniques != null) {
            for (Unique unique : uniques.values()) {
                appendUniqueSqlParts(columnDef, unique);
            }
        }

        Unique unique = clazz.getAnnotation(Unique.class);
        if (unique != null) {
            appendUniqueSqlParts(columnDef, unique);
        }

        // ## Foreign Key constraint >>>FOREIGN KEY(Column1) REFERENCES PARENT(id)
        for (Field field : clazz.getDeclaredFields()) {
            appendForeignKeySqlParts(columnDef, field);
        }

        columnDef.deleteCharAt(columnDef.length() - 1);
        columnDef.append(" );");
        String sql = columnDef.toString();
        return sql;
    }

    private void appendForeignKeySqlParts(StringBuilder columnDef, Field field) {
        ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
        Column column = field.getAnnotation(Column.class);

        if (foreignKey != null && column != null) {
            String referrenceTableName;
            if (!ReflectUtils.isEntity(field.getType())) {
                referrenceTableName = foreignKey.entityClass().getAnnotation(Table.class).name();
            } else {
                referrenceTableName = field.getType().getAnnotation(Table.class).name();
            }

            String columnName = column.name();
            columnDef.append("FOREIGN KEY(").append(columnName).append(") REFERENCES ");
            columnDef.append(referrenceTableName);
            columnDef.append("(");
            Field idField = ReflectUtils.getIdField();
            if (idField != null) {
                Column idColumn = idField.getAnnotation(Column.class);
                String idColumnName = idColumn.name();
                columnDef.append(idColumnName);
            }
            columnDef.append(")");
            if (!foreignKey.onDelete().equals(ForeignKey.ReferentialAction.NONE)) {
                columnDef.append(" ON DELETE ").append(foreignKey.onDelete().keyword());
            }
            if (!foreignKey.onUpdate().equals(ForeignKey.ReferentialAction.NONE)) {
                columnDef.append(" ON UPDATE ").append(foreignKey.onUpdate().keyword());
            }
            if (!foreignKey.deferrable().equals(ForeignKey.Deferrable.NONE)) {
                columnDef.append(" ").append(foreignKey.deferrable().keyword());

                if (!foreignKey.deferrableTiming().equals(ForeignKey.DeferrableTiming.NONE)) {
                    columnDef.append(" ").append(foreignKey.deferrableTiming().keyword());
                }
            }
            columnDef.append(",");

        }
    }

    private void appendColumSqlParts(StringBuilder columnDef, Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            Class<?> type = field.getType();
            @SuppressWarnings("rawtypes")
            TypeConverter typeConverter = TypeConverterCache.getTypeConverter(type);
            String columnName = column.name();
            columnDef.append(columnName);
            columnDef.append(" ");
            if (typeConverter != null) {
                columnDef.append(typeConverter.getSqlType());
            } else {
                if (ReflectUtils.isEntity(type)) {
                    columnDef.append(TypeConverter.SQLiteType.INTEGER);
                }
            }
            columnDef.append(" ");

            if (!TextUtils.isEmpty(column.defaultValue())) {
                columnDef.append(DEFAULT);
                columnDef.append(" ");
                if (TypeConverter.SQLiteType.TEXT == typeConverter.getSqlType()) {
                    columnDef.append("'");
                    columnDef.append(column.defaultValue());
                    columnDef.append("'");
                    columnDef.append(" ");
                } else {
                    columnDef.append(column.defaultValue());
                    columnDef.append(" ");
                }
            }
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                columnDef.append(PRIMARY_KEY);
                columnDef.append(" ");
                if (id.isAutoIncrement()) {
                    columnDef.append(AUTOINCREMENT);
                    columnDef.append(" ");
                }
            }
            if (column.unique()) {
                columnDef.append(UNIQUE);
                columnDef.append(" ");
            }
            NotNull notNull = field.getAnnotation(NotNull.class);
            if (notNull != null) {
                columnDef.append(NOT_NULL);
                columnDef.append(" ");
                if (notNull.value() != ConflictClause.NONE) {
                    columnDef.append(" ");
                    columnDef.append(ON_CONFLICT);
                    columnDef.append(" ");
                    columnDef.append(notNull.value().keyword());
                    columnDef.append(" ");
                }
            } else if (!column.nullable()) {
                columnDef.append(NOT_NULL);
                columnDef.append(" ");
            }
            columnDef.append(",");
        }
    }

    private void appendUniqueSqlParts(StringBuilder columnDef, Unique unique) {
        String[] columns = unique.columns();
        columnDef.append(UNIQUE);
        columnDef.append(" ");
        columnDef.append("(" + TextUtils.join(",", columns) + " )");
        columnDef.append(",");
    }

    private String buildIndexSql(Table table, Index index) {
        StringBuilder sb = new StringBuilder();
        sb.append(CREATE_INDEX_IF_NOT_EXISTS);
        sb.append(" ");
        sb.append(index.name());
        sb.append(" ");
        sb.append(ON);
        sb.append(" ");
        sb.append(table.name());
        sb.append(" (" + TextUtils.join(",", index.colums()) + " )");
        String createIndexSql = sb.toString();
        return createIndexSql;
    }

    /**
     * Drop文を実行する
     *
     * @param clazz
     */
    public void executeDropTableOrView(Class<? extends Entity> clazz) {
        // Table名を取得
        if (clazz.isAnnotationPresent(Table.class)) {
            String tableName = MonoTalk.getTableInfo(clazz).getTableName();
            // SQL生成
            String sql = DROP_TABLE_IF_EXISTS + " " + tableName + ";";
            // log出力
            DBLog.d(TAG_NAME, sql);
            // データベース作成
            db.execSQL(sql);
        } else if (clazz.isAnnotationPresent(View.class)) {
            View view = clazz.getAnnotation(View.class);
            // SQL生成
            String sql = DROP_VIEW_IF_EXISTS + " " + view.name() + ";";
            // log出力
            DBLog.d(TAG_NAME, sql);
            // データベース作成
            db.execSQL(sql);
        } else {
            DBLog.i(TAG_NAME, "Drop entity skipped.. Table Annotation Not Present className [%1$s]. ", clazz.getName());
        }
    }

    /**
     * Indexを削除する
     *
     * @param clazz
     */
    public void executeDropIndex(Class<? extends Entity> clazz) {
        Indexies indexies = clazz.getAnnotation(Indexies.class);
        Table table = clazz.getAnnotation(Table.class);
        if (indexies != null && table != null) {
            for (Index index : indexies.values()) {
                String dropIndexSql = DROP_INDEX + " " + index.name();
                db.execSQL(dropIndexSql);
            }
        }
        Index index = clazz.getAnnotation(Index.class);
        if (index != null && table != null) {
            String dropIndexSql = DROP_INDEX + " " + index.name();
            db.execSQL(dropIndexSql);
        }
    }

    /**
     * Indexを作成する
     *
     * @param clazz
     */
    public void executeCreateIndex(Class<? extends Entity> clazz) {
        Indexies indexies = clazz.getAnnotation(Indexies.class);
        Table table = clazz.getAnnotation(Table.class);
        if (indexies != null && table != null) {
            for (Index index : indexies.values()) {
                String createIndexSql = buildIndexSql(table, index);
                db.execSQL(createIndexSql);
            }
        }
        Index index = clazz.getAnnotation(Index.class);
        if (index != null && table != null) {
            String createIndexSql = buildIndexSql(table, index);
            db.execSQL(createIndexSql);
        }
    }
}
