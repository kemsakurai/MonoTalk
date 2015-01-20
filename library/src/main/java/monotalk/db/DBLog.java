/*******************************************************************************
 * Copyright (C) 2012-2013 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package monotalk.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.List;

public class DBLog {

    private final static String C_PACKAGE_ROOT = "monotalk.db.";

    private final static String C_PACKAGE_TAG = "Monotalk.DB#";

    private static LogLevel sLogLevel = LogLevel.NONE;

    // ===================================================================================
    // Static Method
    // ==========
    public static void d(String tag, ContentValues values) {
        if (sLogLevel.isLoggable(LogLevel.DEBUG)) {
            Log.d(tag, ">>>>>>>>>>ContentValues'value [" + values.toString() + "]");
        }
    }

    public static void d(String tag, Cursor cursor) {
        if (sLogLevel.isLoggable(LogLevel.DEBUG)) {
            Log.d(tag, dumpCursorToString(cursor));
        }
    }

    public static void d(String tag, List<?> items) {
        if (sLogLevel.isLoggable(LogLevel.DEBUG)) {
            for (Object item : items) {
                Log.d(tag, item.toString());
            }
        }
    }

    public static void d(String tag, String msg) {
        if (sLogLevel.isLoggable(LogLevel.DEBUG)) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Object... params) {
        if (sLogLevel.isLoggable(LogLevel.DEBUG)) {
            Log.d(tag, String.format(msg, params));
        }
    }

    public static void e(String tagName, String msg, IllegalArgumentException e) {
        if (sLogLevel.isLoggable(LogLevel.ERROR)) {
            Log.e(tagName, msg, e);
        }
    }

    public static void e(String tag, String msg) {
        if (sLogLevel.isLoggable(LogLevel.ERROR)) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Object... params) {
        if (sLogLevel.isLoggable(LogLevel.ERROR)) {
            Log.e(tag, String.format(msg, params));
        }
    }

    /**
     * タグ名を返す
     *
     * @param clazz クラス名
     * @return
     */
    public static String getTag(Class<?> clazz) {
        String name = clazz.getName();
        return name.replace(C_PACKAGE_ROOT, C_PACKAGE_TAG);
    }

    public static void i(String tag, String msg) {
        if (sLogLevel.isLoggable(LogLevel.INFO)) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Object... params) {
        if (sLogLevel.isLoggable(LogLevel.INFO)) {
            Log.i(tag, String.format(msg, params));
        }
    }

    public static boolean isLoggable(LogLevel level) {
        return sLogLevel.isLoggable(level);
    }

    public static void setLogLevel(LogLevel level) {
        sLogLevel = level;
    }

    public static void v(String tag, String msg) {
        if (sLogLevel.isLoggable(LogLevel.VERBOSE)) {
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (sLogLevel.isLoggable(LogLevel.WARN)) {
            Log.w(tag, msg);
        }
    }

    /**
     * Prints the contents of a Cursor's current row to a StringBuilder.
     *
     * @param cursor the cursor to print
     * @param sb     the StringBuilder to print to
     */
    private static void dumpCurrentRowQuetly(Cursor cursor, StringBuilder sb) {
        // REMENBER Cursorのデータが壊れている場合、ログ出力ができなかった。
        // このため、壊れている場合は、エラー内容をログ出力し処理を継続させるようにした。
        String[] cols = cursor.getColumnNames();
        sb.append("" + cursor.getPosition() + " {\n");
        int length = cols.length;
        for (int i = 0; i < length; i++) {
            String value;
            try {
                value = cursor.getString(i);
            } catch (SQLiteException e) {
                // assume that if the getString threw this exception then the
                // column is not
                // representable by a string, e.g. it is a BLOB.
                value = "<unprintable>";
            } catch (IllegalStateException e) {
                value = "<unprintable cause by error [" + e.getMessage() + "]>";
            }
            sb.append("   " + cols[i] + '=' + value + "\n");
        }
        sb.append("}\n");
    }

    private static String dumpCursorToString(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        sb.append(">>>>> Dumping cursor " + cursor + "\n");
        if (cursor != null) {
            int startPos = cursor.getPosition();

            cursor.moveToPosition(-1);
            try {

                while (cursor.moveToNext()) {
                    dumpCurrentRowQuetly(cursor, sb);
                }
            } catch (IllegalStateException e) {
                sb.append("" + cursor.getPosition() + " {\n");
                sb.append("<unprintable row cause by error [" + e.getMessage() + "]");
                sb.append("}\n");
            }
            cursor.moveToPosition(startPos);
        }
        sb.append("<<<<<\n");
        return sb.toString();
    }

    private static void log(LogLevel level, String tagName, String msg) {
        switch (level) {
            case VERBOSE:
                Log.v(tagName, msg);
                break;
            case DEBUG:
                Log.d(tagName, msg);
                break;
            case INFO:
                Log.i(tagName, msg);
                break;
            case WARN:
                Log.w(tagName, msg);
                break;
            case ERROR:
                Log.e(tagName, msg);
                break;
            case NONE:
                // Do Nothing...
                break;
            default:
                throw new IllegalStateException("LogLevel is Illegal value=" + level);
        }
    }

    /**
     * Controls the level of logging.
     */
    public enum LogLevel {
        /**
         * Log VERBOSE level
         */
        VERBOSE,
        /**
         * Log Debug level
         */
        DEBUG,
        /**
         * Log INFO level
         */
        INFO,
        /**
         * Log WARN level
         */
        WARN,
        /**
         * Log ERROR level
         */
        ERROR,
        /**
         * No logging.
         */
        NONE;

        public boolean isLoggable(LogLevel logLevel) {
            return this.ordinal() <= logLevel.ordinal();
        }
    }

    public static abstract class AbstractStopWatch<T> {

        private LogLevel level;
        private String tagName = null;

        public AbstractStopWatch(String tagName) {
            this.tagName = tagName;
            this.level = LogLevel.DEBUG;
        }

        public AbstractStopWatch(String tagName, LogLevel level) {
            this.tagName = tagName;
            this.level = level;
        }

        public T measure(String methodName) {
            long startTime = -1;
            if (sLogLevel.isLoggable(level)) {
                startTime = System.currentTimeMillis();
            }
            T obj = process();
            if (sLogLevel.isLoggable(level)) {
                long now = System.currentTimeMillis();
                double executionTime = (now - startTime);
                log(level, tagName, methodName + ">>>>>METHOD Execution Time is [ " + executionTime + "] ms");
            }
            return obj;
        }

        abstract protected T process();
    }
}
