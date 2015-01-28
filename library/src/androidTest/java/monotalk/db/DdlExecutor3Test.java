package monotalk.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.sqlite.SQLiteException;

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

import java.util.List;

import monotalk.db.manager.EntityManager;
import monotalk.db.rules.LogRule;
import monotalk.db.shadows.PersistentShadowSQLiteOpenHelper;

import static junit.framework.Assert.fail;
import static monotalk.db.query.QueryUtils.allColumns;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Config(emulateSdk = 18, shadows = {PersistentShadowSQLiteOpenHelper.class})
@RunWith(RobolectricTestRunner.class)
public class DdlExecutor3Test {

    @Rule
    public LogRule log = new LogRule(DdlExecutor3Test.class);

    @Before
    public void before() {
        MonoTalk.dispose();
        ContentProvider contentProvider = new JoinTableTestContentProvider();
        ShadowLog.stream = System.out;
        ContentResolver contentResolver = Robolectric.application.getContentResolver();
        Robolectric.shadowOf(contentResolver);
        ShadowContentResolver.registerProvider("monotalk.db.joinTest", contentProvider);
        contentProvider.onCreate();
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        manager.deleteAll(JoinTableTestModel2.class);
        manager.deleteAll(JoinTableTestModel3.class);
        manager.deleteAll(JoinTableTestModel4.class);
        manager.deleteAll(JoinTableTestModel1.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void insetOnlyChildShoudBeError() {
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        JoinTableTestModel2 model2 = new JoinTableTestModel2();
        model2.id = 111l;
        model2.testTableId = 222l;
        try {
            manager.insertExcludesNull(model2);
            fail("外部キー制約エラーとなるため、ここには到達しないはず。");
        } catch (SQLiteException e) {
            assertThat(e.getMessage(), is("Cannot execute for last inserted row ID, base error code: 19"));
        }

        JoinTableTestModel3 model3 = new JoinTableTestModel3();
        model3.id = 111l;
        model3.testTableId = 222l;
        try {
            manager.insertExcludesNull(model3);
            fail("外部キー制約エラーとなるため、ここには到達しないはず。");
        } catch (SQLiteException e) {
            assertThat(e.getMessage(), is("Cannot execute for last inserted row ID, base error code: 19"));
        }
    }

    @Test
    public void deleteChildTableData() {
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        JoinTableTestModel1 model1 = new JoinTableTestModel1();
        model1.columnString = "TEST";
        long id = manager.insertExcludesNull(model1);
        JoinTableTestModel2 model2 = new JoinTableTestModel2();
        model2.testTableId = id;
        manager.insertExcludesNull(model2);
        JoinTableTestModel3 model3 = new JoinTableTestModel3();
        model3.testTableId = id;
        manager.insertExcludesNull(model3);
        try {
            manager.deleteAll(model1.getClass());
            fail("外部キー制約エラーとなるため、ここには到達しないはず。");
        } catch (SQLiteException e) {
//            assertThat(e.getMessage(), is("Cannot execute for changed row count, base error code: 19"));
            assertThat(e.getMessage(), is("Cannot execute, base error code: 19"));
        }
        manager.deleteAll(model2.getClass());
        manager.deleteAll(model1.getClass());

        assertThat("TABLE1 count should be 0", manager.selectCount(model1.getClass()), is(0l));
        assertThat("TABLE1 count should be 0", manager.selectCount(model2.getClass()), is(0l));
        assertThat("TABLE1 count should be 0", manager.selectCount(model3.getClass()), is(0l));
    }

    @Test
    public void deleteChildTableDataWithModel() {
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        JoinTableTestModel1 model1 = new JoinTableTestModel1();
        model1.columnString = "TEST";
        long id = manager.insertExcludesNull(model1);
        JoinTableTestModel4 model4 = new JoinTableTestModel4();
        model4.model1 = model1;
        manager.insertExcludesNull(model4);
        manager.deleteAll(model1.getClass());
        assertThat("TABLE1 count should be 0", manager.selectCount(model1.getClass()), is(0l));
        assertThat("TABLE1 count should be 0", manager.selectCount(model4.getClass()), is(0l));
    }

    @Test
    public void entityCacheTest() {
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        JoinTableTestModel1 model11 = new JoinTableTestModel1();
        model11.columnString = "TEST11";
        long id = manager.insertExcludesNull(model11);
        JoinTableTestModel4 model41 = new JoinTableTestModel4();
        model41.model1 = model11;
        for (int i = 0; i < 2000; i++) {
            manager.insert(model41);
            model41.id = null;
        }

        JoinTableTestModel1 model12 = new JoinTableTestModel1();
        model12.columnString = "TEST12";
        id = manager.insertExcludesNull(model12);
        JoinTableTestModel4 model42 = new JoinTableTestModel4();
        model42.model1 = model12;
        for (int i = 0; i < 2000; i++) {
            manager.insert(model42);
            model42.id = null;
        }

        List<JoinTableTestModel4> model4s = manager
                .newSelect(allColumns(JoinTableTestModel4.class))
                .from(JoinTableTestModel4.class)
                .where("TEST_TABLE_ID").eq(id)
                .selectList();

        JoinTableTestModel4 previous = null;
        for (JoinTableTestModel4 model4 : model4s) {
            if (previous != null) {
                assertThat(previous.model1, is(model4.model1));
            }
            previous = model4;
        }
    }
}