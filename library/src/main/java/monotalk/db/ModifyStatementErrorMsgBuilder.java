package monotalk.db;

import android.content.ContentValues;

import java.util.Arrays;

import monotalk.db.query.QueryUtils;

// ========================================================================
// Builder Class
// ========================================================================
class ModifyStatementErrorMsgBuilder {
    private String selection = null;
    private String[] selectionArgs = null;
    private String sql = null;
    private String tableName = null;
    private ContentValues values = null;

    public ModifyStatementErrorMsgBuilder setSelection(String selection) {
        this.selection = selection;
        return this;
    }

    public ModifyStatementErrorMsgBuilder setSelectionArgs(Object[] selectionArgs) {
        this.selectionArgs = QueryUtils.toStirngArrayArgs(selectionArgs);
        return this;
    }

    public ModifyStatementErrorMsgBuilder setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
        return this;
    }

    public ModifyStatementErrorMsgBuilder setSql(String sql) {
        this.sql = sql;
        return this;
    }

    public ModifyStatementErrorMsgBuilder setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public ModifyStatementErrorMsgBuilder setValues(ContentValues values) {
        this.values = values;
        return this;
    }

    public String buildMsg() {
        StringBuilder builder = new StringBuilder();
        builder.append("ModifyStatementErrorMsgBuilder [");
        if (selection != null) {
            builder.append("selection=").append(selection).append(", ");
        }
        if (selectionArgs != null) {
            builder.append("selectionArgs=").append(Arrays.toString(selectionArgs)).append(", ");
        }
        if (sql != null) {
            builder.append("sql=").append(sql).append(", ");
        }
        if (tableName != null) {
            builder.append("tableName=").append(tableName).append(", ");
        }
        if (values != null) {
            builder.append("values=").append(values);
        }
        builder.append("]");
        return builder.toString();
    }
}