package javax.lang.model.type;


import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;


public interface DeclaredType extends ReferenceType {

    Element asElement();

    TypeMirror getEnclosingType();

    List<? extends TypeMirror> getTypeArguments();
}
