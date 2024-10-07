/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.mirrortest;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.apt.tests.annotations.BaseFactory;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

public class ASTBasedMirrorDeclarationProcessorFactory
	extends BaseFactory
{
	public ASTBasedMirrorDeclarationProcessorFactory(){
		super("test.DeclarationAnno");
	}

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> arg0,
			AnnotationProcessorEnvironment env) {
		return new ASTBasedMirrorDeclarationProcessor(env);
	}

	public static final class ASTBasedMirrorDeclarationProcessor
		extends BaseProcessor{

		public ASTBasedMirrorDeclarationProcessor(AnnotationProcessorEnvironment env){
			super(env);
		}
		public void process() {
			final Collection<TypeDeclaration> typeDecls = _env.getTypeDeclarations();
			boolean done = false;
			for( TypeDeclaration typeDecl : typeDecls ){
				if(typeDecl instanceof ClassDeclaration ){
					examineClass( (ClassDeclaration)typeDecl );
					if( typeDecl.getQualifiedName().equals("test.Foo") )
						done = true;
				}
			}
			// if the current file does not contain "test.Foo",
			// look for it by name and run the same tests.
			if( !done ){
				final ClassDeclaration fooDecl = (ClassDeclaration)_env.getTypeDeclaration("test.Foo");
				examineClass(fooDecl);
				final ClassDeclaration innerTypeDecl = (ClassDeclaration)_env.getTypeDeclaration("test.Foo$Inner");
				examineClass(innerTypeDecl);
			}
		}

		private void examineClass(ClassDeclaration typeDecl ){

			assertNonNull(typeDecl, "missing type declaration");
			if( typeDecl != null ){
				final String typename = typeDecl.getSimpleName();
				if(typename.equals("Foo")){
					final Collection<? extends MethodDeclaration> methods = typeDecl.getMethods();
					assertEqual(2, methods.size(), "number of methods do not match ");
					for(MethodDeclaration method : methods ){
						final String name = method.getSimpleName();
						if( name.equals("getType") ){
							final String methodString = method.toString();
							final String expectedString = "UnknownType getType()";
							assertEqual(expectedString, methodString, "signature mismatch");
						}
						else if( name.equals("voidMethod")){
							final String methodString = method.toString();
							final String expectedString = "void voidMethod()";
							assertEqual(expectedString, methodString, "signature mismatch");
						}
						else{
							assertEqual(null, method.toString(), "unexpected method");
						}
					}

					final Collection<ConstructorDeclaration> constructors =
						typeDecl.getConstructors();
					assertEqual(1, constructors.size(), "number of constructors do not match");
					for( ConstructorDeclaration constructor : constructors ){
						final String constructorString = constructor.toString();
						final String expectedString = "Foo(UnknownType type)";
						assertEqual(expectedString, constructorString, "signature mismatch");
					}
					final Collection<FieldDeclaration> fields =
						typeDecl.getFields();
					assertEqual(2, fields.size(), "number of fields do not match");
					for( FieldDeclaration field : fields ){
						final String name = field.getSimpleName();
						if( "field0".equals(name) || "field1".equals(name) ){
							continue;
						}
						assertEqual(null, name, "unexpected field");
					}
				}
				else if(typename.equals("Inner")){
					final Collection<? extends MethodDeclaration> methods =
						typeDecl.getMethods();
					assertEqual(0, methods.size(), "number of methods do not match ");

					final Collection<ConstructorDeclaration> constructors =
						typeDecl.getConstructors();
					assertEqual(1, constructors.size(), "number of constructors do not match");
					for( ConstructorDeclaration constructor : constructors ){
						final String constructorString = constructor.toString();
						final String expectedString = "Inner()";
						assertEqual(expectedString, constructorString, "signature mismatch");
					}
					final Collection<FieldDeclaration> fields =
						typeDecl.getFields();
					assertEqual(0, fields.size(), "number of fields do not match");
				}
			}
		}
	}
}
