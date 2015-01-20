package monotalk.db;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.ForeignKey;
import monotalk.db.annotation.Table;

/**
 * Created by kem on 2015/01/17.
 */
@Table(name = "JOIN_TEST_TABLE3")
public class JoinTableTestModel3 extends Entity {
    @Column(name = "TEST_TABLE_ID")
    @ForeignKey(entityClass = JoinTableTestModel1.class, onDelete = ForeignKey.ReferentialAction.CASCADE, onUpdate = ForeignKey.ReferentialAction.CASCADE)
    public Long testTableId = null;
}
