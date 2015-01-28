package monotalk.db.manager;

import android.database.sqlite.SQLiteOpenHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import monotalk.db.DatabaseConnectionSource;
import monotalk.db.exception.ConnectionSourceNotFoundException;

import static monotalk.db.utility.AssertUtils.assertNotNull;

/**
 * Created by Kem on 2015/01/16.
 */
public class DBNameMappedEntityManagerFactory implements EntityManagerFactory {

    private String defaultDatabaseName;
    private Map<String, DatabaseConnectionSource> databaseConnectionMap = new ConcurrentHashMap<String, DatabaseConnectionSource>();

    public SQLiteOpenHelper getSQLiteOpenHelper() {
        assertNotNull(defaultDatabaseName, "defaultDatabaseName is null");
        return databaseConnectionMap.get(defaultDatabaseName).getDbHelper();
    }

    public SQLiteOpenHelper getSQLiteOpenHelper(String databaseName) {
        assertNotNull(databaseName, "databaseName is null");
        return databaseConnectionMap.get(databaseName).getDbHelper();
    }

    @Override
    public EntityManager newEntityManager(String name, EntityManagerType type) {
        DatabaseConnectionSource connectionSource = databaseConnectionMap.get(name);
        if (connectionSource == null) {
            throw new ConnectionSourceNotFoundException(name);
        }
        switch (type) {
            case DB_OPEN_HELPER:
                DBOpenHelperEntityManager entityManager = DBOpenHelperEntityManager.newInstance(connectionSource);
                return entityManager;
            default:
                throw new UnsupportedOperationException("Not Support Entity Type... Type = [" + type + "]");
        }
    }

    @Override
    public EntityManager newEntityManager(EntityManagerType type) {
        assertNotNull(defaultDatabaseName, "defaultDatabaseName is null");
        return newEntityManager(defaultDatabaseName, type);
    }

    public void registerDatabaseConnectionSource(String name, DatabaseConnectionSource connectionSource) {
        databaseConnectionMap.put(name, connectionSource);
    }

    public boolean isAlreadyRegistered(String name) {
        return databaseConnectionMap.containsKey(name);
    }

    public void setDefaultDatabaseName(String name) {
        defaultDatabaseName = name;
    }
}
