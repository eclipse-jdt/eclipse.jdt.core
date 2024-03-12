package javax.lang.model.element;

import javax.lang.model.util.Elements;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;

public interface VariableElement extends Element {
    @Override
    TypeMirror asType();

    Object getConstantValue();

    @Override
    Name getSimpleName();

    @Override
    Element getEnclosingElement();
}
