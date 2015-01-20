package monotalk.db.manager;

/**
 * Created by Kem on 2015/01/16.
 */
public interface EntityManagerFactory {
    public EntityManager newEntityManager(String name, EntityManagerType type);

    public EntityManager newEntityManager(EntityManagerType type);
}
