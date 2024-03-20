package javax.lang.model.util;

import javax.lang.model.element.*;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;


@SupportedSourceVersion(RELEASE_6)
public class SimpleElementVisitor6<R, P> extends AbstractElementVisitor6<R, P> {
    protected final R DEFAULT_VALUE;

    @Deprecated(since="9")
    protected SimpleElementVisitor6(){
        DEFAULT_VALUE = null;
    }

    @Deprecated(since="9")
    protected SimpleElementVisitor6(R defaultValue){
        DEFAULT_VALUE = defaultValue;
    }
    protected R defaultAction(Element e, P p) {
        return DEFAULT_VALUE;
    }

    public R visitPackage(PackageElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitType(TypeElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitVariable(VariableElement e, P p) {
        if (e.getKind() != ElementKind.RESOURCE_VARIABLE)
            return defaultAction(e, p);
        else
            return visitUnknown(e, p);
    }

    public R visitExecutable(ExecutableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitTypeParameter(TypeParameterElement e, P p) {
        return defaultAction(e, p);
    }
}
