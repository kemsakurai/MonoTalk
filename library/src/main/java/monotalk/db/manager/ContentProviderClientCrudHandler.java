package monotalk.db.manager;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;

import monotalk.db.exception.OperationApplicationRuntimeException;
import monotalk.db.exception.RemoteRuntimeException;

class ContentProviderClientCrudHandler implements ProviderCrudHandler {

    private final ContentProviderClient contentProviderClient;

    ContentProviderClientCrudHandler(ContentProviderClient contentProviderClient) {
        this.contentProviderClient = contentProviderClient;
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            return contentProviderClient.query(url, projection, selection, selectionArgs, sortOrder);
        } catch (RemoteException e) {
            throw new RemoteRuntimeException(e);
        }
    }

    @Override
    public int delete(Uri url, String selection, String[] selectionArgs) {
        try {
            return contentProviderClient.delete(url, selection, selectionArgs);
        } catch (RemoteException e) {
            throw new RemoteRuntimeException(e);
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String selection, String[] selectionArgs) {
        try {
            return contentProviderClient.update(url, values, selection, selectionArgs);
        } catch (RemoteException e) {
            throw new RemoteRuntimeException(e);
        }
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        try {
            return contentProviderClient.insert(url, initialValues);
        } catch (RemoteException e) {
            throw new RemoteRuntimeException(e);
        }

    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] initialValues) {
        try {
            return contentProviderClient.bulkInsert(uri, initialValues);
        } catch (RemoteException e) {
            throw new RemoteRuntimeException(e);
        }
    }

    @Override
    public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) {
        try {
            return contentProviderClient.applyBatch(operations);
        } catch (RemoteException e) {
            throw new RemoteRuntimeException(e);
        } catch (OperationApplicationException e) {
            throw new OperationApplicationRuntimeException(e);
        }
    }

    @Override
    public void release() {
        contentProviderClient.release();
    }
}
