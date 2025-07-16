package javax.lang.model.util;

import javax.lang.model.element.*;
import javax.annotation.processing.SupportedSourceVersion;
import static javax.lang.model.SourceVersion.*;
import javax.lang.model.SourceVersion;

@SupportedSourceVersion(RELEASE_20)
public class ElementKindVisitor14<R, P> extends ElementKindVisitor9<R, P> {
    protected ElementKindVisitor14() {
        super(null);
    }

    protected ElementKindVisitor14(R defaultValue) {
        super(defaultValue);
    }

    @Override
    public R visitRecordComponent(RecordComponentElement e, P p) {
        return defaultAction(e, p);
    }

    @Override
    public R visitTypeAsRecord(TypeElement e, P p) {
        return defaultAction(e, p);
    }

    @Override
    public R visitVariableAsBindingVariable(VariableElement e, P p) {
        return defaultAction(e, p);
    }
}
