/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.completion;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.apt.core.completion.AptCompletionEvent;
import org.eclipse.jdt.apt.core.completion.CompletionUtils;
import org.eclipse.jdt.apt.core.internal.declaration.AnnotationMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.DeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.env.ITypeConstants;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.TypesUtil;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.completion.ICompletionEvent;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.IResolvedAnnotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

class AptCompletionEventImpl implements AptCompletionEvent{
	
	private static final char[] EMPTY_CHAR_ARRAY = new char[0];
	
	private final ICompletionEvent _origEvent;
	private final String _membername;
	private final CompletionNode _node;
	private final AnnotationsImpl _enclosing;	
	
	AptCompletionEventImpl(ICompletionEvent event,
						   String memberName, 
						   CompletionNode node,
						   AnnotationsImpl enclosing)
	{
		_origEvent = event;
		_membername = memberName;
		_node = node;
		_enclosing = enclosing;
	}
	
	public IJavaProject getJavaProject(){ return _origEvent.getJavaProject(); }
	
	public void acceptProposal(int relevance, Object value, int replaceStart, int replaceEnd) {
		final int kind = COMPLETION_LITERAL;
		if( value == null || ignore(kind) ) return;		
		
		final CompletionProposal p = 
			CompletionProposal.create(kind, getCompletionOffset());
		final String completion = value.toString();
		final int length = completion.length();
		final char[] completionChars;
		char quote = 't'; // set it to something
		if( value instanceof Character )
			quote = '\'';
		else if( value instanceof String )
			quote = '\"';
		if( quote != 't' ){
			completionChars = new char[length + 2];
			completionChars[0] = quote;
			int index = 1;
			for( int i=0; i<length; i++ )
				completionChars[index++] = completion.charAt(i);
			completionChars[index] = quote;
		}
		else 
			completionChars = completion.toCharArray();
	
		p.setSignature(getValueTypeSignature(value));
		p.setCompletion(completionChars);
		p.setName(completionChars);
		// set relevance and replacement range.
		p.setReplaceRange(replaceStart, replaceEnd);	
		p.setRelevance( relevance < 1 ? 1 : relevance );
		
		_origEvent.accept(p);
		
	}
	
	/**
	 * Return the type signature of the given object. If the type of the object
	 * is one of the primitive type wrapper, the corresponding primitive
	 * type signature would be returned. If the type is of <code>java.lang.String</code>
	 * then the String type signature would be returned. 
	 * Everything else will return in an exception.
	 *  
	 * @param obj either of type <code>java.lang.String</code> or a
	 * wrapper for primitve types. 
	 * @return the type signature corresponding to the type of the parameter
	 * @throws IllegalArgumentException if <code>obj</code> is not of type
	 * <code>java.lang.String</code> or a primitive type wrapper.
	 */
	public char[] getValueTypeSignature(Object obj){
		if( obj instanceof String ){	
			// the completion type name would look funny
			// if you use slashes instead of dots. 
			// if you don't use the 'L' and ';' things will blow up
			// along the way and the exception be swallowed and you 
			// won't see your completion.  -theodora
			return "Ljava.lang.String;".toCharArray(); //$NON-NLS-1$
		}
		else{
			final String name;
			if( obj instanceof Boolean )
				name = ITypeConstants.BOOLEAN;
			else if( obj instanceof Byte )
				name = ITypeConstants.BYTE;
			else if( obj instanceof Character )
				name = ITypeConstants.CHAR;
			else if( obj instanceof Double )
				name = ITypeConstants.DOUBLE;
			else if( obj instanceof Float )
				name = ITypeConstants.FLOAT;
			else if( obj instanceof Integer )
				name = ITypeConstants.INT;
			else if( obj instanceof Long )
				name = ITypeConstants.LONG;
			else if( obj instanceof Short )
				name = ITypeConstants.SHORT;
			else
				throw new IllegalArgumentException("Invalid argument " + obj ); //$NON-NLS-1$
			return CompletionUtils.getPrimitiveSig(name);
		}
	}
	
