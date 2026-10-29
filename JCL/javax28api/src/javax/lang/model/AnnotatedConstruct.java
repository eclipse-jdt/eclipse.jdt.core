package javax.lang.model;

import java.lang.annotation.*;
import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.type.*;

public interface AnnotatedConstruct {
    List<? extends AnnotationMirror> getAnnotationMirrors();

    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType);
}
