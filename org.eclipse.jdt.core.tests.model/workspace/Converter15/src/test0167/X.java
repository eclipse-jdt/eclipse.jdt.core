package test0167;

import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import static test0167.Jpf.*;

interface Jpf {
	@Target(value=METHOD)
	@Retention(value=RUNTIME)
	public static @interface ExceptionHandler {}
	
	@Target(value=ANNOTATION_TYPE)
	@Retention(value=RUNTIME)
	public static @interface Forward {}
}

public class X {

    @Jpf.ExceptionHandler()
    protected Forward newExceptionHandler1(Exception ex, String actionName,
            String message, Object form)
    {
        return null;
    }
}
