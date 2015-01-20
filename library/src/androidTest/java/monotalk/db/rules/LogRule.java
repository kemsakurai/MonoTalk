package monotalk.db.rules;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import monotalk.db.DBLog;

public class LogRule extends TestWatcher {

    static {
        // you other setup here
        ShadowLog.stream = System.out;
    }
    private long startTime;
    private String tagName;

    public LogRule(Class<?> clazz) {
        tagName = clazz.getName();
    }

    /**
     * Invoked when a test succeeds
     */
    protected void succeeded(Description description) {
    }

    /**
     * Invoked when a test fails
     */
    protected void failed(Throwable e, Description description) {
    }

    /**
     * Invoked when a test is skipped due to a failed assumption.
     */
    protected void skipped(AssumptionViolatedException e, Description description) {
    }

    /**
     * Invoked when a test is about to start
     */
    protected void starting(Description description) {
        DBLog.d(description.getClassName(), description.getMethodName() + ">>>>> starting");
        startTime = System.currentTimeMillis();
    }

    /**
     * Invoked when a test method finishes (whether passing or failing)
     */
    protected void finished(Description description) {
        long now = System.currentTimeMillis();
        double executionTime = (now - startTime);
        i(description.getMethodName() + ">>>>> finished... Method's Execution Time [" + executionTime + "]ms");
    }

    public void d(ContentValues values) {
        DBLog.d(tagName, values);
    }

    public void d(String msg, Object... params) {
        DBLog.d(tagName, msg, params);
    }

    public void e(String msg) {
        DBLog.e(tagName, msg);
    }

    public String getTag(Class<?> clazz) {
        return DBLog.getTag(clazz);
    }

    public void i(String msg, Object... params) {
        DBLog.i(tagName, msg, params);
    }

    public void w(String msg) {
        DBLog.w(tagName, msg);
    }

    public void d(Cursor cursor) {
        DBLog.d(tagName, cursor);
    }

    public void e(String msg, Object... params) {
        DBLog.e(tagName, msg, params);
    }

    public void v(String msg) {
        DBLog.v(tagName, msg);
    }

    public boolean isLoggable(DBLog.LogLevel level) {
        return DBLog.isLoggable(level);
    }

    public void setLogLevel(DBLog.LogLevel level) {
        DBLog.setLogLevel(level);
    }

    public void d(String msg) {
        DBLog.d(tagName, msg);
    }

    public void e(String tagName, String msg, IllegalArgumentException e) {
        DBLog.e(tagName, msg, e);
    }

    public void d(List<?> items) {
        DBLog.d(tagName, items);
    }
    
    public void i(String message) {
        DBLog.i(tagName, message);
    }

    public String ln() {
        return "\n";
    }
}
