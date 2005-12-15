package org.eclipse.jdt.apt.tests.annotations.apitest;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.apt.tests.annotations.BaseFactory;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;


public class APIAnnotationProcessorFactory extends BaseFactory {
	
	public APIAnnotationProcessorFactory(){
		super(Common.class.getName());
	}
	
	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> decls, 
			AnnotationProcessorEnvironment env) {
		return new APIAnnotationProcessor(env);
	}
	
	public static class APIAnnotationProcessor extends BaseProcessor{
		
		public APIAnnotationProcessor(AnnotationProcessorEnvironment env){
			super(env);
		}
		
		public void process() {	
			final Messager msgr = _env.getMessager();
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
								msgr.printError(typeVar + " is assignable to " + collectionType );
							else
								msgr.printError(typeVar + " is not assignable to " + collectionType );
						}
					}
				}else if(decl instanceof TypeDeclaration){
					final TypeDeclaration typeDecl = (TypeDeclaration)decl;
					final Collection<TypeParameterDeclaration> typeParams =
						typeDecl.getFormalTypeParameters();					
					for(TypeParameterDeclaration typeParam : typeParams){
						Declaration owner = typeParam.getOwner();
						msgr.printError("Type parameter '" + typeParam + "' belongs to " + owner.getClass().getName() + " " + owner.getSimpleName() );
					}
				}
				else if( decl instanceof MethodDeclaration ){
					final MethodDeclaration methodDecl = (MethodDeclaration)decl;
					final Collection<TypeParameterDeclaration> typeParams =
						methodDecl.getFormalTypeParameters();					
					for(TypeParameterDeclaration typeParam : typeParams){
						Declaration owner = typeParam.getOwner();
						msgr.printError("Type parameter '" + typeParam + "' belongs to " + owner.getClass().getName() + " " + owner.getSimpleName() );
					}
				}
			}
		}
	}
}
