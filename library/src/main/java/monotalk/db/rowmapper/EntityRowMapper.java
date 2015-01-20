package monotalk.db.rowmapper;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.manager.EntityCache;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;
import monotalk.db.utility.ReflectUtils;

public class EntityRowMapper<T extends Entity> implements RowMapper<T> {

    private Class<T> mType;
    private EntityCache entityCache;

    public EntityRowMapper(Class<T> clazz, EntityCache entityCache) {
        mType = clazz;
        this.entityCache = entityCache;
        if (entityCache != null) {
            this.entityCache.evictAllEntity();
        }
    }

    @SuppressWarnings("unchecked")
    public T mapRow(Cursor cursor) {
        try {
            Map<Field, String> map = MonoTalk.getTableInfo(mType).getColumnNamesMap();
            T result = mType.newInstance();
            List<String> colNames = Arrays.asList(cursor.getColumnNames());
            for (Map.Entry<Field, String> entry : map.entrySet()) {
                String columnName = entry.getValue();
                if (!colNames.contains(columnName)) {
                    continue;
                }
                Field field = entry.getKey();
                field.setAccessible(true);
                int columnIndex = cursor.getColumnIndexOrThrow(columnName);
                if (cursor.isNull(columnIndex)) {
                    field.set(result, null);
                    continue;
                }
                final Class<?> type = field.getType();
                TypeConverter<?> typeConverter = TypeConverterCache.getTypeConverter(type);
                if (typeConverter != null) {
                    Object o = typeConverter.unpack(cursor, columnName);
                    field.set(result, o);
                    continue;
                }

                // ----------------------------------------------------------------
                // if Block By Field's Type
                // ----------------------------------------------------------------
                // TODO: Find a smarter way to do this? This if block is
                // necessary because we
                // can't know the type until runtime.
                Object value = null;
                if (type.equals(Byte.class) || type.equals(byte.class)) {
                    value = cursor.getInt(columnIndex);
                } else if (type.equals(Short.class) || type.equals(short.class)) {
                    value = cursor.getInt(columnIndex);
                } else if (type.equals(Integer.class) || type.equals(int.class)) {
                    value = cursor.getInt(columnIndex);
                } else if (type.equals(Long.class) || type.equals(long.class)) {
                    value = cursor.getLong(columnIndex);
                } else if (type.equals(Float.class) || type.equals(float.class)) {
                    value = cursor.getFloat(columnIndex);
                } else if (type.equals(Double.class) || type.equals(double.class)) {
                    value = cursor.getDouble(columnIndex);
                } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                    value = cursor.getInt(columnIndex) != 0;
                } else if (type.equals(Character.class) || type.equals(char.class)) {
                    value = cursor.getString(columnIndex).charAt(0);
                } else if (type.equals(String.class)) {
                    value = cursor.getString(columnIndex);
                } else if (type.equals(Byte[].class) || type.equals(byte[].class)) {
                    value = cursor.getBlob(columnIndex);
                } else if (ReflectUtils.isEntity(type)) {
                    final long entityId = cursor.getLong(columnIndex);
                    final Class<? extends Entity> entityType = (Class<? extends Entity>) type;
                    Entity entity = null;
                    if (entityCache != null) {
                        entity = entityCache.getEntityOrSelect(entityType, entityId);
                    }
                    if (entity == null) {
                        entity = ReflectUtils.invokeConstructor(entityType, null);
                        Field idField = MonoTalk.getTableInfo(entityType).getIdField();
                        idField.set(entity, entityId);
                    }
                    value = entity;
                } else if (ReflectUtils.isSubclassOf(type, Enum.class)) {
                    @SuppressWarnings("rawtypes")
                    final Class<? extends Enum> enumType = (Class<? extends Enum>) type;
                    value = Enum.valueOf(enumType, cursor.getString(columnIndex));
                }
                // Set the field value
                if (value != null) {
                    field.set(result, value);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
