package monotalk.db.query;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import monotalk.db.DBLog;
import monotalk.db.DBLog.LogLevel;
import monotalk.db.DatabaseConfigration;
import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.TestModel1;
import monotalk.db.TestModel2;
import monotalk.db.manager.EntityManager;
import monotalk.db.rules.LogRule;
import monotalk.db.shadows.PersistentShadowSQLiteOpenHelper;
import monotalk.db.utility.CursorUtils;

import static monotalk.db.query.QueryBuilder.allColumns;
import static monotalk.db.query.QueryBuilder.countRowIdAsCount;
import static monotalk.db.query.QueryBuilder.idEquals;
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
        MonoTalk.dispose();
        MonoTalk.init(Robolectric.application.getApplicationContext(), LogLevel.VERBOSE);
        try {
            String authority = "test";
            DatabaseConfigration config = createDatabaseConfigration(authority);
            MonoTalk.registerDatabaseConnectionSource(config);
            SQLiteDatabase db = MonoTalk.getDbHelperByDefaultDbName().getWritableDatabase();
            MonoTalk.getDbHelperByDefaultDbName().onUpgrade(db, 0, 0);
        } catch (Exception e) {
            // Do Nothing...
        }
        EntityManager manager = MonoTalk.getDBHelperManagerByDefaultDbName();
        manager.deleteAll(TestModel1.class);
        manager.deleteAll(TestModel2.class);
        SQLiteDatabase db = MonoTalk.getDbHelperByDefaultDbName().getWritableDatabase();
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = 'TEST_TABLE1'");
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = 'TEST_TABLE2'");

    }

    protected DatabaseConfigration createDatabaseConfigration(String authority) {
        DatabaseConfigration.Builder builder = new DatabaseConfigration.Builder();
        builder.setDataBaseName("Sample");
        builder.setVersion(2);
        builder.setDefalutDatabase(true);
        builder.setNodeCacheSize(1000);
        builder.setTableCacheSize(1000);
        builder.addTable(TestModel1.class);
        builder.addTable(TestModel2.class);
        return builder.create();
    }

    @Test
    public void selectAllColumn() {
        // ============================================================
        // insert data
        // ============================================================
        EntityManager manager = MonoTalk.getDBHelperManagerByDefaultDbName();
        TestModel1 model = new TestModel1();
        model.columnLong = 100L;
        model.columnBoolean = true;
        model.columnString = "TEST";
        model.dateColumn = new Date();
        Insert<TestModel1> insert = manager.newInsertInto(TestModel1.class).values(model);
        DBLog.d(DBLog.getTag(getClass()), "INSERT_SQL=[" + insert.toSql() + "]");
        long id = insert.execute();

        // ============================================================
        // newSelect data
        // ============================================================
        Select.From<TestModel1> from = manager.newSelect(allColumns(TestModel1.class)).from(TestModel1.class);
        DBLog.d(DBLog.getTag(getClass()), "SELECT_SQL=[" + from.toSql() + "]");
        model = from.selectOne();

        // ============================================================
        // verify
        // ============================================================
        assertEquals((long) model.getId(), id);
    }

    @Test
    public void selectById() {
        // ============================================================
        // insert data
        // ============================================================
        EntityManager manager = MonoTalk.getDBHelperManagerByDefaultDbName();
        TestModel1 model = new TestModel1();
        model.columnLong = null;
        model.columnBoolean = false;
        model.columnString = "TEST";
        model.dateColumn = new Date();
        Insert<TestModel1> insert = manager.newInsertInto(TestModel1.class).values(model);
        DBLog.d(DBLog.getTag(getClass()), "INSERT_SQL=[" + insert.toSql() + "]");
        long id = insert.execute();
        // ============================================================
        // newSelect data
        // ============================================================
        Select.From<TestModel1> from = manager
                .newSelect(allColumns(TestModel1.class))
                .from(TestModel1.class)
                .where(idEquals(TestModel1.class, id));
        DBLog.d(DBLog.getTag(getClass()), "SELECT_SQL=[" + from.toSql() + "]");

        // ============================================================
        // verify
        // ============================================================
        model = from.selectOne();
        assertEquals(model.columnBoolean, false);
    }

    @Test
    public void selectJoin() {
        // ============================================================
        // insert data
        // ============================================================
        EntityManager manager = MonoTalk.getDBHelperManagerByDefaultDbName();
        TestModel1 model = new TestModel1();
        model.setId(100L);
        model.columnLong = 10000L;
        model.columnBoolean = false;
        model.columnString = "TEST";
        model.dateColumn = new Date();
        Insert<TestModel1> insert = manager
                .newInsertInto(TestModel1.class)
                .values(model);

        DBLog.d(DBLog.getTag(getClass()), "INSERT_SQL=[" + insert.toSql() + "]");
        long id = insert.execute();

        TestModel2 model2 = new TestModel2();
        model2.columnBoolean = true;
        model2.testTableId = id;
        model2.columnLong = null;
        model2.columnString = "TEST_TEST";
        model2.dateColumn = new Date(model.dateColumn.getTime() + 1000000);
        manager.newInsertInto(TestModel2.class).values(model2).execute();

        // ============================================================
        // newSelect data
        // ============================================================
        manager = MonoTalk.getDBHelperManagerByDefaultDbName();
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

    @Test
    public void selectOuterJoin() {
        // ============================================================
        // insert data
        // ============================================================
        EntityManager manager = MonoTalk.getDBHelperManagerByDefaultDbName();
        TestModel2 model2 = new TestModel2();
        model2.columnBoolean = true;
        model2.testTableId = 200l;
        model2.columnLong = null;
        model2.columnString = "TEST_TEST";
        model2.dateColumn = new Date(1000000);
        manager.newInsertInto(TestModel2.class).values(model2).execute();

        // ============================================================
        // newSelect data
        // ============================================================
        manager = MonoTalk.getDBHelperManagerByDefaultDbName();
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
        EntityManager manager = MonoTalk.getDBHelperManagerByDefaultDbName();
        TestModel1 model = new TestModel1();
        model.columnLong = null;
        model.columnBoolean = false;
        model.columnString = "TEST";
        model.dateColumn = new Date();
        Insert<TestModel1> insert = manager.newInsertInto(TestModel1.class).values(model);

        DBLog.d(DBLog.getTag(getClass()), "INSERT_SQL=[" + insert.toSql() + "]");
        insert.execute();
        insert.execute();
        // ============================================================
        // newSelect data
        // ============================================================
        Select.From<TestModel1> from = manager.newSelect(countRowIdAsCount()).from(TestModel1.class);
        DBLog.d(DBLog.getTag(getClass()), "SELECT_SQL=[" + from.toSql() + "]");

        // ============================================================
        // verify
        // ============================================================
        long count = from.selectScalar(Long.class);
        DBLog.d(DBLog.getTag(getClass()), "SELECT_COUNT_RESULT=[" + count + "]");
    }
}