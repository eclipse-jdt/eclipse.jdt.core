/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package proc.genclass;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("anno.GenClass")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class GenClassProc extends AbstractProcessor {

	private Messager _messager;
	private Filer _filer;
	private Elements _elementUtil;
	private TypeElement _annoDecl;
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_filer = processingEnv.getFiler();
		_messager = processingEnv.getMessager();
		_elementUtil = processingEnv.getElementUtils();
		_annoDecl = _elementUtil.getTypeElement("anno.GenClass");
		
		//System.out.println("Processor options are: ");
		//for (Map.Entry<String, String> option : processingEnv.getOptions().entrySet()) {
		//	System.out.println(option.getKey() + " -> " + option.getValue());
		//}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		
		if (annotations == null || annotations.isEmpty()) {
			return true;
		}
		// sanity check
		if (!annotations.contains(_annoDecl)) {
			throw new IllegalArgumentException("process() called on an unexpected set of annotations");
		}
		
		// get annotated declarations - could also use getElsAnnoWith(Class) form.
		for (Element d : roundEnv.getElementsAnnotatedWith(_annoDecl)) {
			// get annotations on the declaration
			String clazz = null;
			String method = null;
			for (AnnotationMirror am : d.getAnnotationMirrors()) {
				if (am.getAnnotationType().asElement().equals(_annoDecl)) {
					// query the annotation to get its values
					Map<? extends ExecutableElement, ? extends AnnotationValue> values = am.getElementValues();
					// find the "clazz" and "msg" values
					for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
						// System.out.println("found a value named " + entry.getKey().getSimpleName() + " with value " + entry.getValue().getValue());
						if ("clazz".equals(entry.getKey().getSimpleName().toString())) {
							clazz = (String)entry.getValue().getValue();
						}
						if ("method".equals(entry.getKey().getSimpleName().toString())) {
							method = (String)entry.getValue().getValue();
						}
					}
					
					if (null == clazz || clazz.length() > 40) {
						_messager.printMessage(Diagnostic.Kind.WARNING, "Long name for clazz()", d, am);
						clazz = null;
						break;
					}
					if (null == method || method.length() > 10) {
						_messager.printMessage(Diagnostic.Kind.WARNING, "Long name for method()", d, am);
						method = null;
						break;
					}
				}
			}
			
			if (null != clazz && null != method)
				createSourceFile(clazz, method);
		}
		return true;
	}
	
	/**
	 * Create a source file named 'name', with contents
	 * that reflect 'method' and 'name'.
	 * @param clazz a fully qualified classname
	 * @param method the name of a method that will be
	 * added to the class
	 */
	private void createSourceFile(String clazz, String method) {
		int lastDot = clazz.lastIndexOf('.');
		if (lastDot <= 0 || clazz.length() == lastDot)
			return;
		String pkg = clazz.substring(0, lastDot);
		String lname = clazz.substring(lastDot + 1);
		Writer w = null;
		PrintWriter pw = null;
		try {
			JavaFileObject jfo = _filer.createSourceFile(clazz);
			w = jfo.openWriter();
			pw = new PrintWriter(w);
			pw.println("package " + pkg + ";");
			pw.println("public class " + lname + " {");
			pw.println("\tpublic String " + method + "() {");
			// This compile error won't be reported if the -proc:only flag is set:			
			// pw.println("\t\tb = a;");
			pw.println("\t\treturn new String(\"" + clazz + "\");");
			pw.println("\t}");
			pw.println("}");
			pw.close();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
