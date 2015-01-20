package monotalk.db.querydata;

import android.content.ContentValues;

import monotalk.db.query.Sqlable;

public class UpdateQueryData extends DeleteQueryData implements Sqlable {

    private ContentValues values;

    @Override
    public String toSql() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    public ContentValues getValues() {
        return values;
    }

    public void setValues(ContentValues values) {
        this.values = values;
    }

}
