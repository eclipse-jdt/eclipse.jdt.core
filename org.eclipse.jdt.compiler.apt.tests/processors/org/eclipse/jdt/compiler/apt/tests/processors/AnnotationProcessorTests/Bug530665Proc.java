package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug530665Proc extends BaseProcessor {

	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		filer = processingEnv.getFiler();
	}

	@Override
	public void reportError(String value) {
		super.reportError(value);
		throw new RuntimeException(value);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.errorRaised()) {
			return false;
		}

		try {
			filer.getResource(StandardLocation.CLASS_OUTPUT, "targets/AnnotationProcessorTests/bug530665", "nonexists.txt");
			reportError("Filer.getResource(nonexists.txt) did NOT throw!");
		} catch (IOException e) {
			// ignored
		}
		try {
			// can be done multiple times...
			filer.getResource(StandardLocation.CLASS_OUTPUT, "targets/AnnotationProcessorTests/bug530665", "preexists.txt");
			filer.getResource(StandardLocation.CLASS_OUTPUT, "targets/AnnotationProcessorTests/bug530665", "preexists.txt");
		} catch (IOException e) {
			reportError("Filer.getResource(preexists.txt) threw: " + e);
		}

		if (roundEnv.processingOver()) {
			try {
				FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "targets/AnnotationProcessorTests/bug530665", "nonexists.txt");
				try(Writer writer = fo.openWriter()) {
					writer.append("Test");
				}
			} catch(IOException e) {
				reportError("Filer.createResource(nonexists.txt) threw: " + e);
			}
			try {
				filer.createResource(StandardLocation.CLASS_OUTPUT, "targets/AnnotationProcessorTests/bug530665", "nonexists.txt");
				reportError("Filer.createResource(nonexists.txt) second attempt did NOT throw!");
			} catch(IOException e) {
				// ignored
			}

			try {
				FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "targets/AnnotationProcessorTests/bug530665", "preexists.txt");
				if (!fo.delete()) {
					reportError("FileObject.delete() failed!");
				}
			} catch(IOException e) {
				reportError("Filer.createResource(preexists.txt) threw: " + e);
			}

			reportSuccess();
		}
		return false;
	}

}
