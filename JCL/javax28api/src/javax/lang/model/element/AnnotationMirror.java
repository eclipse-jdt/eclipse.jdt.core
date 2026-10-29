package javax.lang.model.element;

import java.util.Map;
import javax.lang.model.type.DeclaredType;

public interface AnnotationMirror {

    DeclaredType getAnnotationType();

    Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues();
}
