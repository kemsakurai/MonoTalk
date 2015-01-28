package monotalk.db.querydata;

import android.net.Uri;
import android.text.TextUtils;

import java.util.Locale;

import monotalk.db.query.Sqlable;

import static monotalk.db.utility.AssertUtils.assertNotNull;

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
     * groupBy句
     */
    private String groupBy;
    /**
     * HAVING句
     */
    private String having;
    /**
     * JOIN句
     */
    private String join;
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

    /**
     * インスタンスを生成する
     *
     * @param uri
     * @param tableName
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    public static SelectQueryData newInstance(Uri uri, String tableName, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SelectQueryData data = new SelectQueryData();
        data.setTableName(tableName);
        data.setColumns(projection);
        data.setWhere(selection);
        data.setSelectionArgs(selectionArgs);
        data.setOrderBy(sortOrder);
        data.setTableAlias(uri.getQueryParameter("_tableAlias"));
        data.setGroupBy(uri.getQueryParameter("_groupBy"));
        data.setHaving(uri.getQueryParameter("_having"));
        data.setLimit(uri.getQueryParameter("_limit"));
        String distinct = uri.getQueryParameter("_distinct");
        data.setDistinct(Boolean.valueOf(distinct));
        data.setOffSet(uri.getQueryParameter("_offSet"));
        data.setJoin(uri.getQueryParameter("_join"));
        return data;
    }

    public String getJoin() {
        return join;
    }

    public void setJoin(String join) {
        this.join = join;
    }

    public void appendJoin(String joinString) {
        if (this.join == null) {
            this.join = joinString;
        } else {
            this.join += joinString;
        }
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

    public String getTableAndJoin() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(tableName)) {
            sb.append(tableName);
        }
        if (!TextUtils.isEmpty(join)) {
            sb.append(join);
        }
        return sb.toString();
    }

    @Override
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        addQueryBase(sql);
        addFrom(sql);
        addJoins(sql);
        addWhere(sql);
        addGroupBy(sql);
        addHaving(sql);
        addOrderBy(sql);
        addLimit(sql);
        addOffset(sql);
        return sql.toString().trim();
    }

    private void addQueryBase(StringBuilder sql) {
        sql.append("SELECT ");
        if (distinct) {
            sql.append("DISTINCT ");
        }
        if (columns != null && columns.length > 0) {
            sql.append(TextUtils.join(", ", columns) + " ");
        } else {
            sql.append("* ");
        }
    }

    private void addJoins(final StringBuilder sql) {
        if (join != null && join.length() > 0) {
            sql.append(join);
        }
    }

    private void addGroupBy(final StringBuilder sql) {
        if (groupBy != null && groupBy.length() > 0) {
            sql.append("GROUP BY ");
            sql.append(groupBy);
            sql.append(" ");
        }
    }

    private void addHaving(final StringBuilder sql) {
        if (having != null && having.length() > 0) {
            sql.append("HAVING ");
            sql.append(having);
            sql.append(" ");
        }
    }

    private void addOrderBy(final StringBuilder sql) {
        if (orderBy != null && orderBy.length() > 0) {
            sql.append("ORDER BY ");
            sql.append(orderBy);
            sql.append(" ");
        }
    }

    private void addLimit(final StringBuilder sql) {
        if (limit != null && limit.length() > 0) {
            sql.append("LIMIT ");
            sql.append(limit);
            sql.append(" ");
        }
    }

    private void addOffset(final StringBuilder sql) {
        if (offSet != null && offSet.length() > 0) {
            sql.append("OFFSET ");
            sql.append(offSet);
            sql.append(" ");
        }
    }

    /**
     * コンテンツプロバイダーSELECTクエリ用のURIを生成する
     *
     * @param authorityUri
     * @return Uri
     */
    public Uri buildQueryUri(Uri authorityUri) {
        assertNotNull(tableName, "tableName is null");

        Uri.Builder builder = authorityUri.buildUpon();
        builder.path("/" + tableName.toLowerCase(Locale.getDefault()));

        // TableAlias
        if (!TextUtils.isEmpty(tableAlias)) {
            builder.appendQueryParameter("_tableAlias", tableAlias);
        }
        // LIMIT句が設定されている場合はGETパラメータに設定
        if (!TextUtils.isEmpty(limit)) {
            builder.appendQueryParameter("_limit", limit);
        }
        // HAVING句が設定されている場合はGETパラメータに設定
        if (!TextUtils.isEmpty(having)) {
            builder.appendQueryParameter("_having", having);
        }
        // GROUPBY句が設定されている場合はGETパラメータに設定
        if (!TextUtils.isEmpty(groupBy)) {
            builder.appendQueryParameter("_groupBy", groupBy);
        }
        if (!TextUtils.isEmpty(offSet)) {
            builder.appendQueryParameter("_offSet", offSet);
        }
        if (!TextUtils.isEmpty(join)) {
            builder.appendQueryParameter("_join", join);
        }
        // GROUPBY句が設定されている場合はGETパラメータに設定
        builder.appendQueryParameter("_distinct", Boolean.toString(distinct));
        return builder.build();
    }
}
