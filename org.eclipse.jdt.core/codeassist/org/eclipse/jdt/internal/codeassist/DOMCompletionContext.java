/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.codeassist.DOMCompletionEngine.Bindings;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;

class DOMCompletionContext extends CompletionContext {
	private final int offset;
	private final char[] token;
	private final IJavaElement enclosingElement;
	private final Supplier<Stream<IBinding>> bindingsAcquirer;
	final ExpectedTypes expectedTypes;
	private boolean inJavadoc = false;
	final ASTNode node;
	private IBuffer cuBuffer;
	private boolean isJustAfterStringLiteral;

	DOMCompletionContext(CompilationUnit domUnit, ICompilationUnit modelUnit, IBuffer cuBuffer, int offset, AssistOptions assistOptions, Bindings bindings) {
		this.cuBuffer = cuBuffer;
		this.offset = offset;
		// Use the raw text to walk back the offset to the first non-whitespace spot
		int adjustedOffset = this.offset;
		if (adjustedOffset >= cuBuffer.getLength()) {
			adjustedOffset = cuBuffer.getLength() - 1;
		}
		if (adjustedOffset + 1 >= cuBuffer.getLength()
				|| !Character.isJavaIdentifierStart(cuBuffer.getChar(adjustedOffset))) {
			while (adjustedOffset > 0 && Character.isWhitespace(cuBuffer.getChar(adjustedOffset - 1)) ) {
				adjustedOffset--;
			}
		}
		ASTNode previousNodeBeforeWhitespaces = NodeFinder.perform(domUnit, adjustedOffset, 0);
//		if (cuBuffer.getChar(adjustedOffset - 1) == ',' && Character.isWhitespace(cuBuffer.getChar(adjustedOffset))) {
//			// probably an empty parameter
//			adjustedOffset = this.offset;
//			while (adjustedOffset < cuBuffer.getLength() && Character.isWhitespace(cuBuffer.getChar(adjustedOffset))) {
//				adjustedOffset++;
//			}
//		}
		this.node = previousNodeBeforeWhitespaces instanceof SimpleName || previousNodeBeforeWhitespaces instanceof StringLiteral || previousNodeBeforeWhitespaces instanceof CharacterLiteral || previousNodeBeforeWhitespaces instanceof NumberLiteral
			?  NodeFinder.perform(domUnit, this.offset, 0) // keep default node from initial offset
			: previousNodeBeforeWhitespaces; // use previous node
		this.expectedTypes = new ExpectedTypes(assistOptions, this.node, offset);
		this.token = tokenBefore(cuBuffer).toCharArray();
		this.enclosingElement = computeEnclosingElement(domUnit, modelUnit);
		this.bindingsAcquirer = bindings::stream;
		this.isJustAfterStringLiteral = this.node instanceof StringLiteral && this.node.getLength() > 1 && this.offset >= node.getStartPosition() + node.getLength() && cuBuffer.getChar(this.offset - 1) == '"';
	}

	private String tokenBefore(IBuffer cuBuffer) {
		int position = this.offset - 1;
		StringBuilder builder = new StringBuilder();
		while (position >= 0 && Character.isJavaIdentifierPart(cuBuffer.getChar(position))) {
			builder.append(cuBuffer.getChar(position));
			position--;
		}
		builder.reverse();
		return builder.toString();
	}

	private IJavaElement computeEnclosingElement(CompilationUnit domUnit, ICompilationUnit modelUnit) {
		IJavaElement enclosingElement = modelUnit;
		try {
			enclosingElement = modelUnit.getElementAt(this.offset);
		} catch (JavaModelException e) {
			ILog.get().error(e.getMessage(), e);
		}
		if (enclosingElement == null) {
			return modelUnit;
		}
		// then refine to get "resolved" element from the matching binding
		// pitfall: currently resolve O(depth(node)) bindings while we can
		// most likely find a O(1) solution
		ASTNode node = NodeFinder.perform(domUnit, this.offset, 0);
		while (node != null) {
			IBinding binding = resolveBindingForContext(node);
			if (binding != null) {
				IJavaElement bindingBasedJavaElement = binding.getJavaElement();
				if (enclosingElement.equals(bindingBasedJavaElement)) {
					return bindingBasedJavaElement;
				}
			}
			node = node.getParent();
		}
		return enclosingElement;
	}

