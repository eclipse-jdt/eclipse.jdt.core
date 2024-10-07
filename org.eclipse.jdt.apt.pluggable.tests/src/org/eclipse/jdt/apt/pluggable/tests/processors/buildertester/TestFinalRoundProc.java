/*******************************************************************************
 * Copyright (c) 2010 Walter Harley and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    eclipse@cafewalter.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.processors.buildertester;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.eclipse.jdt.apt.pluggable.tests.annotations.FinalRoundTestTrigger;

/**
 * Do nothing when first triggered; then, in the final round, generate a
 * new type that is annotated with {@link FinalRoundTestTrigger}.
 * Used to verify that a new type generated in the final round does not get
 * annotations processed, but does get compiled. The JSR269 spec is somewhat
 * vague about whether it should be possible to generate a new type during
 * the final round (since the final round does not happen until after a
 * round in which no new types are generated); but apparently javac behaves
 * this way.
 * <p>
 * See <a href="http://bugs.eclipse.org/329156">Bug 329156</a> and <a
 * href="http://bugs.sun.com/view_bug.do?bug_id=6634138">the corresponding
 * bug in javac</a>, which Sun fixed.
 */
@SupportedAnnotationTypes( { "org.eclipse.jdt.apt.pluggable.tests.annotations.FinalRoundTestTrigger" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions( {})
public class TestFinalRoundProc extends AbstractProcessor {
	private static int _numRounds;

	public static int getNumRounds() {
		return _numRounds;
	}

	public static void resetNumRounds() {
		_numRounds = 0;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			createFile();
		}
		_numRounds++;
		return true;
	}

	private void createFile() {
		PrintWriter pw = null;
		try {
			JavaFileObject jfo = processingEnv.getFiler().createSourceFile("g.FinalRoundGen");
			pw = new PrintWriter(jfo.openWriter());
			pw.println("package g;");
			pw.println("import org.eclipse.jdt.apt.pluggable.tests.annotations.FinalRoundTestTrigger;");
			pw.println("@FinalRoundTestTrigger");
			pw.println("public class FinalRoundGen {}");
		} catch (IOException e) {
			e.printStackTrace();
			processingEnv.getMessager().printMessage(Kind.ERROR, "Unable to create source file! Exception message was: " + e.getMessage());
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

}
