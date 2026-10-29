package javax.lang.model.util;

import javax.lang.model.element.*;
import javax.annotation.processing.SupportedSourceVersion;
import static javax.lang.model.SourceVersion.*;
import javax.lang.model.SourceVersion;

@SupportedSourceVersion(RELEASE_14)
public class ElementKindVisitor9<R, P> extends ElementKindVisitor8<R, P> {
    protected ElementKindVisitor9() {
        super(null);
    }

    protected ElementKindVisitor9(R defaultValue) {
        super(defaultValue);
    }

    @Override
    public R visitModule(ModuleElement e, P p) {
        return defaultAction(e, p);
    }
}
