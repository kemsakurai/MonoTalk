package monotalk.db.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.annotation.Id;
import monotalk.db.exception.IllegalAccessRuntimeException;
import monotalk.db.exception.InstantiationRuntimeException;

public class ReflectUtils {
    /**
     * ログに使用するタグ名
     */
    private final static String TAG_NAME = DBLog.getTag(ReflectUtils.class);

    public static Field getIdField() {
        Field[] fields = Entity.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        // Tableアノテーションが付与されている場合,Idアノテーションは必須
        throw new IllegalStateException("@Id not Present ClassName= [" + Entity.class.getName() + "]");
    }


    private static <T> Constructor<T> getAccessibleConstructor(Class<T> klass, Class<?> parameterType) {
        Class<?>[] parameterTypes = {parameterType};
        return getAccessibleConstructor(klass, parameterTypes);

    }

    private static <T> Constructor<T> getAccessibleConstructor(Class<T> klass, Class<?>[] parameterTypes) {
        try {
            return getAccessibleConstructor(klass.getConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            return (null);
        }

    }

    private static <T> Constructor<T> getAccessibleConstructor(Constructor<T> ctor) {
        // Make sure we have a method to check
        if (ctor == null) {
            return (null);
        }

        // If the requested method is not public we cannot call it
        if (!Modifier.isPublic(ctor.getModifiers())) {
            return (null);
        }

        // If the declaring class is public, we are done
        Class<T> clazz = ctor.getDeclaringClass();
        if (Modifier.isPublic(clazz.getModifiers())) {
            return (ctor);
        }

        // what else can we do?
        return null;

    }

    /**
     * コンストラクターを実行する
     *
     * @param cls
     * @param args
     * @return
     */
    public static <T> T newInstance(Class<T> cls) {
        T object = null;
        try {
            object = cls.newInstance();
        } catch (InstantiationException e) {
            throw new InstantiationRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException(e);
        }
        return object;
    }

    public static boolean isEntity(Class<?> type) {
        return isSubclassOf(type, Entity.class) && (!Modifier.isAbstract(type.getModifiers()));
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    // ////////////////////////////////////////////////////////////////////////////////////
    public static boolean isSubclassOf(Class<?> type, Class<?> superClass) {
        if (type.getSuperclass() != null) {
            if (type.getSuperclass().equals(superClass)) {
                return true;
            }

            return isSubclassOf(type.getSuperclass(), superClass);
        }

        return false;
    }

    /**
     * オブジェクトのフィールド値を読み込む
     *
     * @param field
     * @param target
     * @return
     */
    public static Object readField(Field field, Object target) {
        try {
            if (field == null) {
                throw new IllegalArgumentException("The field must not be null");
            }
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
            throw new IllegalAccessRuntimeException(e);
        }
    }

    /**
     * オブジェクトのフィールド値を書き込む
     *
     * @param field
     * @param target
     * @param value
     */
    public static void writeField(Field field, Object target, Object value) {
        try {
            if (field == null) {
                throw new IllegalArgumentException("The field must not be null");
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
            throw new IllegalAccessRuntimeException(e);
        }
    }
}
