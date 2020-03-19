package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

@SupportedAnnotationTypes({"org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug493837Anno"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug493837Proc extends BaseProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(roundEnv.processingOver()) return false;

		for(TypeElement ann: annotations) {
			for(Element type: roundEnv.getElementsAnnotatedWith(ann)) {
				Bug493837Anno anno = type.getAnnotation(Bug493837Anno.class);
				try {
					anno.value();
					reportError(type.getSimpleName() + " didn't thorw a MirroredTypesException");
				} catch(MirroredTypesException e) {
				}
			}
		}

		return false;
	}

}