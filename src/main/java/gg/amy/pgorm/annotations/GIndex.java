package gg.amy.pgorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The JSONB fields to GIN index on. Fields are passed as an array of strings, and
 * the indexes will be automatically generated based on this.
 *
 * @author amy
 * @since 6/21/18.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GIndex {
    String[] value() default {};
}
