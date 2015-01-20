package monotalk.db;

import java.util.Date;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.Table;

/**
 * Created by Kem on 2015/01/17.
 */
@Table(name = "JOIN_TEST_TABLE1")
public class JoinTableTestModel1 extends Entity {
    @Column(name = "LongColumn")
    public Long columnLong = null;

    @Column(name = "StringColumn")
    public String columnString = null;

    @Column(name = "BooleanColumn")
    public Boolean columnBoolean = null;

    @Column(name = "DateColumn")
    public Date dateColumn = null;
}
