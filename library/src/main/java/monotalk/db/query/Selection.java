package monotalk.db.query;

import java.util.ArrayList;
import java.util.List;

public class Selection extends AbstractSelection {
    private StringBuilder selection = null;
    private List<Object> selectionArgs = null;

    public Selection() {
        selection = new StringBuilder();
        selectionArgs = new ArrayList<Object>();
    }

    protected void setSelectionArgs(Object... args) {
        selectionArgs = new ArrayList<Object>();
        for (Object arg : args) {
            selectionArgs.add(arg);
        }
    }

    protected void setSelection(String string) {
        selection = new StringBuilder();
        selection.append(string);
    }

    protected void addArguments(Object... args) {
        for (Object arg : args) {
            selectionArgs.add(arg);
        }
    }

    protected void addSelection(String string) {
        selection.append(string);
    }

    public Expressions<Selection> and(String clause) {
        return where(clause);
    }

    public String getSelection() {
        return selection.toString();
    }

    public Object[] getSelectionArgs() {
        return selectionArgs.toArray(new Object[selectionArgs.size()]);
    }

    public String[] getSelectionStringArgs() {
        return QueryUtils.toStirngArrayArgs(selectionArgs.toArray(new Object[selectionArgs.size()]));
    }

    public Expressions<Selection> or(String clause) {
        appendOrCondition(clause);
        return new Expressions<Selection>(this);
    }

    public Selection or(String clause, Object[] args) {
        appendOrCondition(clause);
        addArguments(args);
        return this;
    }

    public Expressions<Selection> where(String clause) {
        appendAndCondition(clause);
        return new Expressions<Selection>(this);
    }

    public Selection where(String clause, Object... args) {
        appendAndCondition(clause);
        addArguments(args);
        return this;
    }

    private void appendAndCondition(String clause) {
        // Chain conditions if a previous condition exists.
        if (selection.length() > 0) {
            selection.append(" AND ");
        }
        selection.append(clause);
    }

    private void appendOrCondition(String clause) {
        if (selection.length() > 0) {
            selection.append(" OR ");
        }
        selection.append(clause);
    }
}
