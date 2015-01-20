package monotalk.db.querydata;

public class DeleteQueryData extends BaseQueryData {
    /**
     * selectionArgs
     */
    protected Object[] selectionArgs;
    /**
     * Where句
     */
    protected String where;

    public Object[] getSelectionArgs() {
        return selectionArgs;
    }

    public void setSelectionArgs(Object[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }
}
