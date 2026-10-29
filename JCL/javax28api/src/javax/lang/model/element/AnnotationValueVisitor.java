package javax.lang.model.element;


import java.util.List;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.*;

public interface AnnotationValueVisitor<R, P> {
    R visit(AnnotationValue av, P p);

    default R visit(AnnotationValue av) {
        return visit(av, null);
    }

    R visitBoolean(boolean b, P p);

    R visitByte(byte b, P p);

    R visitChar(char c, P p);

    R visitDouble(double d, P p);

    R visitFloat(float f, P p);

    R visitInt(int i, P p);

    R visitLong(long i, P p);

    R visitShort(short s, P p);

    R visitString(String s, P p);

    R visitType(TypeMirror t, P p);

    R visitEnumConstant(VariableElement c, P p);

    R visitAnnotation(AnnotationMirror a, P p);

    R visitArray(List<? extends AnnotationValue> vals, P p);

    R visitUnknown(AnnotationValue av, P p);
}
