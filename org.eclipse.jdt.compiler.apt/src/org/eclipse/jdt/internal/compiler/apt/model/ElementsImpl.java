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

package org.eclipse.jdt.internal.compiler.apt.model;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;

/**
 * Utilities for working with language elements.
 * There is one of these for every ProcessingEnvironment.
 */
public class ElementsImpl implements Elements {

	// Used for parsing Javadoc comments: matches initial delimiter, followed by whitespace
	private static final Pattern INITIAL_DELIMITER = Pattern.compile("^\\s*/\\*\\*\\s*"); //$NON-NLS-1$
	// Used for parsing Javadoc comments: matches initial whitespace followed by one or more stars
	private static final Pattern INITIAL_WHITESPACE_STARS = Pattern.compile("^\\s*\\*+"); //$NON-NLS-1$

	
	private final BaseProcessingEnvImpl _env;
	
	/*
	 * The processing env creates and caches an ElementsImpl.  Other clients should
	 * not create their own; they should ask the env for it.
	 */
	public ElementsImpl(BaseProcessingEnvImpl env) {
		_env = env;
	}

	/**
	 * Return all the annotation mirrors on this element, including inherited annotations.
	 * Annotations are inherited only if the annotation type is meta-annotated with @Inherited,
	 * and the annotation is on a class: e.g., annotations are not inherited for interfaces, methods,
	 * or fields.
	 */
	@Override
	public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
		// if e is a class, walk up its superclass hierarchy looking for @Inherited annotations not already in the list
		if (e.getKind() == ElementKind.CLASS && e instanceof TypeElementImpl) {
			List<AnnotationBinding> annotations = new ArrayList<AnnotationBinding>();
			// A class can only have one annotation of a particular annotation type.
			Set<ReferenceBinding> annotationTypes = new HashSet<ReferenceBinding>();
			ReferenceBinding binding = (ReferenceBinding)((TypeElementImpl)e)._binding;
			while (null != binding) {
				for (AnnotationBinding annotation : binding.getAnnotations()) {
					ReferenceBinding annotationType = annotation.getAnnotationType(); 
					if (!annotationTypes.contains(annotationType)) {
						annotationTypes.add(annotationType);
						annotations.add(annotation);
					}
				}
				binding = binding.superclass();
			}
			List<AnnotationMirror> list = new ArrayList<AnnotationMirror>(annotations.size());
			for (AnnotationBinding annotation : annotations) {
				list.add(_env.getFactory().newAnnotationMirror(annotation));
			}
			return Collections.unmodifiableList(list);
		}
		else {
			return e.getAnnotationMirrors();
		}
	}

	/**
	 * Compute a list of all the visible entities in this type.  Specifically:
	 * <ul>
	 * <li>All nested types declared in this type, including interfaces and enums</li>
	 * <li>All protected or public nested types declared in this type's superclasses 
	 * and superinterfaces, that are not hidden by a name collision</li>
	 * <li>All methods declared in this type, including constructors but not
	 * including static or instance initializers, and including abstract
	 * methods and unimplemented methods declared in interfaces</li>
	 * <li>All protected or public methods declared in this type's superclasses,
	 * that are not overridden by another method, but not including constructors
	 * or initializers.  Includes abstract methods and methods declared in 
	 * superinterfaces but not implemented</li>
	 * <li>All fields declared in this type, including constants</li>
	 * <li>All non-private fields declared in this type's superclasses and
	 * superinterfaces, that are not hidden by a name collision.</li>
	 * </ul>
	 */
	@Override
	public List<? extends Element> getAllMembers(TypeElement type) {
		if (null == type || !(type instanceof TypeElementImpl)) {
			return Collections.emptyList();
		}
		ReferenceBinding binding = (ReferenceBinding)((TypeElementImpl)type)._binding;
		// Map of element simple name to binding
		Map<String, ReferenceBinding> types = new HashMap<String, ReferenceBinding>();
		// Javac implementation does not take field name collisions into account
		List<FieldBinding> fields = new ArrayList<FieldBinding>();
		// For methods, need to compare parameters, not just names
		Map<String, Set<MethodBinding>> methods = new HashMap<String, Set<MethodBinding>>();
		Set<ReferenceBinding> superinterfaces = new LinkedHashSet<ReferenceBinding>();
		boolean ignoreVisibility = true;
		while (null != binding) {
			addMembers(binding, ignoreVisibility, types, fields, methods);
			Set<ReferenceBinding> newfound = new LinkedHashSet<ReferenceBinding>();
			collectSuperInterfaces(binding, superinterfaces, newfound);
			for (ReferenceBinding superinterface : newfound) {
				addMembers(superinterface, false, types, fields, methods);
			}
			superinterfaces.addAll(newfound);
			binding = binding.superclass();
			ignoreVisibility = false;
		}
		List<Element> allMembers = new ArrayList<Element>();
		for (ReferenceBinding nestedType : types.values()) {
			allMembers.add(_env.getFactory().newElement(nestedType));
		}
		for (FieldBinding field : fields) {
			allMembers.add(_env.getFactory().newElement(field));
		}
		for (Set<MethodBinding> sameNamedMethods : methods.values()) {
			for (MethodBinding method : sameNamedMethods) {
				allMembers.add(_env.getFactory().newElement(method));
			}
		}
		return allMembers;
	}
	
	/**
	 * Recursively depth-first walk the tree of superinterfaces of a type, collecting
	 * all the unique superinterface bindings.  (Note that because of generics, a type may
	 * have multiple unique superinterface bindings corresponding to the same interface
	 * declaration.)
	 * @param existing bindings already in this set will not be re-added or recursed into
	 * @param newfound newly found bindings will be added to this set
	 */
	private void collectSuperInterfaces(ReferenceBinding type, 
			Set<ReferenceBinding> existing, Set<ReferenceBinding> newfound) {
		for (ReferenceBinding superinterface : type.superInterfaces()) {
			if (!existing.contains(superinterface) && !newfound.contains(superinterface)) {
				newfound.add(superinterface);
				collectSuperInterfaces(superinterface, existing, newfound);
			}
		}
	}

	/**
	 * Add the members of a type to the maps of subtypes, fields, and methods.  Add only those
	 * which are non-private and which are not overridden by an already-discovered member. 
	 * For fields, add them all; javac implementation does not take field hiding into account.
	 * @param binding the type whose members will be added to the lists
	 * @param ignoreVisibility if true, all members will be added regardless of whether they
	 * are private, overridden, etc.
	 * @param types a map of type simple name to type binding
	 * @param fields a list of field bindings
	 * @param methods a map of method simple name to set of method bindings with that name
	 */
	private void addMembers(ReferenceBinding binding, boolean ignoreVisibility, Map<String, ReferenceBinding> types,
			List<FieldBinding> fields, Map<String, Set<MethodBinding>> methods)
	{
		for (ReferenceBinding subtype : binding.memberTypes()) {
			if (ignoreVisibility || !subtype.isPrivate()) {
				String name = new String(subtype.sourceName());
				if (null == types.get(name)) {
					types.put(name, subtype);
				}
			}
		}
		for (FieldBinding field : binding.fields()) {
			if (ignoreVisibility || !field.isPrivate()) {
				fields.add(field);
			}
		}
		for (MethodBinding method : binding.methods()) {
			if (!method.isSynthetic() && (ignoreVisibility || (!method.isPrivate() && !method.isConstructor()))) {
				String methodName = new String(method.selector);
				Set<MethodBinding> sameNamedMethods = methods.get(methodName);
				if (null == sameNamedMethods) {
					// New method name.  Create a set for it and add it to the list.
					// We don't expect many methods with same name, so only 4 slots:
					sameNamedMethods = new HashSet<MethodBinding>(4); 
					methods.put(methodName, sameNamedMethods);
					sameNamedMethods.add(method);
				}
				else {
					// We already have a method with this name.  Is this method overridden?
					boolean unique = true;
					if (!ignoreVisibility) {
						for (MethodBinding existing : sameNamedMethods) {
							MethodVerifier verifier = _env.getLookupEnvironment().methodVerifier();
							if (verifier.doesMethodOverride(existing, method)) {
								unique = false;
								break;
							}
						}
					}
					if (unique) {
						sameNamedMethods.add(method);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getBinaryName(javax.lang.model.element.TypeElement)
	 */
	@Override
	public Name getBinaryName(TypeElement type) {
		TypeElementImpl typeElementImpl = (TypeElementImpl) type;
		ReferenceBinding referenceBinding = (ReferenceBinding) typeElementImpl._binding;
		return new NameImpl(
			CharOperation.replaceOnCopy(referenceBinding.constantPoolName(), '/', '.'));
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getConstantExpression(java.lang.Object)
	 */
	@Override
	public String getConstantExpression(Object value) {
		if (value instanceof Character) {
			StringBuilder builder = new StringBuilder();
			builder.append('\'').append(value).append('\'');
			return String.valueOf(builder);
		}
		return String.valueOf(value);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getDocComment(javax.lang.model.element.Element)
	 */
	@Override
	public String getDocComment(Element e) {
		char[] unparsed = getUnparsedDocComment(e);
		return formatJavadoc(unparsed);
	}

	/**
	 * Return the entire javadoc comment on e, including the comment characters and whitespace
	 * @param e an Element of any sort, possibly with a javadoc comment.
	 * @return a String, or null if the comment is not available
	 */
	private char[] getUnparsedDocComment(Element e)
	{
		Javadoc javadoc = null;
		ReferenceContext referenceContext = null;
		switch(e.getKind()) {
			case ANNOTATION_TYPE :
			case CLASS :
			case ENUM :
			case INTERFACE :
				TypeElementImpl typeElementImpl = (TypeElementImpl) e;
				ReferenceBinding referenceBinding = (ReferenceBinding)typeElementImpl._binding;
				if (referenceBinding instanceof SourceTypeBinding) {
					SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) referenceBinding;
					referenceContext = sourceTypeBinding.scope.referenceContext;
					javadoc = ((TypeDeclaration) referenceContext).javadoc;
				}
				break;
			case PACKAGE :
				// might need to handle javadoc of package-info.java file
				return null;
			case CONSTRUCTOR :
			case METHOD :
				ExecutableElementImpl executableElementImpl = (ExecutableElementImpl) e;
				MethodBinding methodBinding = (MethodBinding) executableElementImpl._binding;
				AbstractMethodDeclaration sourceMethod = methodBinding.sourceMethod();
				if (sourceMethod != null) {
					javadoc = sourceMethod.javadoc;
					referenceContext = sourceMethod;
				}
				break;
			case ENUM_CONSTANT :
			case FIELD :
				VariableElementImpl variableElementImpl = (VariableElementImpl) e;
				FieldBinding fieldBinding = (FieldBinding) variableElementImpl._binding;
				FieldDeclaration sourceField = fieldBinding.sourceField();
				if (sourceField != null) {
					javadoc = sourceField.javadoc;
					if (fieldBinding.declaringClass instanceof SourceTypeBinding) {
						SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) fieldBinding.declaringClass;
						referenceContext = sourceTypeBinding.scope.referenceContext;
					}
				}
		}
		if (javadoc != null && referenceContext != null) {
			char[] contents = referenceContext.compilationResult().getCompilationUnit().getContents();
			if (contents != null) {
				return CharOperation.subarray(contents, javadoc.sourceStart, javadoc.sourceEnd - 1);
			}
		}
		return null;
	}

	/**
	 * Strip the comment characters from a javadoc comment. Assume the comment is already
	 * missing its closing delimiter.
	 * 
	 * We mainly do not attempt to emulate the baroque behavior of javac with respect to
	 * treatment of whitespace. The rules here are simpler: eliminate the opening and
	 * closing delimiter, and eliminate [whitespace plus stars] at the beginning of each
	 * line. If the first or last line then contain only whitespace, discard them
	 * entirely.  Javac also does things like: expand tabs at the beginning of the line if
	 * the first non-whitespace char is not a star; if the first line contains whitespace
	 * after the delimiter, but no non-whitespace chars, then delete the whitespace but
	 * preserve the newline; and so forth.
	 */
	public static String formatJavadoc(char[] unparsed)
	{
		if (unparsed == null || unparsed.length < 5) { // delimiters take 5 chars
			return null;
		}
		
		String[] lines = new String(unparsed).split("\n"); //$NON-NLS-1$
		Matcher delimiterMatcher = INITIAL_DELIMITER.matcher(lines[0]);
		if (!delimiterMatcher.find()) {
			return null;
		}
		int iOpener = delimiterMatcher.end();
		lines[0] = lines[0].substring(iOpener);
		if (lines.length == 1) {
			// single-line comment.  Should trim(), but javac doesn't.
			return lines[0];
		}
		
		int firstLine = lines[0].trim().length() > 0 ? 0 : 1;
		
		// for each line after the first, including the last, if it starts with whitespace
		// followed by stars, skip all that. 
		for (int line = 1; line < lines.length; ++line) {
			Matcher whitespaceMatcher = INITIAL_WHITESPACE_STARS.matcher(lines[line]);
			if (whitespaceMatcher.find()) {
				int firstAfterStars = whitespaceMatcher.end();
				lines[line] = lines[line].substring(firstAfterStars);
			}
		}
		
		// If the last line is now empty, skip it
		int lastLine = lines[lines.length - 1].trim().length() > 0 ? lines.length - 1 : lines.length - 2;
		
		StringBuilder sb = new StringBuilder();
		for (int line = firstLine; line <= lastLine; ++line) {
			sb.append(lines[line]);
			// append a newline at the end of each line except the last, even if we skipped the last entirely
			if (line < lines.length - 1) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}
	
	/**
	 * @return all the annotation instance's explicitly set values, plus default values
	 *         for all the annotation members that are not explicitly set but that have
	 *         defaults. By comparison, {@link AnnotationMirror#getElementValues()} only
	 *         returns the explicitly set values.
	 * @see javax.lang.model.util.Elements#getElementValuesWithDefaults(javax.lang.model.element.AnnotationMirror)
	 */
	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
			AnnotationMirror a) {
		return ((AnnotationMirrorImpl)a).getElementValuesWithDefaults();
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getName(java.lang.CharSequence)
	 */
	@Override
	public Name getName(CharSequence cs) {
		return new NameImpl(cs);
	}

	@Override
	public PackageElement getPackageElement(CharSequence name) {
		LookupEnvironment le = _env.getLookupEnvironment();
		if (name.length() == 0) {
			return new PackageElementImpl(_env, le.defaultPackage);
		}
		char[] packageName = name.toString().toCharArray();
		PackageBinding packageBinding = le.createPackage(CharOperation.splitOn('.', packageName));
		if (packageBinding == null) {
			return null;
		}
		return new PackageElementImpl(_env, packageBinding);
	}

	@Override
	public PackageElement getPackageOf(Element type) {
		switch(type.getKind()) {
			case ANNOTATION_TYPE :
			case CLASS :
			case ENUM :
			case INTERFACE :
				TypeElementImpl typeElementImpl = (TypeElementImpl) type;
				ReferenceBinding referenceBinding = (ReferenceBinding)typeElementImpl._binding;
				return (PackageElement) _env.getFactory().newElement(referenceBinding.fPackage);
			case PACKAGE :
				return (PackageElement) type;
			case CONSTRUCTOR :
			case METHOD :
				ExecutableElementImpl executableElementImpl = (ExecutableElementImpl) type;
				MethodBinding methodBinding = (MethodBinding) executableElementImpl._binding;
				return (PackageElement) _env.getFactory().newElement(methodBinding.declaringClass.fPackage);
			case ENUM_CONSTANT :
			case FIELD :
				VariableElementImpl variableElementImpl = (VariableElementImpl) type;
				FieldBinding fieldBinding = (FieldBinding) variableElementImpl._binding;
				return (PackageElement) _env.getFactory().newElement(fieldBinding.declaringClass.fPackage);
			case PARAMETER :
				variableElementImpl = (VariableElementImpl) type;
				LocalVariableBinding localVariableBinding = (LocalVariableBinding) variableElementImpl._binding;
				return (PackageElement) _env.getFactory().newElement(localVariableBinding.declaringScope.classScope().referenceContext.binding.fPackage);
			case EXCEPTION_PARAMETER :
			case INSTANCE_INIT :
			case OTHER :
			case STATIC_INIT :
			case TYPE_PARAMETER :
			case LOCAL_VARIABLE :
				return null;
		}
		// unreachable
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getTypeElement(java.lang.CharSequence)
	 */
	@Override
	public TypeElement getTypeElement(CharSequence name) {
		LookupEnvironment le = _env.getLookupEnvironment();
		final char[][] compoundName = CharOperation.splitOn('.', name.toString().toCharArray());
		ReferenceBinding binding = le.getType(compoundName);
		// If we didn't find the binding, maybe it's a nested type;
		// try finding the top-level type and then working downwards.
		if (null == binding) {
			ReferenceBinding topLevelBinding = null;
			int topLevelSegments = compoundName.length; 
			while (--topLevelSegments > 0) {
				char[][] topLevelName = new char[topLevelSegments][];
				for (int i = 0; i < topLevelSegments; ++i) {
					topLevelName[i] = compoundName[i];
				}
				topLevelBinding = le.getType(topLevelName);
				if (null != topLevelBinding) {
					break;
				}
			}
			if (null == topLevelBinding) {
				return null;
			}
			binding = topLevelBinding;
			for (int i = topLevelSegments; null != binding && i < compoundName.length; ++i) {
				binding = binding.getMemberType(compoundName[i]);
			}
		}
		if (null == binding) {
			return null;
		}
		return new TypeElementImpl(_env, binding);
	}

	/* (non-Javadoc)
	 * Element A hides element B if: A and B are both fields, both nested types, or both methods; and
	 * the enclosing element of B is a superclass or superinterface of the enclosing element of A.
	 * See JLS 8.3 (for hiding of fields), 8.4.8.2 (hiding of class methods), and 8.5 (for hiding of member types).
	 * @see javax.lang.model.util.Elements#hides(javax.lang.model.element.Element, javax.lang.model.element.Element)
	 */
	@Override
	public boolean hides(Element hider, Element hidden) {
		if (hidden == null) {
			// required by API spec
			throw new NullPointerException();
		}
		return ((ElementImpl)hider).hides(hidden);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#isDeprecated(javax.lang.model.element.Element)
	 */
	@Override
	public boolean isDeprecated(Element e) {
		if (!(e instanceof ElementImpl)) {
			return false;
		}
		return (((ElementImpl)e)._binding.getAnnotationTagBits() & TagBits.AnnotationDeprecated) != 0;
	}

	/* (non-Javadoc)
	 * See JLS 8.4.8.1 for discussion of hiding of methods
	 * @see javax.lang.model.util.Elements#overrides(javax.lang.model.element.ExecutableElement, javax.lang.model.element.ExecutableElement, javax.lang.model.element.TypeElement)
	 */
	@Override
	public boolean overrides(ExecutableElement overrider, ExecutableElement overridden,
			TypeElement type) {
		if (overridden == null || type == null) {
			throw new NullPointerException();
		}
		return ((ExecutableElementImpl)overrider).overrides(overridden, type);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#printElements(java.io.Writer, javax.lang.model.element.Element[])
	 */
	@Override
	public void printElements(Writer w, Element... elements) {
		String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		for (Element element : elements) {
			try {
				w.write(element.toString());
				w.write(lineSeparator);
			} catch (IOException e) {
				// ignore
			}
		}
		try {
			w.flush();
		} catch (IOException e) {
			// ignore
		}
	}

}
