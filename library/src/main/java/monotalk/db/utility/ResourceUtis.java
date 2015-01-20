/*******************************************************************************
 * Copyright (C) 2012-2013 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package monotalk.db.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import monotalk.db.DBLog;

/**
 * <p>
 * Assetフォルダ配下からpropertyファイルを取得するクラス 1回目の取得時に、内部のMapにproperties値を保持し、 2回目以降はそれを返却する
 * </p>
 *
 * @author Kem
 */
public final class ResourceUtis {

    public final static String METADATA_MONOTALK_LOGLEVEL = "MONOTALK.LOG_LEVEL";
    public final static String METADATA_MONOTALK_ASSERT_ENTITY = "MONOTALK.ASSERT_ENTITY";

    /**
     * ログ用のタグ
     */
    private static final String TAG = ResourceUtis.class.getName();

    /**
     * @param context
     * @param clazz
     * @param sqlFileName
     * @return
     */
    public static String getSQL(Context context, Class<?> clazz, String sqlFileName) {
        String pathPrefix = getSqlFilePathPrefix(clazz);
        return getSQL(context, pathPrefix + sqlFileName);
    }

    /**
     * <p>
     * Class名を"/"区切りのファイルパスに変換する
     * </p>
     *
     * @return 変換結果
     * @name className クラス名
     */
    public static String getSqlFilePathPrefix(Class<?> clazz) {
        AssertUtils.assertNotNull(clazz, "clazz is null");
        String className = clazz.getName();
        return className.replace(".", "/") + "/";
    }

    /**
     * @param context
     * @param sqlFilePath
     * @return
     */
    public static String getSQL(Context context, String sqlFilePath) {
        // StringBuilder
        StringBuilder sb = null;
        // InputStream
        InputStream is = null;
        // AssetManagerを取得
        AssetManager assetManager = context.getAssets();
        try {
            is = assetManager.open(sqlFilePath);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is));
            sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            // ファイルが存在したが、開ける状態ではなかった
            throw new IllegalStateException(e);
        } finally {
            closeQuietly(is);
        }
        return sb.toString();
    }

    /**
     * InputStreamを閉じる
     *
     * @param is InputStream
     */
    private static void closeQuietly(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // Log出力のみ行う
                Log.w(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * @param context
     * @return
     */
    public static boolean isAsertEntity(Context context) {
        Boolean asertEntity = getMetaDataBoolean(context, METADATA_MONOTALK_ASSERT_ENTITY);
        if (asertEntity == null) {
            return false;
        }
        return asertEntity;
    }

    /**
     * @param context
     * @param name
     * @return
     */
    private static Boolean getMetaDataBoolean(Context context, String name) {
        Boolean value = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getBoolean(name);
        } catch (Exception e) {
            Log.i(DBLog.getTag(ResourceUtis.class), "Couldn't find config value: " + name);
        }

        return value;
    }

    /**
     * @param context
     * @return
     */
    public static DBLog.LogLevel getLogLevel(Context context) {
        String logLevel = getMetaDataString(context, METADATA_MONOTALK_LOGLEVEL);
        if (logLevel == null) {
            return DBLog.LogLevel.NONE;
        }
        return DBLog.LogLevel.valueOf(logLevel);
    }

    /**
     * @param context
     * @param name
     * @return
     */
    private static String getMetaDataString(Context context, String name) {
        String value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getString(name);
        } catch (Exception e) {
            Log.i(DBLog.getTag(ResourceUtis.class), "Couldn't find config value: " + name);
        }

        return value;
    }
}