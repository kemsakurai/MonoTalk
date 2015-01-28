package monotalk.db.query;

import android.content.ContentValues;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.querydata.UpdateQueryData;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;

import static monotalk.db.query.QueryUtils.excludesNullFrom;
import static monotalk.db.query.QueryUtils.from;

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

public final class Update<T extends Entity> {
    private Class<T> type;
    private String alias;
    private QueryCrudHandler crudProcessor;

    public Update(Class<T> type, QueryCrudHandler crudProcessor) {
        this.type = type;
        this.crudProcessor = crudProcessor;
    }

    public Set value(String key, Object value) {
        return new Set(key, value, type);
    }

    public Set valuesExcludesNull(T object) {
        return new Set(type, object, true);
    }

    public Set values(T object) {
        return new Set(type, object, false);
    }

    public Set values(T object, String... inculdeColumns) {
        return new Set(type, object, inculdeColumns);
    }

    public Set values(ContentValues values) {
        return new Set(values, type);
    }

    public Update<T> as(String alias) {
        this.alias = alias;
        return this;
    }

    public class Set extends AbstractFrom<T, Set> implements Executable<Integer> {

        private ContentValues mValues;

        Set(Class<T> type, T object, boolean isExuludedNull) {
            super(type);
            if (isExuludedNull) {
                mValues = excludesNullFrom(object);
            } else {
                mValues = from(object);
            }
            selection = new Selection();
        }

        Set(Class<T> type, T object, String... includeColums) {
            super(type);
            mValues = from(object, includeColums);
            selection = new Selection();
        }

        Set(ContentValues values, Class<T> type) {
            super(type);
            mValues = values;
        }

        Set(String key, Object value, Class<T> type) {
            super(type);
            mValues = new ContentValues();
            if (value == null) {
                mValues.putNull(key);
                return;
            } else {
                Class<?> fieldType = value.getClass();
                @SuppressWarnings("rawtypes")
                final TypeConverter typeConverter = TypeConverterCache.getTypeConverterOrThrow(fieldType);
                // serialize data
                typeConverter.pack(value, mValues, key);
            }
            selection = new Selection();
        }

        public Set value(String key, String value) {
            if (value == null) {
                mValues.putNull(key);
            } else {
                Class<?> fieldType = value.getClass();
                @SuppressWarnings("rawtypes")
                final TypeConverter typeConverter = TypeConverterCache.getTypeConverterOrThrow(fieldType);
                // serialize data
                typeConverter.pack(value, mValues, key);
            }
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
    }
}
