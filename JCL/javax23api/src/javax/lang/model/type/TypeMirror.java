package javax.lang.model.type;

import java.lang.annotation.Annotation;
import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.util.Types;

public interface TypeMirror extends javax.lang.model.AnnotatedConstruct {

    TypeKind getKind();

    boolean equals(Object obj);

    int hashCode();

    String toString();

    @Override
    List<? extends AnnotationMirror> getAnnotationMirrors();

    @Override
    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    @Override
    <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType);

    <R, P> R accept(TypeVisitor<R, P> v, P p);
}
