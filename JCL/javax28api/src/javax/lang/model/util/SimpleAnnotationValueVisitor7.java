package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_7)
public class SimpleAnnotationValueVisitor7<R, P> extends SimpleAnnotationValueVisitor6<R, P> {
    @Deprecated(since="12")
    protected SimpleAnnotationValueVisitor7() {
        super(null); // Superclass constructor deprecated too
    }

    @Deprecated(since="12")
    protected SimpleAnnotationValueVisitor7(R defaultValue) {
        super(defaultValue); // Superclass constructor deprecated too
    }
}
