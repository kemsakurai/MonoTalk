package monotalk.db.query;

/*
 * Copyright (C) 2014 Kem
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.querydata.SelectQueryData;
import monotalk.db.rowmapper.RowMapper;

public final class Select implements Sqlable {
    private String[] mColumns;
    private boolean mDistinct = false;
    private QueryCrudHandler mModelManager;

    Select(QueryCrudHandler mManager, String... columns) {
        mColumns = columns;
        mModelManager = mManager;
    }

    Select(QueryCrudHandler mManager, Column... columns) {
        final int size = columns.length;
        mColumns = new String[size];
        for (int i = 0; i < size; i++) {
            mColumns[i] = columns[i].name + " AS " + columns[i].alias;
        }
        mModelManager = mManager;
    }

    public Select distinct() {
        mDistinct = true;
        return this;
    }

    public Select all() {
        mDistinct = false;
        return this;
    }

    public <T extends Entity> From<T> from(Class<T> clazz) {
        return new From<T>(clazz, this);
    }

    @Override
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        if (mDistinct) {
            sql.append("DISTINCT ");
        }
        if (mColumns != null && mColumns.length > 0) {
            sql.append(TextUtils.join(", ", mColumns) + " ");
        } else {
            sql.append("* ");
        }
        return sql.toString();
    }

    static enum JoinType {
        LEFT_JOIN("LEFT JOIN"), LEFT_OUTER_JOIN("LEFT OUTER JOIN"), INNER_JOIN("INNER JOIN"), CROSS_JOIN("CROSS JOIN");
        private String sql;

        JoinType(String sql) {
            this.sql = sql;
        }

        public String toSql() {
            return sql;
        }
    }

    public static class Column {
        String name;
        String alias;

        public Column(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }
    }

    public class From<T extends Entity> extends AbstractFrom<T, From<T>> implements Selectable {
        private String mGroupBy;
        private String mHaving;
        private List<Join<T>> mJoins;
        private String mLimit;
        private String mOffset;
        private String mOrderBy;

        From(Class<T> table, Sqlable queryBase) {
            super(table, queryBase);
            selection = new Selection();
            mJoins = new ArrayList<Join<T>>();
        }

        public Join<T> crossJoin(Class<? extends Entity> table) {
            Join<T> join = new Join<T>(this, table, JoinType.CROSS_JOIN);
            mJoins.add(join);
            return join;
        }

        public From<T> groupBy(String groupBy) {
            mGroupBy = groupBy;
            return this;
        }

        public From<T> having(String having) {
            mHaving = having;
            return this;
        }

        public Join<T> innerJoin(Class<? extends Entity> table) {
            Join<T> join = new Join<T>(this, table, JoinType.INNER_JOIN);
            mJoins.add(join);
            return join;
        }

        public Join<T> join(Class<? extends Entity> table) {
            Join<T> join = new Join<T>(this, table, null);
            mJoins.add(join);
            return join;
        }

        public Join<T> leftJoin(Class<? extends Entity> table) {
            Join<T> join = new Join<T>(this, table, JoinType.LEFT_JOIN);
            mJoins.add(join);
            return join;
        }

        public From<T> limit(int limit) {
            return limit(String.valueOf(limit));
        }

        public From<T> limit(String limit) {
            mLimit = limit;
            return this;
        }

        public From<T> offset(int offset) {
            return offset(String.valueOf(offset));
        }

        public From<T> offset(String offset) {
            mOffset = offset;
            return this;
        }

        public From<T> orderBy(String orderBy) {
            mOrderBy = orderBy;
            return this;
        }

        public Join<T> outerJoin(Class<? extends Entity> table) {
            Join<T> join = new Join<T>(this, table, JoinType.LEFT_OUTER_JOIN);
            mJoins.add(join);
            return join;
        }

        public String toCountSql() {
            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) ");
            addFrom(sql);
            addJoins(sql);
            addWhere(sql);
            addGroupBy(sql);
            addHaving(sql);
            addLimit(sql);
            addOffset(sql);
            return sqlString(sql);
        }

        public String toExistsSql() {
            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT EXISTS(SELECT 1 ");
            addFrom(sql);
            addJoins(sql);
            addWhere(sql);
            addGroupBy(sql);
            addHaving(sql);
            addLimit(sql);
            addOffset(sql);
            sql.append(")");
            return sqlString(sql);
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
            return sqlString(sql);
        }

        private SelectQueryData createQueryData() {
            SelectQueryData data = new SelectQueryData();
            data.setColumns(mColumns);
            data.setDistinct(mDistinct);
            StringBuilder sql = new StringBuilder();
            sql.append(MonoTalk.getTableName(mType)).append(" ");
            if (mAlias != null) {
                sql.append("AS ");
                sql.append(mAlias);
                sql.append(" ");
            }
            data.setTableName(sql.toString());
            data.setSelectionArgs(selection.getSelectionArgs());
            data.setWhere(selection.getSelection());
            data.setGroupBy(mGroupBy);
            data.setHaving(mHaving);
            for (final Join<T> join : mJoins) {
                data.appendJoinString(join.toSql());
            }
            data.setOrderBy(mOrderBy);
            data.setLimit(mLimit);
            data.setOffSet(mOffset);
            return data;
        }

        private void addGroupBy(final StringBuilder sql) {
            if (mGroupBy != null) {
                sql.append("GROUP BY ");
                sql.append(mGroupBy);
                sql.append(" ");
            }
        }

        private void addHaving(final StringBuilder sql) {
            if (mHaving != null) {
                sql.append("HAVING ");
                sql.append(mHaving);
                sql.append(" ");
            }
        }

        private void addJoins(final StringBuilder sql) {
            for (final Join<T> join : mJoins) {
                sql.append(join.toSql());
            }
        }

        private void addLimit(final StringBuilder sql) {
            if (mLimit != null) {
                sql.append("LIMIT ");
                sql.append(mLimit);
                sql.append(" ");
            }
        }

        private void addOffset(final StringBuilder sql) {
            if (mOffset != null) {
                sql.append("OFFSET ");
                sql.append(mOffset);
                sql.append(" ");
            }
        }

        private void addOrderBy(final StringBuilder sql) {
            if (mOrderBy != null) {
                sql.append("ORDER BY ");
                sql.append(mOrderBy);
                sql.append(" ");
            }
        }

        /**
         * CursorLoaderを生成して返す
         *
         * @return
         */
        @Override
        public CursorLoader buildLoader() {
            SelectQueryData data = createQueryData();
            return mModelManager.buildLoader(data);
        }

        @SuppressWarnings("unchecked")
        @Override
        public T selectOne() {
            SelectQueryData data = createQueryData();
            data.setLimit("1");
            return mModelManager.selectOne(mType, data);
        }

        @Override
        public Cursor selectCursor() {
            SelectQueryData data = createQueryData();
            return mModelManager.selectCursor(data);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<T> selectList() {
            SelectQueryData data = createQueryData();
            return mModelManager.selectList(mType, data);
        }

        @Override
        public <E> E selectOne(RowMapper<E> mapper) {
            SelectQueryData data = createQueryData();
            data.setLimit("1");
            return mModelManager.selectOne(mapper, data);
        }

        @Override
        public <E> List<E> selectList(RowMapper<E> mapper) {
            SelectQueryData data = createQueryData();
            return (List<E>) mModelManager.selectList(mapper, data);
        }

        @Override
        public <E> E selectScalar(Class<E> clazz) {
            SelectQueryData data = createQueryData();
            return mModelManager.selectScalar(clazz, data);
        }
    }

    public class Join<T extends Entity> implements Sqlable {
        @SuppressLint("RtlHardcoded")
        private From<T> mFrom;
        private Class<? extends Entity> mType;
        private String mAlias;
        private JoinType mJoinType;
        private String mOn;
        private String[] mUsing;

        Join(From<T> from, Class<? extends Entity> table, JoinType joinType) {
            mFrom = from;
            mType = table;
            mJoinType = joinType;
        }

        public Join<T> as(String alias) {
            mAlias = alias;
            return this;
        }

        public From<T> on(String on) {
            mOn = on;
            return mFrom;
        }

        public From<T> on(String on, Object... args) {
            mOn = on;
            mFrom.addArguments(args);
            return mFrom;
        }

        public From<T> using(String... columns) {
            mUsing = columns;
            return mFrom;
        }

        @Override
        public String toSql() {
            StringBuilder sql = new StringBuilder();
            if (mJoinType != null) {
                sql.append(mJoinType.toSql()).append(" ");
            }
            sql.append(MonoTalk.getTableName(mType));
            sql.append(" ");
            if (mAlias != null) {
                sql.append("AS ");
                sql.append(mAlias);
                sql.append(" ");
            }
            if (mOn != null) {
                sql.append("ON ");
                sql.append(mOn);
                sql.append(" ");
            } else if (mUsing != null) {
                sql.append("USING (");
                sql.append(TextUtils.join(", ", mUsing));
                sql.append(") ");
            }
            return sql.toString();
        }
    }
}