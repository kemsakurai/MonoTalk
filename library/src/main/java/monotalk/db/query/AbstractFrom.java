package monotalk.db.query;

import android.text.TextUtils;

import monotalk.db.DBLog;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;

abstract public class AbstractFrom<T extends Entity, M extends AbstractSelection> extends
        AbstractSelection implements Sqlable {

    private static final String TAG_NAME = DBLog.getTag(AbstractFrom.class);

    protected Sqlable baseDmlQuery;
    protected Selection selection;
    protected String mAlias;
    protected Class<T> mType;

    AbstractFrom(Class<T> table, Sqlable queryBase) {
        mType = table;
        baseDmlQuery = queryBase;
        selection = new Selection();
    }

    @Override
    protected void addArguments(Object... args) {
        selection.addArguments(args);
    }

    @Override
    protected void addSelection(String string) {
        selection.addSelection(string);
    }

    public Expressions<M> and(String clause) {
        return where(clause);
    }

    public M and(String clause, Object... args) {
        return where(clause, args);
    }

    @SuppressWarnings("unchecked")
    public M as(String alias) {
        mAlias = alias;
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    public Expressions<M> or(String clause) {
        selection.or(clause);
        return new Expressions<M>((M) this);
    }

    @SuppressWarnings("unchecked")
    public M or(String clause, Object... args) {
        selection.or(clause, args);
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    public M where(Selection selection) {
        this.selection = selection;
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    public Expressions<M> where(String clause) {
        selection.where(clause);
        return new Expressions<M>((M) this);
    }

    @SuppressWarnings("unchecked")
    public M where(String clause, Object... args) {
        selection.where(clause, args);
        return (M) this;
    }

    protected void addQueryBase(final StringBuilder sql) {
        sql.append(baseDmlQuery.toSql());
    }

    protected void addWhere(final StringBuilder sql) {
        sql.append(selection.toSql());
    }

    protected String sqlString(final StringBuilder sql) {
        final String sqlString = sql.toString().trim();
        DBLog.d(TAG_NAME, sqlString + " " + TextUtils.join(",", selection.getSelectionArgs()));
        return sqlString;
    }

    protected void addFrom(final StringBuilder sql) {
        sql.append("FROM ");
        sql.append(MonoTalk.getTableName(mType)).append(" ");
        if (mAlias != null) {
            sql.append("AS ");
            sql.append(mAlias);
            sql.append(" ");
        }
    }
}
