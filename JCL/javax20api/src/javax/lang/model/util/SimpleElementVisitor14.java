package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.RecordComponentElement;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_20)
public class SimpleElementVisitor14<R, P> extends SimpleElementVisitor9<R, P> {
    protected SimpleElementVisitor14(){
        super(null);
    }

    protected SimpleElementVisitor14(R defaultValue){
        super(defaultValue);
    }

    @Override
    public R visitRecordComponent(RecordComponentElement e, P p) {
        return defaultAction(e, p);
    }
}
