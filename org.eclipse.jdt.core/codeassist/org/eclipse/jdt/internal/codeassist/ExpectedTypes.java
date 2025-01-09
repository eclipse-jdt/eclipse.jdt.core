/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;

/**
 * Utility to evaluate what particular types are expected or not at a given position, used to
 * compute completion item relevance.
 * @implNote This is extracted and partly adapted from CompletionEngine. The current implementation
 * is incomplete, further work is needed to support all constructs.
 */
public class ExpectedTypes {
	private static enum TypeFilter {
		SUPERTYPE, SUBTYPE;
	}

	private static final Set<StructuralPropertyDescriptor> CONDITION_LOCATIONS = Set.of(
			IfStatement.EXPRESSION_PROPERTY,
			WhileStatement.EXPRESSION_PROPERTY,
			DoStatement.EXPRESSION_PROPERTY,
			ForStatement.EXPRESSION_PROPERTY,
			ConditionalExpression.EXPRESSION_PROPERTY
		);

	private final int offset;
	private Collection<TypeFilter> expectedTypesFilters = Set.of(TypeFilter.SUPERTYPE, TypeFilter.SUBTYPE);
	private final Collection<ITypeBinding> expectedTypes = new LinkedHashSet<>();
	private final Collection<ITypeBinding> uninterestingBindings = new LinkedHashSet<>();
	private final Collection<ITypeBinding> forbiddenBindings = new LinkedHashSet<>();
	private final AssistOptions options;
	private final ASTNode node;
	private boolean isReady;

	public ExpectedTypes(AssistOptions options, ASTNode toComplete, int offset) {
		this.offset = offset;
		this.options = options;
		this.node = toComplete;
	}

