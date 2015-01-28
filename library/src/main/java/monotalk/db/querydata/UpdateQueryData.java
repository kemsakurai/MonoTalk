package monotalk.db.querydata;

import android.content.ContentValues;
import android.text.TextUtils;

import java.util.Map;

import monotalk.db.query.Sqlable;

public class UpdateQueryData extends DeleteQueryData implements Sqlable {

    /**
     * Column名
     */
    protected String[] columnNames;

    /**
     * ContentValues
     */
    private ContentValues values;

    @Override
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append("SET ");
        sql.append(TextUtils.join("= ? ,", columnNames));
        sql.append("= ?");
        addWhere(sql);
        return sql.toString().trim();
    }

    public ContentValues getValues() {
        return values;
    }

    public void setValues(ContentValues values) {
        this.values = values;
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
}
