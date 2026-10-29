package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.type.IntersectionType;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_14)
public class SimpleTypeVisitor9<R, P> extends SimpleTypeVisitor8<R, P> {
    protected SimpleTypeVisitor9(){
        super(null);
    }

    protected SimpleTypeVisitor9(R defaultValue){
        super(defaultValue);
    }
}
