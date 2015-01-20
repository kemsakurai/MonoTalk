package monotalk.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {

    public String[] columns();

    /**
     * Returns a behaviour when the operation encounters a conflict.
     * trueの場合、
     * ConflictClauseは、ConflictClause.NONE(デフォルト動作:ON CONFLICT ABORT)として動作する
     * @return The conflict clause.
     */
    public ConflictClause value() default ConflictClause.NONE;
}
