package org.eclipse.jdt.apt.tests.annotations.exceptionhandling;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Set;

import org.eclipse.jdt.apt.tests.annotations.BaseFactory;


public class ExceptionHandlingProcessorFactory extends BaseFactory
{

	public ExceptionHandlingProcessorFactory() {
        super(ExceptionHandlingAnnotation.class.getName());
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> declarations, AnnotationProcessorEnvironment env) {
        return new ExceptionHandlingProcessor(declarations, env);
    }
} 
