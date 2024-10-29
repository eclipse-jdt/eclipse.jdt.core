package javax.lang.model.util;

import javax.lang.model.element.*;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;


@SupportedSourceVersion(RELEASE_8)
public class ElementScanner8<R, P> extends ElementScanner7<R, P> {
    @SuppressWarnings("deprecation") // Superclass constructor deprecated
    protected ElementScanner8(){
        super(null);
    }

    @SuppressWarnings("deprecation") // Superclass constructor deprecated
    protected ElementScanner8(R defaultValue){
        super(defaultValue);
    }
}
