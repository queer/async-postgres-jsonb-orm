package gg.amy.pgorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The field that should be the primary key. Will be automatically found and
 * used at runtime. Note that annotating multiple fields with this annotation
 * will throw exceptions at runtime.<p />
 *
 * You may optionally pass a string to this annotation as the name of the PK
 * column. If none is passed in, it will default to <code>id</code>.
 *
 * @author amy
 * @since 4/10/18.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {
    String value() default "id";
}