	private IBinding resolveBindingForContext(ASTNode node) {
		var res = DOMCodeSelector.resolveBinding(node);
		if (res != null) {
			return res;
		}
		// Some declaration types are intentionally skipped by
		// DOMCodeSelector.resolveBinding() as they're not
		// expected by `codeSelect` add them here
		if (node instanceof TypeDeclaration typeDecl) {
			return typeDecl.resolveBinding();
		}
		if (node instanceof MethodDeclaration methodDecl) {
			return methodDecl.resolveBinding();
		}
		if (node instanceof VariableDeclaration varDecl) {
			return varDecl.resolveBinding();
		}
		return null;
	}

	@Override
	public int getOffset() {
		return this.offset;
	}

	@Override
	public char[] getToken() {
		return isJustAfterStringLiteral ? null : this.token;
	}

	@Override
	public boolean isInJavadoc() {
		return this.inJavadoc;
	}

	public void setInJavadoc(boolean inJavadoc) {
		this.inJavadoc = inJavadoc;
	}

	@Override
	public IJavaElement getEnclosingElement() {
		return this.enclosingElement;
	}

	@Override
	public IJavaElement[] getVisibleElements(String typeSignature) {
		return this.bindingsAcquirer.get() //
			.filter(binding -> matchesSignature(binding, typeSignature)) //
			.map(binding -> binding.getJavaElement()) //
			.filter(obj -> obj != null) // eg. ArrayList.getFirst() when working with a Java 8 project
			.toArray(IJavaElement[]::new);
	}

	/// Checks if the binding matches the given type signature
	/// TODO: this should probably live in a helper method/utils class,
	/// along with `castCompatable`
	public static boolean matchesSignature(IBinding binding, String typeSignature) {
		if (typeSignature == null) {
			return binding instanceof IVariableBinding || binding instanceof IMethodBinding;
		}
		if (binding instanceof IVariableBinding variableBinding) {
			return castCompatable(variableBinding.getType(),
					typeSignature);
		} else if (binding instanceof IMethodBinding methodBinding) {
			return castCompatable(methodBinding.getReturnType(),
					typeSignature);
		}
		// notably, ITypeBinding is not used to complete values,
		// even, for instance, in the case that a `java.lang.Class<?>` is desired.
		return false;
	}

	@Override
	public char[][] getExpectedTypesKeys() {
		if (isJustAfterStringLiteral) {
			return null;
		}
		var res = this.expectedTypes.getExpectedTypes().stream() //
				.map(ITypeBinding::getKey) //
				.map(String::toCharArray) //
				.toArray(char[][]::new);
		return res.length == 0 ? null : res;
	}
	@Override
	public char[][] getExpectedTypesSignatures() {
		if (isJustAfterStringLiteral) {
			return null;
		}
		var res = this.expectedTypes.getExpectedTypes().stream() //
				.map(type -> type.getKey()) //
				.map(name -> name.replace('/', '.'))
				.map(String::toCharArray) //
				.toArray(char[][]::new);
		return res.length == 0 ? null : res;
	}

	@Override
	public boolean isExtended() {
		return true;
	}

	@Override
	public int getTokenLocation() {
		ASTNode parent = this.node;
		while (parent != null) {
			if (parent instanceof ImportDeclaration) {
				return TL_IN_IMPORT;
			}
			if (parent instanceof ClassInstanceCreation newObj) {
				return getTokenStart() <= newObj.getType().getStartPosition() ? TL_CONSTRUCTOR_START : 0;
			}
			if (parent instanceof Statement stmt && getTokenStart() == stmt.getStartPosition()) {
				return getTokenStart() == stmt.getStartPosition() ? TL_STATEMENT_START : 0;
			}
			if (parent instanceof BodyDeclaration member) {
				return (member.getParent() instanceof AbstractTypeDeclaration || member.getParent() instanceof AnonymousClassDeclaration) && getTokenStart() == member.getStartPosition() ? TL_MEMBER_START : 0;
			}
			if (parent instanceof Block block) {
				return block.statements().isEmpty() ? TL_STATEMENT_START : 0;
			}
			parent = parent.getParent();
		}
		return 0;
	}

	@Override
	public int getTokenStart() {
		if (isJustAfterStringLiteral) {
			return -1;
		}
		if (this.node instanceof StringLiteral) {
			return this.node.getStartPosition();
		}
		if (this.node instanceof SimpleName name && !Arrays.equals(name.getIdentifier().toCharArray(), RecoveryScanner.FAKE_IDENTIFIER)) {
			return this.node.getStartPosition();
		}
		return this.offset - getToken().length;
	}
	@Override
	public int getTokenEnd() {
		if (isJustAfterStringLiteral) {
			return -1;
		}
		if (this.node instanceof SimpleName || this.node instanceof StringLiteral) {
			return this.node.getStartPosition() + this.node.getLength() - 1;
		}
		int position = this.offset;
		while (position <= this.cuBuffer.getLength() && Character.isJavaIdentifierPart(this.cuBuffer.getChar(position))) {
			position++;
		}
		return position - 1;
	}

