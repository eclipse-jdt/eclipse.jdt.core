package org.eclipse.jdt.apt.tests.annotations.readAnnotationType;

import java.util.Set;

import org.eclipse.jdt.apt.tests.annotations.BaseFactory;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class ReadAnnotationTypeProcessorFactory extends BaseFactory {

    public ReadAnnotationTypeProcessorFactory() {
        super(SimpleAnnotation.class.getName());
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> declarations, AnnotationProcessorEnvironment env) {
        return new ReadAnnotationTypeProcessor(declarations, env);
    }

}
