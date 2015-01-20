package monotalk.db.utility;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.annotation.Id;
import monotalk.db.exception.IllegalAccessRuntimeException;
import monotalk.db.exception.InstantiationRuntimeException;
import monotalk.db.exception.InvocationTargetRuntimeException;
import monotalk.db.exception.NoSuchMethodRuntimeException;

public class ReflectUtils {
    private static final int ACCESS_TEST = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;
    /**
     * ログに使用するタグ名
     */
    private final static String TAG_NAME = DBLog.getTag(ReflectUtils.class);
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final Class<?>[] EMPTY_CLASS_PARAMETERS = new Class<?>[0];

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
    public static <T> T invokeConstructor(Class<T> cls, Object[] args) {
        if (null == args) {
            args = EMPTY_OBJECT_ARRAY;
        }
        T object = null;
        int arguments = args.length;
        Class<?> parameterTypes[] = new Class<?>[arguments];
        for (int i = 0; i < arguments; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        try {
            object = (T) invokeConstructor(cls, args, parameterTypes);
        } catch (NoSuchMethodException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
            throw new NoSuchMethodRuntimeException(e);
        } catch (IllegalAccessException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
            throw new IllegalAccessRuntimeException(e);
        } catch (InvocationTargetException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
            throw new InvocationTargetRuntimeException(e);
        } catch (InstantiationException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
            throw new InstantiationRuntimeException(e);
        }
        return object;
    }

    private static <T> T invokeConstructor(Class<T> klass, Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        if (parameterTypes == null) {
            parameterTypes = EMPTY_CLASS_PARAMETERS;
        }
        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }
        Constructor<T> ctor = getMatchingAccessibleConstructor(klass, parameterTypes);
        if (null == ctor) {
            throw new NoSuchMethodException("No such accessible constructor on object: " + klass.getName());
        }
        return ctor.newInstance(args);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getMatchingAccessibleConstructor(Class<T> clazz, Class<?>[] parameterTypes) {
        // see if we can find the method directly
        // most of the time this works and it's much faster
        try {
            Constructor<T> ctor = clazz.getConstructor(parameterTypes);
            try {
                //
                // XXX Default access superclass workaround
                //
                // When a public class has a default access superclass
                // with public methods, these methods are accessible.
                // Calling them from compiled code works fine.
                //
                // Unfortunately, using reflection to invoke these methods
                // seems to (wrongly) to prevent access even when the method
                // modifer is public.
                //
                // The following workaround solves the problem but will only
                // work from sufficiently privilages code.
                //
                // Better workarounds would be greatfully accepted.
                //
                ctor.setAccessible(true);
            } catch (SecurityException se) {
            }
            return ctor;

        } catch (NoSuchMethodException e) { /* SWALLOW */
        }

        // search through allColumns methods
        int paramSize = parameterTypes.length;
        Constructor<T>[] ctors = (Constructor<T>[]) clazz.getConstructors();
        for (int i = 0, size = ctors.length; i < size; i++) {
            // compare parameters
            Class<?>[] ctorParams = ctors[i].getParameterTypes();
            int ctorParamSize = ctorParams.length;
            if (ctorParamSize == paramSize) {
                boolean match = true;
                for (int n = 0; n < ctorParamSize; n++) {
                    if (!isAssignmentCompatible(ctorParams[n], parameterTypes[n])) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    // get accessible version of method
                    Constructor<T> ctor = getAccessibleConstructor(ctors[i]);
                    if (ctor != null) {
                        try {
                            ctor.setAccessible(true);
                        } catch (SecurityException se) {
                        }
                        return ctor;
                    }
                }
            }
        }
        return null;
    }

    public static final boolean isAssignmentCompatible(Class<?> parameterType, Class<?> parameterization) {
        // try plain assignment
        if (parameterType.isAssignableFrom(parameterization)) {
            return true;
        }

        if (parameterType.isPrimitive()) {
            // this method does *not* do widening - you must specify exactly
            // is this the right behaviour?
            Class<?> parameterWrapperClazz = getPrimitiveWrapper(parameterType);
            if (parameterWrapperClazz != null) {
                return parameterWrapperClazz.equals(parameterization);
            }
        }

        return false;
    }

    public static Class<?> getPrimitiveWrapper(Class<?> primitiveType) {
        // does anyone know a better strategy than comparing names?
        if (boolean.class.equals(primitiveType)) {
            return Boolean.class;
        } else if (float.class.equals(primitiveType)) {
            return Float.class;
        } else if (long.class.equals(primitiveType)) {
            return Long.class;
        } else if (int.class.equals(primitiveType)) {
            return Integer.class;
        } else if (short.class.equals(primitiveType)) {
            return Short.class;
        } else if (byte.class.equals(primitiveType)) {
            return Byte.class;
        } else if (double.class.equals(primitiveType)) {
            return Double.class;
        } else if (char.class.equals(primitiveType)) {
            return Character.class;
        } else {
            return null;
        }
    }

    /**
     * <p>
     * クラス名からクラスを取得する
     * </p>
     *
     * @param className
     * @return
     */
    public static boolean isArrayFeildType(Field field, Class<?> clazz) {
        Class<?> type = field.getType();
        if (type.isArray()) {
            Class<?> elementType = type.getComponentType();
            if (clazz.equals(elementType)) {
                return true;
            }
        }
        return false;
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
     * @param forceAccess
     * @return
     */
    public static Object readField(Field field, Object target, boolean forceAccess) {
        try {
            if (field == null) {
                throw new IllegalArgumentException("The field must not be null");
            }
            if (forceAccess && !field.isAccessible()) {
                field.setAccessible(true);
            } else {
                setAccessibleWorkaround(field);
            }
            return field.get(target);
        } catch (IllegalAccessException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
            throw new IllegalAccessRuntimeException(e);
        }
    }

    static void setAccessibleWorkaround(AccessibleObject o) {
        if (o == null || o.isAccessible()) {
            return;
        }
        Member m = (Member) o;
        if (Modifier.isPublic(m.getModifiers()) && isPackageAccess(m.getDeclaringClass().getModifiers())) {
            try {
                o.setAccessible(true);
            } catch (SecurityException e) {
                // ignore in favor of subsequent IllegalAccessException
            }
        }
    }

    static boolean isPackageAccess(int modifiers) {
        return (modifiers & ACCESS_TEST) == 0;
    }

    /**
     * オブジェクトのフィールド値を書き込む
     *
     * @param field
     * @param target
     * @param value
     * @param forceAccess
     */
    public static void writeField(Field field, Object target, Object value, boolean forceAccess) {
        try {
            if (field == null) {
                throw new IllegalArgumentException("The field must not be null");
            }
            if (forceAccess && !field.isAccessible()) {
                field.setAccessible(true);
            } else {
                setAccessibleWorkaround(field);
            }
            field.set(target, value);
        } catch (IllegalAccessException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
            throw new IllegalAccessRuntimeException(e);
        }
    }
}
