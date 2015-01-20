/*******************************************************************************
 * Copyright (C) 2012-2013 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package monotalk.db;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.Table;
import monotalk.db.annotation.View;
import monotalk.db.utility.ReflectUtils;

import static monotalk.db.utility.StringUtils.ln;

public class TableInfo {
    //////////////////////////////////////////////////////////////////////////////////////
    // Field
    //////////////////////////////////////////////////////////////////////////////////////
    private Map<Field, String> columnNamesMap = new LinkedHashMap<Field, String>();
    private Pair<Field, String> idInfo;
    private String tableName;
    private String[] columnNames;
    private Field[] fields;
    private Class<? extends Entity> type;

    // ////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////////////////////////////////////
    public TableInfo(Class<? extends Entity> type) {
        this.type = type;
        this.tableName = getTableNameFromTableAnnotation(type);
        // Manually add the id column since it is not declared like the other
        // columns.
        Field idField = ReflectUtils.getIdField();
        if (idField != null) {
            Column column = idField.getAnnotation(Column.class);
            String columnName = column.name();
            this.idInfo = Pair.create(idField, columnName);
            this.columnNamesMap.put(idField, columnName);
        }

        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                final Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation != null) {
                    String columnName = columnAnnotation.name();
                    if (TextUtils.isEmpty(columnName)) {
                        columnName = field.getName();
                    }
                    this.columnNamesMap.put(field, columnName);
                }
            }
        }
        this.fields = columnNamesMap.keySet().toArray(new Field[0]);
        columnNames = columnNamesMap.values().toArray(new String[0]);
        // -----------------------------------------------------------------------------
        // Debug
        // -----------------------------------------------------------------------------
        DBLog.d(DBLog.getTag(this.getClass()), "TableInfo Construct end...>>>>" + this);
    }

    private String getTableNameFromTableAnnotation(Class<? extends Entity> type) {
        String tableName;
        final Table tableAnnotation = type.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            tableName = tableAnnotation.name();
            return tableName;
        }
        final View viewAnnotation = type.getAnnotation(View.class);
        if (viewAnnotation != null) {
            tableName = viewAnnotation.name();
            return tableName;
        }
        tableName = type.getSimpleName();
        return tableName;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public String getColumnName(Field key) {
        return columnNamesMap.get(key);
    }

    public Field[] getFields() {
        return fields;
    }

    public Map<Field, String> getColumnNamesMap() {
        return columnNamesMap;
    }

    public Field getIdField() {
        return idInfo.first;
    }

    public String getIdName() {
        return idInfo.second;
    }

    public Class<? extends Entity> getType() {
        return type;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TableInfo [").append(ln());
        if (columnNamesMap != null) {
            builder.append("columnNamesMap=").append(columnNamesMap).append(", ").append(ln());
        }
        if (this.idInfo != null) {
            if (idInfo.first != null) {
                builder.append("idField=").append(idInfo.first).append(", ").append(ln());
            }
            if (idInfo.second != null) {
                builder.append("idName=").append(idInfo.second).append(", ").append(ln());
            }
        }
        if (tableName != null) {
            builder.append("tableName=").append(tableName).append(", ").append(ln());
        }
        if (type != null) {
            builder.append("type=").append(type).append(", ").append(ln());
        }
        builder.append("]").append(ln());
        return builder.toString();
    }
}
