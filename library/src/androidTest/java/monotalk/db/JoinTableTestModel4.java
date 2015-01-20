package monotalk.db;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.ForeignKey;
import monotalk.db.annotation.Table;

/**
 * Created by kem on 2015/01/17.
 */
@Table(name = "JOIN_TEST_TABLE4")
public class JoinTableTestModel4 extends Entity {
    @Column(name = "TEST_TABLE_ID")
    @ForeignKey(onDelete = ForeignKey.ReferentialAction.CASCADE, onUpdate = ForeignKey.ReferentialAction.CASCADE)
    public JoinTableTestModel1 model1 = null;

    @Column(name = "StringColumn")
    public String columnString = null;

}
