/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class CompletionTests21 extends AbstractJavaModelCompletionTests {

	static {
		//TESTS_NAMES = new String[] { "testGH2260_CaseTypePattern_If_Destructor" };
	}

	public CompletionTests21(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "21");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "21");
		}
		super.setUpSuite();
		COMPLETION_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests21.class);
	}

	public void testGH2260_CaseTypePattern_Switch_NonRecord() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchTypePattern.java", """
				public class SwitchTypePattern  {
					public class Shape {}
					public class Circle extends Shape {}

					public foo(Shape s) {
						switch(s) {
							case Circ
						}
					}
				}\
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case Circ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("""
				SwitchTypePattern.Circle[TYPE_REF]{Circle, , LSwitchTypePattern$Circle;, null, null, 72}\
						""", requestor.getResults());
	}

	public void testGH2260_CaseTypePattern_Switch_Destructor() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/Shape.java", """
				public interface Shape  {
					public record Circle1(String name, int radius) implements Shape  {
					}

				}\
				""");
		this.workingCopies[1] = getWorkingCopy("/Completion/src/Circle.java", """
				public record Circle(String name, int radius) implements Shape  {
				}\
				""");

		this.workingCopies[2] = getWorkingCopy("/Completion/src/SwitchTypePattern.java", """
				public class SwitchTypePattern  {
					public record Circle2(String name, int radius) implements Shape  {
					}
					public foo(Shape s) {
						switch(s) {
							case Circle
						}
					}
				}\
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[2].getSource();
		String completeBehind = "case Circle";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("""
				Shape.Circle1[TYPE_REF]{Shape.Circle1, , LShape$Circle1;, null, null, 49}
				Circle[TYPE_REF]{Circle, , LCircle;, null, null, 56}
				[TYPE_PATTERN]{Circle1(String name,int radius), LShape$Circle1;, null, null, null, 61}
				[TYPE_PATTERN]{Circle2(String name,int radius), LSwitchTypePattern$Circle2;, null, null, null, 61}
				[TYPE_PATTERN]{Circle(String name,int radius), LCircle;, null, null, null, 65}
				SwitchTypePattern.Circle2[TYPE_REF]{Circle2, , LSwitchTypePattern$Circle2;, null, null, 72}\
						""", requestor.getResults());
	}

	public void testGH2260_CaseTypePattern_If_Destructor() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/Shape.java", """
				public interface Shape  {
					public record Circle1(String name, int radius) implements Shape  {
					}

				}\
				""");
		this.workingCopies[1] = getWorkingCopy("/Completion/src/Circle.java", """
				public record Circle(String name, int radius) implements Shape  {
				}\
				""");

		this.workingCopies[2] = getWorkingCopy("/Completion/src/IfTypePattern.java", """
				public class IfTypePattern  {
					public record Circle2(String name, int radius) implements Shape  {
					}
					public foo(Shape s) {
						if(s instanceof Circle) {
						}
					}
				}\
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[2].getSource();
		String completeBehind = "instanceof Circle";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("""
				Shape.Circle1[TYPE_REF]{Shape.Circle1, , LShape$Circle1;, null, null, 49}
				Circle[TYPE_REF]{Circle, , LCircle;, null, null, 56}
				[TYPE_PATTERN]{Circle1(String name,int radius), LShape$Circle1;, null, null, null, 61}
				[TYPE_PATTERN]{Circle2(String name,int radius), LIfTypePattern$Circle2;, null, null, null, 61}
				[TYPE_PATTERN]{Circle(String name,int radius), LCircle;, null, null, null, 65}
				IfTypePattern.Circle2[TYPE_REF]{Circle2, , LIfTypePattern$Circle2;, null, null, 72}\
							""", requestor.getResults());
	}

	public void testGH2260_CaseTypePattern_If_NonRecord() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/IfTypePattern.java", """
				public class IfTypePattern  {
					public class Shape {}
					public class Circle extends Shape {}

					public foo(Shape s) {
						if(s instanceof Circle) {
						}
					}
				}\
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "instanceof Circle";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("""
				IfTypePattern.Circle[TYPE_REF]{Circle, , LIfTypePattern$Circle;, null, null, 76}\
						""", requestor.getResults());

	}
}
