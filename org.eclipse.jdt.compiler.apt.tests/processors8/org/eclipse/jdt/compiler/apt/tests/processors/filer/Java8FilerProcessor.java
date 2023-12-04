/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.filer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that reads the GenResource annotation and produces the specified Java type
 */
@SupportedAnnotationTypes({"targets.filer8.PackageAnnot"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({})
public class Java8FilerProcessor extends BaseProcessor {

	String simpleName = "filer8";
	String packageName = "targets.filer8";
	int roundNo = 0;
	boolean reportSuccessAlready = true;

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		String packageName = "targets.filer8";
		this.reportSuccessAlready = true;
		if (++roundNo == 1) {
			this.reportSuccessAlready = false;
			try {
				createPackageBinary();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.setProperty(this.getClass().getName(), "Processor did not fully do the job");
		} else if (roundNo == 2){
			PackageElement packageEl = null;
			for (Element e : roundEnv.getRootElements()) {
				if (e.getKind() == ElementKind.PACKAGE) {
					packageEl = (PackageElement) e;
				}
			}
			if (packageEl == null) {
				reportError("Package Element not found");
				return false;
			}
			if (!packageName.equals(packageEl.getQualifiedName().toString())) {
				reportError("Package Element \"targets.filer8\" not found");
				return false;
			}
			if (packageEl.isUnnamed()) {
				reportError("Package info not found");
				return false;
			}
			if (packageEl.getAnnotationMirrors().isEmpty()) {
				reportError("Annotatons not found in package-info");
				return false;
			}
		}
		if (this.reportSuccessAlready) {
			super.reportSuccess();
		}
		return false;
	}
	private void createPackageBinary() throws IOException {
		String path = packageName.replace('.', '/');
		ClassLoader loader = getClass().getClassLoader();
		try (InputStream in = loader.getResourceAsStream(path + "/package-info.class")) {
			Filer filer = processingEnv.getFiler();
			try (OutputStream out = filer.createClassFile(packageName + ".package-info").openOutputStream()) {
				if (in != null && out != null) {
					int c = in.read();
					while (c != -1) {
						out.write(c);
						c = in.read();
					}
				}
			}
		}
	}
}
