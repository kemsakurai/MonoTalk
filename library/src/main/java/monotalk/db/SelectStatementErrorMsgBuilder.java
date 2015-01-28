/*
* Copyright (C) 2013-2015 Kem
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package monotalk.db;

import java.util.Arrays;

/**
 * Created by Kem on 2015/01/21.
 */
// ========================================================================
// Error Message Builder Class
// ========================================================================
class SelectStatementErrorMsgBuilder {
    private String groupBy = null;
    private String having = null;
    private String limit = null;
    private String[] projection = null;
    private String selection = null;
    private String[] selectionArgs = null;
    private String sortOrder = null;
    private String sql = null;
    private String tableName = null;

    public String buildMsg() {
        StringBuilder builder = new StringBuilder();
        builder.append("SelectStatementErrorMsgBuilder [");
        if (groupBy != null) {
            builder.append("groupBy=").append(groupBy).append(", ");
        }
        if (having != null) {
            builder.append("having=").append(having).append(", ");
        }
        if (limit != null) {
            builder.append("limit=").append(limit).append(", ");
        }
        if (projection != null) {
            builder.append("projection=").append(Arrays.toString(projection)).append(", ");
        }
        if (selection != null) {
            builder.append("selection=").append(selection).append(", ");
        }
        if (selectionArgs != null) {
            builder.append("selectionArgs=").append(Arrays.toString(selectionArgs)).append(", ");
        }
        if (sortOrder != null) {
            builder.append("sortOrder=").append(sortOrder).append(", ");
        }
        if (sql != null) {
            builder.append("sql=").append(sql).append(", ");
        }
        if (tableName != null) {
            builder.append("tableName=").append(tableName);
        }
        builder.append("]");
        return builder.toString();
    }

    public SelectStatementErrorMsgBuilder setGroupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public SelectStatementErrorMsgBuilder setHaving(String having) {
        this.having = having;
        return this;
    }

    public SelectStatementErrorMsgBuilder setLimit(String limit) {
        this.limit = limit;
        return this;
    }

    public SelectStatementErrorMsgBuilder setProjection(String[] projection) {
        this.projection = projection;
        return this;
    }

    public SelectStatementErrorMsgBuilder setSelection(String selection) {
        this.selection = selection;
        return this;
    }

    public SelectStatementErrorMsgBuilder setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
        return this;
    }

    public SelectStatementErrorMsgBuilder setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public SelectStatementErrorMsgBuilder setSql(String sql) {
        this.sql = sql;
        return this;
    }

    public SelectStatementErrorMsgBuilder setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
}