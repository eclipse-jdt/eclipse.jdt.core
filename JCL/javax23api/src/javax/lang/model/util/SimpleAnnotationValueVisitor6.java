package javax.lang.model.util;


import java.util.List;
import javax.lang.model.element.*;

import javax.lang.model.type.TypeMirror;
import static javax.lang.model.SourceVersion.*;
import javax.lang.model.SourceVersion;
import javax.annotation.processing.SupportedSourceVersion;

@SupportedSourceVersion(RELEASE_6)
public class SimpleAnnotationValueVisitor6<R, P>
    extends AbstractAnnotationValueVisitor6<R, P> {

    protected final R DEFAULT_VALUE;

    @Deprecated(since="9")
    protected SimpleAnnotationValueVisitor6() {
        super();
        DEFAULT_VALUE = null;
    }

    @Deprecated(since="9")
    protected SimpleAnnotationValueVisitor6(R defaultValue) {
        super();
        DEFAULT_VALUE = defaultValue;
    }

    protected R defaultAction(Object o, P p) {
        return DEFAULT_VALUE;
    }

    public R visitBoolean(boolean b, P p) {
        return defaultAction(b, p);
    }

    public R visitByte(byte b, P p) {
        return defaultAction(b, p);
    }

    public R visitChar(char c, P p) {
        return defaultAction(c, p);
    }

    public R visitDouble(double d, P p) {
        return defaultAction(d, p);
    }

    public R visitFloat(float f, P p) {
        return defaultAction(f, p);
    }

    public R visitInt(int i, P p) {
        return defaultAction(i, p);
    }

    public R visitLong(long i, P p) {
        return defaultAction(i, p);
    }

    public R visitShort(short s, P p) {
        return defaultAction(s, p);
    }

    public R visitString(String s, P p) {
        return defaultAction(s, p);
    }

    public R visitType(TypeMirror t, P p) {
        return defaultAction(t, p);
    }

    public R visitEnumConstant(VariableElement c, P p) {
        return defaultAction(c, p);
    }

    public R visitAnnotation(AnnotationMirror a, P p) {
        return defaultAction(a, p);
    }

    public R visitArray(List<? extends AnnotationValue> vals, P p) {
        return defaultAction(vals, p);
    }
}
