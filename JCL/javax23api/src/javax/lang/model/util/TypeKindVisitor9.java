package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.type.*;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_14)
public class TypeKindVisitor9<R, P> extends TypeKindVisitor8<R, P> {
    protected TypeKindVisitor9() {
        super(null);
    }

    protected TypeKindVisitor9(R defaultValue) {
        super(defaultValue);
    }

    @Override
    public R visitNoTypeAsModule(NoType t, P p) {
        return defaultAction(t, p);
    }
}
