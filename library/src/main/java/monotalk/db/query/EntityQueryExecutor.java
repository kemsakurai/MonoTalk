package monotalk.db.query;

import android.database.Cursor;

import org.seasar.framework.util.tiger.GenericUtil;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import monotalk.db.Entity;
import monotalk.db.manager.EntityManager;

/**
 * コンテンツプロバイダー用途にクエリを生成するSQLクエリ規定クラス
 *
 * @param <T> Tableの行オブジェクトの継承クラス
 * @author Kem
 */
public abstract class EntityQueryExecutor<T extends Entity> {

    /**
     * ModelManager
     */
    protected EntityManager manager;
    /**
     * Entity Class
     */
    protected Class<T> entity;

    /**
     * Constructor
     */
    public EntityQueryExecutor(EntityManager manager) {
        init(manager);
    }

    // -------------------------------------------------------------
    // public Method
    // -------------------------------------------------------------
    public int deleteAll() {
        return manager.deleteAll(entity);
    }

    public int deleteById(long id) {
        return manager.deleteById(entity, id);
    }

    public long insertExcludesNull(T object) {
        return manager.insertExcludesNull(object);
    }

    public long insert(T object) {
        return manager.insert(object);
    }

    public Cursor selectCursorAll() {
        return manager.selectCursorAll(entity);
    }

    public List<T> selectListAll() {
        return manager.selectListAll(entity);
    }

    public T selectOneById(long id) {
        return manager.selectOneById(entity, id);
    }

    public Cursor selectCursorById(long id) {
        return manager.selectCursorById(entity, id);
    }

    public long selectCountAll() {
        return manager.selectCount(entity);
    }

    public long selectCountById(long id) {
        return manager.selectCountById(entity, id);
    }

    public int update(T object) {
        return manager.update(object);
    }

    public int updateExcludesNull(T object) {
        return manager.updateExcludesNull(object);
    }

    public long store(T object) {
        return manager.store(object);
    }

    // -------------------------------------------------------------
    // protected Method
    // -------------------------------------------------------------
    protected Delete.From<T> newDeleteFrom() {
        return manager.newDeleteFrom(entity);
    }

    protected Insert<T> newInsertInto() {
        return manager.newInsertInto(entity);
    }

    protected Select newSelect(Select.Column... columns) {
        return manager.newSelect(columns);
    }

    protected Select newSelect(String... columns) {
        return manager.newSelect(columns);
    }

    protected TwoWayQuerySelect<T> newSelectBySqlFile(String fileName) {
        return manager.newSelectBySqlFile(entity, fileName);
    }

    protected Update<T> update() {
        return manager.newUpdate(entity);
    }

    /**
     * init method
     */
    @SuppressWarnings("unchecked")
    private void init(EntityManager manager) {
        this.manager = manager;
        Map<TypeVariable<?>, Type> map = GenericUtil.getTypeVariableMap(getClass());
        for (Class<?> c = getClass(); c != Object.class; c = c.getSuperclass()) {
            if (c.getSuperclass() == EntityQueryExecutor.class) {
                Type type = c.getGenericSuperclass();
                Type[] arrays = GenericUtil.getGenericParameter(type);
                Class<T> clazz = (Class<T>) GenericUtil.getActualClass(arrays[0], map);
                this.entity = clazz;
                break;
            }
        }
    }
}
