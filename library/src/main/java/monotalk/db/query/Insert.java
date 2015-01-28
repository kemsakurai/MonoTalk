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
package monotalk.db.query;

import android.content.ContentValues;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.querydata.InsertQueryData;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;
import monotalk.db.utility.AssertUtils;

import static monotalk.db.query.QueryUtils.from;

public final class Insert<T extends Entity> implements Executable<Long> {
    private Class<T> type;
    private ContentValues values;
    private QueryCrudHandler crudHandler;

    Insert(Class<T> table, QueryCrudHandler crudHandler) {
        this.type = table;
        this.crudHandler = crudHandler;
    }

    @SuppressWarnings("unchecked")
    public Insert<T> value(String key, Object value) {
        AssertUtils.assertNotNull(key, "key is Null");

        if (values == null) {
            values = new ContentValues();
        }
        if (value == null) {
            values.putNull(key);
            return this;
        }
        @SuppressWarnings("rawtypes")
        TypeConverter typeConverter = TypeConverterCache.getTypeConverter(value.getClass());
        if (typeConverter != null) {
            typeConverter.pack(value, values, key);
            return this;
        }
        return this;
    }

    @Override
    public Long execute() {
        InsertQueryData data = createInsertQueryData();
        return crudHandler.insert(data);
    }

    private InsertQueryData createInsertQueryData() {
        InsertQueryData data = new InsertQueryData();
        String tableName = MonoTalk.getTableName(type);
        data.setTableName(tableName);
        data.setValues(values);
        return data;
    }

    public Insert<T> values(ContentValues values) {
        AssertUtils.assertNotNull(values, "values is Null");
        this.values = values;
        return this;
    }

    public Insert<T> values(T object) {
        AssertUtils.assertNotNull(object, "object is Null");
        values = from(object);
        return this;
    }

    public Insert<T> values(T object, String... includeColumns) {
        AssertUtils.assertNotNull(object, "object is Null");
        AssertUtils.assertNotNull(includeColumns, "includesColumns is Null");
        values = from(object, includeColumns);
        return this;
    }
}