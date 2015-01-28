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

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConfigration {

    private String dataBaseName;
    private boolean isDefaultDatabase = true;
    private int nodeCacheSize;
    private List<Class<? extends Entity>> entityList;
    private SparseArray<Migration> migrations;
    private int version;

    DatabaseConfigration(String dataBaseName, int version,
                         int nodeCacheSize, List<Class<? extends Entity>> entityList, boolean isDefalutDatabase, SparseArray<Migration> migrations) {
        super();
        this.dataBaseName = dataBaseName;
        this.version = version;
        this.nodeCacheSize = nodeCacheSize;
        this.entityList = entityList;
        this.isDefaultDatabase = isDefalutDatabase;
        this.migrations = migrations;
    }

    public SparseArray<Migration> getMigrations() {
        return migrations;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public int getNodeCacheSize() {
        return nodeCacheSize;
    }

    public List<Class<? extends Entity>> getEntityList() {
        return entityList;
    }

    public int getVersion() {
        return version;
    }

    public boolean isDefaultDatabase() {
        return isDefaultDatabase;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2
                .append("DatabaseConfigration [dataBaseName=")
                .append(dataBaseName)
                .append(", isDefaultDatabase=")
                .append(isDefaultDatabase)
                .append(", nodeCacheSize=")
                .append(nodeCacheSize)
                .append(", entityList=")
                .append(entityList)
                .append(", version=")
                .append(version)
                .append("]");
        return builder2.toString();
    }

    // ===================================================================================
    // Builder
    // ===================================================================================
    public static class Builder {
        private static final int DEFAULT_NODE_CACHE_SIZE = 50;
        // ===================================================================================
        // Member Filed
        // ===================================================================================
        private String dataBaseName;
        private boolean isDefalutDatabase;
        private int nodeCacheSize;
        private List<Class<? extends Entity>> entityList;
        private int version;
        private SparseArray<Migration> migrations;

        // ===================================================================================
        // Constructor
        // ===================================================================================
        public Builder() {
            nodeCacheSize = DEFAULT_NODE_CACHE_SIZE;
        }

        // ===================================================================================
        // public method
        // ===================================================================================
        public Builder addTable(Class<? extends Entity> table) {
            if (entityList == null) {
                entityList = new ArrayList<Class<? extends Entity>>();
            }
            entityList.add(table);
            return this;
        }

        public DatabaseConfigration create() {
            return new DatabaseConfigration(
                    dataBaseName,
                    version,
                    nodeCacheSize,
                    entityList,
                    isDefalutDatabase,
                    migrations);
        }

        public void addMigration(int index, Migration migration) {
            if (migrations == null) {
                migrations = new SparseArray<Migration>();
            }
            migrations.append(index, migration);
        }

        public Builder setDataBaseName(String dataBaseName) {
            this.dataBaseName = dataBaseName;
            return this;
        }

        public Builder setDefalutDatabase(boolean isDefalutDatabase) {
            this.isDefalutDatabase = isDefalutDatabase;
            return this;
        }

        public Builder setNodeCacheSize(int nodeCacheSize) {
            this.nodeCacheSize = nodeCacheSize;
            return this;
        }

        public Builder setEntityList(List<Class<? extends Entity>> entities) {
            entityList = entities;
            return this;
        }

        public Builder setVersion(int version) {
            this.version = version;
            return this;
        }
    }
}
