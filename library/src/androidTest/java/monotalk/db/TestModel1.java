package monotalk.db;

import java.util.Date;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.Table;

@Table(name = "TEST_TABLE")
public class TestModel1 extends Entity {

    @Column(name = "LongColumn")
    public Long columnLong = null;

    @Column(name = "StringColumn")
    public String columnString = null;

    @Column(name = "BooleanColumn")
    public Boolean columnBoolean = null;

    @Column(name = "DateColumn")
    public Date dateColumn = null;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("TestModel [columnLong=")
                .append(columnLong)
                .append(", columnString=")
                .append(columnString)
                .append(", columnBoolean=")
                .append(columnBoolean)
                .append(", dateColumn=")
                .append(dateColumn)
                .append("]");
        return builder.toString();
    }
}
