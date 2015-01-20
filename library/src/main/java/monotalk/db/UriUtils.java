package monotalk.db;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import org.seasar.dbflute.cbean.SimpleMapPmb;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UriUtils {

    public static Uri buildAuthorityUri(String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        return uriBuilder.scheme("content").authority(authority).build();
    }

    /**
     * <p>
     * トランザクション開始URIを取得する
     * </p>
     *
     * @param clazz テーブルクラス
     * @return トランザクション開始URI
     */
    public static Uri buildBeginTransactionUri(Uri authorityUri) {
        return appendParameter(authorityUri, "_transaction", "0");
    }

    /**
     * <p>
     * トランザクション終了URIを取得する
     * </p>
     *
     * @param authorityUri テーブルクラス
     * @return トランザクション終了URI
     */
    public static Uri buildEndTransaction(Uri authorityUri) {
        Uri uri = appendParameter(authorityUri, "_transaction", "2");
        return uri;
    }

    /**
     * コンテンツプロバイダー用のTABLEのベースURIを返す
     *
     * @param authorityUri
     * @param tableName
     * @return URI
     */
    public static Uri buildEntityUri(Uri authorityUri, String tableName) {
        if (TextUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("tableName is empty");
        }
        // ===================================================
        // cut parts for URI path
        //      table="TABLE_NAME INNER JOIN ----"
        //      table="TABLE_NAME1 , TABLE_NAME2 ----"
        //      table="TABLE_NAME LEFT OUTER JOIN ----"の形式となる場合があるので、
        //      TABLE_NAMEのみを切り出している
        // ===================================================
        String tmpTableName = SQLiteDatabase.findEditTable(tableName);
        // URIを作成し返す
        return authorityUri.buildUpon().path("/" + tmpTableName.toLowerCase(Locale.getDefault())).build();
    }

    /**
     * コンテンツプロバイダーSELECTクエリ用のURIを生成する
     *
     * @param authorityUri
     * @param tableName
     * @param limit
     * @param having
     * @param groupBy
     * @param distinct
     * @return
     */
    public static Uri buildQueryUri(Uri authorityUri, String tableName, String limit, String having, String groupBy,
                                    boolean distinct) {
        // create baseUri
        Uri baseUri = buildEntityUri(authorityUri, tableName);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // LIMIT句が設定されている場合はGETパラメータに設定
        if (!TextUtils.isEmpty(limit)) {
            uriBuilder.appendQueryParameter("_limit", limit);
        }
        // HAVING句が設定されている場合はGETパラメータに設定
        if (!TextUtils.isEmpty(having)) {
            uriBuilder.appendQueryParameter("_having", having);
        }
        // GROUPBY句が設定されている場合はGETパラメータに設定
        if (!TextUtils.isEmpty(groupBy)) {
            uriBuilder.appendQueryParameter("_groupBy", groupBy);
        }
        // GROUPBY句が設定されている場合はGETパラメータに設定
        uriBuilder.appendQueryParameter("_distinct", Boolean.toString(distinct));
        return uriBuilder.build();
    }

    /**
     * @param authorityUri
     * @param sqlFilePath
     * @param mapPmb
     * @return
     */
    public static Uri buildTwoWaySqlUri(Uri authorityUri, String tableName, String sqlFilePath, SimpleMapPmb<Object> mapPmb) {
        // create baseUri
        Uri baseUri = buildEntityUri(authorityUri, tableName);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // SQLIDが指定されている場合は、SQLIDを設定
        if (!TextUtils.isEmpty(sqlFilePath)) {
            uriBuilder.appendQueryParameter("_sqlFilePath", sqlFilePath);
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
                        uriBuilder.appendQueryParameter(key, String.valueOf(elem));
                    }
                } else {
                    if (value != null) {
                        uriBuilder.appendQueryParameter(key, String.valueOf(value));
                    }
                }
                // create Key List
                uriBuilder.appendQueryParameter("_key", key);
            }
        }
        return uriBuilder.build();
    }

    /**
     * <p>
     * yieldIfContendedのURIを取得する
     * </p>
     *
     * @param con     コンテキスト
     * @param builder クエリクラス
     */
    public static Uri buildYieldIfContendedUri(Uri authorityUri) {
        return buildYieldIfContendedUri(authorityUri, 0);
    }

    @SuppressLint("NewApi")
    public static SimpleMapPmb<Object> createSimpleMapPmbFromUri(Uri uri) {
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
        return mapPmb;
    }

    /**
     * GroupBy句を取得する
     *
     * @param uri
     * @return
     */
    public static String getGroupBy(Uri uri) {
        return uri.getQueryParameter("_groupBy");
    }

    /**
     * Having句を取得する
     *
     * @param uri
     * @return
     */
    public static String getHaving(Uri uri) {
        return uri.getQueryParameter("_having");
    }

    /**
     * Limit句を取得する
     *
     * @param uri
     * @return
     */
    public static String getLimit(Uri uri) {
        return uri.getQueryParameter("_limit");
    }

    /**
     * distinct句を取得する
     *
     * @param uri
     * @return
     */
    public static boolean getDistinct(Uri uri) {
        String distinct = uri.getQueryParameter("_distinct");
        return Boolean.valueOf(distinct);
    }

    /**
     * トランザクションコミットURIを取得する
     *
     * @param clazz テーブルクラス
     * @return トランザクションコミットURI
     */
    public static Uri buildSetTransactionSuccessfulUri(Uri authUri) {
        Uri uri = appendParameter(authUri, "_transaction", "1");
        return uri;
    }

    /**
     * SQLFilePathを取得する
     *
     * @param uri
     * @return
     */
    public static String getSqlFilePath(Uri uri) {
        return uri.getQueryParameter("_sqlFilePath");
    }

    /**
     * Transactionパラメータを取得する
     *
     * @param uri
     * @return
     */
    public static String getTransaction(Uri uri) {
        return uri.getQueryParameter("_transaction");
    }

    /**
     * YieldIfContendedの値を取得する
     *
     * @param uri
     * @return
     */
    public static String getYieldIfContendedFromUri(Uri uri) {
        return uri.getQueryParameter("_yieldIfContended");
    }

    /**
     * <p>
     * yieldIfContended(時間指定)のURIを取得する
     * </p>
     *
     * @param con     コンテキスト
     * @param builder クエリクラス
     */
    public static Uri buildYieldIfContendedUri(Uri authUri, int param) {
        String paramString = String.valueOf(param);
        Uri uri = appendParameter(authUri, "_yieldIfContended", paramString);
        return uri;
    }

    // ベースURIにパラメータを付与する
    private static Uri appendParameter(Uri baseUri, String query, String param) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(baseUri.getScheme());
        uriBuilder.authority(baseUri.getAuthority());
        uriBuilder.path(baseUri.getPath());
        uriBuilder.appendQueryParameter(query, param);
        Uri uri = uriBuilder.build();
        return uri;
    }
}
