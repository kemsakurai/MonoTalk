package monotalk.db.rowmapper;

import android.database.Cursor;
import android.support.v4.util.LruCache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import monotalk.db.Entity;
import monotalk.db.FieldInfo;
import monotalk.db.MonoTalk;
import monotalk.db.exception.IllegalAccessRuntimeException;
import monotalk.db.manager.EntityManager;
import monotalk.db.utility.ReflectUtils;

/**
 * Created by Kem on 2015/01/24.
 */
public class EntityRowListMapper<T extends Entity> implements RowListMapper {

    private static final int C_DEFAULT_CACHE_SIZE = 100;
    private Class<T> mType;
    private EntityManager mEntityManager;
    private LruCache<String, Entity> mCache;
    private boolean mUseCache;

    public EntityRowListMapper(Class<T> clazz, EntityManager entityManager, int cacheSize, boolean useCache) {
        mType = clazz;
        mEntityManager = entityManager;
        mCache = new LruCache<>(cacheSize);
        mUseCache = useCache;
    }

    public EntityRowListMapper(Class<T> clazz, EntityManager entityManager) {
        this(clazz, entityManager, C_DEFAULT_CACHE_SIZE, true);
    }

    @Override
    public List mapRowListAndClose(Cursor cursor) {
        List<T> entities1 = new ArrayList<T>(cursor.getCount());
        List<FieldInfo> fieldInfos = MonoTalk.getTableInfo(mType).getFieldInfos();
        List<String> colNames = Arrays.asList(cursor.getColumnNames());
        if (cursor.moveToFirst()) {
            do {
                try {
                    T result = ReflectUtils.newInstance(mType);
                    for (FieldInfo info : fieldInfos) {
                        String columnName = info.getColumnName();
                        if (!colNames.contains(columnName)) {
                            continue;
                        }
                        Field field = info.getField();
                        int columnIndex = cursor.getColumnIndexOrThrow(columnName);
                        field.setAccessible(true);
                        if (cursor.isNull(columnIndex)) {
                            // If NULL, use the default value
                            continue;
                        }
                        Object o = info.getConverter().unpack(cursor, columnName);
                        if (!info.isEntity()) {
                            field.set(result, o);
                            continue;
                        }
                        Entity entity = (Entity) o;
                        Class<? extends Entity> entityClass = entity.getClass();
                        if (mUseCache) {
                            String key = MonoTalk.getTableName(entityClass) + "@" + entity.id;
                            Entity tmpEntity = mCache.get(key);
                            if (tmpEntity == null) {
                                if (mEntityManager != null) {
                                    entity = mEntityManager.selectOneById(entityClass, entity.id);
                                    mCache.put(key, entity);
                                }
                            } else {
                                entity = tmpEntity;
                            }
                        } else {
                            if (mEntityManager != null) {
                                entity = mEntityManager.selectOneById(entityClass, entity.id);
                            }
                        }
                        field.set(result, entity);
                    }
                    entities1.add(result);

                } catch (IllegalAccessException e) {
                    throw new IllegalAccessRuntimeException(e);
                }
            } while (cursor.moveToNext());
        }
        List<T> entities = entities1;
        cursor.close();
        return entities;
    }

}