package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import static javax.lang.model.SourceVersion.*;


@SupportedSourceVersion(RELEASE_6)
public abstract class AbstractElementVisitor6<R, P> implements ElementVisitor<R, P> {
    @Deprecated(since="9")
    protected AbstractElementVisitor6(){}

    public final R visit(Element e, P p) {
        return e.accept(this, p);
    }

    public final R visit(Element e) {
        return e.accept(this, null);
    }

    @Override
    public R visitUnknown(Element e, P p) {
        throw new UnknownElementException(e, p);
    }

    @Override
    public R visitModule(ModuleElement e, P p) {
        // Use implementation from interface default method
        return ElementVisitor.super.visitModule(e, p);
    }

    @Override
    public R visitRecordComponent(RecordComponentElement e, P p) {
        // Use implementation from interface default method
        return ElementVisitor.super.visitRecordComponent(e, p);
    }
}
