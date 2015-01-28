package monotalk.db.typeconverter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;

import monotalk.db.Entity;
import monotalk.db.utility.ReflectUtils;

/**
 * Created by Kem on 2015/01/23.
 */
public class EntityConverter<T extends Entity> extends BaseTypeConverter<T> {

    private Class<T> entityClass = null;

    public EntityConverter(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T unpack(Cursor c, int index) {
        long id = c.getLong(index);
        T entity = ReflectUtils.newInstance(entityClass);
        entity.id = id;
        return entity;
    }

    @Override
    public void pack(T object, ContentValues cv, String name) {
        cv.put(name, object.id);
    }

    @Override
    public String toSql(T object) {
        return String.valueOf(object.id);
    }

    @Override
    public String toStringBindArg(T object) {
        return String.valueOf(object.id);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.INTEGER;
    }

    @Override
    public void bind(SQLiteProgram program, int index, T value) {
        program.bindLong(index, value.id);
    }
}
