package javax.lang.model.element;

import java.util.List;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

public interface TypeParameterElement extends Element {
    @Override
    TypeMirror asType();

    Element getGenericElement();

    List<? extends TypeMirror> getBounds();

    @Override
    Element getEnclosingElement();
}
