package monotalk.db.querydata;

public class BaseQueryData {
    /**
     * Table名
     */
    protected String tableName;

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
}
