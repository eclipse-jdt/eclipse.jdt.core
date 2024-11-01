package javax.lang.model.util;

import static javax.lang.model.SourceVersion.*;
import javax.lang.model.SourceVersion;
import javax.annotation.processing.SupportedSourceVersion;

@SupportedSourceVersion(RELEASE_7)
public abstract class AbstractAnnotationValueVisitor7<R, P> extends AbstractAnnotationValueVisitor6<R, P> {

    @Deprecated(since="12")
    protected AbstractAnnotationValueVisitor7() {
        super(); // Superclass constructor deprecated too
    }
}
