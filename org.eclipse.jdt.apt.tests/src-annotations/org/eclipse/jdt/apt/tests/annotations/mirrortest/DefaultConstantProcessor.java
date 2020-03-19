/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.mirrortest;

import java.util.Collection;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;

public class DefaultConstantProcessor extends BaseProcessor {
	public DefaultConstantProcessor(AnnotationProcessorEnvironment env){
		super(env);
	}
	public void process() {
		final AnnotationTypeDeclaration trigger = (AnnotationTypeDeclaration)_env.getTypeDeclaration("test.Trigger");

		final Messager msger = _env.getMessager();
		if( trigger == null)
			msger.printError("cannot find test.Trigger");

		final Collection<Declaration> decls = _env.getDeclarationsAnnotatedWith(trigger);
		for(Declaration decl : decls ){
			if( decl instanceof TypeDeclaration ){
				final TypeDeclaration typeDecl = (TypeDeclaration)decl;
				if( "test.EntryPoint".equals(typeDecl.getQualifiedName()) ){
					final Collection<FieldDeclaration> fields = typeDecl.getFields();
					for( FieldDeclaration field : fields ){
						final String name = field.getSimpleName();
						if( "nestedAnno".equals(name)){
							final DeclaredType fieldType = (DeclaredType)field.getType();
							final Collection<TypeDeclaration> nestedTypes = fieldType.getDeclaration().getNestedTypes();
							for(TypeDeclaration nestedType : nestedTypes ){
								if( "NestedAnnotation".equals(nestedType.getSimpleName()) ){
									final Collection<? extends MethodDeclaration> annotationMethods = nestedType.getMethods();
									for( MethodDeclaration annotationMethod : annotationMethods ){
										if( "value".equals(annotationMethod.getSimpleName()) ){
											final AnnotationTypeElementDeclaration value =
												(AnnotationTypeElementDeclaration)annotationMethod;
											final String defaultString = value.getDefaultValue() == null ? "" :
												value.getDefaultValue().toString();
											final String expected = "Eore";
											if(!defaultString.equals(expected) )
												msger.printError("expecting default=" + expected + " but got " +defaultString);
										}
									}
								}
							}

							final Collection<FieldDeclaration> nestedAnnoFields = fieldType.getDeclaration().getFields();
							for(FieldDeclaration nestedAnnoField : nestedAnnoFields ){
								if(nestedAnnoField.getSimpleName().equals("FOUR")){
									final Object constant = nestedAnnoField.getConstantValue();
									final String expected = "4";
									final String constantStr = constant == null ? "" : constant.toString();
									if(!constantStr.equals(expected) )
										msger.printError("expecting constant=" + expected + " but got " + constantStr);
								}
							}
							continue;
						}
						else{
							msger.printError("found unexpected field " + field );
						}

					}

					continue;
				}
			}
			msger.printError("found unexpected declaration " + decl );
		}
	}
}
