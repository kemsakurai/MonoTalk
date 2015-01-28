package monotalk.db.query;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.Date;

import monotalk.db.DBLog;
import monotalk.db.DatabaseConfigration;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.TestContentProvider1;
import monotalk.db.TestModel1;
import monotalk.db.TestModel2;
import monotalk.db.TestModel3;
import monotalk.db.manager.EntityManager;
import monotalk.db.manager.EntityManagerType;
import monotalk.db.rules.LogRule;
import monotalk.db.shadows.PersistentShadowSQLiteOpenHelper;
import monotalk.db.utility.CursorUtils;

import static monotalk.db.query.QueryUtils.allColumns;
import static monotalk.db.query.QueryUtils.countRowIdAsCount;
import static monotalk.db.query.QueryUtils.idEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@Config(emulateSdk = 18, shadows = {PersistentShadowSQLiteOpenHelper.class})
@RunWith(RobolectricTestRunner.class)
public class QueryBuilderSelectMethodTest {

    @Rule
    public LogRule log = new LogRule(QueryBuilderSelectMethodTest.class);

    @Before
    public void before() {
        ContentProvider contentProvider = new TestContentProvider1();
        ContentResolver contentResolver = Robolectric.application.getContentResolver();
        Robolectric.shadowOf(contentResolver);
        ShadowContentResolver.registerProvider("monotalk.db", contentProvider);
        contentProvider.onCreate();
        SQLiteDatabase db = MonoTalk.getWritableDatabaseByDbName("Sample");
        EntityManager manager = MonoTalk.getManagerByDefaultAuth(EntityManagerType.DB_OPEN_HELPER);
        manager.deleteAll(TestModel3.class);
        manager.deleteAll(TestModel2.class);
        manager.deleteAll(TestModel1.class);
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = 'TEST_TABLE1'");
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = 'TEST_TABLE2'");
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = 'TEST_TABLE3'");
    }

    protected DatabaseConfigration createDatabaseConfigration() {
        DatabaseConfigration.Builder builder = new DatabaseConfigration.Builder();
        builder.setDataBaseName("Sample");
        builder.setVersion(2);
        builder.setDefalutDatabase(true);
        builder.setNodeCacheSize(1000);
        builder.addTable(TestModel1.class);
        builder.addTable(TestModel2.class);
        return builder.create();
    }

    @Test
    public void selectAllColumn() {
        // ============================================================
        // insert data
        // ============================================================
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        TestModel1 model = new TestModel1();
        model.columnLong = 100L;
        model.columnBoolean = true;
        model.columnString = "TEST";
        model.dateColumn = new Date();
        Insert<TestModel1> insert = manager.newInsert(TestModel1.class).values(model);
        long id = insert.execute();

        // ============================================================
        // newSelect data
        // ============================================================
        Select.From<TestModel1> from = manager.newSelect(allColumns(TestModel1.class)).from(TestModel1.class);
        model = from.selectOne();

        // ============================================================
        // verify
        // ============================================================
        assertEquals((long) model.id, id);
    }

    @Test
    public void selectById() {
        // ============================================================
        // insert data
        // ============================================================
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        TestModel1 model = new TestModel1();
        model.columnLong = null;
        model.columnBoolean = false;
        model.columnString = "TEST";
        model.dateColumn = new Date();
        Insert<TestModel1> insert = manager.newInsert(TestModel1.class).values(model);
        long id = insert.execute();
        // ============================================================
        // newSelect data
        // ============================================================
        Select.From<TestModel1> from = manager
                .newSelect(allColumns(TestModel1.class))
                .from(TestModel1.class)
                .where(idEquals(TestModel1.class, id));

        // ============================================================
        // verify
        // ============================================================
        model = from.selectOne();
        assertEquals(model.columnBoolean, false);
    }

    @Test
    public void selectJoin() {
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        // ============================================================
        // insert data
        // ============================================================
        executeInnerJoinTest(manager);
    }

