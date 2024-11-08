package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ModuleElement;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_14)
public class SimpleElementVisitor9<R, P> extends SimpleElementVisitor8<R, P> {
    protected SimpleElementVisitor9(){
        super(null);
    }

    protected SimpleElementVisitor9(R defaultValue){
        super(defaultValue);
    }

    @Override
    public R visitModule(ModuleElement e, P p) {
        return defaultAction(e, p);
    }
}
