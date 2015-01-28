package monotalk.db.manager;

import android.content.ContentProvider;
import android.content.ContentResolver;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import monotalk.db.DBLog;
import monotalk.db.LazyList;
import monotalk.db.TestContentProvider1;
import monotalk.db.TestModel1;
import monotalk.db.query.Selection;
import monotalk.db.rules.LogRule;

import static monotalk.db.query.QueryUtils.from;
import static monotalk.db.query.QueryUtils.idEquals;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * Created by Kem on 2015/01/10.
 */
@Ignore
public abstract class BaseEntityManagerTest {

    @Rule
    public LogRule log = new LogRule(BaseEntityManagerTest.class);

    @Before
    public void before() {
        ContentProvider contentProvider = new TestContentProvider1();
        ShadowLog.stream = System.out;
        ContentResolver contentResolver = Robolectric.application.getContentResolver();
        Robolectric.shadowOf(contentResolver);
        ShadowContentResolver.registerProvider("monotalk.db", contentProvider);
        contentProvider.onCreate();
    }

    @After
    public void after() {
        EntityManager manager = getEntityManager();
        manager.deleteAll(TestModel1.class);
        teardown();
    }

    protected abstract void teardown();

    private long setUpTestData(Boolean bool, Long lon, String str) {
        EntityManager manager = getEntityManager();
        TestModel1 model = new TestModel1();
        model.columnBoolean = bool;
        model.columnLong = lon;
        model.columnString = str;
        Long id = manager.insert(model);
        assertThat(id, is(notNullValue()));
        return id;
    }

    private long setUpTestData() {
        return setUpTestData(true, 124l, "TESTColumns");
    }

    protected abstract EntityManager getEntityManager();

    @Test
    public void deleteById() {
        // ==========================
        // insert test data
        // ==========================
        long id = setUpTestData();
        EntityManager manager = getEntityManager();
        int updateCount = manager.deleteById(TestModel1.class, id);
        assertThat(updateCount, is(1));
        TestModel1 model1 = manager.selectOneById(TestModel1.class, id);
        assertThat(model1, is(nullValue()));
    }

    @Test
    public void deleteAll() {
        // ==========================
        // insert test data
        // ==========================
        setUpTestData();
        setUpTestData();

        EntityManager manager = getEntityManager();
        int updateCount = manager.deleteAll(TestModel1.class);
        assertThat(updateCount, is(2));
        List<TestModel1> model1s = manager.selectListAll(TestModel1.class);
        assertThat(model1s.size(), is(0));
    }

    @Test
    public void deleteEntityClassByCondition() {
        // ==========================
        // insert test data
        // ==========================
        long id = setUpTestData();
        EntityManager manager = getEntityManager();
        int updateCount = manager.delete(TestModel1.class, "_id = ?", id);
        assertThat(updateCount, is(1));
        TestModel1 model1 = manager.selectOneById(TestModel1.class, id);
        assertThat(model1, is(nullValue()));
    }

    @Test
    public void deleteEntityClassByObject() {
        // ==========================
        // insert test data
        // ==========================
        long id = setUpTestData();
        EntityManager manager = getEntityManager();
        TestModel1 model1 = new TestModel1();
        model1.id = id;
        int updateCount = manager.delete(model1);
        assertThat(updateCount, is(1));
        model1 = manager.selectOneById(TestModel1.class, id);
        assertThat(model1, is(nullValue()));
    }

    @Test
    public void updateEntityById() {
        // ==========================
        // insert test data
        // ==========================
        long id = setUpTestData();
        EntityManager manager = getEntityManager();
        TestModel1 model1 = new TestModel1();
        model1.id = id;
        model1.columnLong = 456l;
        model1.columnBoolean = true;
        model1.dateColumn = new Date(111111111111111l);

        int updateCount = manager.updateById(model1.getClass(), from(model1), model1.id);
        assertThat(updateCount, is(1));
        model1 = manager.selectOneById(TestModel1.class, id);
        assertThat(model1.columnLong, is(456l));
        assertThat(model1.columnBoolean, is(true));
        assertThat(model1.dateColumn, is(new Date(111111111111111l)));
    }

    @Test
    public void updateEntityByEntity() {
        // ==========================
        // insert test data
        // ==========================
        long id = setUpTestData();
        EntityManager manager = getEntityManager();
        TestModel1 model1 = new TestModel1();
        model1.id = id;
        model1.columnLong = 777l;
        model1.columnBoolean = false;
        model1.dateColumn = new Date(22222222222222222l);

        int updateCount = manager.update(model1);
        assertThat(updateCount, is(1));
        TestModel1 model2 = manager.selectOneById(TestModel1.class, id);
        assertThat(model2.columnLong, is(777l));
        assertThat(model2.columnBoolean, is(false));
        assertThat(model2.dateColumn, is(new Date(22222222222222222l)));
        assertThat(model2, is(model1));
    }

