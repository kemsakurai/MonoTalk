/*
* Copyright (C) 2013-2015 Kem
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package monotalk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import monotalk.db.DBLog.LogLevel;
import monotalk.db.manager.AuthNameMappedEntityManagerFactory;
import monotalk.db.manager.DBNameMappedEntityManagerFactory;
import monotalk.db.manager.EntityManager;
import monotalk.db.manager.EntityManagerType;
import monotalk.db.utility.AssertUtils;
import monotalk.db.utility.ResourceUtis;

import static monotalk.db.utility.AssertUtils.assertNotNull;

public class MonoTalk {
    // =================================================================================================
    // TAG_NAME
    // =================================================================================================
    private static final String TAG_NAME = DBLog.getTag(MonoTalk.class);

    // =================================================================================================
    // static Field
    // =================================================================================================
    private static Context context;
    private static boolean initialized = false;
    private static AuthNameMappedEntityManagerFactory authMappedFactory = new AuthNameMappedEntityManagerFactory();
    private static DBNameMappedEntityManagerFactory nameMappedFactory = new DBNameMappedEntityManagerFactory();
    private static Map<Class<? extends Entity>, TableInfo> tableInfoMap = new ConcurrentHashMap<Class<? extends Entity>, TableInfo>();

    // =================================================================================================
    // Constructor
    // =================================================================================================
    private MonoTalk() {
    }

    // =================================================================================================
    // static Method
    // =================================================================================================
    public static void dispose() {
        authMappedFactory = new AuthNameMappedEntityManagerFactory();
        nameMappedFactory = new DBNameMappedEntityManagerFactory();
        tableInfoMap = new ConcurrentHashMap<Class<? extends Entity>, TableInfo>();
        initialized = false;
        context = null;
    }

    public static String getTableName(Class<? extends Entity> key) {
        TableInfo info = getTableInfo(key);
        return info.getTableName();
    }

    public static TableInfo getTableInfo(Class<? extends Entity> key) {
        TableInfo info = tableInfoMap.get(key);
        if (info == null) {
            DBLog.w(TAG_NAME, "TableInfo is Null keyClass= [ " + key.getName() + " ]");
        }
        return info;
    }

    /**
     * 初期化メソッド
     *
     * @param context アプリケーションコンテキスト
     * @param config
     */
    public static DatabaseConnectionSource init(Context context, DatabaseConfigration config) {
        LogLevel level = ResourceUtis.getLogLevel(context);
        boolean assertEntity = ResourceUtis.isAsertEntity(context);
        return init(context, level, assertEntity, config);
    }

    /**
     * 初期化メソッド
     *
     * @param context
     * @param logLevel
     * @param assertEntity
     * @param config
     */
    public synchronized static DatabaseConnectionSource init(Context context, LogLevel logLevel, boolean assertEntity, DatabaseConfigration config) {
        if (initialized) {
            DBLog.i(TAG_NAME, "Already initialized.");
        } else {
            // ## set LogLevel
            DBLog.setLogLevel(logLevel);
            // ## set doAssert
            AssertUtils.setAssertEntity(assertEntity);
            // ## set Context
            MonoTalk.context = context.getApplicationContext();
        }
        return registerDatabaseConnectionSource(config);
    }

    /**
     * initialize method
     *
     * @param context
     * @param logLevel
     * @param config
     */
    public static DatabaseConnectionSource init(Context context, LogLevel logLevel, DatabaseConfigration config) {
        boolean assertEntity = ResourceUtis.isAsertEntity(context);
        return init(context, logLevel, assertEntity, config);
    }

    /**
     * initialize method
     *
     * @param context
     * @param assertEntity
     * @param config
     */
    public static DatabaseConnectionSource init(Context context, boolean assertEntity, DatabaseConfigration config) {
        LogLevel level = ResourceUtis.getLogLevel(context);
        return init(context, level, assertEntity, config);
    }

    /**
     * @param config
     * @return
     */
    public static DatabaseConnectionSource registerDatabaseConnectionSource(DatabaseConfigration config) {
        assertNotNull(getContext(), "Context is Null");
        assertNotNull(config, "config is Null");

        // Assert Entity Definition
        if (AssertUtils.canAssertEntity()) {
            AssertUtils.assertAnnotation(config);
        }

        /* tableInfo */
        Map<Class<? extends Entity>, TableInfo> infos = new LinkedHashMap<Class<? extends Entity>, TableInfo>();
        for (Class<? extends Entity> entity : config.getEntityList()) {
            TableInfo info = new TableInfo(entity);
            infos.put(entity, info);
        }
        tableInfoMap.putAll(infos);

        // newDatabaseConnectionSource
        DatabaseConnectionSource connectionSource = new DatabaseConnectionSource(getContext(), config);

	    /* values isDefaultDatabase */
        if (config.isDefaultDatabase()) {
            nameMappedFactory.setDefaultDatabaseName(connectionSource.getDataBaseName());
        }
        if (nameMappedFactory.isAlreadyRegistered(connectionSource.getDataBaseName())) {
            DBLog.i(TAG_NAME, "It is already registered key . Overrides registration . key value= [" + connectionSource.getDataBaseName() + "] ");
        }
        nameMappedFactory.registerDatabaseConnectionSource(connectionSource.getDataBaseName(), connectionSource);
        DBLog.i(TAG_NAME, "---registerDatabaseConnectionSource---," + connectionSource.toString());
        return connectionSource;
    }

    /**
     * @return
     */
    public static Context getContext() {
        return context;
    }

    /**
     * @param providerConnectionSource
     * @return
     */
    public static DatabaseProviderConnectionSource registerProvierConnnectionSource(DatabaseProviderConnectionSource providerConnectionSource) {

        assertNotNull(providerConnectionSource, "providerConnectionSource is Null");
        String authority = providerConnectionSource.getAuthority();

        if (authMappedFactory.isAlreadyRegistered(authority)) {
            DBLog.i(TAG_NAME, "It is already registered key . Overrides registration . key value= [" + authority + "] ");
        }
        authMappedFactory.registerProvierConnectionSource(authority, providerConnectionSource);
        if (providerConnectionSource.isDefaultAuthority()) {
            authMappedFactory.setDefaultAuthority(authority);
        }
        DBLog.i(TAG_NAME, providerConnectionSource.toString());
        return providerConnectionSource;
    }

    // -------------------------------------------------------------
    // EntityManager getter
    // -------------------------------------------------------------

    /**
     * DefaultDatabaseに紐付けられたEntityManagerを返します。
     *
     * @return
     */
    public static EntityManager getDBManagerByDefaultDbName() {
        return nameMappedFactory.newEntityManager(EntityManagerType.DB_OPEN_HELPER);
    }

    /**
     * Database名を元に紐づけられEntityManagerを返します。
     *
     * @param name database名
     * @return EntityManager
     */
    public static EntityManager getDBManagerByDbName(String name) {
        return nameMappedFactory.newEntityManager(name, EntityManagerType.DB_OPEN_HELPER);
    }

    /**
     * DefaultDatabaseに紐付けられたSQLiteDatabaseを返します。
     *
     * @return SQLiteDatabase
     */
    public static SQLiteDatabase getWritableDatabaseByDefaultDbName() {
        return nameMappedFactory.getSQLiteOpenHelper().getWritableDatabase();
    }

    /**
     * Database名を元に紐づけられたSQLiteDatabaseを返します。
     *
     * @param name database名
     * @return
     */
    public static SQLiteDatabase getWritableDatabaseByDbName(String name) {
        return nameMappedFactory.getSQLiteOpenHelper(name).getWritableDatabase();
    }

    /**
     * DefaultAuthorityに紐付けられたEntityManagerを返します。
     *
     * @param type EntityManagerType
     * @return
     */
    public static EntityManager getManagerByDefaultAuth(EntityManagerType type) {
        return authMappedFactory.newEntityManager(type);
    }

    /**
     * DefaultAuthorityに紐付けられたEntityManagerを返します。
     * 返すEntityManagerはContentProviderにアクセスします。
     *
     * @return EntityManager
     */
    public static EntityManager getDBProvierManagerByDefaultAuth() {
        return authMappedFactory.newEntityManager(EntityManagerType.CONTENTES_PROVIER);
    }

    /**
     * DefaultAuthorityに紐付けられたEntityManagerを返します。
     * 返すEntityManagerはDatabaseに直接にアクセスします。
     *
     * @return EntityManager
     */
    public static EntityManager getDBManagerByDefaultAuth() {
        return authMappedFactory.newEntityManager(EntityManagerType.DB_OPEN_HELPER);
    }

    /**
     * DefaultAuthorityに紐付けられたEntityManagerを返します。
     * 返すEntityManagerはProviderClient経由でDatabaseにアクセスします。
     *
     * @return EntityManager
     */
    public static EntityManager getDBProviderClientManagerByDefaultAuth() {
        return authMappedFactory.newEntityManager(EntityManagerType.PROVIER_CLIENT);
    }

    /**
     * Authorityに紐付けられたEntityManagerを返します。
     *
     * @param name Authority
     * @param type EntityManagerType
     * @return EntityManager
     */
    public static EntityManager getManagerByAuth(String name, EntityManagerType type) {
        return authMappedFactory.newEntityManager(name, type);
    }
}
