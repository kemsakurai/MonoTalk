package monotalk.db.query;

import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import org.seasar.dbflute.cbean.SimpleMapPmb;

import java.util.List;
import java.util.Locale;

import monotalk.db.Entity;
import monotalk.db.LazyList;
import monotalk.db.MonoTalk;
import monotalk.db.querydata.TwoWayQueryData;
import monotalk.db.rowmapper.RowListMapper;
import monotalk.db.rowmapper.RowMapper;
import monotalk.db.utility.ResourceUtis;

public class TwoWayQuerySelect<T extends Entity> implements Selectable {
    private SimpleMapPmb<Object> mapPmb;
    private String sqlFilePath;
    private QueryCrudHandler manager;
    private Class<T> type;

    public TwoWayQuerySelect(QueryCrudHandler manager, Class<T> entity, String sqlFilePath) {
        this.sqlFilePath = ResourceUtis.getSqlFilePathPrefix(entity) + sqlFilePath;
        this.manager = manager;
        this.type = entity;
    }

    public TwoWayQuerySelect<T> setParameter(String key, Object value) {
        if (mapPmb == null) {
            mapPmb = new SimpleMapPmb<Object>();
        }
        mapPmb.addParameter(key, value);
        return this;
    }

    private TwoWayQueryData createQueryData() {
        TwoWayQueryData data = new TwoWayQueryData();
        String tableName = MonoTalk.getTableName(type).toLowerCase(Locale.getDefault());
        data.setTableName(tableName);
        data.setMapPmb(mapPmb);
        data.setSqlFilePath(sqlFilePath);
        return data;
    }

    @Override
    public CursorLoader buildLoader() {
        TwoWayQueryData data = createQueryData();
        return manager.buildLoader(data);
    }

    @Override
    public Cursor selectCursor() {
        TwoWayQueryData data = createQueryData();
        return manager.selectCursorBySqlFile(data);
    }

    @Override
    public LazyList<T> selectLazyList() {
        TwoWayQueryData data = createQueryData();
        return manager.selectLazyList(type, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T selectOne() {
        TwoWayQueryData data = createQueryData();
        return (T) manager.selectOneBySqlFile(type, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> selectList() {
        TwoWayQueryData data = createQueryData();
        return manager.selectListBySqlFile(type, data);
    }

    @Override
    public <E> E selectOne(RowMapper<E> mapper) {
        TwoWayQueryData data = createQueryData();
        return manager.selectOneBySqlFile(mapper, data);
    }

    @Override
    public <E> E selectScalar(Class<E> clazz) {
        TwoWayQueryData data = createQueryData();
        return manager.selectScalarBySqlFile(clazz, data);
    }

    @Override
    public <E> List<E> selectList(RowListMapper<E> mapper) {
        TwoWayQueryData data = createQueryData();
        return manager.selectListBySqlFile(mapper, data);
    }
}
