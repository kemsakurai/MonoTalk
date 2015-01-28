package monotalk.db.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import monotalk.db.DatabaseProviderConnectionSource;
import monotalk.db.MonoTalk;
import monotalk.db.exception.ConnectionSourceNotFoundException;

import static monotalk.db.utility.AssertUtils.assertNotNull;

/**
 * Created by Kem on 2015/01/16.
 */
public class AuthNameMappedEntityManagerFactory implements EntityManagerFactory {

    private String defaultAuthority;
    private Map<String, DatabaseProviderConnectionSource> provierConnectionMap = new ConcurrentHashMap<String, DatabaseProviderConnectionSource>();

    @Override
    public EntityManager newEntityManager(String name, EntityManagerType type) {
        DatabaseProviderConnectionSource providerSource = provierConnectionMap.get(name);
        if (providerSource == null) {
            throw new ConnectionSourceNotFoundException(name);
        }
        EntityManager entityManager = null;
        switch (type) {
            case DB_OPEN_HELPER:
                return MonoTalk.getDBManagerByDbName(providerSource.getDatabaseName());
            case CONTENTES_PROVIER:
                entityManager = DBContentsProviderEntityManager.newInstance(providerSource, MonoTalk.getContext());
                return entityManager;
            case PROVIER_CLIENT:
                entityManager = DBProviderClientEntityManager.newInstance(providerSource, MonoTalk.getContext());
                return entityManager;
            default:
                throw new UnsupportedOperationException("Not Support Entity Type... Type = [" + type + "]");
        }
    }

    @Override
    public EntityManager newEntityManager(EntityManagerType type) {
        assertNotNull(defaultAuthority, "defaultAuthority is null");
        return newEntityManager(defaultAuthority, type);
    }

    public void registerProvierConnectionSource(String name, DatabaseProviderConnectionSource connectionSource) {
        provierConnectionMap.put(name, connectionSource);
    }

    public boolean isAlreadyRegistered(String name) {
        return provierConnectionMap.containsKey(name);
    }

    public void setDefaultAuthority(String name) {
        defaultAuthority = name;
    }
}
