package javax.annotation.processing;

import javax.lang.model.element.*;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import java.lang.annotation.Annotation;

public interface RoundEnvironment {
    boolean processingOver();

    boolean errorRaised();

    Set<? extends Element> getRootElements();

    Set<? extends Element> getElementsAnnotatedWith(TypeElement a);

    default Set<? extends Element> getElementsAnnotatedWithAny(TypeElement... annotations){
        // Use LinkedHashSet rather than HashSet for predictability
        Set<Element> result = new LinkedHashSet<>();
        for (TypeElement annotation : annotations) {
            result.addAll(getElementsAnnotatedWith(annotation));
        }
        return Collections.unmodifiableSet(result);
    }

    Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a);

    default Set<? extends Element> getElementsAnnotatedWithAny(Set<Class<? extends Annotation>> annotations){
        // Use LinkedHashSet rather than HashSet for predictability
        Set<Element> result = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : annotations) {
            result.addAll(getElementsAnnotatedWith(annotation));
        }
        return Collections.unmodifiableSet(result);
    }
}
