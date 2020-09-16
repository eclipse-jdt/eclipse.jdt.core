package java.lang;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({CONSTRUCTOR, METHOD})
public @interface SafeVarargs {}