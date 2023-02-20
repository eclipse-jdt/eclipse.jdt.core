package javax.lang.model.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Objects;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.*;


public interface Elements {

    PackageElement getPackageElement(CharSequence name);

    default PackageElement getPackageElement(ModuleElement module, CharSequence name) {
        return null;
    }

    default Set<? extends PackageElement> getAllPackageElements(CharSequence name) {
        Set<? extends ModuleElement> modules = getAllModuleElements();
        if (modules.isEmpty()) {
            PackageElement packageElt = getPackageElement(name);
            return (packageElt != null) ?
                Collections.singleton(packageElt):
                Collections.emptySet();
        } else {
            Set<PackageElement> result = new LinkedHashSet<>(1); // Usually expect at most 1 result
            for (ModuleElement module: modules) {
                PackageElement packageElt = getPackageElement(module, name);
                if (packageElt != null)
                    result.add(packageElt);
            }
            return Collections.unmodifiableSet(result);
        }
    }

    TypeElement getTypeElement(CharSequence name);

    default TypeElement getTypeElement(ModuleElement module, CharSequence name) {
        return null;
    }

    default Set<? extends TypeElement> getAllTypeElements(CharSequence name) {
        Set<? extends ModuleElement> modules = getAllModuleElements();
        if (modules.isEmpty()) {
            TypeElement typeElt = getTypeElement(name);
            return (typeElt != null) ?
                Collections.singleton(typeElt):
                Collections.emptySet();
        } else {
            Set<TypeElement> result = new LinkedHashSet<>(1); // Usually expect at most 1 result
            for (ModuleElement module: modules) {
                TypeElement typeElt = getTypeElement(module, name);
                if (typeElt != null)
                    result.add(typeElt);
            }
            return Collections.unmodifiableSet(result);
        }
    }

    default ModuleElement getModuleElement(CharSequence name) {
        return null;
    }

    default Set<? extends ModuleElement> getAllModuleElements() {
        return Collections.emptySet();
    }

    Map<? extends ExecutableElement, ? extends AnnotationValue>
            getElementValuesWithDefaults(AnnotationMirror a);

    String getDocComment(Element e);

    boolean isDeprecated(Element e);

    default Origin getOrigin(Element e) {
        return Origin.EXPLICIT;
    }

    default Origin getOrigin(AnnotatedConstruct c,
                             AnnotationMirror a) {
        return Origin.EXPLICIT;
    }

    default Origin getOrigin(ModuleElement m,
                             ModuleElement.Directive directive) {
        return Origin.EXPLICIT;
    }

    public enum Origin {
        EXPLICIT,

        MANDATED,

        SYNTHETIC;

        public boolean isDeclared() {
            return this != SYNTHETIC;
        }
    }

    default boolean isBridge(ExecutableElement e) {
        return false;
    }

    Name getBinaryName(TypeElement type);


    PackageElement getPackageOf(Element e);

    default ModuleElement getModuleOf(Element e) {
        return null;
    }

    List<? extends Element> getAllMembers(TypeElement type);

    default TypeElement getOutermostTypeElement(Element e) {
        switch (e.getKind()) {
        case PACKAGE:
        case MODULE:
        case OTHER:
        	return null; // Outside of base model of the javax.lang.model API

        // Elements of all remaining kinds should be enclosed in some
        // sort of class or interface. Check to see if the element is
        // a top-level type; if so, return it. Otherwise, keep going
        // up the enclosing element chain until a top-level type is
        // found.
        default : {
            Element enclosing = e;
            // This implementation is susceptible to infinite loops
            // for misbehaving element implementations.
            while (true) {
                // Conceptual instanceof TypeElement check. If the
                // argument is a type element, put it into a
                // one-element list, otherwise an empty list.
                List<TypeElement> possibleTypeElement = ElementFilter.typesIn(List.of(enclosing));
                if (!possibleTypeElement.isEmpty()) {
                    TypeElement typeElement = possibleTypeElement.get(0);
                    if (typeElement.getNestingKind() == NestingKind.TOP_LEVEL) {
                        return typeElement;
                    }
                }
                enclosing = enclosing.getEnclosingElement();
            }
        }
        }
    }

    List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e);

    boolean hides(Element hider, Element hidden);

    boolean overrides(ExecutableElement overrider, ExecutableElement overridden,
                      TypeElement type);

    String getConstantExpression(Object value);

    void printElements(java.io.Writer w, Element... elements);

    Name getName(CharSequence cs);

    boolean isFunctionalInterface(TypeElement type);

    default boolean isAutomaticModule(ModuleElement module) {
        return false;
    }

    default RecordComponentElement recordComponentFor(ExecutableElement accessor) {
        if (accessor.getEnclosingElement().getKind() == ElementKind.RECORD) {
            for (RecordComponentElement rec : ElementFilter.recordComponentsIn(accessor.getEnclosingElement().getEnclosedElements())) {
                if (Objects.equals(rec.getAccessor(), accessor)) {
                    return rec;
                }
            }
        }
        return null;
    }

    default boolean isCanonicalConstructor(ExecutableElement e) {
        return false;
    }

    default boolean isCompactConstructor(ExecutableElement e) {
        return false;
    }

    default javax.tools.JavaFileObject getFileObjectOf(Element e) {
        throw new UnsupportedOperationException();
    }
}