    private void executeInnerJoinTest(EntityManager manager) {
        // ============================================================
        // insert data
        // ============================================================
        insertInnerJoinData();

        // ============================================================
        // newSelect data
        // ============================================================
        Select.From<TestModel2> from = manager
                .newSelect(
                        "TEST_MODEL._id as TEST_MODEL_id",
                        "TEST_MODEL.BooleanColumn as TEST_MODEL_BooleanColumn",
                        "TEST_MODEL2._id as TEST_MODEL2_id",
                        "TEST_MODEL2.BooleanColumn as TEST_MODEL2_BooleanColumn",
                        "TEST_MODEL2.TEST_TABLE_ID as TEST_MODEL2_TEST_TABLE_ID")
                .from(TestModel2.class)
                .as("TEST_MODEL2")
                .innerJoin(TestModel1.class)
                .as("TEST_MODEL")
                .on("TEST_MODEL._id = TEST_MODEL2.TEST_TABLE_ID");
        Cursor cursor = from.selectCursor();

        DBLog.d(DBLog.getTag(getClass()), cursor);

        long model2Id = 0;
        long testTableId = 0;
        if (cursor.moveToFirst()) {
            model2Id = cursor.getLong(cursor.getColumnIndex("TEST_MODEL2_id"));
            testTableId = cursor.getLong(cursor.getColumnIndex("TEST_MODEL2_TEST_TABLE_ID"));
        } else {
            fail();
        }
        // ============================================================
        // verify
        // ============================================================
        assertEquals(model2Id, 1);
        assertEquals(testTableId, 100);
    }

    private void insertInnerJoinData() {
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        TestModel1 model = new TestModel1();
        model.id = 100L;
        model.columnLong = 10000L;
        model.columnBoolean = false;
        model.columnString = "TEST";
        model.dateColumn = new Date();
        Insert<TestModel1> insert = manager
                .newInsert(TestModel1.class)
                .values(model);

        long id = insert.execute();

        TestModel2 model2 = new TestModel2();
        model2.columnBoolean = true;
        model2.testTableId = id;
        model2.columnLong = null;
        model2.columnString = "TEST_TEST";
        model2.dateColumn = new Date(model.dateColumn.getTime() + 1000000);
        manager.newInsert(TestModel2.class).values(model2).execute();
    }

    @Test
    public void selectOuterJoin() {
        // ============================================================
        // insert data
        // ============================================================
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        TestModel2 model2 = new TestModel2();
        model2.columnBoolean = true;
        model2.testTableId = 200l;
        model2.columnLong = null;
        model2.columnString = "TEST_TEST";
        model2.dateColumn = new Date(1000000);
        manager.newInsert(TestModel2.class).values(model2).execute();

        // ============================================================
        // newSelect data
        // ============================================================
        manager = MonoTalk.getDBManagerByDefaultDbName();
        Select.From<? extends Entity> from = manager
                .newSelect(
                        "TEST_MODEL._id as TEST_MODEL_id",
                        "TEST_MODEL.BooleanColumn as TEST_MODEL_BooleanColumn",
                        "TEST_MODEL2._id as TEST_MODEL2_id",
                        "TEST_MODEL2.BooleanColumn as TEST_MODEL2_BooleanColumn",
                        "TEST_MODEL2.TEST_TABLE_ID as TEST_MODEL2_TEST_TABLE_ID")
                .from(TestModel2.class)
                .as("TEST_MODEL2")
                .outerJoin(TestModel1.class)
                .as("TEST_MODEL")
                .on("TEST_MODEL._id = TEST_MODEL2.TEST_TABLE_ID");

        Cursor cursor = from.selectCursor();
        DBLog.d(DBLog.getTag(getClass()), cursor);
        long longColumn = 0;
        long testTableId = 0;
        Boolean booleanColumn = null;
        if (cursor.moveToFirst()) {
            longColumn = cursor.getLong(cursor.getColumnIndex("TEST_MODEL2_id"));
            testTableId = cursor.getLong(cursor.getColumnIndex("TEST_MODEL2_TEST_TABLE_ID"));
            booleanColumn = CursorUtils.getBoolean(cursor, "TEST_MODEL_BooleanColumn");

        } else {
            fail();
        }
        // ============================================================
        // verify
        // ============================================================
        assertEquals(1, longColumn);
        assertEquals(200, testTableId);
        // Boolean Column is Null >>> false
        assertFalse(booleanColumn);
    }

    @Test
    public void selectCount() {
        // ============================================================
        // insert data
        // ============================================================
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        TestModel1 model = new TestModel1();
        model.columnLong = null;
        model.columnBoolean = false;
        model.columnString = "TEST";
        model.dateColumn = new Date();
        Insert<TestModel1> insert = manager.newInsert(TestModel1.class).values(model);

        insert.execute();
        insert.execute();
        // ============================================================
        // newSelect data
        // ============================================================
        Select.From<TestModel1> from = manager.newSelect(countRowIdAsCount()).from(TestModel1.class);

        // ============================================================
        // verify
        // ============================================================
        long count = from.selectScalar(Long.class);
        DBLog.d(DBLog.getTag(getClass()), "SELECT_COUNT_RESULT=[" + count + "]");
    }

    @Test
    public void selectJoinContentsProvider() {
        EntityManager manager = MonoTalk.getManagerByDefaultAuth(EntityManagerType.CONTENTES_PROVIER);
        executeInnerJoinTest(manager);
    }
}