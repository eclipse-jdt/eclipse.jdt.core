package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.RecordComponentElement;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_20)
public abstract class AbstractElementVisitor14<R, P> extends AbstractElementVisitor9<R, P> {
    protected AbstractElementVisitor14(){
        super();
    }

    @Override
    public abstract R visitRecordComponent(RecordComponentElement e, P p);
}
