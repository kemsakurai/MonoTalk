package monotalk.db.valuesmapper;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.TableInfo;
import monotalk.db.utility.AssertUtils;
import monotalk.db.utility.ConvertUtils;

/**
 * Created by Kem on 2015/01/11.
 */
public class EntityValuesMapper<T extends Entity> implements ValuesMapper<T> {

    private static final String TAG_NAME = DBLog.getTag(EntityValuesMapper.class);

    private TableInfo info;
    private boolean isExcludeNull;
    private boolean isExcludeColums;
    private String[] columns;

    private EntityValuesMapper(Class<T> type, boolean isExcludeNull, boolean isExcludeColums, String[] columns) {
        AssertUtils.assertNotNull(columns, "columns is Null");
        AssertUtils.assertNotNull(type, "type is Null");
        TableInfo info = MonoTalk.getTableInfo(type);
        AssertUtils.assertNotNull(info, "info is Null");
        this.info = info;
        this.isExcludeNull = isExcludeNull;
        this.isExcludeColums = isExcludeColums;
        this.columns = columns;
    }

    @Override
    public ContentValues mapValues(T object) {
        ContentValues values = new ContentValues();
        List<String> listColums = Arrays.asList(columns);
        for (Field field : info.getFields()) {
            final String fieldName = info.getColumnName(field);
            if (isExcludeColums) {
                if (listColums.contains(fieldName)) {
                    continue;
                }
            } else {
                if (!listColums.contains(fieldName)) {
                    continue;
                }
            }
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                if (value == null && isExcludeNull) {
                    continue;
                }
                ConvertUtils.addValue(fieldName, value, values);
            } catch (IllegalArgumentException e) {
                DBLog.e(TAG_NAME, e.getClass().getName(), e);
            } catch (IllegalAccessException e) {
                DBLog.e(TAG_NAME, e.getClass().getName(), e);
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
}
