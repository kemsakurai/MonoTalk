package monotalk.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import monotalk.db.Entity;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoticeTarget {
    public String authority();
    public Class<? extends Entity>[] targetClasses();
}