	@Override
	public int getTokenKind() {
		if (isJustAfterStringLiteral) {
			return TOKEN_KIND_UNKNOWN;
		}
		return this.node instanceof StringLiteral ? TOKEN_KIND_STRING_LITERAL : TOKEN_KIND_NAME;
	}

	/// adapted from org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext
	public boolean canUseDiamond(String[] parameterTypes, char[][] typeVariables) {
		// If no LHS or return type expected, then we can safely use diamond
		char[][] expectedTypekeys = this.getExpectedTypesKeys();
		if (expectedTypekeys == null || expectedTypekeys.length == 0)
			return true;
		// Next, find out whether any of the constructor parameters are the same as one of the
		// class type variables. If yes, diamond cannot be used.
		if (typeVariables != null) {
			for (String parameterType : parameterTypes) {
				for (char[] typeVariable : typeVariables) {
					if (CharOperation.equals(parameterType.toCharArray(), typeVariable))
						return false;
				}
			}
		}

		return true;
	}

	/// adapted from org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext
	public boolean canUseDiamond(String[] parameterTypes, char[] fullyQualifiedTypeName) {
		ITypeBinding guessedType = null;
		// If no LHS or return type expected, then we can safely use diamond
		char[][] expectedTypekeys= this.getExpectedTypesKeys();
		if (expectedTypekeys == null || expectedTypekeys.length == 0)
			return true;

		// Next, find out whether any of the constructor parameters are the same as one of the
		// class type variables. If yes, diamond cannot be used.
		Optional<IBinding> potentialMatch = this.bindingsAcquirer.get() //
				.filter(binding -> {
					for (char[] expectedTypekey : expectedTypekeys) {
						if (CharOperation.equals(expectedTypekey, binding.getKey().toCharArray())) {
							return true;
						}
					}
					return false;
				}) //
				.findFirst();
		if (potentialMatch.isPresent() && potentialMatch.get() instanceof ITypeBinding match) {
			guessedType = match;
		}
		if (guessedType != null && !guessedType.isRecovered()) {
			// the erasure must be used because guessedType can be a RawTypeBinding
			guessedType = guessedType.getErasure();
			ITypeBinding[] typeVars = guessedType.getTypeParameters();
			for (String parameterType : parameterTypes) {
				for (ITypeBinding typeVar : typeVars) {
					if (CharOperation.equals(parameterType.toCharArray(), typeVar.getName().toCharArray())) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}



	private static boolean castCompatable(ITypeBinding typeBinding, String sig2) {
		String sig1 = typeBinding.getKey().replace('/', '.');
		// NOTE: this is actually the "raw" version (no type arguments, no type params)
		String sig1Raw = new String(Signature.getTypeErasure(sig1.toCharArray()));
		// TODO: consider autoboxing numbers; upstream JDT doesn't handle this yet but it would be nice
		switch (sig1) {
			case Signature.SIG_LONG:
				return sig2.equals(Signature.SIG_LONG)
						|| sig2.equals(Signature.SIG_DOUBLE)
						|| sig2.equals(Signature.SIG_FLOAT);
			case Signature.SIG_INT:
				return sig2.equals(Signature.SIG_LONG)
						|| sig2.equals(Signature.SIG_INT)
						|| sig2.equals(Signature.SIG_DOUBLE)
						|| sig2.equals(Signature.SIG_FLOAT);
			case Signature.SIG_BYTE:
				return sig2.equals(Signature.SIG_LONG)
						|| sig2.equals(Signature.SIG_INT)
						|| sig2.equals(Signature.SIG_BYTE)
						|| sig2.equals(Signature.SIG_DOUBLE)
						|| sig2.equals(Signature.SIG_FLOAT);
			case Signature.SIG_DOUBLE:
			case Signature.SIG_FLOAT:
				return sig2.equals(Signature.SIG_DOUBLE)
						|| sig2.equals(Signature.SIG_FLOAT);
		}
		if (sig1.equals(sig2) || sig1Raw.equals(sig2)) {
			return true;
		}
		if (typeBinding.getSuperclass() != null && castCompatable(typeBinding.getSuperclass(), sig2)) {
			return true;
		}
		for (ITypeBinding superInterface : typeBinding.getInterfaces()) {
			if (castCompatable(superInterface, sig2)) {
				return true;
			}
		}
		return false;
	}
}