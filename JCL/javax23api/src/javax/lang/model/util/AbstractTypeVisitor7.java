package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.type.*;

import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_7)
public abstract class AbstractTypeVisitor7<R, P> extends AbstractTypeVisitor6<R, P> {
    @Deprecated(since="12")
    protected AbstractTypeVisitor7() {
        super();  // Superclass constructor deprecated too
    }

    @Override
    public abstract R visitUnion(UnionType t, P p);
}
