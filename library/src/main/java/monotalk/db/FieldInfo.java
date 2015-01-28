package monotalk.db;

import java.lang.reflect.Field;

import monotalk.db.typeconverter.TypeConverter;

/**
 * Created by Kem on 2015/01/24.
 */
public class FieldInfo {
    private Field field;
    private String columnName;
    private boolean isEntity;
    private TypeConverter converter;
    
    public FieldInfo(Field field, String columnName, TypeConverter converter, boolean isEntity) {
        this.field = field;
        this.columnName = columnName;
        this.converter = converter;
        this.isEntity = isEntity;
    }

    public Field getField() {
        return field;
    }

    public String getColumnName() {
        return columnName;
    }

    public TypeConverter getConverter() {
        return converter;
    }

    public boolean isEntity() {
        return isEntity;
    }
}
