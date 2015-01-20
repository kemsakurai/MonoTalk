package monotalk.db.query;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.querydata.DeleteQueryData;

/*
 * Copyright (C) 2014 Kem
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

public final class Delete implements Sqlable {

    QueryCrudHandler crudHandler;

    public Delete(QueryCrudHandler crudHandler) {
        this.crudHandler = crudHandler;
    }

    public <T extends Entity> From<T> from(Class<T> table) {
        return new From<T>(table, this);
    }

    @Override
    public String toSql() {
        return "DELETE ";
    }

    public class From<T extends Entity> extends AbstractFrom<T, From<T>> implements Sqlable,
            Executable<Integer> {

        From(Class<T> table, Sqlable queryBase) {
            super(table, queryBase);
        }

        @Override
        public String toSql() {
            StringBuilder sql = new StringBuilder();
            addQueryBase(sql);
            addFrom(sql);
            addWhere(sql);
            return sql.toString();
        }

        @Override
        public Integer execute() {
            DeleteQueryData param = newDeleteQueryData();
            int updateCount = crudHandler.delete(param);
            return updateCount;
        }

        private DeleteQueryData newDeleteQueryData() {
            DeleteQueryData param = new DeleteQueryData();
            StringBuilder sql = new StringBuilder();
            sql.append(MonoTalk.getTableName(mType)).append(" ");
            if (mAlias != null) {
                sql.append("AS ");
                sql.append(mAlias);
                sql.append(" ");
            }
            param.setTableName(sql.toString());
            param.setSelectionArgs(selection.getSelectionArgs());
            param.setWhere(selection.getSelection());
            return param;
        }
    }
}