package package1;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Javadoc for MyAnnotation
 */
@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.CLASS)
public @interface MyAnnotation {

	/**
	 * Javadoc for explanation
	 *
	 * @return some value
	 */
	String explanation() default "";
}