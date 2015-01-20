package monotalk.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowLog;

import monotalk.db.manager.EntityManager;
import monotalk.db.manager.EntityManagerType;
import monotalk.db.rules.LogRule;
import monotalk.db.shadows.PersistentShadowSQLiteOpenHelper;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@Config(emulateSdk = 18, shadows = {PersistentShadowSQLiteOpenHelper.class})
@RunWith(RobolectricTestRunner.class)
public class DdlExecutor1Test {

    @Rule
    public LogRule log = new LogRule(DdlExecutor1Test.class);

    @Before
    public void before() {
        ContentProvider contentProvider = new TestContentProvider1();
        ShadowLog.stream = System.out;
        ContentResolver contentResolver = Robolectric.application.getContentResolver();
        Robolectric.shadowOf(contentResolver);
        ShadowContentResolver.registerProvider("monotalk.db", contentProvider);
        contentProvider.onCreate();
        SQLiteOpenHelper dbHelper = MonoTalk.getDbHelperByDbName("Sample");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db, 0, 0);
        EntityManager manager = MonoTalk.getManagerByDefaultAuth(EntityManagerType.DB_OPEN_HELPER);
        manager.deleteAll(TestModel3.class);
        manager.deleteAll(TestModel2.class);
        manager.deleteAll(TestModel1.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    // ----------------------------------------------------------------
    // Test for Column Annotation
    // ----------------------------------------------------------------
    @Test
    public void columnString1ValueShouldBeDEFAULT() {
        EntityManager manager = MonoTalk.getManagerByDefaultAuth(EntityManagerType.DB_OPEN_HELPER);
        TestModel3 model31 = new TestModel3();
        model31.setId(111l);
        model31.columnString1 = null;
        model31.columnString2 = "columnString2";
        model31.columnString3 = "columnString3";
        manager.insertExcludesNull(model31);

        TestModel3 model32 = manager.selectOneById(TestModel3.class, 111l);
        assertThat(model32.columnString1, is("DEFAULT"));
    }

    @Test
    public void columnBoolean1ValueShouldBeTrue() {
        EntityManager manager = MonoTalk.getManagerByDefaultAuth(EntityManagerType.DB_OPEN_HELPER);
        TestModel3 model31 = new TestModel3();
        model31.setId(111l);
        model31.columnString1 = null;
        model31.columnString2 = "columnString2";
        model31.columnString3 = "columnString3";
        model31.columnBoolean1 = null;
        manager.insertExcludesNull(model31);

        TestModel3 model32 = manager.selectOneById(TestModel3.class, 111l);
        assertThat(model32.columnBoolean1, is(true));
    }

    @Test
    public void columnBoolean2ValueShouldBeFalse() {
        EntityManager manager = MonoTalk.getManagerByDefaultAuth(EntityManagerType.DB_OPEN_HELPER);
        TestModel3 model31 = new TestModel3();
        model31.setId(111l);
        model31.columnString1 = null;
        model31.columnString2 = "columnString2";
        model31.columnString3 = "columnString3";
        model31.columnBoolean2 = null;
        manager.insertExcludesNull(model31);

        TestModel3 model32 = manager.selectOneById(TestModel3.class, 111l);
        assertThat(model32.columnBoolean2, is(false));
    }

    @Test
    public void columnBoolean3IsUniqueAndIsDefalutValueTrue() {
        EntityManager manager = MonoTalk.getManagerByDefaultAuth(EntityManagerType.DB_OPEN_HELPER);
        TestModel3 model31 = new TestModel3();
        model31.setId(111l);
        model31.columnString1 = null;
        model31.columnString2 = "columnString2";
        model31.columnString3 = "columnString3";
        model31.columnBoolean3 = null;
        manager.insertExcludesNull(model31);

        model31.setId(null);
        model31.columnString1 = null;
        model31.columnString2 = "columnString2";
        model31.columnString3 = "columnString3";
        model31.columnBoolean3 = null;
        try {
            manager.insertExcludesNull(model31);
            fail("ここには到達しないはず");
        } catch (RuntimeException e) {
        }
        TestModel3 model32 = manager.selectOneById(TestModel3.class, 111l);
        assertThat(model32.columnBoolean3, is(false));
    }

    @Test
    public void columnString2CannotInsertNull() {
        EntityManager manager = MonoTalk.getManagerByDefaultAuth(EntityManagerType.DB_OPEN_HELPER);
        TestModel3 model31 = new TestModel3();
        model31.setId(111l);
        model31.columnString1 = null;
        model31.columnString2 = null;
        model31.columnString3 = "columnString3";
        try {
            manager.insertExcludesNull(model31);
            fail("ここには到達しないはず");
        } catch (RuntimeException e) {
        }

        TestModel3 model32 = manager.selectOneById(TestModel3.class, 111l);
        assertThat(model32, nullValue());
    }
}