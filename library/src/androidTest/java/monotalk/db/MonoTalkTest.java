package monotalk.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import monotalk.db.shadows.PersistentShadowSQLiteOpenHelper;

@Config(emulateSdk = 18, shadows = {PersistentShadowSQLiteOpenHelper.class})
@RunWith(RobolectricTestRunner.class)
public class MonoTalkTest {
    @Test
    public void initFromApplication() {
//        MonoTalk
    }
}