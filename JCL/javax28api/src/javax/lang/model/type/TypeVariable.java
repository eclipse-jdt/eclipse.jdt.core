package javax.lang.model.type;


import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.util.Types;


public interface TypeVariable extends ReferenceType {

    Element asElement();

    TypeMirror getUpperBound();

    TypeMirror getLowerBound();
}
