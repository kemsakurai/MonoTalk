package monotalk.db.typeconverter;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;
import android.os.Build;

public class ByteArrayConverter extends BaseTypeConverter<byte[]> {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public byte[] unpack(Cursor c, int index) {
        byte[] bytes = c.getBlob(index);
        return bytes;
    }

    @Override
    public void pack(byte[] object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(byte[] object) {
        return new String(object);
    }

    @Override
    public String toStringBindArg(byte[] object) {
        return new String(object);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.BLOB;
    }

    @Override
    public void bind(SQLiteProgram program, int index, byte[] value) {
        program.bindBlob(index, value);
    }

}
