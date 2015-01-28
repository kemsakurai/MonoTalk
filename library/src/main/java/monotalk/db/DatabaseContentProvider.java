/*******************************************************************************
 * Copyright (C) 2013-2015 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package monotalk.db;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import monotalk.db.DBLog.LogLevel;
import monotalk.db.annotation.NoticeTarget;
import monotalk.db.annotation.NoticeTargets;
import monotalk.db.annotation.Table;
import monotalk.db.compat.DatabaseCompat;
import monotalk.db.querydata.SelectQueryData;
import monotalk.db.querydata.TwoWayQueryData;

import static monotalk.db.TransactionManager.ModifyExecuteLister;
import static monotalk.db.TransactionManager.SelectExecuteLister;

/**
 * DatabaseContentProvider.java
 * データベースのCRUDを行うContentProviderの規定クラス
 *
 * @author Kem
 */
public abstract class DatabaseContentProvider extends ContentProvider {

    private static final String TAG_NAME = DBLog.getTag(DatabaseContentProvider.class);
    private static final int DEFAULT_DELAY = 500;
    // =================================================================
    // Member Filed
    // =================================================================
    private DmlExecutor dmlExecutor;
    private TransactionManager txManager;
    private NotificationUriManager uriManager;
    private SparseArray<Class<? extends Entity>> typeCodes = new SparseArray<Class<? extends Entity>>();
    private UriMatcher uriMatcer = new UriMatcher(UriMatcher.NO_MATCH);
    private String authority;

    @Override
    public boolean onCreate() {
        // register DatabaseConfigration
        DatabaseConfigration config = newDatabaseConfigration();
        DatabaseConnectionSource databaseConnnection = MonoTalk.init(getContext(), config);
        dmlExecutor = new DmlExecutor(databaseConnnection);
        uriManager = new NotificationUriManager();
        txManager = new TransactionManager(databaseConnnection.getDbHelper());

        // register DatabaseProviderConfigration
        String authority = getAuthority(getContext(), getClass());
        DatabaseProviderConnectionSource providerConnection = newProviderConnectionSource(authority, databaseConnnection);
        MonoTalk.registerProvierConnnectionSource(providerConnection);

        int i = 0;
        for (Class<? extends Entity> entityClass : providerConnection.getEntityClasses()) {
            TableInfo info = MonoTalk.getTableInfo(entityClass);
            final int tableKey = i++;
            // content://<authority>/<entity>
            String entityPath = info.getTableName().toLowerCase(Locale.getDefault());
            uriMatcer.addURI(authority, entityPath, tableKey);
            typeCodes.put(tableKey, info.getType());

            final int itemKey = i++;
            // content://<authority>/<table>/<id>
            uriMatcer.addURI(authority, entityPath + "/#", itemKey);
            typeCodes.put(itemKey, info.getType());

            // notifyUri
            uriManager.registerNotifyUri(entityClass, authority);
        }
        return true;
    }

    @Override
    public Cursor query(final Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DBLog.isLoggable(LogLevel.DEBUG)) {
            DBLog.d(TAG_NAME, "Method[query] start");
            DBLog.d(TAG_NAME, "parameter uri=[" + uri + "]");
        }
        // --------------------------------------------------------
        // ## Process Transaction ##
        // --------------------------------------------------------
        String transaction = UriUtils.getTransaction(uri);
        if (!TextUtils.isEmpty(transaction)) {
            // Transactionパラメータが設定されている場合は、Transaction処理を行う
            return processTransaction(transaction);

        }
        // --------------------------------------------------------
        // ## Process YieidIfContended ##
        // --------------------------------------------------------
        String yieldIfContended = UriUtils.getYieldIfContendedFromUri(uri);
        if (!TextUtils.isEmpty(yieldIfContended)) {
            // YieidIfContendedSafelyを実行
            return processYieidIfContendedSafely(yieldIfContended);
        }

