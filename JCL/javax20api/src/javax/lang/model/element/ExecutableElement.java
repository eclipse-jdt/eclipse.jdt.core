package javax.lang.model.element;

import java.util.List;
import javax.lang.model.type.*;

public interface ExecutableElement extends Element, Parameterizable {
    @Override
    TypeMirror asType();

    List<? extends TypeParameterElement> getTypeParameters();

    TypeMirror getReturnType();

    List<? extends VariableElement> getParameters();

    TypeMirror getReceiverType();

    boolean isVarArgs();

    boolean isDefault();

    List<? extends TypeMirror> getThrownTypes();

    AnnotationValue getDefaultValue();

    @Override
    Element getEnclosingElement();

    @Override
    Name getSimpleName();
}
