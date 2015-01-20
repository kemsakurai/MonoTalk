package monotalk.db;

import monotalk.db.annotation.Column;
import monotalk.db.annotation.Table;

/**
 * Created by Kem on 2015/01/12.
 */
@Table(name = "TEST_TABLE3")
public class TestModel3 extends Entity {
    @Column(name = "StringColumn1", defaultValue = "DEFAULT")
    public String columnString1 = null;

    @Column(name = "StringColumn2", nullable = false, unique = false)
    public String columnString2 = null;

    // unique true の場合、
    @Column(name = "StringColumn3", nullable = true, unique = true)
    public String columnString3 = null;

    @Column(name = "BooleanColumn1", defaultValue = "1", nullable = false)
    public Boolean columnBoolean1 = null;

    // defalutValueの指定があった場合も、nullのinsertが優先される
    // nullable=falseの場合は、null制約となるため、null制約エラー(ON CONFLICT ABORT)となる
    @Column(name = "BooleanColumn2", defaultValue = "0", nullable = true)
    public Boolean columnBoolean2 = null;

    @Column(name = "BooleanColumn3", defaultValue = "0", nullable = true, unique = true)
    public Boolean columnBoolean3 = null;

}
