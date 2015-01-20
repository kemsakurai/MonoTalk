package monotalk.db.manager;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

interface ProviderCrudHandler {

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder);

    public int delete(Uri url, String selection, String[] selectionArgs);

    public int update(Uri url, ContentValues values, String selection, String[] selectionArgs);

    public Uri insert(Uri url, ContentValues initialValues);

    public int bulkInsert(Uri uri, ContentValues[] initialValues);

    public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations);

    public void release();
}
