package java.lang.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Documented
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface Documented {
}