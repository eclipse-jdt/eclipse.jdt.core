package javax.annotation.processing;

import java.lang.annotation.*;
import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SupportedOptions {
    String [] value();
}
