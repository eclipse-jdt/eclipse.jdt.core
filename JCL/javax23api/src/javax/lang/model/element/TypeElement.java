package javax.lang.model.element;

import java.util.List;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public interface TypeElement extends Element, Parameterizable, QualifiedNameable {
    @Override
    TypeMirror asType();

    @Override
    List<? extends Element> getEnclosedElements();

    NestingKind getNestingKind();

    Name getQualifiedName();

    @Override
    Name getSimpleName();

    TypeMirror getSuperclass();

    List<? extends TypeMirror> getInterfaces();

    List<? extends TypeParameterElement> getTypeParameters();

    default List<? extends RecordComponentElement> getRecordComponents() {
        return List.of();
    }

    default List<? extends TypeMirror> getPermittedSubclasses() {
        return List.of();
    }

    @Override
    Element getEnclosingElement();
}
