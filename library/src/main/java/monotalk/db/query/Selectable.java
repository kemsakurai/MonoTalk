package monotalk.db.query;

import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import java.util.List;

import monotalk.db.Entity;
import monotalk.db.LazyList;
import monotalk.db.rowmapper.RowListMapper;
import monotalk.db.rowmapper.RowMapper;

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

public interface Selectable {
    public Cursor selectCursor();

    public <T extends Entity> T selectOne();

    public <T extends Entity> List<T> selectList();

    public <T extends Entity> LazyList<T> selectLazyList();

    public <T extends Object> T selectOne(RowMapper<T> mapper);

    public <T extends Object> List<T> selectList(RowListMapper<T> mapper);

    public <T extends Object> T selectScalar(Class<T> clazz);

    public CursorLoader buildLoader();
}