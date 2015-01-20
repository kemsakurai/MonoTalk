package monotalk.db.querydata;

import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;

import monotalk.db.query.Sqlable;

public class SelectQueryData extends DeleteQueryData implements Sqlable {

    /**
     * SELECT HINT句
     */
    private String[] columns;
    /**
     * SELECT distinct句
     */
    private boolean distinct = false;
    /**
     * GROUPBY句
     */
    private String groupBy;
    /**
     * HAVING句
     */
    private String having;
    /**
     * JOIN句
     */
    private String joinString;
    /**
     * SELECTレコード件数
     */
    private String limit;
    /**
     * OFFSET
     */
    private String offSet;
    /**
     * ソート順
     */
    private String orderBy;

    public void appendJoinString(String joinString) {
        if (this.joinString == null) {
            this.joinString = joinString;
        } else {
            this.joinString += joinString;
        }
    }

    public String getTableAndJoin() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(tableName)) {
            sb.append(tableName);
        }

        if (!TextUtils.isEmpty(joinString)) {
            sb.append(joinString);
        }
        return sb.toString();
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffSet() {
        return offSet;
    }

    public void setOffSet(String offSet) {
        this.offSet = offSet;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public String toSql() {
        return SQLiteQueryBuilder.buildQueryString(
                distinct,
                getTableAndJoin(),
                columns,
                where,
                groupBy,
                having,
                orderBy,
                limit);
    }
}
