package monotalk.db;

/**
 * Created by Kem on 2015/01/10.
 */
public class TestContentProvider1 extends DatabaseContentProvider {
    @Override
    protected DatabaseConfigration newDatabaseConfigration() {
        DatabaseConfigration.Builder builder = new DatabaseConfigration.Builder();
        builder.setDataBaseName("Sample");
        builder.setVersion(1);
        builder.setDefalutDatabase(true);
        builder.setNodeCacheSize(1000);
        builder.setTableCacheSize(1000);
        builder.addTable(TestModel1.class);
        builder.addTable(TestModel2.class);
        builder.addTable(TestModel3.class);
        return builder.create();
    }
    
    @Override
    public String getAuthority() {
        return TestContentProvider1.class.getPackage().getName();
    }

    @Override
    protected DBLog.LogLevel getLogLevel() {
        return DBLog.LogLevel.ERROR;
    }
}
