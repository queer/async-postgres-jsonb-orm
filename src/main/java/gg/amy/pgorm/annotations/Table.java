package gg.amy.pgorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The table the entity should be stored in. Will be created automatically if
 * none exists. <p />
 *
 * This annotation is simply used as <code>@Table("my_cool_table")</code> on
 * the entity class. <p />
 *
 * Generated tables will be of the format
 * <pre>
 * primary key | jsonb data
 * ------------------------
 * "whatever"  | {"a": "b"}
 * </pre>
 * where the primary key is the field annotated with {@link PrimaryKey}.
 * Indexes over the JSONB data will be generated based on the values passed to
 * the {@link BtreeIndex} or {@link GIndex} annotations.
 *
 * @author amy
 * @since 4/10/18.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String value();
}
