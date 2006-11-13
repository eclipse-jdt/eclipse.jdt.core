package test0230;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE,METHOD})
@Retention(RUNTIME)
public @interface Test  {
        String value() default "";
}