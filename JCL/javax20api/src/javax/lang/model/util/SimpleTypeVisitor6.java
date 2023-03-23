package javax.lang.model.util;

import javax.lang.model.type.*;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;


@SupportedSourceVersion(RELEASE_6)
public class SimpleTypeVisitor6<R, P> extends AbstractTypeVisitor6<R, P> {
    protected final R DEFAULT_VALUE;

    @Deprecated(since="9")
    protected SimpleTypeVisitor6(){
        DEFAULT_VALUE = null;
    }

    @Deprecated(since="9")
    protected SimpleTypeVisitor6(R defaultValue){
        DEFAULT_VALUE = defaultValue;
    }

    protected R defaultAction(TypeMirror t, P p) {
        return DEFAULT_VALUE;
    }

    public R visitPrimitive(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    public R visitNull(NullType t, P p){
        return defaultAction(t, p);
    }

    public R visitArray(ArrayType t, P p){
        return defaultAction(t, p);
    }

    public R visitDeclared(DeclaredType t, P p){
        return defaultAction(t, p);
    }

    public R visitError(ErrorType t, P p){
        return defaultAction(t, p);
    }

    public R visitTypeVariable(TypeVariable t, P p){
        return defaultAction(t, p);
    }

    public R visitWildcard(WildcardType t, P p){
        return defaultAction(t, p);
    }

    public R visitExecutable(ExecutableType t, P p) {
        return defaultAction(t, p);
    }

    public R visitNoType(NoType t, P p){
        return defaultAction(t, p);
    }
}