	public void acceptProposal(int relevance, PackageDeclaration pkg, int replaceStart, int replaceEnd) {
		final int kind = COMPLETION_PACKAGE_REF;
		if( pkg == null || ignore(kind) ) return;
		
		final CompletionProposal p = 
			CompletionProposal.create(kind, getCompletionOffset());		
		setFlags((DeclarationImpl)pkg, p);
		p.setCompletion(pkg.getSimpleName().toCharArray());		
		// set relevance and replacement range.
		p.setReplaceRange(replaceStart, replaceEnd);	
		p.setRelevance( relevance < 1 ? 1 : relevance );
		
		_origEvent.accept(p);		
	}
	
	public void acceptProposal(int relevance, FieldDeclaration field, int replaceStart, int replaceEnd) {
		final int kind = COMPLETION_FIELD_REF;
		if( field == null || ignore(kind) ) return;
		final CompletionProposal p = 
			CompletionProposal.create(kind, getCompletionOffset());
		
		final char[] fieldName = field.getSimpleName().toCharArray();
		final TypeDeclaration type = field.getDeclaringType();
		p.setName(fieldName);
		final char[] typeSig = CompletionUtils.getSignature(type); 
		p.setDeclarationSignature(typeSig);
		
		setFlags((DeclarationImpl)field, p);
		boolean isEnum = field instanceof EnumConstantDeclaration;
		
		if( isEnum ){		
			
			final String typename = type.getSimpleName();
			final char[] fullname = new char[typename.length() + fieldName.length + 1];			
			int index = 0;
			for( int i=0, len=typename.length(); i<len; i++ )
				fullname[index++] = typename.charAt(i);				
			fullname[index++] = '.';
			System.arraycopy(fieldName, 0, fullname, index, fieldName.length);
			
			// set the completion text itself
			p.setCompletion(fullname);
			
			// set the type of the constant which is the same as the declaration types
			p.setSignature(typeSig);
		}
		else{
			p.setCompletion(fieldName);
			p.setSignature( CompletionUtils.getSignature(field.getType()));
		}
		
		// set relevance and replacement range.
		p.setReplaceRange(replaceStart, replaceEnd);	
		p.setRelevance( relevance < 1 ? 1 : relevance );
		
		_origEvent.accept(p);
	}
	
	public void acceptProposal(int relevance, TypeDeclaration type, int replaceStart, int replaceEnd) {
		final int kind = COMPLETION_TYPE_REF;
		if( type == null || ignore(kind) ) return;
		final CompletionProposal p = 
			CompletionProposal.create(kind, getCompletionOffset());		
		
		final char[] pkgName = getPackageName(type.getPackage());
		final int pkgNameLen = pkgName.length;		
		final char[] typeName = CompletionUtils.getSignature(type);
		final char[] completion;
		if(pkgNameLen == 0)
			completion = typeName;
		else{
			// -1 to take out the dot between package portion and type name portion
			// of the type qname.
			// -2 to take out the 'L' and ';' in the signature.
			int completionLen = typeName.length - pkgNameLen - 1 - 2;
			completion = new char[completionLen];
			int index = 0;
			for( int i=pkgNameLen+1, len = typeName.length-1; i<len; i++ ){
				completion[index] = typeName[i];
			}			
		}
		setFlags((DeclarationImpl)type, p);
		p.setCompletion(completion);
		p.setDeclarationSignature(pkgName);
		p.setSignature(typeName);				
		
		// set relevance and replacement range.
		p.setReplaceRange(replaceStart, replaceEnd);	
		p.setRelevance( relevance < 1 ? 1 : relevance );
		
	}	
	
	/**
	 * @param decl
	 * @return dot separated qualified name of a package declaration.
	 */
	private char[] getPackageName(PackageDeclaration decl)
	{
		if( decl == null )
			 return EMPTY_CHAR_ARRAY;
		final IPackageBinding binding = TypesUtil.getPackageBinding(decl);
		final String name = binding.getName();
		if( name == null || name.length() == 0 )
			return EMPTY_CHAR_ARRAY;
		return binding.getName().toCharArray();
	}
	
