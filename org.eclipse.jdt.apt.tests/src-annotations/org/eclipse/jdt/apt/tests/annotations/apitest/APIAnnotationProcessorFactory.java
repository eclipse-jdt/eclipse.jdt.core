/*******************************************************************************
 * Copyright (c) 2005, 2017 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.apitest;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.apt.tests.annotations.BaseFactory;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;


public class APIAnnotationProcessorFactory extends BaseFactory {

	public APIAnnotationProcessorFactory(){
		super(Common.class.getName(), SubtypeOf.class.getName(), AssignableTo.class.getName());
	}

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> decls,
			AnnotationProcessorEnvironment env) {
		return new APIAnnotationProcessor(env);
	}

	public static class APIAnnotationProcessor extends BaseProcessor{

		private Messager _msgr;
		private Types _types;

		public APIAnnotationProcessor(AnnotationProcessorEnvironment env){
			super(env);
		}

		public void process() {
			_msgr = _env.getMessager();
			_types = _env.getTypeUtils();
			checkCommon();
			checkSubtypeOf();
			checkAssignableTo();
		}

		/**
		 * validate instances of the Common annotation
		 */
		private void checkCommon()
		{
			final AnnotationTypeDeclaration commonAnnoType =
				(AnnotationTypeDeclaration)_env.getTypeDeclaration(Common.class.getName());
			final Collection<Declaration> decls =
				_env.getDeclarationsAnnotatedWith(commonAnnoType);
			for( Declaration decl : decls ){
				if(decl instanceof FieldDeclaration ){
					final FieldDeclaration field = (FieldDeclaration)decl;
					final TypeMirror type = field.getType();
					if( type instanceof DeclaredType ){
						final TypeMirror collectionType =
							_env.getTypeUtils().getDeclaredType(_env.getTypeDeclaration(Collection.class.getName()));
						final Collection<TypeMirror> typeVars =
							((DeclaredType)type).getActualTypeArguments();
						if(typeVars.size() == 1 ){
							TypeMirror typeVar = typeVars.iterator().next();
							boolean assignable = _env.getTypeUtils().isAssignable(typeVar, collectionType);
							if( assignable )
								_msgr.printError(typeVar + " is assignable to " + collectionType );
							else
								_msgr.printError(typeVar + " is not assignable to " + collectionType );
						}
					}
				}else if(decl instanceof TypeDeclaration){
					final TypeDeclaration typeDecl = (TypeDeclaration)decl;
					final Collection<TypeParameterDeclaration> typeParams =
						typeDecl.getFormalTypeParameters();
					for(TypeParameterDeclaration typeParam : typeParams){
						Declaration owner = typeParam.getOwner();
						_msgr.printError("Type parameter '" + typeParam + "' belongs to " + owner.getClass().getName() + " " + owner.getSimpleName() );
					}
				}
				else if( decl instanceof MethodDeclaration ){
					final MethodDeclaration methodDecl = (MethodDeclaration)decl;
					final Collection<TypeParameterDeclaration> typeParams =
						methodDecl.getFormalTypeParameters();
					for(TypeParameterDeclaration typeParam : typeParams){
						Declaration owner = typeParam.getOwner();
						_msgr.printError("Type parameter '" + typeParam + "' belongs to " + owner.getClass().getName() + " " + owner.getSimpleName() );
					}
				}
			}
		}

		/**
		 * Validate all the fields annotated with @SubtypeOf, in order to test
		 * the Types.subtypeOf() method.
		 * We ignore anything but fields, out of laziness.
		 */
		private void checkSubtypeOf() {
			final AnnotationTypeDeclaration annoType =
				(AnnotationTypeDeclaration)_env.getTypeDeclaration(SubtypeOf.class.getName());
			final Collection<Declaration> decls =
				_env.getDeclarationsAnnotatedWith(annoType);
			for( Declaration decl : decls ){
				if(decl instanceof FieldDeclaration ) {
					AnnotationMirror mirror = findMirror(decl, annoType);
					if (mirror == null) {
						return;
					}
					TypeMirror valueType = getTypeValue(mirror);
					final FieldDeclaration field = (FieldDeclaration)decl;
					final TypeMirror fieldType = field.getType();
					boolean isSubtype = _types.isSubtype(fieldType, valueType);
					if( isSubtype )
						_msgr.printError(fieldType + " is a subtype of " + valueType );
					else
						_msgr.printError(fieldType + " is not a subtype of " + valueType );
				}
			}
		}

		/**
		 * Validate all the fields annotated with @AssignableTo.
		 * We ignore anything but fields, out of laziness.
		 */
		private void checkAssignableTo() {
			final AnnotationTypeDeclaration annoType =
				(AnnotationTypeDeclaration)_env.getTypeDeclaration(AssignableTo.class.getName());
			final Collection<Declaration> decls =
				_env.getDeclarationsAnnotatedWith(annoType);
			for( Declaration decl : decls ){
				if(decl instanceof FieldDeclaration ) {
					AnnotationMirror mirror = findMirror(decl, annoType);
					if (mirror == null) {
						return;
					}
					TypeMirror valueType = getTypeValue(mirror);
					final FieldDeclaration field = (FieldDeclaration)decl;
					final TypeMirror fieldType = field.getType();
					boolean isAssignableTo = _types.isAssignable(fieldType, valueType);
					if( isAssignableTo )
						_msgr.printError(fieldType + " is assignable to " + valueType );
					else
						_msgr.printError(fieldType + " is not assignable to " + valueType );
				}
			}
		}

		/**
		 * @return a mirror for the instance of the specified annotation on the specified
		 * declaration, or null if one is not present.
		 */
		private AnnotationMirror findMirror(Declaration decl, AnnotationTypeDeclaration at) {
			for (AnnotationMirror mirror : decl.getAnnotationMirrors()) {
				if (mirror.getAnnotationType().getDeclaration().equals(at)) {
					return mirror;
				}
			}
			return null;
		}

		/**
		 * @return the value() of an annotation instance <code>mirror</code>, if it is a
		 * class value, or null if not.
		 */
		private TypeMirror getTypeValue(AnnotationMirror mirror) {
			Map<AnnotationTypeElementDeclaration, AnnotationValue> values = mirror.getElementValues();
			for (Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : values.entrySet()) {
				if ("value".equals(entry.getKey().getSimpleName())) {
					if (entry.getValue().getValue() instanceof TypeMirror)
						return (TypeMirror)entry.getValue().getValue();
					else
						return null;
				}
			}
			return null;
		}
	}
}
