package monotalk.db;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.ForeignKey;
import monotalk.db.annotation.Table;

/**
 * Created by kem on 2015/01/17.
 */
@Table(name = "JOIN_TEST_TABLE2")
public class JoinTableTestModel2 extends Entity {
    @Column(name = "TEST_TABLE_ID")
    @ForeignKey(entityClass = JoinTableTestModel1.class, onUpdate = ForeignKey.ReferentialAction.RESTRICT, onDelete = ForeignKey.ReferentialAction.RESTRICT)
    public Long testTableId = null;
}
