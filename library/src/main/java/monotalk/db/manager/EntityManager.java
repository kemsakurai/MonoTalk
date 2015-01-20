/*******************************************************************************
 * Copyright (C) 2012-2014 Kem
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
package monotalk.db.manager;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

import monotalk.db.Entity;
import monotalk.db.query.Delete;
import monotalk.db.query.DeleteOperationBuilder;
import monotalk.db.query.Insert;
import monotalk.db.query.InsertOperationBuilder;
import monotalk.db.query.Select;
import monotalk.db.query.TwoWayQuerySelect;
import monotalk.db.query.Update;
import monotalk.db.query.UpdateOperationBuilder;

public interface EntityManager {
    /**
     * beginTransaction トランザクションを開始する
     */
    public void beginTransactionNonExclusive();

    /**
     * 一括登録を実行する
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T extends Entity> int bulkInsert(List<T> entities);

    /**
     * 一括更新する
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T extends Entity> int bulkUpdate(List<T> entities);

    /**
     * 一括削除する
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T extends Entity> int bulkDelete(List<T> entities);

    /**
     * Delete文を発行するQueryBuilderクラスを生成する
     *
     * @param table Entityクラス
     * @param <T>   Entityクラスの型
     * @return Delete.From
     */
    public <T extends Entity> Delete.From<T> newDeleteFrom(Class<T> table);

    /**
     * Insert文を発行するQueryBuilderクラスを生成する
     *
     * @param table
     * @param <T>
     * @return
     */
    public <T extends Entity> Insert<T> newInsertInto(Class<T> table);

    /**
     * Select文を発行するQueryBuilderクラスを生成する
     *
     * @param columns Columnオブジェクト
     * @return Select
     */
    public Select newSelect(Select.Column... columns);

    /**
     * Select文を発行するQueryBuilderクラスを生成する
     *
     * @param columns Column名のString配列
     * @return Select
     */
    public Select newSelect(String... columns);

    public <T extends Entity> TwoWayQuerySelect<T> newSelectBySqlFile(Class<T> table, String filePath);

    public <T extends Entity> Update<T> newUpdate(Class<T> table);

    /**
     * データを更新するContentProviderOperation.Builderを作成します。
     *
     * @param clazz Entityクラス
     * @return
     */
    public UpdateOperationBuilder newUpdateOperationBuilder(Class<? extends Entity> clazz);

    /**
     * データを登録するContentProviderOperation.Builderを作成します。
     *
     * @param clazz Entityクラス
     * @return
     */
    public InsertOperationBuilder newInsertOperationBuilder(Class<? extends Entity> clazz);

    /**
     * データを削除するContentProviderOperation.Builderを作成します。
     *
     * @param clazz Entityクラス
     * @return
     */
    public DeleteOperationBuilder newDeleteOperationBuilder(Class<? extends Entity> clazz);

    /**
     * データを削除する
     *
     * @param clazz       Modelクラス名
     * @param whereClause Where句
     * @param whereArgs   パラメータ
     * @return 更新件数
     */
    public <T extends Entity> int delete(Class<T> clazz, String whereClause, Object... whereArgs);

    /**
     * データを削除する
     *
     * @param object Modelオブジェクト
     * @return 更新件数
     */
    public <T extends Entity> int delete(T object);

    /**
     * データを全て削除する
     *
     * @param clazz Modelクラス名
     * @return 更新件数
     */
    public <T extends Entity> int deleteAll(Class<T> clazz);

    /**
     * idをキーにレコードを削除する
     *
     * @param clazz モデルクラス名
     * @param id    ID
     * @return 更新件数
     */
    public <T extends Entity> int deleteById(Class<T> clazz, long id);

    /**
     * idをキーにレコードを更新する
     *
     * @param clazz
     * @param values
     * @param id
     * @param <T>
     * @return
     */
    public <T extends Entity> int updateById(Class<T> clazz, ContentValues values, long id);

    public void endTransaction();

    /**
     * データを登録する
     *
     * @param clazz
     * @param values
     * @param <T>
     * @return
     */
    public <T extends Entity> long insert(Class<T> clazz, ContentValues values);

    /**
     * データを登録する
     *
     * @param object
     * @return
     */
    public <T extends Entity> long insert(T object);

    /**
     * データを登録する
     *
     * @param object
     * @return
     */
    public <T extends Entity> long insertExcludesNull(T object);

    /**
     * テーブルのデータ件数を返す
     *
     * @param clazz Modelクラス名
     * @return データ件数
     */
    public <T extends Entity> long selectCount(Class<T> clazz);

    /**
     * テーブルのデータ件数を返す
     *
     * @param clazz Modelクラス名
     * @return データ件数
     */
    public <T extends Entity> long selectCountById(Class<T> clazz, long id);

    /**
     * データを全件取得し、取得結果をCursorで返す
     *
     * @param clazz テーブルクラスインスタンス
     * @return データの取得結果
     */
    public <T extends Entity> Cursor selectCursorAll(Class<T> clazz);

    /**
     * IDをキーにデータを取得し、取得結果をCursorで返す
     *
     * @param clazz
     * @param id
     * @return
     */
    public <T extends Entity> Cursor selectCursorById(Class<T> clazz, long id);


    /**
     * 引数を元にSQLを生成、実行しCursorを返す
     *
     * @param sql
     * @param selectionArgs
     */
    public Cursor selectCursorBySql(String sql, Object... selectionArgs);

    /**
     * テーブルからデータを全件取得する
     *
     * @param clazz Modelクラス名
     * @return データ取得結果(List)
     */
    public <T extends Entity> List<T> selectListAll(Class<T> clazz);

    /**
     * キー値を指定して、データを取得する
     *
     * @param clazz Entityクラス
     * @return データの取得結果
     */
    public <T extends Entity> T selectOneById(Class<T> clazz, long id);


    /**
     * 引数を元にSQLを生成、実行しModelを返す
     *
     * @param sql
     * @param selectionArgs
     */
    public <T extends Entity> T selectOneBySql(Class<T> clazz, String sql, Object... selectionArgs);

    /**
     * トランザクションをコミットする
     */
    public void setTransactionSuccessful();

    /**
     * データを更新する
     *
     * @param object Entity
     * @return 更新件数
     */
    public <T extends Entity> long store(T object);

    /**
     * データを更新する
     *
     * @param clazz
     * @param data
     * @param selection
     * @param selectionArgs
     * @return
     */
    public <T extends Entity> int update(Class<T> clazz, ContentValues data, String selection,
                                         Object... selectionArgs);

    /**
     * updateする
     *
     * @param object
     * @return
     */
    public <T extends Entity> int update(T object);

    /**
     * データを更新する
     *
     * @param object ModelObject
     * @return 更新件数
     */
    public <T extends Entity> int updateExcludesNull(T object);

    /**
     * yieldIfContendedSafelyを実行する
     *
     * @return true or false
     */
    public boolean yieldIfContendedSafely();

    /**
     * yieldIfContendedSafelyを実行する
     *
     * @param sleepAfterYieldDelay
     * @return true or false
     */
    public boolean yieldIfContendedSafely(int sleepAfterYieldDelay);

    /**
     * クラス内で保持するContentProviderClientをリリースする
     */
    public void release();

    /**
     * データを更新する
     *
     * @param tableName
     * @param data
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int update(String tableName, ContentValues data, String selection, Object... selectionArgs);

    /**
     * データを削除する
     *
     * @param tableName   テーブル名
     * @param whereClause Where句
     * @param whereArgs   パラメータ
     * @return 更新件数
     */
    public int delete(String tableName, String whereClause, Object... whereArgs);

    /**
     * データを登録する
     *
     * @param tableName
     * @param values
     * @return
     */
    public long insert(String tableName, ContentValues values);

}