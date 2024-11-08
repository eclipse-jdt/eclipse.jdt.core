package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.type.*;

import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_6)
public abstract class AbstractTypeVisitor6<R, P> implements TypeVisitor<R, P> {
    @Deprecated(since="9")
    protected AbstractTypeVisitor6() {}

    public final R visit(TypeMirror t, P p) {
        return t.accept(this, p);
    }

    public final R visit(TypeMirror t) {
        return t.accept(this, null);
    }

    public R visitUnion(UnionType t, P p) {
        return visitUnknown(t, p);
    }

    @Override
    public R visitIntersection(IntersectionType t, P p) {
        return visitUnknown(t, p);
    }

    @Override
    public R visitUnknown(TypeMirror t, P p) {
        throw new UnknownTypeException(t, p);
    }
}
