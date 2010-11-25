package test325481;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface _Path {
	String value() default "";
	
	String from() default "";
	String to() default "";
	
	String defaultDefinition() default "";
	
	boolean refines() default false;
}
