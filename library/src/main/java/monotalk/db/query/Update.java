package monotalk.db.query;

import android.content.ContentValues;

import java.util.Map.Entry;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.querydata.UpdateQueryData;
import monotalk.db.utility.ConvertUtils;

/*
 * Copyright (C) 2010 Michael Pardo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

public final class Update<T extends Entity> implements Sqlable {
    private Class<T> type;
    private String alias;
    private QueryCrudHandler crudProcessor;

    public Update(Class<T> type, QueryCrudHandler crudProcessor) {
        this.type = type;
        this.crudProcessor = crudProcessor;
    }

    public Set value(String key, Object value) {
        return new Set(this, key, value, type);
    }

    public Set valuesExcludesNull(T object) {
        return new Set(this, type, object, true);
    }

    public Set values(T object) {
        return new Set(this, type, object, false);
    }

    public Set values(T object, String... inculdeColumns) {
        return new Set(this, type, object, inculdeColumns);
    }

    public Set values(ContentValues values) {
        return new Set(this, values, type);
    }

    public Update<T> as(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(MonoTalk.getTableName(type));
        sql.append(" ");
        if (alias != null) {
            sql.append(alias);
            sql.append(" ");
        }
        return sql.toString();
    }

    public class Set extends AbstractFrom<T, Set> implements Executable<Integer> {

        private ContentValues mValues;

        Set(Sqlable queryBase, Class<T> type, T object, boolean isExuludedNull) {
            super(type, queryBase);
            if (isExuludedNull) {
                mValues = QueryBuilder.toValuesExcludesNull(object);
            } else {
                mValues = QueryBuilder.toValues(object);
            }
            selection = new Selection();
        }

        Set(Sqlable queryBase, Class<T> type, T object, String... includeColums) {
            super(type, queryBase);
            mValues = QueryBuilder.toValues(object, includeColums);
            selection = new Selection();
        }

        Set(Sqlable queryBase, ContentValues values, Class<T> type) {
            super(type, queryBase);
            mValues = values;
        }

        Set(Sqlable queryBase, String key, Object value, Class<T> type) {
            super(type, queryBase);
            mValues = new ContentValues();
            ConvertUtils.addValue(key, value, mValues);
            selection = new Selection();
        }

        public Set value(String key, String value) {
            ConvertUtils.addValue(key, value, mValues);
            return this;
        }

        @Override
        public Integer execute() {
            UpdateQueryData data = newUpdateQueryData();
            int updateCount = crudProcessor.update(data);
            return updateCount;
        }

        private UpdateQueryData newUpdateQueryData() {
            UpdateQueryData data = new UpdateQueryData();
            String name = MonoTalk.getTableName(mType);
            data.setTableName(name);
            data.setValues(mValues);
            data.setSelectionArgs(selection.getSelectionArgs());
            data.setWhere(selection.getSelection());
            return data;
        }

        @Override
        public String toSql() {
            StringBuilder sql = new StringBuilder();
            sql.append(baseDmlQuery.toSql());
            sql.append("SET ");
            for (Entry<String, Object> entry : mValues.valueSet()) {
                sql.append(entry.getKey() + " = " + entry.getValue());
                sql.append(",");
            }
            sql.delete(sql.length() - 1, sql.length());
            sql.append(" ");
            sql.append(selection.toSql());
            return sql.toString();
        }
    }
}
