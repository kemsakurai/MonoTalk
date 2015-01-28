package monotalk.db.typeconverter;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteProgram;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.ByteArrayOutputStream;

public class BitmapConverter extends BaseTypeConverter<Bitmap> {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Bitmap unpack(Cursor c, int index) {
        byte[] bytes = c.getBlob(index);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            opts.inMutable = true;
        }
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        return bmp;
    }

    @Override
    public void pack(Bitmap object, ContentValues cv, String name) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        object.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        cv.put(name, bytes);
    }

    @Override
    public String toSql(Bitmap object) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        object.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return new String(bytes);
    }

    @Override
    public String toStringBindArg(Bitmap object) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        object.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return new String(bytes);
    }

    @Override
    public SQLiteType getSqlType() {
        return SQLiteType.BLOB;
    }

    @Override
    public void bind(SQLiteProgram program, int index, Bitmap value) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        value.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        program.bindBlob(index, bytes);
    }

}
