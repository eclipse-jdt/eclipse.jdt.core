package org.eclipse.jdt.apt.tests.annotations.generic;

import java.util.Collection;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;

public abstract class AbstractGenericProcessor implements AnnotationProcessor {
	protected AnnotationProcessorEnvironment env;
	protected AnnotationTypeDeclaration genericAnnotation;
	protected Collection<Declaration> decls;
	
	public void setEnv(AnnotationProcessorEnvironment env) {
		this.env = env;
		genericAnnotation = (AnnotationTypeDeclaration) env.getTypeDeclaration(GenericAnnotation.class.getName());
		decls = env.getDeclarationsAnnotatedWith(genericAnnotation);
	}
	
}
