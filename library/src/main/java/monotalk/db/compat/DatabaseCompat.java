/*******************************************************************************
 * Copyright (C) 2012-2015 Kem
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
package monotalk.db.compat;

import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteTransactionListener;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

public class DatabaseCompat {

    private static ApiCompatibility compat = null;

    static {
        if (Integer.parseInt(VERSION.SDK) >= VERSION_CODES.JELLY_BEAN) {
            compat = new JellyBeanApiCompatibility();
        } else if (Integer.parseInt(VERSION.SDK) >= VERSION_CODES.HONEYCOMB) {
            compat = new HoneyCombApiCompatibility();
        } else {
            compat = new BasicApiCompatibility();
        }
    }

    public static String buildQuery(SQLiteQueryBuilder builder, String[] projectionIn, String selection,
                                    String groupBy, String having, String sortOrder, String limit) {
        return compat.buildQuery(builder, projectionIn, selection, groupBy, having, sortOrder, limit);
    }

    public static MatrixCursor newMatrixCursor(String[] columnNames, int initialCapacity) {
        return compat.newMatrixCursor(columnNames, initialCapacity);
    }

    /**
     * @param db SQLiteDatabase
     */
    public static void beginTransactionNonExclusive(SQLiteDatabase db) {
        compat.beginTransactionNonExclusive(db);
    }

    /**
     * @param db
     * @param listener
     */
    public static void beginTransactionWithListenerNonExclusive(SQLiteDatabase db, SQLiteTransactionListener listener) {
        compat.beginTransactionWithListenerNonExclusive(db, listener);
    }

    public static int executeUpdateDelete(SQLiteDatabase db, SQLiteStatement statement) {
        return compat.executeUpdateDelete(db, statement);
    }

    public static void bindAllArgsAsStrings(SQLiteStatement realStatement, String[] bindArgs) {
        compat.bindAllArgsAsStrings(realStatement, bindArgs);
    }
}