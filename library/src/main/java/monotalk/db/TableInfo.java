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

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.Table;
import monotalk.db.annotation.View;
import monotalk.db.typeconverter.EntityConverter;
import monotalk.db.typeconverter.EnumConverter;
import monotalk.db.typeconverter.TypeConverterCache;
import monotalk.db.utility.ReflectUtils;

import static monotalk.db.utility.StringUtils.ln;

public class TableInfo {
    //////////////////////////////////////////////////////////////////////////////////////
    // Field
    //////////////////////////////////////////////////////////////////////////////////////
    private FieldInfo idFieldInfo;
    private List<FieldInfo> fieldInfos;
    private String tableName;
    private String[] columnNames;
    private Class<? extends Entity> type;
//    private Map<Class<? extends Entity>, String> joinKeyColumns;

    // ////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////////////////////////////////////
    public TableInfo(Class<? extends Entity> type) {
        // --------
        // Add TypeConverter
        // ------------------------
        // ## Add TypeConverter for Entity.class
        TypeConverterCache.registerTypeConverter(type, new EntityConverter(type));

        this.type = type;
        this.tableName = getTableNameFromTableAnnotation(type);
        this.fieldInfos = new ArrayList<FieldInfo>();
//        this.joinKeyColumns = new HashMap<Class<? extends Entity>, String>();

        List<String> columnNameList = new ArrayList<String>();

        // Manually add the id column since it is not declared like the other
        // columns.
        Field idField = ReflectUtils.getIdField();
        if (idField != null) {
            Column column = idField.getAnnotation(Column.class);
            String columnName = column.name();
            Class<?> filedType = idField.getType();
            this.idFieldInfo = newFieldInfo(idField, columnName, filedType);
            this.fieldInfos.add(idFieldInfo);
            columnNameList.add(columnName);
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
                    Class<?> filedType = field.getType();
                    // --------
                    // Add TypeConverter
                    // ------------------------
                    // ## for Enum.class
                    if (ReflectUtils.isSubclassOf(filedType, Enum.class)) {
                        TypeConverterCache.registerTypeConverter(filedType, new EnumConverter(filedType));
                    } else if (ReflectUtils.isEntity(filedType)) {
                        // ## for Entity.class
                        TypeConverterCache.registerTypeConverter(filedType, new EntityConverter(type));
                    }
                    FieldInfo fieldInfo = newFieldInfo(field, columnName, filedType);
                    this.fieldInfos.add(fieldInfo);
                    columnNameList.add(columnName);
                }
//                if (field.isAnnotationPresent(ForeignKey.class)) {
//                    final ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
//                    Class<? extends Entity> entityClass = foreignKey.entityClass();
//                    this.joinKeyColumns.put(entityClass, columnAnnotation.name());
//                }
            }
        }
        columnNames = columnNameList.toArray(new String[0]);
        // ---------
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

    private FieldInfo newFieldInfo(Field field, String columnName, Class<?> filedType) {
        return new FieldInfo(field, columnName, TypeConverterCache.getTypeConverterOrThrow(filedType), ReflectUtils.isEntity(filedType));
    }

    @Override
    public String toString() {
        return "TableInfo{" + ln() +
                "idFieldInfo=" + idFieldInfo + ln() +
                ", fieldInfos=" + fieldInfos + ln() +
                ", tableName='" + tableName + ln() +
                ", columnNames=" + Arrays.toString(columnNames) + ln() +
                ", type=" + type + ln() +
                '}';
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public Field getIdField() {
        return idFieldInfo.getField();
    }

    public String getIdName() {
        return idFieldInfo.getColumnName();
    }

    public Class<? extends Entity> getType() {
        return type;
    }

    public String getTableName() {
        return tableName;
    }

    public List<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }
}
