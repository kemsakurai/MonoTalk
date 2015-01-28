package monotalk.db.manager;

import android.content.ContentProviderClient;
import android.content.Context;
import android.support.v4.content.CursorLoader;

import monotalk.db.DatabaseProviderConnectionSource;
import monotalk.db.querydata.SelectQueryData;
import monotalk.db.querydata.TwoWayQueryData;

public class DBProviderClientEntityManager extends DBContentsProviderEntityManager {

    /**
     * Constructor
     *
     * @param connectionSource
     * @param context
     */
    DBProviderClientEntityManager(DatabaseProviderConnectionSource connectionSource, Context context) {
        super(connectionSource, context);
    }

    /**
     * CrudHandlerを返す
     */
    @Override
    protected ProviderCrudHandler newCrudHandler() {
        ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(mAuthorityUri);
        return new ContentProviderClientCrudHandler(client);
    }

    public static DBProviderClientEntityManager newInstance(DatabaseProviderConnectionSource connectionSource, Context context) {
        return new DBProviderClientEntityManager(connectionSource, context);
    }

    @Override
    public CursorLoader buildLoader(SelectQueryData data) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public CursorLoader buildLoader(TwoWayQueryData data) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public void release() {
        if (crudHandler != null) {
            crudHandler.release();
        }
    }
}
