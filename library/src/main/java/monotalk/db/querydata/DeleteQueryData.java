package monotalk.db.querydata;

import monotalk.db.query.QueryUtils;
import monotalk.db.query.Sqlable;

public class DeleteQueryData extends BaseQueryData implements Sqlable {
    /**
     * バインドパラメータ
     */
    protected Object[] selectionArgs;
    /**
     * Where句
     */
    protected String where;

    /**
     * バインドパラメータを返します
     *
     * @return
     */
    public Object[] getSelectionArgs() {
        return selectionArgs;
    }

    /**
     * バインドパラメータをString配列に変換して返します
     *
     * @return
     */
    public String[] getStringSelectionArgs() {
        return QueryUtils.toStirngArrayArgs(selectionArgs);
    }

    /**
     * バインドパラメータを設定します
     *
     * @param selectionArgs
     */
    public void setSelectionArgs(Object[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    /**
     * Where句を返します
     *
     * @return
     */
    public String getWhere() {
        return where;
    }

    /**
     * Where句を設定します
     *
     * @param where
     */
    public void setWhere(String where) {
        this.where = where;
    }

    /**
     * StringBuilderにWhere句を追加します。
     *
     * @param sql
     */
    protected void addWhere(StringBuilder sql) {
        if (where != null && where.length() > 0) {
            sql.append("WHERE ");
            sql.append(where);
            sql.append(" ");
        }
    }

    @Override
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE ");
        addFrom(sql);
        addWhere(sql);
        return sql.toString();
    }
}