	private void setFlags(final DeclarationImpl decl,
						  final CompletionProposal p)
	{
		if( decl == null ) return;
		final IBinding binding = decl.getDeclarationBinding();
		if(binding == null) return;
		int flags = 0;		
	    
        final int modBits = binding.getModifiers();
	        
        if( org.eclipse.jdt.core.dom.Modifier.isAbstract(modBits) )	
        	flags |= Flags.AccAbstract;        	
        if( org.eclipse.jdt.core.dom.Modifier.isFinal(modBits) ) 		
        	flags |= Flags.AccFinal;
        if( org.eclipse.jdt.core.dom.Modifier.isNative(modBits) ) 		
        	flags |= Flags.AccNative;
        if( org.eclipse.jdt.core.dom.Modifier.isPrivate(modBits) ) 		
        	flags |= Flags.AccPrivate;
        if( org.eclipse.jdt.core.dom.Modifier.isProtected(modBits) ) 
        	flags |= Flags.AccProtected;
        if( org.eclipse.jdt.core.dom.Modifier.isPublic(modBits) ) 	
        	flags |= Flags.AccPublic;
        if( org.eclipse.jdt.core.dom.Modifier.isStatic(modBits) ) 	
        	flags |= Flags.AccStatic;
        if( org.eclipse.jdt.core.dom.Modifier.isStrictfp(modBits) ) 
        	flags |= Flags.AccStrictfp;
        if( org.eclipse.jdt.core.dom.Modifier.isSynchronized(modBits) ) 
        	flags |= Flags.AccSynchronized;
        if( org.eclipse.jdt.core.dom.Modifier.isTransient(modBits) ) 
        	flags |= Flags.AccTransient;
        if( org.eclipse.jdt.core.dom.Modifier.isVolatile(modBits) ) 	
        	flags |= Flags.AccVolatile;
        
        if( decl instanceof EnumConstantDeclaration || 
    		decl instanceof EnumDeclaration	)
        	flags |= Flags.AccEnum;
        
        p.setFlags(flags);
	}
	
	
	
	public void accept(CompletionProposal proposal) {
		if( _origEvent.ignore(proposal.getKind()) ) 
			return;
		_origEvent.accept(proposal);
	}
	
	public boolean ignore(int completionType) {
		return _origEvent.ignore(completionType);
	}
	
	public Declaration getAnnotated() {
		return _enclosing._annotated;
	}
	
	public CompletionNode getCompletionNode() {
		return _node;
	}
	
	public int getCompletionOffset() {
		return _origEvent.getCompletionOffset();
	}
	
	public String getMemberName() {
		return _membername;
	}
	
	public AnnotationStack getEnclosingAnnotations() {
		return _enclosing;
	}
	
	static class CompletionNodeImpl implements CompletionNode{
		
		private final Object _value;
		private final int _start;
		private final int _len;
		
		CompletionNodeImpl(Object value, int start, int len )
		{
			_value = value;
			_start = start;
			_len = len;
		}
		
		public Object getValue() {
			return _value;
		} 
		
		public int getStartingPosition() {
			return _start;
		}
		
		public int getLength() {
			return _len;
		}
	}
	
	static class NameReferenceImpl implements CompletionNode.NameReference {
		private final Name _domName;

		private final BaseProcessorEnv _env;

		NameReferenceImpl(Name domName, BaseProcessorEnv env) {
			assert domName != null : "domName is null"; //$NON-NLS-1$
			_domName = domName;
			_env = env;
		}

		public String getName() {

			final SimpleName simpleName;
			if (_domName instanceof QualifiedName)
				simpleName = ((QualifiedName) _domName).getName();
			else
				simpleName = ((SimpleName) _domName);
			return simpleName.getIdentifier();
		}

		public CompletionNode.NameReference getQualifier() {
			if (_domName instanceof QualifiedName) {
				final Name qualifier = ((QualifiedName) _domName)
						.getQualifier();
				if (qualifier == null)
					return null;
				return new NameReferenceImpl(qualifier, _env);
			}
			return null;
		}

		public Declaration getReference() {
			final IBinding binding = _domName.resolveBinding();
			if( binding == null )
				return null;
			return Factory.createDeclaration(binding, _env);
		}
	}
	
	static class AnnotationsImpl implements AnnotationStack
	{		
		// could be Annotation[] or Annotation
		private final Object _stack;
		private DeclarationImpl _annotated = null;
		CompletionEnvImpl _env = null;
		
		AnnotationsImpl(Object annos)
		{			
			assert annos != null : "'annos' cannot be null."; //$NON-NLS-1$
			_stack = annos;		
		}
		
