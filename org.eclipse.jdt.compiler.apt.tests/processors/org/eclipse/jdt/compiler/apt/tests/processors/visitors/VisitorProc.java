/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.visitors;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner6;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * Processor that tests a variety of Visitors
 */
@SupportedAnnotationTypes({"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class VisitorProc extends BaseProcessor
{
	/**
	 * This visitor is invoked on the top-level types in resources/targets/model.
	 * We expect to see each of the visitX() methods get hit as a result.
	 */
	private static class ElementVisitorTester extends ElementScanner6<Void, Void> {
		
		public enum Visited { TYPE, EXECUTABLE, VARIABLE, TYPEPARAM, PACKAGE, UNKNOWN }
		
		private EnumSet<Visited> _visited = EnumSet.noneOf(Visited.class);
		
		public boolean checkVisits() {
			boolean asExpected = true;
			asExpected &= _visited.contains(Visited.TYPE);
			asExpected &= _visited.contains(Visited.EXECUTABLE);
			asExpected &= _visited.contains(Visited.VARIABLE);
			// TODO: Following two cases not yet implemented:
			//asExpected &= _visited.contains(Visited.TYPEPARAM);
			//asExpected &= _visited.contains(Visited.PACKAGE);
			return asExpected;
		}
		
        /**
         * Check that we can visit types.
         * @return true if all tests passed
         */
        @Override
        public Void visitType(TypeElement e, Void p) {
        	_visited.add(Visited.TYPE);
        	// Scan the type's subtypes, fields, and methods
            return super.visitType(e, p);
        }
        
        /**
         * Check that we can visit methods.
         */
        @Override
        public Void visitExecutable(ExecutableElement e, Void p) {
        	_visited.add(Visited.EXECUTABLE);
        	// Scan the method's parameters
            return super.visitExecutable(e, p);
        }
        
        /**
         * Check that we can visit variables.
         */
        @Override
        public Void visitVariable(VariableElement e, Void p) {
        	_visited.add(Visited.VARIABLE);
            // Variables do not enclose any elements, so no need to call super.
        	return null;
        }

        /**
         * Check that we can visit type parameters.
         */
        @Override
        public Void visitTypeParameter(TypeParameterElement e, Void p) {
        	_visited.add(Visited.TYPEPARAM);
            // Type parameters do not enclose any elements, so no need to call super.
        	return null;
        }
        
        /**
         * Check that we can visit packages.
         */
        @Override
        public Void visitPackage(PackageElement e, Void p) {
        	_visited.add(Visited.PACKAGE);
            // We don't want to scan the package's types here, so don't call super.
        	return null;
        }
        
        /**
         * This should not actually be encountered.
         */
        @Override
        public Void visitUnknown(Element e, Void p) {
        	_visited.add(Visited.UNKNOWN);
        	return null;
        }
        
	}
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver()) {
			return false;
		}
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}
		ElementVisitorTester elementVisitor = new ElementVisitorTester();
		elementVisitor.scan(roundEnv.getRootElements(), null);
		if (!elementVisitor.checkVisits()) {
			reportError("Element visitor was not visited as expected");
			return false;
		}
		reportSuccess();
		return false;
	}

}
