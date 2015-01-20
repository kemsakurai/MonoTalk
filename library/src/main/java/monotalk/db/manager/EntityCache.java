package monotalk.db.manager;

import monotalk.db.Entity;

/**
 * Created by kem on 2015/01/17.
 */
public interface EntityCache {
    public <T extends Entity> T getEntityOrSelect(Class<T> entityType, long entityId);

    public void evictAllEntity();
}