        // --------------------------------------------------------
        // ## Process Select data
        // --------------------------------------------------------
        String sqlFilePath = UriUtils.getSqlFilePath(uri);
        Cursor cursor;
        // SQLFilePathが指定されている場合は、2waySql向けの処理を行う
        SelectExecuteLister listener = createSelectStatementExecuteListener(uri);
        if (!TextUtils.isEmpty(sqlFilePath)) {
            // ## create Parameter Uri
            TwoWayQueryData data = TwoWayQueryData.newInstance(sqlFilePath, uri);
            cursor = dmlExecutor.selectCursorBySqlFile(data.getSqlFilePath(), data.getMapPmb(), listener);
        } else if (isItemUri(uri)) {
            long id = ContentUris.parseId(uri);
            Class<? extends Entity> clazz = getEntityClass(uri);
            cursor = dmlExecutor.selectCursorById(clazz, id, listener);
        } else {
            // ## Get Uri Parameter ##
            Class<? extends Entity> clazz = getEntityClass(uri);
            if (clazz == null) {
                DBLog.d(TAG_NAME, "clazz is null Uri = " + uri);
                return null;
            }
            String tableName = MonoTalk.getTableName(clazz);
            SelectQueryData data =
                    SelectQueryData.newInstance(uri,
                            tableName,
                            projection,
                            selection,
                            selectionArgs,
                            sortOrder);
            cursor = dmlExecutor.selectCursorBySql(data.toSql(), listener, data.getSelectionArgs());
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        if (DBLog.isLoggable(LogLevel.DEBUG)) {
            DBLog.d(TAG_NAME, "Method[getType] start");
            DBLog.d(TAG_NAME, "parameter uri=[" + uri + "]");
        }

        String type;
        if (isNoMatch(uri)) {
            type = null;
        } else if (isItemUri(uri)) {
            Class<? extends Entity> clazz = getEntityClass(uri);
            String auth = uri.getAuthority();
            type = "vnd.android.cursor.dir/item." + auth + "." + clazz.getSimpleName();
        } else {
            Class<? extends Entity> clazz = getEntityClass(uri);
            String auth = uri.getAuthority();
            type = "vnd.android.cursor.dir/vnd." + auth + "." + clazz.getSimpleName();
        }
        return type;
    }

