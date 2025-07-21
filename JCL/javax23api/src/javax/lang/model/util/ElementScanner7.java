package javax.lang.model.util;

import javax.lang.model.element.*;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;


@SupportedSourceVersion(RELEASE_7)
public class ElementScanner7<R, P> extends ElementScanner6<R, P> {
    @Deprecated(since="12")
    protected ElementScanner7(){
        super(null); // Superclass constructor deprecated too
    }

    @Deprecated(since="12")
    protected ElementScanner7(R defaultValue){
        super(defaultValue); // Superclass constructor deprecated too
    }

    @Override
    public R visitVariable(VariableElement e, P p) {
        return scan(e.getEnclosedElements(), p);
    }
}
