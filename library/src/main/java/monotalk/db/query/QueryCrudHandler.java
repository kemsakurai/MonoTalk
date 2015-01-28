package monotalk.db.query;

import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import java.util.List;

import monotalk.db.Entity;
import monotalk.db.LazyList;
import monotalk.db.querydata.DeleteQueryData;
import monotalk.db.querydata.InsertQueryData;
import monotalk.db.querydata.SelectQueryData;
import monotalk.db.querydata.TwoWayQueryData;
import monotalk.db.querydata.UpdateQueryData;
import monotalk.db.rowmapper.RowListMapper;
import monotalk.db.rowmapper.RowMapper;

/**
 * Created by Kem on 2015/01/11.
 */
public interface QueryCrudHandler {

    /**
     * データを更新する
     *
     * @param data 更新クエリのデータ
     * @return
     */
    public int update(UpdateQueryData data);

    /**
     * データを登録する
     *
     * @param data InsertQueryのパラメータ
     * @return
     */
    public Long insert(InsertQueryData data);

    /**
     * @param clazz
     * @param data
     * @param <T>
     * @return
     */
    public <T extends Entity> List<T> selectList(Class<T> clazz, SelectQueryData data);

    /**
     * @param clazz
     * @param data
     * @param <T>
     * @return
     */
    public <T extends Entity> LazyList<T> selectLazyList(Class<T> clazz, SelectQueryData data);

    /**
     * Cursorローダを生成します。
     *
     * @param data
     * @return
     */
    public CursorLoader buildLoader(SelectQueryData data);

    /**
     * パラメータを元にSQLを実行し、Entityを返します。
     *
     * @param type
     * @param data
     * @param <T>
     * @return
     */
    public <T extends Entity> T selectOne(Class<T> type, SelectQueryData data);

    /**
     * パラメータを元にSQLを実行し、Cursorを返します。
     *
     * @param data
     * @return
     */
    public Cursor selectCursor(SelectQueryData data);

    /**
     * @param mapper
     * @param data
     * @param <T>
     * @return
     */
    public <T> T selectOne(RowMapper<T> mapper, SelectQueryData data);

    /**
     * @param mapper
     * @param data
     * @return
     */
    public <T> List<T> selectList(RowListMapper<T> mapper, SelectQueryData data);

    /**
     * @param clazz
     * @param data
     * @param <T>
     * @return
     */
    public <T> T selectScalar(Class<T> clazz, SelectQueryData data);


    /**
     * @param data
     * @return
     */
    public CursorLoader buildLoader(TwoWayQueryData data);

    /**
     * @param data
     * @return
     */
    public Cursor selectCursorBySqlFile(TwoWayQueryData data);

    /**
     * SqlファイルのSqlを実行し、Entityを取得する
     *
     * @param type
     * @param data
     * @param <T>
     */
    public <T extends Entity> T selectOneBySqlFile(Class<T> type, TwoWayQueryData data);

    /**
     * @param type
     * @param data
     * @param <T>
     * @return
     */
    public <T extends Entity> List<T> selectListBySqlFile(Class<T> type, TwoWayQueryData data);

    /**
     * Entityを取得する
     *
     * @param mapper
     * @param data
     * @param <E>
     * @return
     */
    public <E> E selectOneBySqlFile(RowMapper<E> mapper, TwoWayQueryData data);

    /**
     * @param mapper
     * @param data
     * @return
     */
    public <E> List<E> selectListBySqlFile(RowListMapper<E> mapper, TwoWayQueryData data);

    /**
     * @param clazz
     * @param data
     * @param <E>
     * @return
     */
    public <E> E selectScalarBySqlFile(Class<E> clazz, TwoWayQueryData data);

    /**
     * @param param
     * @return
     */
    public int delete(DeleteQueryData param);

    /**
     *
     * @param type
     * @param data
     * @param <T>
     * @return
     */
    public <T extends Entity> LazyList<T> selectLazyList(Class<T> type, TwoWayQueryData data);
}
