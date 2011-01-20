package test0348;

import java.lang.annotation.ElementType; 
import java.lang.annotation.Retention; 
import java.lang.annotation.RetentionPolicy; 
import java.lang.annotation.Target; 

@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.TYPE) 
public @interface TestAnnotationWithStringDefault { 
	String emptyString() default ""; 
	String string() default "string";
	
	@TestAnnotationWithStringDefault 
	public static class Annotated {}
	
	@TestAnnotationWithStringDefault 
	public interface AnnotatedInterface {} 
}