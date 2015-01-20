package monotalk.db.query;

import android.text.TextUtils;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.TableInfo;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;

/**
 * Created by Kem on 2015/01/10.
 */
public class QueryUtils {
    public static String[] toStirngArrayArgs(Object... args) {
        String[] stringArgs = null;
        if (args != null) {
            stringArgs = new String[args.length];
            int index = 0;
            for (Object arg : args) {
                TypeConverter converter = TypeConverterCache.getTypeConverter(arg.getClass());
                stringArgs[index] = converter.toBindSql(arg);
                index++;
            }
        }
        return stringArgs;
    }

    public static String toInsertSql(String tableName, String[] data) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(" INTO ");
        sql.append(tableName.toLowerCase());
        sql.append('(');
        sql.append(TextUtils.join(",", data));
        sql.append(')');
        sql.append(" VALUES (");
        for (int i = 0; i < data.length; i++) {
            sql.append((i > 0) ? ",?" : "?");
        }
        sql.append(')');
        return sql.toString();

    }

    public static String toInsertSql(Class<? extends Entity> type) {
        TableInfo info = MonoTalk.getTableInfo(type);
        String[] columnNames = info.getColumnNames();
        String tableName = info.getTableName();
        return toInsertSql(tableName, columnNames);
    }

    public static String toUpdateSql(Class<? extends Entity> type) {
        StringBuilder sql = new StringBuilder(120);
        TableInfo info = MonoTalk.getTableInfo(type);
        sql.append("UPDATE ");
        sql.append(info.getTableName().toLowerCase());
        sql.append(" SET ");
        // move all bind args to one array
        String[] columnNames = info.getColumnNames();
        sql.append(TextUtils.join("= ? ,", columnNames));
        sql.append("= ?");
        sql.append(" WHERE ");
        sql.append(info.getIdName());
        sql.append("= ?");
        return sql.toString();
    }

    public static String toSelectSql(Class<? extends Entity> type) {
        TableInfo info = MonoTalk.getTableInfo(type);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        String[] columnNames = info.getColumnNames();
        sql.append(TextUtils.join(",", columnNames));
        sql.append("FROM ");
        sql.append(info.getTableName().toLowerCase());
        sql.append(" WHERE ");
        sql.append(info.getIdName());
        sql.append("= ?");
        return sql.toString();
    }

    public static String toDeleteSql(Class<? extends Entity> type) {
        TableInfo info = MonoTalk.getTableInfo(type);
        StringBuilder sql = new StringBuilder(120);
        sql.append("DELETE ");
        sql.append("FROM ");
        sql.append(info.getTableName().toLowerCase());
        sql.append(" WHERE ");
        sql.append(info.getIdName());
        sql.append("= ?");
        return sql.toString();
    }

    public static String toDeleteSql(String tableName, String whereClause) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE ");
        sb.append("FROM ");
        sb.append(tableName.toLowerCase());
        if(!TextUtils.isEmpty(whereClause)) {
            sb.append(" WHERE ");
            sb.append(whereClause);
        }
        return sb.toString();
    }

    public static String toUpdateSql(String tableName, String[] columNames, String whereClause) {
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(tableName.toLowerCase());
        sql.append(" SET ");
        sql.append(TextUtils.join("= ? ,", columNames));
        sql.append("= ?");
        if(!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
        }
        return sql.toString();
    }
}