    @Test
    public void updateEntityByCondition() {
        // ==========================
        // insert test data
        // ==========================
        long id = setUpTestData();
        EntityManager manager = getEntityManager();
        TestModel1 model1 = new TestModel1();
        model1.id = id;
        model1.columnLong = 777l;
        model1.columnBoolean = false;
        model1.dateColumn = new Date(22222222222222222l);
        // ===========================
        // execute
        // ===========================
        Selection selection = idEquals(TestModel1.class, id);
        int updateCount = manager.update(TestModel1.class, from(model1), selection.getSelection(), selection.getSelectionArgs());
        assertThat(updateCount, is(1));

        TestModel1 model2 = manager.selectOneById(TestModel1.class, id);
        // ===========================
        // verify
        // ===========================
        assertThat(model2.columnLong, is(777l));
        assertThat(model2.columnBoolean, is(false));
        assertThat(model2.dateColumn, is(new Date(22222222222222222l)));
        assertThat(model2, is(model1));
    }

    @Test
    public void updateExcludeNullByEntity() {
        // ==========================
        // insert test data
        // ==========================
        long id = setUpTestData();
        EntityManager manager = getEntityManager();
        TestModel1 model1 = new TestModel1();
        model1.id = id;
        model1.columnLong = null;
        model1.columnBoolean = null;
        model1.dateColumn = new Date(22222222222222222l);
        // ===========================
        // execute
        // ===========================
        int updateCount = manager.updateExcludesNull(model1);
        assertThat(updateCount, is(1));

        TestModel1 model2 = manager.selectOneById(TestModel1.class, id);
        // ===========================
        // verify
        // ===========================
        assertThat(model2.columnLong, is(124l));
        assertThat(model2.columnBoolean, is(true));
        assertThat(model2.dateColumn, is(new Date(22222222222222222l)));
        assertThat(model2, is(model1));
    }

    @Test
    public void selectLazyList() {
        EntityManager manager = getEntityManager();
        // ==========================
        // insert test data
        // ==========================
        setUpTestData();
        setUpTestData();
        // ===========================
        // execute
        // ===========================
        LazyList<TestModel1> list = manager.selectLazyListAll(TestModel1.class);

        // ===========================
        // verify
        // ===========================
        assertThat(list.size(), is(2));

        for(TestModel1 model1 : list) {
            assertThat(model1.columnString, is("TESTColumns"));
        }
    }

    @Test
    public void selectCount() {
        EntityManager manager = getEntityManager();
        // ==========================
        // insert test data
        // ==========================
        setUpTestData();
        // ===========================
        // execute
        // ===========================
        long count1 = manager.selectCount(TestModel1.class);

        // ===========================
        // verify
        // ===========================
        assertThat(count1, is(1l));

        // ==========================
        // insert test data
        // ==========================
        setUpTestData();

        // ===========================
        // execute
        // ===========================
        long count2 = manager.selectCount(TestModel1.class);

        // ===========================
        // verify
        // ===========================
        assertThat(count2, is(2l));
    }

    @Test
    public void selectCountById() {
        EntityManager manager = getEntityManager();
        // ==========================
        // insert test data
        // ==========================
        long id = setUpTestData();
        // ===========================
        // execute
        // ===========================
        long count1 = manager.selectCountById(TestModel1.class, id);

        // ===========================
        // verify
        // ===========================
        assertThat(count1, is(1l));

        // ===========================
        // execute
        // ===========================
        long count2 = manager.selectCountById(TestModel1.class, Long.MAX_VALUE);

        // ===========================
        // verify
        // ===========================
        assertThat(count2, is(0l));
    }

    @Test
    public void selectListAll() {
        EntityManager manager = getEntityManager();
        // ==========================
        // insert test data
        // ==========================
        setUpTestData(true, 111l, "TEST1");
        setUpTestData(true, 222l, "TEST2");
        setUpTestData(true, 333l, "TEST3");

        // ===========================
        // execute
        // ===========================
        List<TestModel1> models1 = manager.selectListAll(TestModel1.class);

        // ===========================
        // verify
        // ===========================
        assertThat(models1.size(), is(3));
        List<String> expects = new ArrayList<String>() {
            {
                add("TEST1");
                add("TEST2");
                add("TEST3");
            }
        };
        for (TestModel1 model1 : models1) {
            if (!expects.contains(model1.columnString)) {
                fail("Illegal Value =" + model1.columnString);
            }
        }
    }

