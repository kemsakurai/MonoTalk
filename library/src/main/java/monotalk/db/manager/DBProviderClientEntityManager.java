package monotalk.db.manager;

import android.content.ContentProviderClient;
import android.content.Context;
import android.support.v4.content.CursorLoader;

import org.seasar.dbflute.cbean.SimpleMapPmb;

import monotalk.db.DatabaseProviderConnectionSource;

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
    public void release() {
        if (crudHandler != null) {
            crudHandler.release();
        }
    }

    @Override
    protected CursorLoader buildLoader(boolean distinct, String tableName, String[] columns, String where, String groupBy,
                                       String having, String orderBy, String limit, Object[] selectionArgs) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    protected CursorLoader buildLoader(String entityPath, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }
}
