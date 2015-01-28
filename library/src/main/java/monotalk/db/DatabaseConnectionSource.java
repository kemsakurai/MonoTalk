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

import android.content.Context;
import android.support.v4.util.LruCache;

import org.seasar.dbflute.twowaysql.factory.DefaultSqlAnalyzerFactory;
import org.seasar.dbflute.twowaysql.factory.SqlAnalyzerFactory;
import org.seasar.dbflute.twowaysql.node.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import monotalk.db.utility.ResourceUtis;

import static monotalk.db.utility.StringUtils.ln;

public class DatabaseConnectionSource {
    private DatabaseOpenHelper dbHelper;
    private String dataBaseName;
    private LruCache<String, Node> nodeCache;
    private List<Class<? extends Entity>> entityClasses;
    private Map<Class<? extends Entity>, TableStatement> tableStatements;
    private int version;

    // =================================================================================================
    // TAG_NAME
    // =================================================================================================
    private static final String TAG_NAME = DBLog.getTag(DatabaseConnectionSource.class);

    DatabaseConnectionSource(Context context, DatabaseConfigration config) {
        this.version = config.getVersion();
        this.dataBaseName = config.getDataBaseName();
        this.entityClasses = config.getEntityList();
        this.dbHelper = new DatabaseOpenHelper(
                context,
                config.getDataBaseName(),
                config.getVersion(),
                config.getEntityList(),
                config.getMigrations());

        /* NodeCache */
        this.nodeCache = new LruCache<String, Node>(config.getNodeCacheSize());

        this.tableStatements = new HashMap<Class<? extends Entity>, TableStatement>();
        for (Class<? extends Entity> entity : config.getEntityList()) {
            TableStatement tableStatement = new TableStatement(entity);
            tableStatements.put(entity, tableStatement);
        }
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    protected TableStatement.TableUpdateStatement getTableUpdateStatement(Class<? extends Entity> type) {
        return tableStatements.get(type).getUpdateStatement(dbHelper.getWritableDatabase());
    }

    protected TableStatement.TableInsertStatement getTableInsertStatement(Class<? extends Entity> type) {
        return tableStatements.get(type).getInsertStatement(dbHelper.getWritableDatabase());
    }

    protected monotalk.db.TableStatement.TableDeleteStatement getTableDeleteStatement(Class<? extends Entity> type) {
        return tableStatements.get(type).getDeleteStatement(dbHelper.getWritableDatabase());
    }

    protected TableStatement.TableSelectStatement getTableSelectStatement(Class<? extends Entity> type) {
        return tableStatements.get(type).getSelectStatement(dbHelper.getReadableDatabase());
    }

    public DatabaseOpenHelper getDbHelper() {
        return dbHelper;
    }

    public List<Class<? extends Entity>> getEntityClasses() {
        return entityClasses;
    }

    // adapter method
    public synchronized Node getNode(Context context, String sqlFilePath) {
        Node node = nodeCache.get(sqlFilePath);
        if (node == null) {
            // get SQL from Resource
            String toWaySql = ResourceUtis.getSQL(context, sqlFilePath);
            // Analyze SQL
            SqlAnalyzerFactory factory = new DefaultSqlAnalyzerFactory();
            node = factory.create(toWaySql, true).analyze();
            nodeCache.put(sqlFilePath, node);
        }
        return node;
    }

    public synchronized Node getNode(String sqlFilePath) {
        return getNode(MonoTalk.getContext(), sqlFilePath);
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DatabaseConnectionSource [").append(ln())
                .append(", dbHelper=").append(dbHelper).append(ln())
                .append(", dataBaseName=").append(dataBaseName).append(ln())
                .append(", nodeCache=").append(nodeCache).append(ln())
                .append(", entityClasses=").append(entityClasses).append(ln())
                .append(", version=").append(version).append(ln())
                .append("]");
        return builder.toString();
    }
}
