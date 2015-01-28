package monotalk.db;

/**
 * Created by Kem on 2015/01/10.
 */
public class TestContentProvider2 extends DatabaseContentProvider {
    @Override
    protected DatabaseConfigration newDatabaseConfigration() {
        DatabaseConfigration.Builder builder = new DatabaseConfigration.Builder();
        builder.setDataBaseName("Sample2");
        builder.setVersion(1);
        builder.setDefalutDatabase(true);
        builder.setNodeCacheSize(1000);
        builder.addTable(TestModel4.class);
        return builder.create();
    }

    @Override
    public String getAuthority() {
        return TestContentProvider2.class.getPackage().getName();
    }
}
