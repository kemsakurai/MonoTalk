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
package monotalk.db.rowmapper;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import monotalk.db.Entity;
import monotalk.db.FieldInfo;
import monotalk.db.MonoTalk;
import monotalk.db.exception.IllegalAccessRuntimeException;
import monotalk.db.manager.EntityManager;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.utility.ReflectUtils;

public class EntityRowMapper<T extends Entity> implements RowMapper<T> {

    private Class<T> mType;
    private EntityManager mEntityManager;
    private List<FieldInfo> infos;

    public EntityRowMapper(Class<T> clazz) {
        this.mType = clazz;
        this.infos = MonoTalk.getTableInfo(mType).getFieldInfos();
    }

    public EntityRowMapper(Class<T> clazz, EntityManager entityManager) {
        this.mType = clazz;
        this.infos = MonoTalk.getTableInfo(mType).getFieldInfos();
        this.mEntityManager = entityManager;

    }

    @SuppressWarnings("unchecked")
    public T mapRow(Cursor cursor) {
        T result = ReflectUtils.newInstance(mType);
        List<String> colNames = Arrays.asList(cursor.getColumnNames());
        try {
            for (FieldInfo info : infos) {
                String columnName = info.getColumnName();
                if (!colNames.contains(columnName)) {
                    continue;
                }
                Field field = info.getField();
                int columnIndex = cursor.getColumnIndexOrThrow(columnName);
                field.setAccessible(true);
                if (cursor.isNull(columnIndex)) {
                    // If NULL, use the initial value
                    continue;
                }
                TypeConverter typeConverter = info.getConverter();
                Object o = typeConverter.unpack(cursor, columnName);
                if (!info.isEntity()) {
                    field.set(result, o);
                    continue;
                }
                // ------------------------------------
                // ## process for Entity 1to1 relation
                // ------------------------------------
                Entity entity = (Entity) o;
                Class<? extends Entity> entityClass = entity.getClass();
                if (mEntityManager != null) {
                    entity = mEntityManager.selectOneById(entityClass, entity.id);
                }
                field.set(result, entity);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
        return result;
    }
}