package monotalk.db.manager;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import monotalk.db.MonoTalk;
import monotalk.db.shadows.PersistentShadowSQLiteOpenHelper;


/**
 * Created by Kem on 2015/01/10.
 */
@Config(emulateSdk = 18, shadows = {PersistentShadowSQLiteOpenHelper.class})
@RunWith(RobolectricTestRunner.class)
public class DBProviderClientEntityManagerTest extends BaseEntityManagerTest {

    @Override
    protected void teardown() {
        MonoTalk.getManagerByDefaultAuth(EntityManagerType.PROVIER_CLIENT).release();
    }

    @Override
    protected EntityManager getEntityManager() {
        return MonoTalk.getManagerByDefaultAuth(EntityManagerType.PROVIER_CLIENT);
    }
}
