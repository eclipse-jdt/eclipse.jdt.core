package javax.lang.model.util;


import javax.lang.model.element.*;

import static javax.lang.model.SourceVersion.*;
import javax.lang.model.SourceVersion;
import javax.annotation.processing.SupportedSourceVersion;

@SupportedSourceVersion(RELEASE_6)
public abstract class AbstractAnnotationValueVisitor6<R, P>
    implements AnnotationValueVisitor<R, P> {

    @Deprecated(since="9")
    protected AbstractAnnotationValueVisitor6() {}

    public final R visit(AnnotationValue av, P p) {
        return av.accept(this, p);
    }

    public final R visit(AnnotationValue av) {
        return av.accept(this, null);
    }

    @Override
    public R visitUnknown(AnnotationValue av, P p) {
        throw new UnknownAnnotationValueException(av, p);
    }
}
