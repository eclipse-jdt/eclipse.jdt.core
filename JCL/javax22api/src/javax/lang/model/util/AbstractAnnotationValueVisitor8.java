package javax.lang.model.util;

import static javax.lang.model.SourceVersion.*;
import javax.lang.model.SourceVersion;
import javax.annotation.processing.SupportedSourceVersion;

@SupportedSourceVersion(RELEASE_8)
public abstract class AbstractAnnotationValueVisitor8<R, P> extends AbstractAnnotationValueVisitor7<R, P> {

    @SuppressWarnings("deprecation") // Superclass constructor deprecated
    protected AbstractAnnotationValueVisitor8() {
        super();
    }
}