	private void computeExpectedTypes(){
		ASTNode parent2 = this.node;
		// find the parent that contains type information
		while (parent2 != null) {
			if (parent2 instanceof VariableDeclarationFragment fragment && this.offset > fragment.getName().getStartPosition() + fragment.getName().getLength()) {
				this.expectedTypes.add(fragment.resolveBinding().getType());
			}
			if (parent2 instanceof MethodInvocation method && this.offset > method.getName().getStartPosition() + method.getName().getLength()) {
				// consider params, implemented out of this loop
				break;
			}
			if (parent2 instanceof InfixExpression) {
				break;
			}
			if (parent2 instanceof ReturnStatement) {
				break;
			}
			if (parent2 instanceof Block) {
				break;
			}
			if (parent2 instanceof LambdaExpression) {
				break;
			}
			if (parent2 instanceof Assignment assign && this.offset > assign.getLeftHandSide().getStartPosition() + assign.getLeftHandSide().getLength()) {
				this.expectedTypes.add(assign.resolveTypeBinding());
				return;
			}
			if (parent2 instanceof ClassInstanceCreation newObj && this.offset > newObj.getType().getStartPosition() + newObj.getType().getLength()) {
				// TODO find params
				break;
			}
			if (parent2 instanceof CastExpression cast && this.offset > cast.getType().getStartPosition() + cast.getType().getLength()) {
				this.expectedTypes.add(cast.getType().resolveBinding());
				return;
			}
			if (parent2.getLocationInParent() != null && CONDITION_LOCATIONS.contains(parent2.getLocationInParent())) {
				this.expectedTypes.add(parent2.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
				return;
			}
 			parent2 = parent2.getParent();
		}
		ASTNode parent = parent2;
		if (parent == null) {
			return; // no construct to infer possible types
		}
		// default filter
		this.expectedTypesFilters = Set.of(TypeFilter.SUBTYPE);

		// find types from parent
		if(parent instanceof VariableDeclaration variable && !(parent instanceof TypeParameter)
			&& this.offset > variable.getName().getStartPosition() + variable.getName().getLength()) {
			ITypeBinding binding = variable.resolveBinding().getType();
			if(binding != null) {
				if(!(variable.getInitializer() instanceof ArrayInitializer)) {
					this.expectedTypes.add(binding);
				} else {
					this.expectedTypes.add(binding.getComponentType());
				}
			}
		} else if(parent instanceof Assignment assignment) {
			ITypeBinding binding = assignment.resolveTypeBinding();
			if(binding != null) {
				this.expectedTypes.add(binding);
			}
		} else if (parent instanceof ReturnStatement) {
			findLambda(parent)
				.map(LambdaExpression::resolveMethodBinding)
				.or(() -> findMethod(parent).map(MethodDeclaration::resolveBinding))
				.map(IMethodBinding::getReturnType)
				.ifPresent(this.expectedTypes::add);
		} else if (parent instanceof LambdaExpression lambda) {
			if (lambda.getBody() == this.node) {
				Optional.ofNullable(lambda.resolveMethodBinding())
					.map(IMethodBinding::getReturnType)
					.ifPresent(this.expectedTypes::add);
			}
		} else if(parent instanceof CastExpression castExpression) {
			ITypeBinding binding = castExpression.resolveTypeBinding();
			if(binding != null){
				this.expectedTypes.add(binding);
				this.expectedTypesFilters = Set.of(TypeFilter.SUBTYPE, TypeFilter.SUPERTYPE);
			}
		} else if (parent instanceof MethodInvocation messageSend) {
			if (messageSend.getExpression() != null) {
				final ITypeBinding initialBinding = messageSend.getExpression().resolveTypeBinding();
				ITypeBinding currentBinding = initialBinding; // messageSend.actualReceiverType
				boolean isStatic = messageSend.getExpression() instanceof Name name && name.resolveBinding() instanceof ITypeBinding;
				while(currentBinding != null) {
					computeExpectedTypesForMessageSend(
							currentBinding,
							messageSend.getName().toString(),
							messageSend.arguments(),
							initialBinding,
							messageSend,
							isStatic);
					computeExpectedTypesForMessageSendForInterface(
							currentBinding,
							messageSend.getName().toString(),
							messageSend.arguments(),
							initialBinding,
							messageSend,
							isStatic);
					currentBinding = currentBinding.getSuperclass();
				}
			} else {
				// find the param
				IMethodBinding methodBinding = messageSend.resolveMethodBinding();
				if (this.node != parent) {
					ASTNode cursor = this.node;
					while (cursor != null && cursor.getParent() != messageSend) {
						cursor = cursor.getParent();
					}
					if (cursor != null && methodBinding != null) {
						int i = messageSend.arguments().indexOf(cursor);
						if (0 <= i && i < methodBinding.getParameterTypes().length) {
							this.expectedTypes.add(methodBinding.getParameterTypes()[i]);
						} else if (0 <= i && methodBinding.isVarargs()) {
							this.expectedTypes.add(methodBinding.getParameterTypes()[methodBinding.getParameterTypes().length - 1]);
						}
					}
				}
			}
		} else if(parent instanceof ClassInstanceCreation allocationExpression) {
			ITypeBinding binding = allocationExpression.resolveTypeBinding();
			if(binding != null) {
				computeExpectedTypesForAllocationExpression(
					binding,
					allocationExpression.arguments(),
					allocationExpression);
			}
		} else if(parent instanceof InstanceofExpression e) {
			ITypeBinding binding = e.getLeftOperand().resolveTypeBinding();
			/*if (binding == null) {
				if (scope instanceof BlockScope)
					binding = e.expression.resolveType((BlockScope) scope);
				else if (scope instanceof ClassScope)
					binding = e.expression.resolveType((ClassScope) scope);
			}*/
			if(binding != null){
				this.expectedTypes.add(binding);
				this.expectedTypesFilters = Set.of(TypeFilter.SUBTYPE, TypeFilter.SUPERTYPE);
			}
		} else if(parent instanceof InfixExpression binaryExpression) {
			var operator = binaryExpression.getOperator();
			if (operator == InfixExpression.Operator.EQUALS || operator == InfixExpression.Operator.NOT_EQUALS) {
				ITypeBinding binding = binaryExpression.getLeftOperand().resolveTypeBinding();
				if (binding != null) {
					this.expectedTypes.add(binding);
					this.expectedTypesFilters = Set.of(TypeFilter.SUBTYPE, TypeFilter.SUPERTYPE);
				}
			} else if (operator == InfixExpression.Operator.PLUS) {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.SHORT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.INT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.LONG.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.FLOAT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.DOUBLE.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.CHAR.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BYTE.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(String.class.getName()));
			} else if (operator == InfixExpression.Operator.CONDITIONAL_AND
					|| operator == InfixExpression.Operator.CONDITIONAL_OR
					|| operator == InfixExpression.Operator.XOR) {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
			} else {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.SHORT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.INT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.LONG.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.FLOAT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.DOUBLE.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.CHAR.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BYTE.toString()));
			}
			if(operator == InfixExpression.Operator.LESS) {
				if(binaryExpression.getLeftOperand() instanceof Name name){
					// TODO port further code to IBinding
					/*Binding b = scope.getBinding(name.token, Binding.VARIABLE | Binding.TYPE, name, false);
					if(b instanceof ReferenceBinding) {
						TypeVariableBinding[] typeVariableBindings =((ReferenceBinding)b).typeVariables();
						if(typeVariableBindings != null && typeVariableBindings.length > 0) {
							this.expectedTypes.add(typeVariableBindings[0]);
						}
					}*/
				}
			}
		} else if(parent instanceof PrefixExpression prefixExpression) {
			var operator = prefixExpression.getOperator();
			if (operator == PrefixExpression.Operator.NOT) {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
			} else if (operator == PrefixExpression.Operator.COMPLEMENT) {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.SHORT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.INT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.LONG.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.CHAR.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BYTE.toString()));
			} else if (operator == PrefixExpression.Operator.PLUS
					|| operator == PrefixExpression.Operator.MINUS
					|| operator == PrefixExpression.Operator.INCREMENT
					|| operator == PrefixExpression.Operator.DECREMENT) {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.SHORT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.INT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.LONG.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.FLOAT.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.DOUBLE.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.CHAR.toString()));
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BYTE.toString()));
			}
		} else if(parent instanceof ArrayAccess) {
			this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.SHORT.toString()));
			this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.INT.toString()));
			this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.LONG.toString()));
		} else if (parent instanceof DoStatement) {
			this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
		} // TODO port next code to IBinding
		/*else if(parent instanceof ParameterizedSingleTypeReference ref) {
			ITypeBinding expected = null;
			if (this.parser.enclosingNode instanceof AbstractVariableDeclaration ||
					this.parser.enclosingNode instanceof ReturnStatement) {
				// completing inside the diamond
				if (this.parser.enclosingNode instanceof AbstractVariableDeclaration) {
					AbstractVariableDeclaration abstractVariableDeclaration = (AbstractVariableDeclaration) this.parser.enclosingNode;
					expected = abstractVariableDeclaration.initialization != null ? abstractVariableDeclaration.initialization.expectedType() : null;
				} else {
					ReturnStatement returnStatement = (ReturnStatement) this.parser.enclosingNode;
					if (returnStatement.getExpression() != null) {
						expected = returnStatement.getExpression().expectedType();
					}
				}
				this.expectedTypes.add(expected);
			} else {
				TypeVariableBinding[] typeVariables = ((ReferenceBinding)ref.resolvedType).typeVariables();
				int length = ref.typeArguments == null ? 0 : ref.typeArguments.length;
				if(typeVariables != null && typeVariables.length >= length) {
					int index = length - 1;
					while(index > -1 && ref.typeArguments[index] != node) index--;

					TypeBinding bound = typeVariables[index].firstBound;
					addExpectedType(bound == null ? scope.getJavaLangObject() : bound, scope);
				}
			}
		} else if(parent instanceof ParameterizedQualifiedTypeReference ref) {
			TypeReference[][] arguments = ref.typeArguments;
			ITypeBinding expected = null;
			if (this.parser.enclosingNode instanceof AbstractVariableDeclaration ||
					this.parser.enclosingNode instanceof ReturnStatement) {
				// completing inside the diamond
				if (this.parser.enclosingNode instanceof AbstractVariableDeclaration) {
					AbstractVariableDeclaration abstractVariableDeclaration = (AbstractVariableDeclaration) this.parser.enclosingNode;
					expected = abstractVariableDeclaration.initialization != null ? abstractVariableDeclaration.initialization.expectedType() : null;
				} else {
					ReturnStatement returnStatement = (ReturnStatement) this.parser.enclosingNode;
					if (returnStatement.getExpression() != null) {
						expected = returnStatement.getExpression().expectedType();
					}
				}
				this.expectedTypes.add(expected);
			} else {
				TypeVariableBinding[] typeVariables = ((ReferenceBinding)ref.resolvedType).typeVariables();
				if(typeVariables != null) {
					int iLength = arguments == null ? 0 : arguments.length;
					done: for (int i = 0; i < iLength; i++) {
						int jLength = arguments[i] == null ? 0 : arguments[i].length;
						for (int j = 0; j < jLength; j++) {
							if(arguments[i][j] == node && typeVariables.length > j) {
								TypeBinding bound = typeVariables[j].firstBound;
								addExpectedType(bound == null ? scope.getJavaLangObject() : bound, scope);
								break done;
							}
						}
					}
				}
			}
		} */ else if(parent instanceof MemberValuePair pair) {
			Optional.ofNullable(pair.resolveMemberValuePairBinding())
				.map(IMemberValuePairBinding::getMethodBinding)
				.map(IMethodBinding::getReturnType)
				.map(ITypeBinding::getComponentType)
				.ifPresent(this.expectedTypes::add);
			// TODO port next code to IBinding
		/*} else if (parent instanceof NormalAnnotation annotation) {
			List<MemberValuePair> memberValuePairs = annotation.values();
			if(memberValuePairs == null || memberValuePairs.isEmpty()) {
				ITypeBinding annotationType = annotation.resolveTypeBinding();
				if(annotationType != null) {
					IMethodBinding[] methodBindings = annotationType.getDeclaredMethods(); // TODO? Missing super interface methods?
					if (methodBindings != null &&
							methodBindings.length > 0 &&
							CharOperation.equals(methodBindings[0].selector, VALUE)) {
						boolean canBeSingleMemberAnnotation = true;
						done : for (int i = 1; i < methodBindings.length; i++) {
							if((methodBindings[i].getModifiers() & ClassFileConstants.AccAnnotationDefault) == 0) {
								canBeSingleMemberAnnotation = false;
								break done;
							}
						}
						if (canBeSingleMemberAnnotation) {
							this.assistNodeCanBeSingleMemberAnnotation = canBeSingleMemberAnnotation;
							this.expectedTypes.add(methodBindings[0].getReturnType().getComponentType());
						}
					}
				}
			}
		} else if (parent instanceof AssistNodeParentAnnotationArrayInitializer parent1) {
			if(parent1.type.resolvedType instanceof ReferenceBinding) {
				MethodBinding[] methodBindings =
					((ReferenceBinding)parent1.type.resolvedType).availableMethods();
				if (methodBindings != null) {
					for (MethodBinding methodBinding : methodBindings) {
						if(CharOperation.equals(methodBinding.selector, parent1.name)) {
							addExpectedType(methodBinding.returnType.leafComponentType(), scope);
							break;
						}
					}
				}
			}
		} else if (parent instanceof TryStatement) {
			boolean isException = false;
			if (node instanceof CompletionOnSingleTypeReference) {
				isException = ((CompletionOnSingleTypeReference)node).isException();
			} else if (node instanceof CompletionOnQualifiedTypeReference) {
				isException = ((CompletionOnQualifiedTypeReference)node).isException();
			} else if (node instanceof CompletionOnParameterizedQualifiedTypeReference) {
				isException = ((CompletionOnParameterizedQualifiedTypeReference)node).isException();
			}
			if (isException) {
				ThrownExceptionFinder thrownExceptionFinder = new ThrownExceptionFinder();
				thrownExceptionFinder.processThrownExceptions((TryStatement) parent, (BlockScope)scope);
				ReferenceBinding[] bindings = thrownExceptionFinder.getThrownUncaughtExceptions();
				ReferenceBinding[] alreadyCaughtExceptions = thrownExceptionFinder.getAlreadyCaughtExceptions();
				ReferenceBinding[] discouragedExceptions = thrownExceptionFinder.getDiscouragedExceptions();
				if (bindings != null && bindings.length > 0) {
					for (ReferenceBinding binding : bindings) {
						this.expectedTypes.add(binding);
					}
					this.expectedTypesFilters = Set.of(TypeFilter.SUPERTYPE);
				}
				if (alreadyCaughtExceptions != null && alreadyCaughtExceptions.length > 0) {
					for (ReferenceBinding alreadyCaughtException : alreadyCaughtExceptions) {
						this.forbiddenBindings.add(alreadyCaughtException);
						this.knownTypes.put(CharOperation.concat(alreadyCaughtException.qualifiedPackageName(), alreadyCaughtException.qualifiedSourceName(), '.'), KNOWN_TYPE_WITH_KNOWN_CONSTRUCTORS);
					}
				}
				if (discouragedExceptions != null && discouragedExceptions.length > 0) {
					for (ReferenceBinding discouragedException : discouragedExceptions) {
						this.uninterestingBindings.add(discouragedException);
						// do not insert into known types. We do need these types to come from
						// searchAllTypes(..) albeit with lower relevance
					}
				}
			}
		} else if (parent instanceof SwitchStatement switchStatement) {
			this.assistNodeIsInsideCase = assistNodeIsInsideCase(node, parent);
			if (switchStatement.getExpression() != null &&
					switchStatement.getExpression().resolveTypeBinding() != null) {
				if (this.assistNodeIsInsideCase &&
						switchStatement.getExpression().resolveTypeBinding().getName() == String.class.getName() &&
						this.compilerOptions.complianceLevel >= ClassFileConstants.JDK1_7) {
					// set the field to true even though the expected types array will contain String as
					// expected type to avoid traversing the array in every case later on.
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=343476
					this.assistNodeIsString = true;
				}
				this.expectedTypes.add(switchStatement.getExpression().resolveTypeBinding());
			}
		*/
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253008, flag boolean as the expected
		// type if we are completing inside if(), for (; ;), while() and do while()
		} else if (parent instanceof WhileStatement) {  // covers both while and do-while loops
			this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
		} else if (parent instanceof IfStatement) {
			this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
		} else if (parent instanceof AssertStatement assertStatement) {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=274466
			// If the assertExpression is same as the node , then the assistNode is the conditional part of the assert statement
			if (assertStatement.getExpression() == this.node) {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
			}
		} else if (parent instanceof ForStatement forStatement) {
			if (forStatement.getExpression().equals(this.node)) {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
			}
		} else if (parent instanceof Javadoc) { // Expected types for javadoc
			findMethod(parent)
				.map(MethodDeclaration::resolveBinding)
				.map(IMethodBinding::getExceptionTypes)
				.map(Arrays::stream)
				.orElseGet(Stream::of)
				.forEach(this.expectedTypes::add);
		} else if (parent instanceof ConditionalExpression conditionalExpr) {
			if (conditionalExpr.getExpression() == this.node) {
				this.expectedTypes.add(this.node.getAST().resolveWellKnownType(PrimitiveType.BOOLEAN.toString()));
			} else {
				ITypeBinding typeBinding = conditionalExpr.resolveTypeBinding();
				if (typeBinding != null && !typeBinding.isRecovered()) {
					this.expectedTypes.add(typeBinding);
				}
			}
		}

		// Guard it, otherwise we end up with a empty array which cause issues down the line
