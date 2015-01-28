package monotalk.db.typeconverter;

import android.graphics.Bitmap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import monotalk.db.exception.NoTypeSerializerFoundException;

public class TypeConverterCache {

    @SuppressWarnings("rawtypes")
    public static TypeConverter getTypeConverter(Class<?> key) {
        TypeConverter converter = sTypeSerializers.get(key);
        return sTypeSerializers.get(key);
    }

    @SuppressWarnings("rawtypes")
    public static TypeConverter getTypeConverterOrThrow(Class<?> key) {
        if (!sTypeSerializers.containsKey(key)) {
            throw new NoTypeSerializerFoundException(key);
        }
        return sTypeSerializers.get(key);
    }

    @SuppressWarnings("rawtypes")
    public static TypeConverter registerTypeConverter(Class<?> key, TypeConverter typeConverter) {
        if (!sTypeSerializers.containsKey(key)) {
            return sTypeSerializers.put(key, typeConverter);
        }
        return sTypeSerializers.get(key);
    }

    @SuppressWarnings("rawtypes")
    public static TypeConverter overrideRegisterTypeConverter(Class<?> key, TypeConverter typeConverter) {
        return sTypeSerializers.put(key, typeConverter);
    }

    private static Map<Class<?>, TypeConverter<?>> sTypeSerializers = new HashMap<Class<?>, TypeConverter<?>>() {
        private static final long serialVersionUID = -4911605630739752012L;

        {
            // ========================================
            // Default DataTypeConverter
            // ========================================
            put(Date.class, new DateConverter());

            put(Boolean.class, new BooleanConverter());
            put(boolean.class, new BooleanConverter());

            put(int.class, new IntConverter());
            put(Integer.class, new IntConverter());

            put(long.class, new LongConverter());
            put(Long.class, new LongConverter());

            put(float.class, new FloatConverter());
            put(Float.class, new FloatConverter());

            put(double.class, new DoubleConverter());
            put(Double.class, new DoubleConverter());

            put(boolean.class, new BooleanConverter());
            put(Boolean.class, new BooleanConverter());

            put(String.class, new StringConverter());

            put(Date.class, new DateConverter());
            put(Bitmap.class, new BitmapConverter());

            put(byte[].class, new ByteArrayConverter());
        }
    };
}