		void setEnvironment(CompletionEnvImpl env)
		{ 
			assert _env == null : "environment is already set"; //$NON-NLS-1$
			_env = env;
		}
		
		void setAnnotated(DeclarationImpl annotated)
		{
			assert _annotated == null : "declaration is already set"; //$NON-NLS-1$
			_annotated = annotated;
		}
		
		public AnnotationMirror get() {
			final Annotation anno;
			if( _stack instanceof Annotation ){
				anno = (Annotation)_stack;
			}
			else {
				anno = ((Annotation[])_stack )[0];				
			}
		
			return Factory.createAnnotationMirror(anno.resolveAnnotation(), _annotated, _env);
		}
		
		String getOuterMostTypeName(){
			final Annotation anno;
			if( _stack instanceof Annotation )
				anno = (Annotation)_stack;
			else{
				final Annotation[] annos = (Annotation[])_stack;
				final int len = annos.length;
				anno = annos[len-1];
			}
			
			final IResolvedAnnotation resolved = anno.resolveAnnotation();
			if( resolved == null ) return null;				
			final ITypeBinding type = resolved.getAnnotationType();
			return type == null ? null : type.getQualifiedName();
		}
		
		Annotation getOuterMost(){
			if( _stack instanceof Annotation )
				return (Annotation)_stack;
			else{
				final Annotation[] annos = (Annotation[])_stack;
				final int len = annos.length;
				return annos[len-1];
			}
		}
		
		public AnnotationMirror getEnclosingAnnotationOf(AnnotationMirror anno) {
			final AnnotationMirrorImpl impl = (AnnotationMirrorImpl)anno;
			if(_stack instanceof List){			
				final Annotation[] list = (Annotation[])_stack;				
				for( int i=0, len=list.length; i<len; i++ ){
					final Annotation cur = list[i];
					if(cur.resolveAnnotation() == impl.getResolvedAnnotaion()){
						if( i > 0 ){
							final Annotation parent = list[i-1];
							return Factory.createAnnotationMirror(parent.resolveAnnotation(), _annotated, _env);
						}							
					}
				}				
			}			
			return null;
		}
		
		public String memberValueNameOf(AnnotationMirror anno) {
			final AnnotationMirrorImpl impl = (AnnotationMirrorImpl)anno;
			if(_stack instanceof List){				
				final Annotation[] list = (Annotation[])_stack;				
				for( int i=0, len=list.length; i<len; i++ ){
					final Annotation cur = list[i];
					if(cur.resolveAnnotation() == impl.getResolvedAnnotaion()){
						if( i > 0 ){
							final ASTNode parentASTNode = cur.getParent();
							if( parentASTNode instanceof MemberValuePair ){
								final MemberValuePair pair = (MemberValuePair)parentASTNode;
								if( pair.getName() != null ) return pair.getName().getIdentifier();
								return ""; //$NON-NLS-1$
							}
						}
					}
				}			
			}
			
			return null;
		}
		
		Set<AnnotationTypeDeclaration> computeAnnotationTypes()
		{
			if( _stack instanceof Annotation ){
				final ITypeBinding type = ((Annotation)_stack).resolveAnnotation().getAnnotationType();
				if( type.isAnnotation() ){
					final AnnotationTypeDeclaration decl = 
						(AnnotationTypeDeclaration)Factory.createReferenceType(type, _env);
					return Collections.singleton(decl);
				}
				// should reach here since computerAnnotationStack() would have
				// eliminated this case.
				return null;
			}
			else{
				final Annotation[] annos = (Annotation[])_stack;
				final Set<AnnotationTypeDeclaration> decls = 
					new HashSet<AnnotationTypeDeclaration>(annos.length * 4 / 3 + 1 );
				for( int i=0, len=annos.length; i<len; i++ ){
					final IResolvedAnnotation resolved = annos[i].resolveAnnotation();
					final ITypeBinding type = resolved.getAnnotationType();
					if( type != null && type.isAnnotation() ){
						final AnnotationTypeDeclaration decl =
							(AnnotationTypeDeclaration)Factory.createReferenceType(type, _env);
						decls.add(decl);
					}				
				}
				return decls;
			}
		}
	}
}
