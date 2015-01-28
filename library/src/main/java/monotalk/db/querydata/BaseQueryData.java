package monotalk.db.querydata;

import static monotalk.db.utility.AssertUtils.assertNotNull;

public class BaseQueryData {
    /**
     * Table名
     */
    protected String tableName;
    /**
     * TableAlias名
     */
    protected String tableAlias;

    /**
     * Table名を返します
     *
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Table名を設定します。
     *
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * TableAliasを設定します。
     *
     * @param tableAlias
     */
    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    /**
     * TableAliasを返します。
     *
     * @return
     */
    public String getTableAlias() {
        return tableAlias;
    }

    /**
     * StringBuilderにFROM句を追加します。
     *
     * @param sql
     */
    protected void addFrom(final StringBuilder sql) {
        assertNotNull(tableName, "tableName is null");
        sql.append("FROM ");
        sql.append(tableName).append(" ");
        if (tableAlias != null && tableAlias.length() > 0) {
            sql.append("AS ");
            sql.append(tableAlias);
            sql.append(" ");
        }
    }
}
