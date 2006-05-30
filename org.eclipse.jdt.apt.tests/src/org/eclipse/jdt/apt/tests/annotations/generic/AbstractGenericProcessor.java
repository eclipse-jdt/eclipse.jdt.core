package org.eclipse.jdt.apt.tests.annotations.generic;

import java.util.Collection;

import junit.framework.AssertionFailedError;

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
	
	public abstract void _process();
	
	public final void process() {
		try {
			_process();
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw new AssertionFailedError("Processor threw an exception during processing; " + t);
		}
	}
	
}
