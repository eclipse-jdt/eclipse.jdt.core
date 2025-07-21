package javax.annotation.processing;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Objects;
import javax.lang.model.element.*;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;

public abstract class AbstractProcessor implements Processor {
    protected ProcessingEnvironment processingEnv;
    private boolean initialized = false;

    protected AbstractProcessor() {}

    @Override
    public Set<String> getSupportedOptions() {
        SupportedOptions so = this.getClass().getAnnotation(SupportedOptions.class);
        return (so == null) ?
            Set.of() :
            arrayToSet(so.value(), false, "option value", "@SupportedOptions");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
            SupportedAnnotationTypes sat = this.getClass().getAnnotation(SupportedAnnotationTypes.class);
            boolean initialized = isInitialized();
            if  (sat == null) {
                if (initialized)
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                                             "No SupportedAnnotationTypes annotation " +
                                                             "found on " + this.getClass().getName() +
                                                             ", returning an empty set.");
                return Set.of();
            } else {
                boolean stripModulePrefixes =
                        initialized &&
                        processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_8) <= 0;
                return arrayToSet(sat.value(), stripModulePrefixes,
                                  "annotation interface", "@SupportedAnnotationTypes");
            }
        }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        SourceVersion sv = null;
        if (ssv == null) {
            sv = SourceVersion.RELEASE_6;
            if (isInitialized())
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                                         "No SupportedSourceVersion annotation " +
                                                         "found on " + this.getClass().getName() +
                                                         ", returning " + sv + ".");
        } else
            sv = ssv.value();
        return sv;
    }


    public synchronized void init(ProcessingEnvironment processingEnv) {
        if (initialized)
            throw new IllegalStateException("Cannot call init more than once.");
        Objects.requireNonNull(processingEnv, "Tool provided null ProcessingEnvironment");

        this.processingEnv = processingEnv;
        initialized = true;
    }

    @Override
    public abstract boolean process(Set<? extends TypeElement> annotations,
                                    RoundEnvironment roundEnv);

    @Override
    public Iterable<? extends Completion> getCompletions(Element element,
                                                         AnnotationMirror annotation,
                                                         ExecutableElement member,
                                                         String userText) {
        return List.of();
    }

    protected synchronized boolean isInitialized() {
        return initialized;
    }

    private Set<String> arrayToSet(String[] array,
                                          boolean stripModulePrefixes,
                                   String contentType,
                                   String annotationName) {
        assert array != null;
        Set<String> set = new HashSet<>();
        for (String s : array) {
            boolean stripped = false;
            if (stripModulePrefixes) {
                int index = s.indexOf('/');
                if (index != -1) {
                    s = s.substring(index + 1);
                    stripped = true;
                }
            }
            boolean added = set.add(s);
            // Don't issue a duplicate warning when the module name is
            // stripped off to avoid spurious warnings in a case like
            // "foo/a.B", "bar/a.B".
            if (!added && !stripped && isInitialized() ) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                                         "Duplicate " + contentType  +
                                                         " ``" + s  + "'' for processor " +
                                                         this.getClass().getName() +
                                                         " in its " + annotationName  +
                                                         "annotation.");
            }
        }
        return Collections.unmodifiableSet(set);
    }
}
