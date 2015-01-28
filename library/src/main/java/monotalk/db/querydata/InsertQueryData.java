package monotalk.db.querydata;

import android.content.ContentValues;

import java.util.Map;

import monotalk.db.query.QueryUtils;
import monotalk.db.query.Sqlable;

public class InsertQueryData extends BaseQueryData implements Sqlable {
    /**
     * ContentValues
     */
    protected ContentValues values;

    /**
     * Valueを取得します
     *
     * @return
     */
    public ContentValues getValues() {
        return values;
    }

    /**
     * Column名
     */
    protected String[] columnNames = null;

    /**
     * Valuesを設定します
     *
     * @param values
     */
    public void setValues(ContentValues values) {
        this.values = values;
    }

    /**
     * Column名の配列を返します。
     *
     * @return
     */
    public String[] getColumnNames() {
        if (columnNames == null) {
            columnNames = new String[values.size()];
            int index = 0;
            for (Map.Entry<String, Object> entry : values.valueSet()) {
                columnNames[index] = entry.getKey();
                index++;
            }
        }
        return columnNames;
    }

    @Override
    public String toSql() {
        return QueryUtils.toInsertSql(getTableName(), getColumnNames());
    }

    /**
     * SqlにBindする変数を返します。
     *
     * @return
     */
    public Object[] getBindArgs() {
        Object[] args = new Object[getColumnNames().length];
        int index = 0;
        for (String columnName : getColumnNames()) {
            args[index] = values.get(columnName);
            index++;
        }
        return args;
    }
}