//		if((this.expectedTypesPtr > -1) && ((this.expectedTypesPtr + 1) != this.expectedTypes.length)) {
//			System.arraycopy(this.expectedTypes, 0, this.expectedTypes = new TypeBinding[this.expectedTypesPtr + 1], 0, this.expectedTypesPtr + 1);
//		}
		this.isReady = true;
	}

	private void computeExpectedTypesForAllocationExpression(
		ITypeBinding binding,
		List<Expression> arguments,
		ASTNode invocationSite) {

		if (arguments == null)
			return;

		IMethodBinding[] methods = avaiableMethods(binding).toArray(IMethodBinding[]::new);
		nextMethod : for (IMethodBinding method : methods) {
			if (!method.isConstructor()) continue nextMethod;

			if (method.isSynthetic()) continue nextMethod;

			//if (this.options.checkVisibility && !method.canBeSeenBy(invocationSite, scope)) continue nextMethod;

			ITypeBinding[] parameters = method.getParameterTypes();
			if(parameters.length < arguments.size())
				continue nextMethod;

			int length = arguments.size() - 1;

			for (int j = 0; j < length; j++) {
				Expression argument = arguments.get(j);
				ITypeBinding argType = argument.resolveTypeBinding();
				if(argType != null && !argType.isSubTypeCompatible(parameters[j]))
					continue nextMethod;
			}

			if (arguments.size() > 0) {
				ITypeBinding expectedType = method.getParameterTypes()[arguments.size() - 1];
				if(expectedType != null) {
					this.expectedTypes.add(expectedType);
				}
			}
		}
	}
	private void computeExpectedTypesForMessageSend(
		ITypeBinding binding,
		String selector,
		List<Expression> arguments,
		ITypeBinding receiverType,
		ASTNode invocationSite,
		boolean isStatic) {

		if (arguments == null)
			return;

		IMethodBinding[] methods = avaiableMethods(binding).toArray(IMethodBinding[]::new);
		nextMethod : for (IMethodBinding method : methods) {
			if (method.isSynthetic()) continue nextMethod;

			//if (method.isDefaultAbstract())	continue nextMethod;

			if (method.isConstructor()) continue nextMethod;

			if (isStatic && !Modifier.isStatic(method.getModifiers())) continue nextMethod;

			//if (this.options.checkVisibility && !method.canBeSeenBy(receiverType, invocationSite, scope)) continue nextMethod;

			if(!Objects.equals(method.getName(), selector)) continue nextMethod;

			ITypeBinding[] parameters = method.getParameterTypes();
			if(parameters.length < arguments.size())
				continue nextMethod;

			if (arguments.isEmpty() && parameters.length > 0) {
				this.expectedTypes.add(parameters[0]);
			} else {
				int length = arguments.size() - 1;
				int completionArgIndex = arguments.size() - 1;

				for (int j = 0; j < length; j++) {
					Expression argument = arguments.get(j);
					ITypeBinding argType = argument.resolveTypeBinding();
					if(argType != null && !argType.getErasure().isSubTypeCompatible(parameters[j].getErasure()))
						continue nextMethod;

					/*if((argument.getStartPosition() >= this.startPosition)
							&& (argument.getStartPosition() + argument.getLength() <= this.endPosition)) {
						completionArgIndex = j;
					}*/
				}
				if (completionArgIndex >= 0) {
					ITypeBinding expectedType = method.getParameterTypes()[completionArgIndex];
					if(expectedType != null) {
						this.expectedTypes.add(expectedType);
					}
				}
			}
		}
	}
	private void computeExpectedTypesForMessageSendForInterface(
		ITypeBinding binding,
		String selector,
		List<Expression> arguments,
		ITypeBinding receiverType,
		ASTNode invocationSite,
		boolean isStatic) {

		ITypeBinding[] itsInterfaces = binding.getInterfaces();
		int itsLength = itsInterfaces.length;
		ITypeBinding[] interfacesToVisit = itsInterfaces;
		int nextPosition = interfacesToVisit.length;

		for (int i = 0; i < nextPosition; i++) {
			ITypeBinding currentType = interfacesToVisit[i];
			computeExpectedTypesForMessageSend(
				currentType,
				selector,
				arguments,
				receiverType,
				invocationSite,
				isStatic);

			itsInterfaces = currentType.getInterfaces();
			itsLength = itsInterfaces.length;
			if (nextPosition + itsLength >= interfacesToVisit.length) {
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ITypeBinding[nextPosition + itsLength + 5], 0, nextPosition);
			}
			nextInterface : for (int a = 0; a < itsLength; a++) {
				ITypeBinding next = itsInterfaces[a];
				for (int b = 0; b < nextPosition; b++) {
					if (Objects.equals(next, interfacesToVisit[b])) continue nextInterface;
				}
				interfacesToVisit[nextPosition++] = next;
			}
		}
	}


	private static Optional<MethodDeclaration> findMethod(ASTNode node) {
		while (node != null && !(node instanceof MethodDeclaration)) {
			node = node.getParent();
		}
		return Optional.ofNullable((MethodDeclaration)node);
	}
	private static Optional<LambdaExpression> findLambda(ASTNode node) {
		while (node != null && !(node instanceof LambdaExpression)) {
			node = node.getParent();
		}
		return Optional.ofNullable((LambdaExpression)node);
	}

	private Set<IMethodBinding> avaiableMethods(ITypeBinding typeBinding) {
		Set<IMethodBinding> res = new HashSet<>();
		res.addAll(Arrays.asList(typeBinding.getDeclaredMethods()));
		for (ITypeBinding interfac : typeBinding.getInterfaces()) {
			res.addAll(avaiableMethods(interfac));
		}
		if (typeBinding.getSuperclass() != null) {
			res.addAll(avaiableMethods(typeBinding.getSuperclass()));
		}
		return res;
	}

	public List<ITypeBinding> getExpectedTypes() {
		if (!this.isReady) {
			computeExpectedTypes();
		}
		return new ArrayList<>(this.expectedTypes);
	}

	public boolean allowsSubtypes() {
		return this.expectedTypesFilters.contains(TypeFilter.SUBTYPE);
	}
	public boolean allowsSupertypes() {
		return this.expectedTypesFilters.contains(TypeFilter.SUPERTYPE);
	}
}
