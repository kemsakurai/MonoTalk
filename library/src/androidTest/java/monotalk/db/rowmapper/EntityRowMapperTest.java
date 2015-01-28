package monotalk.db.rowmapper;

import android.database.Cursor;
import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import monotalk.db.DBLog.LogLevel;
import monotalk.db.compat.DatabaseCompat;
import monotalk.db.DatabaseConfigration;
import monotalk.db.MonoTalk;
import monotalk.db.TestModel1;
import monotalk.db.rules.LogRule;

import static org.junit.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class EntityRowMapperTest {

    @Rule
    public LogRule log = new LogRule(EntityRowMapperTest.class);

    @Before
    public void setUp() {
        MonoTalk.dispose();
        String authority = "test";
        DatabaseConfigration config = createDatabaseConfigration();
        MonoTalk.init(Robolectric.application.getApplicationContext(), LogLevel.VERBOSE, config);
    }

    @Test
    public void mapRow() {
        RowMapper<TestModel1> mapper = new EntityRowMapper<TestModel1>(TestModel1.class, null);
        MatrixCursor cursor = DatabaseCompat.newMatrixCursor(new String[]{"LongColumn", "StringColumn",
                "BooleanColumn", "DateColumn"}, 1);
        Date date = new Date();
        long time = date.getTime();
        cursor.addRow(new Object[]{new Long("1"), new String("Test"), new Integer(1), time});
        cursor.moveToFirst();

        TestModel1 model = mapper.mapRow((Cursor) cursor);
        assertEquals((long) model.columnLong, 1);
        assertEquals(model.columnString, "Test");
        assertEquals(model.columnBoolean, true);
        assertEquals(model.dateColumn, date);
    }

    @Test
    public void mapRowPerformanceCheck1() {
        TestModelRowListMapper mapper = new TestModelRowListMapper();
        MatrixCursor cursor = createTestCursor();
        mapper.mapRowListAndClose(cursor);
    }

    @Test
    public void mapRowPerformanceCheck2() {
        EntityRowListMapper<TestModel1> mapper = new EntityRowListMapper<TestModel1>(TestModel1.class, null);
        MatrixCursor cursor = createTestCursor();
        mapper.mapRowListAndClose(cursor);
    }

    private MatrixCursor createTestCursor() {
        MatrixCursor cursor = DatabaseCompat.newMatrixCursor(new String[]{"LongColumn", "StringColumn",
                "BooleanColumn", "DateColumn"}, 250000);
        int maxCount = 250000;
        for (int i = 0; i < maxCount; i++) {
            cursor.addRow(new Object[]{new Long(i), String.valueOf("###" + i + "###"), new Integer(i),
                    i * 10000 + 10000000});
        }
        return cursor;
    }

    protected DatabaseConfigration createDatabaseConfigration() {
        DatabaseConfigration.Builder builder = new DatabaseConfigration.Builder();
        builder.setDataBaseName("Test.db");
        builder.setVersion(1);
        builder.setDefalutDatabase(true);
        builder.setNodeCacheSize(1000);
        builder.addTable(TestModel1.class);
        return builder.create();
    }

    public static class TestModelRowListMapper implements RowListMapper<TestModel1> {
        @Override
        public List<TestModel1> mapRowListAndClose(Cursor cursor) {
            List<TestModel1> models1 = new ArrayList<TestModel1>();
            if (cursor.moveToFirst()) {
                do {
                    TestModel1 model = new TestModel1();
                    model.columnLong = cursor.getLong(cursor.getColumnIndex("LongColumn"));
                    model.columnString = cursor.getString(cursor.getColumnIndex("StringColumn"));
                    model.columnBoolean = cursor.getLong(cursor.getColumnIndex("BooleanColumn")) == 1 ? true : false;
                    model.dateColumn = new Date(cursor.getColumnIndex("DateColumn"));
                    models1.add(model);
                } while (cursor.moveToNext());
            }
            List<TestModel1> models = models1;
            cursor.close();
            return models;
        }
    }
}
