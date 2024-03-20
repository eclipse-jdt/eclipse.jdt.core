package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_14)
public class SimpleAnnotationValueVisitor9<R, P> extends SimpleAnnotationValueVisitor8<R, P> {
    protected SimpleAnnotationValueVisitor9() {
        super(null);
    }

    protected SimpleAnnotationValueVisitor9(R defaultValue) {
        super(defaultValue);
    }
}
