/*
* Copyright (C) 2014 Kem
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
import android.database.sqlite.SQLiteOpenHelper;

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
     * initialize method
     *
     * @param context
     */
    public static void init(Context context) {
        LogLevel level = ResourceUtis.getLogLevel(context);
        boolean assertEntity = ResourceUtis.isAsertEntity(context);
        init(context, level, assertEntity);
    }

    /**
     * @param context
     * @param logLevel
     * @param assertEntity
     */
    public synchronized static void init(Context context, LogLevel logLevel, boolean assertEntity) {
        if (initialized) {
            DBLog.w(TAG_NAME, "Already initialized.");
            return;
        }
        // ## values LogLevel
        DBLog.setLogLevel(logLevel);
        MonoTalk.context = context.getApplicationContext();
        AssertUtils.setAssertEntity(assertEntity);
    }

    /**
     * initialize method
     *
     * @param context
     * @param logLevel
     */
    public static void init(Context context, LogLevel logLevel) {
        boolean assertEntity = ResourceUtis.isAsertEntity(context);
        init(context, logLevel, assertEntity);
    }

    /**
     * initialize method
     *
     * @param context
     * @param assertEntity
     */
    public static void init(Context context, boolean assertEntity) {
        LogLevel level = ResourceUtis.getLogLevel(context);
        init(context, level, assertEntity);
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
        for (Class<? extends Entity> model : config.getEntityList()) {
            TableInfo info = new TableInfo(model);
            infos.put(model, info);
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
        DBLog.i(TAG_NAME, connectionSource.toString());
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
     * @return
     */
    public static EntityManager getDBHelperManagerByDefaultDbName() {
        return nameMappedFactory.newEntityManager(EntityManagerType.DB_OPEN_HELPER);
    }

    /**
     * @param name
     * @return
     */
    public static EntityManager getDBHelperManagerByDbName(String name) {
        return nameMappedFactory.newEntityManager(name, EntityManagerType.DB_OPEN_HELPER);
    }

    /**
     * @return
     */
    public static SQLiteOpenHelper getDbHelperByDefaultDbName() {
        return nameMappedFactory.getSQLiteOpenHelper();
    }

    /**
     * @param name
     * @return
     */
    public static SQLiteOpenHelper getDbHelperByDbName(String name) {
        return nameMappedFactory.getSQLiteOpenHelper(name);
    }

    /**
     * @param type
     * @return
     */
    public static EntityManager getManagerByDefaultAuth(EntityManagerType type) {
        return authMappedFactory.newEntityManager(type);
    }

    /**
     * @param name
     * @param type
     * @return
     */
    public static EntityManager getManagerByAuth(String name, EntityManagerType type) {
        return authMappedFactory.newEntityManager(name, type);
    }
}
