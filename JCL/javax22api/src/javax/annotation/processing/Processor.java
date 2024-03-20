package javax.annotation.processing;

import java.util.Set;
import javax.lang.model.util.Elements;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.*;
import javax.lang.model.SourceVersion;

public interface Processor {
    Set<String> getSupportedOptions();

    Set<String> getSupportedAnnotationTypes();

    SourceVersion getSupportedSourceVersion();

    void init(ProcessingEnvironment processingEnv);

    boolean process(Set<? extends TypeElement> annotations,
                    RoundEnvironment roundEnv);

   Iterable<? extends Completion> getCompletions(Element element,
                                                  AnnotationMirror annotation,
                                                  ExecutableElement member,
                                                  String userText);
}
