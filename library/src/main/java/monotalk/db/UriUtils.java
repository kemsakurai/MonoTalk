package monotalk.db;

import android.net.Uri;
import android.text.TextUtils;

import java.util.Locale;

public class UriUtils {

    public static Uri buildAuthorityUri(String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        return uriBuilder.scheme("content").authority(authority).build();
    }

    /**
     * コンテンツプロバイダー用のTABLEのベースURIを返します。
     *
     * @param authorityUri
     * @param tableName
     * @return URI
     */
    public static Uri buildEntityUri(Uri authorityUri, String tableName) {
        if (TextUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("tableName is empty");
        }
        // URIを作成し返す
        return authorityUri.buildUpon().path("/" + tableName.toLowerCase(Locale.getDefault())).build();
    }

    /**
     * コンテンツプロバイダー用のTABLEのベースURIを返します。
     *
     * @param authorityUri
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends Entity> Uri buildEntityUri(Uri authorityUri, Class<T> clazz) {
        return buildEntityUri(authorityUri, MonoTalk.getTableName(clazz));
    }

    /**
     * <p>
     * トランザクション開始URIを取得する
     * </p>
     *
     * @param authorityUri
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
     * yieldIfContendedのURIを取得する
     *
     * @param authorityUri
     */
    public static Uri buildYieldIfContendedUri(Uri authorityUri) {
        return buildYieldIfContendedUri(authorityUri, 0);
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
