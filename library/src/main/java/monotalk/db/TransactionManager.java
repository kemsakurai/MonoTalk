package monotalk.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;

import monotalk.db.compat.DatabaseCompat;

/**
 * Created by Kem on 2015/01/22.
 */
public class TransactionManager {

    /**
     * SQLiteOpenHelper クラス
     */
    private DatabaseOpenHelper dbHelper = null;

    public TransactionManager(DatabaseOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * トランザクションを開始する
     */
    public void beginTransactionWithListenerNonExclusive(SQLiteTransactionListener listener) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DatabaseCompat.beginTransactionWithListenerNonExclusive(db, listener);
    }

    public void endTransaction() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.endTransaction();
    }

    /**
     * トランザクションを開始する
     */
    public void beginTransactionNonExclusive() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DatabaseCompat.beginTransactionNonExclusive(db);
    }

    /**
     * setTransactionSuccessfulを実行する
     */
    public void setTransactionSuccessful() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.setTransactionSuccessful();
    }

    /**
     * yieldIfContendedSafelyを実行する
     *
     * @return
     */
    public boolean yieldIfContendedSafely() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.yieldIfContendedSafely();
    }

    /**
     * yieldIfContendedSafelyを実行する
     *
     * @param sleepAfterYieldDelay
     * @return
     */
    public boolean yieldIfContendedSafely(int sleepAfterYieldDelay) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.yieldIfContendedSafely(sleepAfterYieldDelay);
    }

    public static interface ModifyExecuteLister extends SQLiteTransactionListener {
        public void onExecuteError(RuntimeException e);

        public void onPreExecuteQuery();

        public void onPostExecuteQeury();
    }

    public static interface SelectExecuteLister extends SQLiteTransactionListener {
        public void onExecuteError(Cursor cursor, RuntimeException e);

        public void onPreExecuteQuery();

        public void onPostExecuteQeury(Cursor cursor);
    }

    public static class DefaultModifyExecuteListener implements ModifyExecuteLister {
        @Override
        public void onExecuteError(RuntimeException e) {

        }

        @Override
        public void onPreExecuteQuery() {

        }

        @Override
        public void onPostExecuteQeury() {

        }

        @Override
        public void onBegin() {

        }

        @Override
        public void onCommit() {

        }

        @Override
        public void onRollback() {

        }
    }

    public static class DefaultSelectExecuteListener implements SelectExecuteLister {
        @Override
        public void onExecuteError(Cursor cursor, RuntimeException e) {

        }

        @Override
        public void onPreExecuteQuery() {

        }

        @Override
        public void onPostExecuteQeury(Cursor cursor) {

        }

        @Override
        public void onBegin() {

        }

        @Override
        public void onCommit() {

        }

        @Override
        public void onRollback() {

        }
    }

    /**
     * Inner Class
     */
    public static abstract class AbstractSelectStatementExecuter {

        private SQLiteOpenHelper helper;
        private SelectExecuteLister listener = new DefaultSelectExecuteListener();

        /**
         * コンストラクター
         *
         * @param helper
         */
        public AbstractSelectStatementExecuter(final SQLiteOpenHelper helper, SelectExecuteLister listener) {
            this.helper = helper;
            this.listener = listener;
        }

        /**
         * 実行メソッド
         */
        public Cursor execute() {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = null;
            boolean doAsTransaction = !db.inTransaction();
            try {
                if (doAsTransaction) {
                    // Transaction内でない(単独実行クエリの場合)
                    DatabaseCompat.beginTransactionWithListenerNonExclusive(db, listener);
                }
                onPreExecuteQuery();
                cursor = doInTransaction(db);
                onPostExecuteQeury(cursor);

                if (doAsTransaction) {
                    db.setTransactionSuccessful();
                }
            } catch (SQLiteException e) {
                // エラー処理
                onExecuteError(cursor, e);
                throw e;
            } finally {
                if (db != null && doAsTransaction) {
                    db.endTransaction();
                }
            }
            // 戻り値を返す
            return cursor;
        }

        private void onPreExecuteQuery() {
            if (listener != null) {
                listener.onPreExecuteQuery();
            }
        }

        /**
         * 抽象メソッド
         *
         * @param statement
         * @return
         */
        protected abstract Cursor doInTransaction(SQLiteDatabase statement);

        private void onPostExecuteQeury(Cursor cursor) {
            if (listener != null) {
                listener.onPostExecuteQeury(cursor);
            }
        }

        private void onExecuteError(Cursor cursor, RuntimeException e) {
            if (listener != null) {
                listener.onExecuteError(cursor, e);
            }
        }
    }

    /**
     * インナークラス
     *
     * @author Kem
     */
    public static abstract class AbstractModifyStatementExecuter<T extends Number> {
        private SQLiteOpenHelper helper;
        private ModifyExecuteLister listener = new DefaultModifyExecuteListener();

        /**
         * コンストラクター
         *
         * @param helper
         */
        public AbstractModifyStatementExecuter(final SQLiteOpenHelper helper, final ModifyExecuteLister listener) {
            this.helper = helper;
            this.listener = listener;
        }

        /**
         * 実行メソッド
         *
         * @return
         */
        public T execute() {
            T result = null;
            SQLiteDatabase db = helper.getWritableDatabase();
            boolean doAsTransaction = !db.inTransaction();

            try {
                if (doAsTransaction) {
                    // Transaction内でない(単独実行クエリの場合)
                    DatabaseCompat.beginTransactionWithListenerNonExclusive(db, listener);
                }
                onPreExecuteQeury();
                result = doInTransaction(db);
                onPostExecuteQeury();

                if (doAsTransaction) {
                    db.setTransactionSuccessful();
                }
            } catch (SQLiteException e) {
                // エラー処理
                onExecuteError(e);
                throw e;
            } finally {
                if (db != null && doAsTransaction) {
                    db.endTransaction();
                }
            }
            // 戻り値を返す
            return result;
        }

        private void onPreExecuteQeury() {
            if (listener != null) {
                listener.onPreExecuteQuery();
            }
        }

        /**
         * 抽象メソッド
         *
         * @return
         */
        protected abstract T doInTransaction(SQLiteDatabase db);

        private void onPostExecuteQeury() {
            if (listener != null) {
                listener.onPostExecuteQeury();
            }
        }

        private void onExecuteError(RuntimeException e) {
            if (listener != null) {
                listener.onExecuteError(e);
            }
        }
    }
}
