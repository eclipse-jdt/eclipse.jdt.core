package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.type.*;

import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_8)
public abstract class AbstractTypeVisitor8<R, P> extends AbstractTypeVisitor7<R, P> {
    @SuppressWarnings("deprecation") // Superclass constructor deprecated
    protected AbstractTypeVisitor8() {
        super();
    }

    @Override
    public abstract R visitIntersection(IntersectionType t, P p);
}