    private boolean isNoMatch(Uri uri) {
        return UriMatcher.NO_MATCH == uriMatcer.match(uri);
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        if (DBLog.isLoggable(LogLevel.DEBUG)) {
            DBLog.d(TAG_NAME, "Method[insert] start");
            DBLog.d(TAG_NAME, "parameter uri=[" + uri + "]");
        }
        Class<? extends Entity> clazz = getEntityClass(uri);
        ModifyExecuteLister listener = newModifyStatementExecuteListener(clazz);
        long id = dmlExecutor.insert(clazz, values, listener);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (DBLog.isLoggable(LogLevel.DEBUG)) {
            DBLog.d(TAG_NAME, "Method[bulkInsert] start");
            DBLog.d(TAG_NAME, "parameter uri=[" + uri + "]");
        }
        Class<? extends Entity> clazz = getEntityClass(uri);
        ModifyExecuteLister listener = newModifyStatementExecuteListener(clazz);
        return dmlExecutor.bulkInsert(clazz, values, listener);
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        if (DBLog.isLoggable(LogLevel.DEBUG)) {
            DBLog.d(TAG_NAME, "Method[delete] start");
            DBLog.d(TAG_NAME, "parameter uri=[" + uri + "]");
        }

        if (isItemUri(uri)) {
            long id = ContentUris.parseId(uri);
            Class<? extends Entity> entity = getEntityClass(uri);
            ModifyExecuteLister listener = newModifyStatementExecuteListener(entity);
            return dmlExecutor.deleteById(entity, id, listener);
        } else {
            Class<? extends Entity> entity = getEntityClass(uri);
            ModifyExecuteLister listener = newModifyStatementExecuteListener(entity);
            return dmlExecutor.deleteByWhereArrayStringArgs(entity, selection, listener, selectionArgs);
        }
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        if (DBLog.isLoggable(LogLevel.DEBUG)) {
            DBLog.d(TAG_NAME, "Method[update] start");
            DBLog.d(TAG_NAME, "parameter uri=[" + uri + "]");
        }

        if (isItemUri(uri)) {
            long id = ContentUris.parseId(uri);
            Class<? extends Entity> clazz = getEntityClass(uri);
            ModifyExecuteLister listener = newModifyStatementExecuteListener(clazz);
            return dmlExecutor.updateById(clazz, values, id, listener);
        } else {
            Class<? extends Entity> clazz = getEntityClass(uri);
            ModifyExecuteLister listener = newModifyStatementExecuteListener(clazz);
            // SQLを実行する
            return dmlExecutor.updateByWhereArrayStringArgs(clazz, values, selection, listener, selectionArgs);
        }
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        txManager.beginTransactionWithListenerNonExclusive(newModifyStatementExecuteListenerForApplyBatch());
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                final ContentProviderOperation operation = operations.get(i);
                if (i > 0 && operation.isYieldAllowed()) {
                    txManager.yieldIfContendedSafely(getYieldDelay());
                }
                results[i] = operation.apply(this, results, i);
            }
            txManager.setTransactionSuccessful();
            return results;
        } finally {
            txManager.endTransaction();
        }
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        throw new UnsupportedOperationException("method [" + method + "] not supported");
    }

    /**
     * ModifyExecuteListerを生成する
     *
     * @return
     */
    private ModifyExecuteLister newModifyStatementExecuteListenerForApplyBatch() {
        ModifyExecuteLister listener = new TransactionManager.DefaultModifyExecuteListener() {
            @Override
            public void onCommit() {
                uriManager.notifyChange(getContext());
            }
        };
        return listener;
    }

    protected int getYieldDelay() {
        return DEFAULT_DELAY;
    }

    /**
     * ModifyExecuteListerを生成する
     *
     * @param clazz
     * @return
     */
    private ModifyExecuteLister newModifyStatementExecuteListener(final Class<? extends Entity> clazz) {
        ModifyExecuteLister listener = new TransactionManager.DefaultModifyExecuteListener() {
            @Override
            public void onPostExecuteQeury() {
                uriManager.addNotifyChangeUri(clazz);
            }

            @Override
            public void onCommit() {
                uriManager.notifyChange(getContext());
            }
        };
        return listener;
    }

    /**
     * <p>
     * トランザクション処理を行う<br>
     * 期待したパラメータではない場合は、<code>IllegalArgumentException</code>をスローする
     * </p>
     *
     * @param transaction 0:トランザクション開始,1:トランザクションコミット,2:トランザクション終了
     * @throws IllegalArgumentException
     */
    private Cursor processTransaction(String transaction) {
        // パラメータtransactionが設定されている場合の処理
        if (TextUtils.isEmpty(transaction)) {
            throw new IllegalArgumentException("Method transaction parameter is Empty");
        }
        if ("0".equals(transaction)) {
            txManager.beginTransactionNonExclusive();
        } else if ("1".equals(transaction)) {
            txManager.setTransactionSuccessful();
            uriManager.notifyChange(getContext());
        } else if ("2".equals(transaction)) {
            txManager.endTransaction();
            uriManager.clearUri();
        } else {
            // 例外を通知する
            throw new IllegalArgumentException(String.format(
                    "Parameter [transaction] is Illegal value = [%1$s]",
                    new Object[]{transaction}));
        }
        return null;
    }

    /**
     * <p>
     * トランザクションを解放し、解放待ちとなっていたスレッドが、<br>
     * DB処理を実行できるようにする<br>
     * ※注.この処理の前にsetTransactionSuccessfulを呼び出すと、YieidIfContendedSafely呼び出しの際に、<br>
     * 新規トランザクションが返されるが、成功がマークされなくなる。
     * </p>
     *
     * @param yieldIfContended
     */
    private Cursor processYieidIfContendedSafely(String yieldIfContended) {
        boolean result = false;
        if (!TextUtils.isDigitsOnly(yieldIfContended)) {
            throw new IllegalArgumentException(String.format(
                    "Parameter is Illegal value = [%1$s]",
                    new Object[]{yieldIfContended}));
        }

        int sleepAfterYieldDelay = Integer.parseInt(yieldIfContended);
        // パラメータtransactionが設定されている場合の処理
        if (sleepAfterYieldDelay == 0) {
            result = txManager.yieldIfContendedSafely();
            if (result) {
                uriManager.notifyChange(getContext());
            }
        } else if (sleepAfterYieldDelay > 0) {
            result = txManager.yieldIfContendedSafely(sleepAfterYieldDelay);
            if (txManager.yieldIfContendedSafely(sleepAfterYieldDelay)) {
                uriManager.notifyChange(getContext());
            }
        }
        MatrixCursor cursor = DatabaseCompat.newMatrixCursor(new String[]{"RESULT"}, 1);
        String resultString = String.valueOf(result ? 1 : 0);
        cursor.addRow(new String[]{resultString});
        return cursor;
    }

    /**
     * SelectExecuteListerを生成する
     *
     * @param uri
     * @return
     */
    private SelectExecuteLister createSelectStatementExecuteListener(final Uri uri) {
        SelectExecuteLister listener = new TransactionManager.DefaultSelectExecuteListener() {
            @Override
            public void onPostExecuteQeury(Cursor cursor) {
                // ## values Uri
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            }
        };
        return listener;
    }

    private boolean isItemUri(Uri uri) {
        final int code = uriMatcer.match(uri);
        if (code == UriMatcher.NO_MATCH) {
            return false;
        }
        return (1 == code % 2);
    }

    private Class<? extends Entity> getEntityClass(Uri uri) {
        final int code = uriMatcer.match(uri);
        if (code != UriMatcher.NO_MATCH) {
            return typeCodes.get(code);
        }
        return null;
    }

    abstract protected DatabaseConfigration newDatabaseConfigration();

    /**
     * get authority from AndroidManifest.xml
     *
     * @param context       context
     * @param providerClass providerClass
     * @return authority
     */
    @SuppressLint("NewApi")
    private String getAuthority(Context context, Class<? extends ContentProvider> providerClass) {
        if (authority != null) {
            return authority;
        }
        PackageManager manager = context.getApplicationContext().getPackageManager();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                ProviderInfo providerInfo = manager.getProviderInfo(
                        new ComponentName(context, providerClass),
                        PackageManager.GET_META_DATA);
                if (providerInfo != null) {
                    authority = providerInfo.authority;
                    return authority;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            DBLog.e(TAG_NAME, e.getMessage(), e);
        }
        // ## getAuthority
        authority = getAuthority();
        return authority;
    }

    /**
     * ProviderConnectionSourceのコネクションソースを生成する
     *
     * @param authority           authority
     * @param databaseConnnection Connection of database
     * @return
     */
    protected DatabaseProviderConnectionSource newProviderConnectionSource(String authority, DatabaseConnectionSource databaseConnnection) {
        return new DatabaseProviderConnectionSource.Builder()
                .authority(authority)
                .databaseName(databaseConnnection.getDataBaseName())
                .isDefaultAuthority(true)
                .entityClasses(databaseConnnection.getEntityClasses())
                .build();
    }

    @SuppressWarnings("deprecation")
    protected String getAuthority() {
        throw new IllegalStateException("Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD Build.VERSION=["
                + Build.VERSION.SDK + "] ");
    }

    class NotificationUriManager {
        // URIを管理するSET
        private Set<Uri> notifyChangeUriSet;
        // NofityTargetUri
        private Map<Class<? extends Entity>, Set<Uri>> nofityTargetUris;

        // =================================================================
        // Constructor
        // =================================================================
        NotificationUriManager() {
            notifyChangeUriSet = Collections.synchronizedSet(new LinkedHashSet<Uri>());
        }

        void registerNotifyUri(Class<? extends Entity> entityClass, String authority) {
            if (nofityTargetUris == null) {
                nofityTargetUris = new HashMap<Class<? extends Entity>, Set<Uri>>();
            }

            // ## create UriList
            NoticeTargets targets = entityClass.getAnnotation(NoticeTargets.class);
            if (targets != null) {
                for (NoticeTarget target : targets.values()) {
                    putNotifyTargetUris(entityClass, authority, target);
                }
            }
            NoticeTarget target = entityClass.getAnnotation(NoticeTarget.class);
            if (target != null) {
                putNotifyTargetUris(entityClass, authority, target);
            }

            Table table = entityClass.getAnnotation(Table.class);
            if (table != null) {
                if (table.canNotifySelf()) {
                    Set<Uri> uris = nofityTargetUris.get(entityClass);
                    if (uris == null) {
                        uris = new LinkedHashSet<Uri>();
                    }
                    Uri authUri = UriUtils.buildAuthorityUri(authority);
                    TableInfo info = MonoTalk.getTableInfo(entityClass);
                    String entityPath = info.getTableName().toLowerCase(Locale.getDefault());
                    uris.add(UriUtils.buildEntityUri(authUri, entityPath));
                    nofityTargetUris.put(entityClass, uris);
                }
            }
        }

        void putNotifyTargetUris(Class<? extends Entity> entityClass, String authority, NoticeTarget target) {
            if (authority.equals(target.authority())) {
                Set<Uri> uris = nofityTargetUris.get(entityClass);
                if (uris == null) {
                    uris = new LinkedHashSet<Uri>();
                }
                for (Class<? extends Entity> clazz : target.targetClasses()) {
                    Uri authUri = UriUtils.buildAuthorityUri(target.authority());
                    TableInfo info = MonoTalk.getTableInfo(entityClass);
                    String entityPath = info.getTableName().toLowerCase(Locale.getDefault());
                    uris.add(UriUtils.buildEntityUri(authUri, entityPath));
                }
                nofityTargetUris.put(entityClass, uris);
            }
        }

        /**
         * 通知先URIを登録する
         *
         * @param clazz
         */
        void addNotifyChangeUri(Class<? extends Entity> clazz) {
            Set<Uri> uris = nofityTargetUris.get(clazz);
            addNotifyChange(uris);
        }

        /**
         * 通知先URIを登録する
         *
         * @param uris
         */
        void addNotifyChange(Set<Uri> uris) {
            synchronized (notifyChangeUriSet) {
                for (Uri notifyUri : uris) {
                    notifyChangeUriSet.add(notifyUri);
                }
            }
        }

        /**
         * <p>
         * 変更通知先のクリアをする
         * </p>
         */
        void clearUri() {
            synchronized (notifyChangeUriSet) {
                notifyChangeUriSet.clear();
            }
        }

        /**
         * <p>
         * 変更を通知する
         * </p>
         */
        void notifyChange(Context context) {
            synchronized (notifyChangeUriSet) {
                for (Uri notifyUri : notifyChangeUriSet) {
                    context.getContentResolver().notifyChange(notifyUri, null);
                }
            }
        }

    }
}