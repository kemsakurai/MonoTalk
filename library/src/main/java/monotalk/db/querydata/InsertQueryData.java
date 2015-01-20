package monotalk.db.querydata;

import android.content.ContentValues;

public class InsertQueryData extends BaseQueryData {
    /**
     * Value
     */
    protected ContentValues values;

    public ContentValues getValues() {
        return values;
    }

    public void setValues(ContentValues values) {
        this.values = values;
    }
}
