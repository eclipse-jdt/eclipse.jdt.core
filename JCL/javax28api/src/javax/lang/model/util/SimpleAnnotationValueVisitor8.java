package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_8)
public class SimpleAnnotationValueVisitor8<R, P> extends SimpleAnnotationValueVisitor7<R, P> {
    @SuppressWarnings("deprecation") // Superclass constructor deprecated
    protected SimpleAnnotationValueVisitor8() {
        super(null);
    }

    @SuppressWarnings("deprecation") // Superclass constructor deprecated
    protected SimpleAnnotationValueVisitor8(R defaultValue) {
        super(defaultValue);
    }
}
