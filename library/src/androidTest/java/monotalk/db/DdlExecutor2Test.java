package monotalk.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowLog;

import monotalk.db.exception.IllegalAnnotationStateException;
import monotalk.db.rules.LogRule;
import monotalk.db.shadows.PersistentShadowSQLiteOpenHelper;

import static junit.framework.Assert.fail;

@Config(emulateSdk = 18, shadows = {PersistentShadowSQLiteOpenHelper.class},
        manifest = "../library/src/androidTest/java/monotalk/db/DdlExecutor2Test_AndroidManifest.xml"
)
@RunWith(RobolectricTestRunner.class)
public class DdlExecutor2Test {

    @Rule
    public LogRule log = new LogRule(DdlExecutor2Test.class);

    @After
    public void tearDown() throws Exception {
    }

    // ----------------------------------------------------------------
    // Test for Id Annotation
    // ----------------------------------------------------------------
    @Test
    public void columnStringCannotBeAutoIncrementIdColumn() {
        try {
            ContentProvider contentProvider = new TestContentProvider2();
            ShadowLog.stream = System.out;
            ContentResolver contentResolver = Robolectric.application.getContentResolver();
            Robolectric.shadowOf(contentResolver);
            ShadowContentResolver.registerProvider("monotalk.db", contentProvider);
            contentProvider.onCreate();
            SQLiteDatabase db = MonoTalk.getDbHelperByDefaultDbName().getWritableDatabase();
            MonoTalk.getDbHelperByDefaultDbName().onUpgrade(db, 0, 0);
            fail("ここには到達しないはず");
        } catch (RuntimeException e) {
            if (!(e instanceof IllegalAnnotationStateException)) {
                fail("IllegalAnnotationStateExceptionが発生するはず");
            }
        }
    }
}