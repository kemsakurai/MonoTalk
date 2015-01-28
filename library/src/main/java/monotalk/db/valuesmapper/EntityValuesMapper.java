package monotalk.db.valuesmapper;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.FieldInfo;
import monotalk.db.MonoTalk;
import monotalk.db.TableInfo;
import monotalk.db.exception.IllegalAccessRuntimeException;
import monotalk.db.utility.AssertUtils;

/**
 * Created by Kem on 2015/01/11.
 */
public class EntityValuesMapper<T extends Entity> implements ValuesMapper<T> {

    private static final String TAG_NAME = DBLog.getTag(EntityValuesMapper.class);
    private final boolean isExcludeNull;
    private TableInfo info = null;
    private List<String> listColumns;
    private ColumnsOpelation columnsOpelation;

    private EntityValuesMapper(Class<T> type, boolean isExcludeNull, boolean isExcludesColumns, String[] columns) {
        AssertUtils.assertNotNull(columns, "columns is Null");
        AssertUtils.assertNotNull(type, "type is Null");
        TableInfo info = MonoTalk.getTableInfo(type);
        AssertUtils.assertNotNull(info, "info is Null");
        this.info = info;
        this.listColumns = Arrays.asList(columns);
        this.isExcludeNull = isExcludeNull;
        if (isExcludesColumns) {
            this.columnsOpelation = new ExcludesColumnsOpelation();
        } else {
            this.columnsOpelation = new IncludesColumnsOpelation();
        }
    }

    @Override
    public ContentValues mapValues(T entity) {
        ContentValues values = new ContentValues();
        for (FieldInfo fieldInfo : info.getFieldInfos()) {
            final String fieldName = fieldInfo.getColumnName();
            if (!columnsOpelation.isTargetColumn(fieldName)) {
                continue;
            }
            Field field = fieldInfo.getField();
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value == null) {
                    if (isExcludeNull) {
                        continue;
                    }
                    values.putNull(fieldName);
                    continue;
                }
                fieldInfo.getConverter().pack(value, values, fieldName);

            } catch (IllegalArgumentException e) {
                DBLog.e(TAG_NAME, e.getClass().getName(), e);
                throw e;
            } catch (IllegalAccessException e) {
                DBLog.e(TAG_NAME, e.getClass().getName(), e);
                throw new IllegalAccessRuntimeException(e);
            }
        }
        return values;
    }

    public static class Builder<T extends Entity> {
        private Class<T> type;
        private boolean isExcludeNull;
        private boolean isExcludeColums;
        private String[] columns;

        public Builder(Class<T> type) {
            this.type = type;
            this.isExcludeNull = false;
            this.isExcludeColums = true;
            this.columns = new String[0];
        }

        public Builder excludesNull() {
            this.isExcludeNull = true;
            return this;
        }

        public Builder includesNull() {
            this.isExcludeNull = false;
            return this;
        }

        public Builder excludesColumns(String[] columns) {
            this.isExcludeColums = true;
            this.columns = columns;
            return this;
        }

        public Builder includesColumns(String[] columns) {
            this.isExcludeColums = false;
            this.columns = columns;
            return this;
        }

        public EntityValuesMapper create() {
            return new EntityValuesMapper(type, isExcludeNull, isExcludeColums, columns);
        }
    }

    abstract class ColumnsOpelation {
        abstract boolean isTargetColumn(String columnName);
    }

    class ExcludesColumnsOpelation extends ColumnsOpelation {
        @Override
        boolean isTargetColumn(String columnName) {
            return !listColumns.contains(columnName);
        }
    }

    class IncludesColumnsOpelation extends ColumnsOpelation {
        @Override
        boolean isTargetColumn(String columnName) {
            return listColumns.contains(columnName);
        }
    }
}
