package monotalk.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Kem on 2015/01/10.
 */
public class JoinTableTestContentProvider extends DatabaseContentProvider {
    @Override
    protected DatabaseConfigration newDatabaseConfigration() {
        DatabaseConfigration.Builder builder = new DatabaseConfigration.Builder();
        builder.setDataBaseName("JoinSample");
        builder.setVersion(1);
        builder.setDefalutDatabase(true);
        builder.setNodeCacheSize(1000);
        builder.setTableCacheSize(1000);
        builder.addMigration(1, new Migration() {
            @Override
            public void upgradeMigrate(SQLiteDatabase db) {
                DBLog.i(DBLog.getTag(JoinTableTestContentProvider.class), "upgradeMigrate() start ver 1");
                DdlExecutor executor = new DdlExecutor(db);
                executor.executeDropTableOrView(JoinTableTestModel2.class);
                executor.executeDropTableOrView(JoinTableTestModel3.class);
                executor.executeDropTableOrView(JoinTableTestModel4.class);
                executor.executeDropTableOrView(JoinTableTestModel1.class);
                executor.executeCreateTableOrCreateView(JoinTableTestModel1.class);
                executor.executeCreateTableOrCreateView(JoinTableTestModel2.class);
                executor.executeCreateTableOrCreateView(JoinTableTestModel3.class);
                executor.executeCreateTableOrCreateView(JoinTableTestModel4.class);
            }

            @Override
            public void downgradeMigrate(SQLiteDatabase db) {
                DdlExecutor executor = new DdlExecutor(db);
                executor.executeDropTableOrView(JoinTableTestModel2.class);
                executor.executeDropTableOrView(JoinTableTestModel3.class);
                executor.executeDropTableOrView(JoinTableTestModel4.class);
                executor.executeDropTableOrView(JoinTableTestModel1.class);
                executor.executeCreateTableOrCreateView(JoinTableTestModel1.class);
                executor.executeCreateTableOrCreateView(JoinTableTestModel2.class);
                executor.executeCreateTableOrCreateView(JoinTableTestModel3.class);
                executor.executeCreateTableOrCreateView(JoinTableTestModel4.class);
            }
        });
        builder.addTable(JoinTableTestModel1.class);
        builder.addTable(JoinTableTestModel2.class);
        builder.addTable(JoinTableTestModel3.class);
        builder.addTable(JoinTableTestModel4.class);
        return builder.create();
    }

    @Override
    public String getAuthority() {
        return "monotalk.db.joinTest";
    }

    @Override
    protected DBLog.LogLevel getLogLevel() {
        return DBLog.LogLevel.DEBUG;
    }
}
