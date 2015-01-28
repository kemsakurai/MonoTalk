package monotalk.db.querydata;

import android.net.Uri;
import android.text.TextUtils;

import org.seasar.dbflute.cbean.SimpleMapPmb;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    /**
     * @param authorityUri
     * @return
     */
    public Uri buildTwoWaySqlUri(Uri authorityUri) {
        // create baseUri
        Uri.Builder builder = authorityUri.buildUpon();
        builder.path("/" + tableName.toLowerCase(Locale.getDefault()));

        // SQLIDが指定されている場合は、SQLIDを設定
        if (!TextUtils.isEmpty(sqlFilePath)) {
            builder.appendQueryParameter("_sqlFilePath", sqlFilePath);
        }

        // mapPmbが設定されている場合は、mapPmbを設定する
        if (mapPmb != null) {
            for (Map.Entry<String, Object> entry : mapPmb.getParameterMap().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Iterable) {
                    @SuppressWarnings("rawtypes")
                    Iterable elems = (Iterable) value;
                    for (Object elem : elems) {
                        builder.appendQueryParameter(key, String.valueOf(elem));
                    }
                } else {
                    if (value != null) {
                        builder.appendQueryParameter(key, String.valueOf(value));
                    }
                }
                // create Key List
                builder.appendQueryParameter("_key", key);
            }
        }
        return builder.build();
    }

    /**
     * インスタンスを生成する
     *
     * @param sqlFilePath
     * @param uri
     * @return
     */
    public static TwoWayQueryData newInstance(String sqlFilePath, Uri uri) {
        TwoWayQueryData data = new TwoWayQueryData();
        SimpleMapPmb<Object> mapPmb = new SimpleMapPmb<Object>();
        List<String> keys = uri.getQueryParameters("_key");
        for (String key : keys) {
            List<String> values = uri.getQueryParameters(key);
            if (values.isEmpty()) {
                mapPmb.addParameter(key, null);
            } else if (values.size() == 1) {
                mapPmb.addParameter(key, values.get(0));
            } else if (values.size() > 1) {
                mapPmb.addParameter(key, values);
            }
        }
        data.setMapPmb(mapPmb);
        data.setSqlFilePath(sqlFilePath);
        return data;
    }
}
