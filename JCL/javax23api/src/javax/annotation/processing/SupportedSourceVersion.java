package javax.annotation.processing;

import java.lang.annotation.*;
import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;
import javax.lang.model.SourceVersion;


@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SupportedSourceVersion {
    SourceVersion value();
}