    @Test
    public void insertTransaction() {
        final EntityManager manager = getEntityManager();
        // ==========================
        // no taransaction
        // ==========================
        long startTime = System.currentTimeMillis();
        new DBLog.AbstractStopWatch<Void>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Void process() {
                int dataCount = 3000;
                for (int i = 0; i < 3000; i++) {
                    setUpTestData(true, Long.valueOf(i), "TEST" + i);
                }
                return null;
            }
        }.measure("insert");
        long endTime = System.currentTimeMillis();
        long interval1 = endTime - startTime;

        // ==========================
        // with transaction
        // ==========================
        startTime = System.currentTimeMillis();
        new DBLog.AbstractStopWatch<Void>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Void process() {
                manager.beginTransactionNonExclusive();
                int dataCount = 3000;
                for (int i = 0; i < 3000; i++) {
                    setUpTestData(true, Long.valueOf(i), "TEST" + i);
                }
                manager.setTransactionSuccessful();
                manager.endTransaction();
                return null;
            }
        }.measure("insertWithTransaction");
        endTime = System.currentTimeMillis();
        long interval2 = endTime - startTime;
        assertThat("速度は1よりも早いはず", interval2 < interval1, is(true));

        // =============================
        // with transaction bulkInsert
        // =============================
        startTime = System.currentTimeMillis();
        final List<TestModel1> models = new ArrayList<TestModel1>();
        int dataCount = 3000;
        for (int i = 0; i < 3000; i++) {
            TestModel1 model1 = new TestModel1();
            model1.columnString = "TEST" + i;
            model1.columnLong = Long.valueOf(i);
            model1.columnBoolean = true;
            models.add(model1);
        }
        int updateCount = new DBLog.AbstractStopWatch<Integer>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Integer process() {
                return manager.bulkInsert(models);
            }
        }.measure("bulkInsert");
        endTime = System.currentTimeMillis();
        long interval3 = endTime - startTime;
        assertThat(updateCount, is(3000));
        assertThat("速度は2よりも早いはず", interval3 < interval2, is(true));
    }

    @Test
    public void updateNoTransactionMeasure() {
        long[] ids = bulkInsertTestData();
        final EntityManager manager = getEntityManager();
        // ================================================================
        // no transaction
        // ================================================================
        final List<TestModel1> models1 = new ArrayList<TestModel1>();
        for (int i = 0; i < 3000; i++) {
            TestModel1 model1 = new TestModel1();
            model1.id = ids[i];
            model1.columnString = "TEST_UPDATE_NO_TRANSACTION" + i;
            model1.columnLong = Long.valueOf(i);
            model1.columnBoolean = true;
            models1.add(model1);
        }
        int updateCount1 = new DBLog.AbstractStopWatch<Integer>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Integer process() {
                int updateCount = 0;
                int dataCount = 3000;
                for (int i = 0; i < dataCount; i++) {
                    updateCount += manager.update(models1.get(i));
                }
                return updateCount;
            }
        }.measure("newUpdate");
        assertThat(updateCount1, is(3000));
        List<TestModel1> actualModels1 = manager.selectListAll(TestModel1.class);
        for (int i = 0; i < 3000; i++) {
            String columnString = actualModels1.get(i).columnString;
            assertThat(columnString, is(startsWith("TEST_UPDATE_NO_TRANSACTION")));
        }
    }

    @Test
    public void updateWithTransactionMeasure() {
        long[] ids = bulkInsertTestData();
        final EntityManager manager = getEntityManager();
        // ================================================================
        // with transaction
        // ================================================================
        final List<TestModel1> models2 = new ArrayList<TestModel1>();
        for (int i = 0; i < 3000; i++) {
            TestModel1 model1 = new TestModel1();
            model1.id = ids[i];
            model1.columnString = "TEST_UPDATE_WITH_TRANSACTION" + i;
            model1.columnLong = Long.valueOf(i);
            model1.columnBoolean = true;
            models2.add(model1);
        }
        int updateCount2 = new DBLog.AbstractStopWatch<Integer>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Integer process() {
                int updateCount = 0;
                int dataCount = 3000;
                manager.beginTransactionNonExclusive();
                for (int i = 0; i < dataCount; i++) {
                    updateCount += manager.update(models2.get(i));
                }
                manager.setTransactionSuccessful();
                manager.endTransaction();
                return updateCount;
            }
        }.measure("updateWithTransaction");
        assertThat(updateCount2, is(3000));
        List<TestModel1> actualModels2 = manager.selectListAll(TestModel1.class);
        for (int i = 0; i < 3000; i++) {
            String columnString = actualModels2.get(i).columnString;
            assertThat(columnString, is(startsWith("TEST_UPDATE_WITH_TRANSACTION")));
        }
    }

    private long[] bulkInsertTestData() {
        EntityManager manager = getEntityManager();
        manager.beginTransactionNonExclusive();
        int dataCount = 3000;
        long[] ids = new long[dataCount];
        for (int i = 0; i < 3000; i++) {
            ids[i] = setUpTestData(true, Long.valueOf(i), "TEST" + i);
        }
        manager.setTransactionSuccessful();
        manager.endTransaction();
        return ids;
    }

    @Test
    public void bulkUpdateTransactionMeasure() {
        long[] ids = bulkInsertTestData();

        // ======================================
        // with transaction bulkUpdate
        // ================================================================
        final EntityManager manager = getEntityManager();

        final List<TestModel1> models3 = new ArrayList<TestModel1>();
        for (int i = 0; i < 3000; i++) {
            TestModel1 model1 = new TestModel1();
            model1.id = ids[i];
            model1.columnString = "TEST_UPDATE" + i;
            model1.columnLong = Long.valueOf(i);
            model1.columnBoolean = true;
            models3.add(model1);
        }

        int updateCount3 = new DBLog.AbstractStopWatch<Integer>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Integer process() {
                return manager.bulkUpdate(models3);
            }
        }.measure("bulkUpdate");
        assertThat(updateCount3, is(3000));
        List<TestModel1> actualModels3 = manager.selectListAll(TestModel1.class);
        for (int i = 0; i < 3000; i++) {
            String columnString = actualModels3.get(i).columnString;
            assertThat(columnString, is(startsWith("TEST_UPDATE")));
        }
    }

    @Test
    public void bulkDeleteTransactionMeasure() {
        long[] ids = bulkInsertTestData();

        // ================================================================
        // with transaction bulkUpdate
        // ================================================================
        final EntityManager manager = getEntityManager();
        final List<TestModel1> models3 = new ArrayList<TestModel1>();
        for (int i = 0; i < 3000; i++) {
            TestModel1 model1 = new TestModel1();
            model1.id = ids[i];
            model1.columnString = "TEST_UPDATE" + i;
            model1.columnLong = Long.valueOf(i);
            model1.columnBoolean = true;
            models3.add(model1);
        }

        int updateCount3 = new DBLog.AbstractStopWatch<Integer>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Integer process() {
                return manager.bulkDelete(models3);
            }
        }.measure("bulkDelete");
        assertThat(updateCount3, is(3000));
        List<TestModel1> actualModels = manager.selectListAll(TestModel1.class);
        assertThat(actualModels.size(), is(0));
    }

    @Test
    public void deleteWithTransactionMeasure() {
        long[] ids = bulkInsertTestData();

        // ================================================================
        // with transaction bulkUpdate
        // ================================================================
        final EntityManager manager = getEntityManager();
        final List<TestModel1> models = new ArrayList<TestModel1>();
        for (int i = 0; i < 3000; i++) {
            TestModel1 model1 = new TestModel1();
            model1.id = ids[i];
            model1.columnString = "TEST_UPDATE" + i;
            model1.columnLong = Long.valueOf(i);
            model1.columnBoolean = true;
            models.add(model1);
        }

        int updateCount = new DBLog.AbstractStopWatch<Integer>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Integer process() {
                int updateCount = 0;
                int dataCount = 3000;
                manager.beginTransactionNonExclusive();
                for (int i = 0; i < dataCount; i++) {
                    updateCount += manager.delete(models.get(i));
                }
                manager.setTransactionSuccessful();
                manager.endTransaction();
                return updateCount;
            }
        }.measure("deleteWithTransaction");

        assertThat(updateCount, is(3000));
        List<TestModel1> actualModels = manager.selectListAll(TestModel1.class);
        assertThat(actualModels.size(), is(0));
    }

    @Test
    public void deleteNoTransactionMeasure() {
        long[] ids = bulkInsertTestData();
        final EntityManager manager = getEntityManager();
        // ================================================================
        // no transaction
        // ================================================================
        final List<TestModel1> models1 = new ArrayList<TestModel1>();
        for (int i = 0; i < 3000; i++) {
            TestModel1 model1 = new TestModel1();
            model1.id = ids[i];
            model1.columnString = "TEST_UPDATE_NO_TRANSACTION" + i;
            model1.columnLong = Long.valueOf(i);
            model1.columnBoolean = true;
            models1.add(model1);
        }
        int updateCount = new DBLog.AbstractStopWatch<Integer>(DBLog.getTag(getClass()), DBLog.LogLevel.ERROR) {
            @Override
            protected Integer process() {
                int updateCount = 0;
                int dataCount = 3000;
                for (int i = 0; i < dataCount; i++) {
                    updateCount += manager.delete(models1.get(i));
                }
                return updateCount;
            }
        }.measure("deleteNoTransaction");
        assertThat(updateCount, is(3000));
        List<TestModel1> actualModels = manager.selectListAll(TestModel1.class);
        assertThat(actualModels.size(), is(0));
    }
}
