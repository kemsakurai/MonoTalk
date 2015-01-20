package monotalk.db.querydata;

import org.seasar.dbflute.cbean.SimpleMapPmb;

public class TwoWayQueryData extends BaseQueryData {
    /**
     * sqlFilePath
     */
    private String sqlFilePath;
    /**
     * ParameterBean
     */
    private SimpleMapPmb<Object> mapPmb;

    public String getSqlFilePath() {
        return sqlFilePath;
    }

    public void setSqlFilePath(String sqlFilePath) {
        this.sqlFilePath = sqlFilePath;
    }

    public SimpleMapPmb<Object> getMapPmb() {
        return mapPmb;
    }

    public void setMapPmb(SimpleMapPmb<Object> mapPmb) {
        this.mapPmb = mapPmb;
    }

}
