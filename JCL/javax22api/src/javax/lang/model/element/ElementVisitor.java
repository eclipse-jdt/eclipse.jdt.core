package javax.lang.model.element;

import javax.lang.model.util.*;

public interface ElementVisitor<R, P> {
    R visit(Element e, P p);

    default R visit(Element e) {
        return visit(e, null);
    }

    R visitPackage(PackageElement e, P p);

    R visitType(TypeElement e, P p);

    R visitVariable(VariableElement e, P p);

    R visitExecutable(ExecutableElement e, P p);

    R visitTypeParameter(TypeParameterElement e, P p);

    R visitUnknown(Element e, P p);

    default R visitModule(ModuleElement e, P p) {
        return visitUnknown(e, p);
    }

    default R visitRecordComponent(RecordComponentElement e, P p) {
        return visitUnknown(e, p);
    }
}
