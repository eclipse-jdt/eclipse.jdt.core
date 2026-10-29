package javax.lang.model.element;


import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.annotation.IncompleteAnnotationException;
import java.util.List;
import java.util.Set;

import javax.lang.model.type.*;
import javax.lang.model.util.*;

public interface Element extends javax.lang.model.AnnotatedConstruct {
    TypeMirror asType();

    ElementKind getKind();

    Set<Modifier> getModifiers();

    Name getSimpleName();

    Element getEnclosingElement();

    List<? extends Element> getEnclosedElements();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    @Override
    List<? extends AnnotationMirror> getAnnotationMirrors();

    @Override
    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    @Override
    <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType);

    <R, P> R accept(ElementVisitor<R, P> v, P p);
}
