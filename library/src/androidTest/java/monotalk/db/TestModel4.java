package monotalk.db;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.Id;
import monotalk.db.annotation.Table;

/**
 * Created by Kem on 2015/01/12.
 */
@Table(name = "TEST_TABLE4")
public class TestModel4 extends Entity {
    @Column(name = "StringColumn1", defaultValue = "DEFAULT")
    @Id(isAutoIncrement = false)
    public String columnString1 = null;
}
