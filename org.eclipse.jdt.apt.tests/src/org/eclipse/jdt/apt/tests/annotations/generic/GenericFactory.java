package org.eclipse.jdt.apt.tests.annotations.generic;

import java.util.Set;

import org.eclipse.jdt.apt.tests.annotations.BaseFactory;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class GenericFactory extends BaseFactory {
	
	public static AbstractGenericProcessor PROCESSOR;
	
	public GenericFactory() {
		super(GenericAnnotation.class.getName());
	}

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> arg0, AnnotationProcessorEnvironment env) {
		PROCESSOR.setEnv(env);
		return PROCESSOR;
	}

}
