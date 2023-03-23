package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;


@SupportedSourceVersion(RELEASE_7)
public abstract class AbstractElementVisitor7<R, P> extends AbstractElementVisitor6<R, P> {
    @Deprecated(since="12")
    protected AbstractElementVisitor7(){
        super(); // Superclass constructor deprecated too
    }
}
