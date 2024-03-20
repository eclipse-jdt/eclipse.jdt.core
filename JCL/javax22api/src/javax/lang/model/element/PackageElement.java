package javax.lang.model.element;

import java.util.List;
import javax.lang.model.type.TypeMirror;

public interface PackageElement extends Element, QualifiedNameable {
    @Override
    TypeMirror asType();

    Name getQualifiedName();

    @Override
    Name getSimpleName();

    @Override
    List<? extends Element> getEnclosedElements();

    boolean isUnnamed();

    @Override
    Element getEnclosingElement();
}
