package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ModuleElement;
import static javax.lang.model.SourceVersion.*;


@SupportedSourceVersion(RELEASE_14)
public abstract class AbstractElementVisitor9<R, P> extends AbstractElementVisitor8<R, P> {
    protected AbstractElementVisitor9(){
        super();
    }

    @Override
    public abstract R visitModule(ModuleElement e, P p);
}
