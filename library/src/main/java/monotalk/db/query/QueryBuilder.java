package monotalk.db.query;

import monotalk.db.Entity;

public class QueryBuilder {

    private QueryCrudHandler queryCrudHandler;

    /**
     * コンストラクター
     *
     * @param queryCrudHandler
     */
    private QueryBuilder(QueryCrudHandler queryCrudHandler) {
        this.queryCrudHandler = queryCrudHandler;
    }

    /**
     * QueryBuilderを返します。
     *
     * @param queryCrudHandler
     * @return
     */
    public static QueryBuilder newQuery(QueryCrudHandler queryCrudHandler) {
        return new QueryBuilder(queryCrudHandler);
    }
    
    /**
     * Deleteクエリオブジェクトを生成する
     *
     * @return
     */
    public Delete newDelete() {
        return new Delete(queryCrudHandler);
    }

    /**
     * Insertクエリオブジェクトを生成する
     *
     * @return
     */
    public <T extends Entity> Insert<T> newInsert(Class<T> table) {
        return new Insert<T>(table, queryCrudHandler);
    }

    /**
     * Selectクエリオブジェクトを生成する
     *
     * @param columns
     * @return
     */
    public Select newSelect(Select.Column... columns) {
        return new Select(queryCrudHandler, columns);
    }

    /**
     * Selectクエリオブジェクトを生成する
     *
     * @param columns
     * @return
     */
    public Select newSelect(String... columns) {
        return new Select(queryCrudHandler, columns);
    }

    /**
     * TwoWayQuerySelectクエリオブジェクトを生成する
     *
     * @param filePath
     * @param table
     * @param <T>
     * @return
     */
    public <T extends Entity> TwoWayQuerySelect<T> newSelectBySqlFile(String filePath, Class<T> table) {
        return new TwoWayQuerySelect<T>(queryCrudHandler, table, filePath);
    }

    /**
     * Updateクエリオブジェクトを生成する
     *
     * @param table
     * @param <T>
     * @return
     */
    public <T extends Entity> Update<T> newUpdate(Class<T> table) {
        return new Update<T>(table, queryCrudHandler);
    }
}
