/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.Iterator;
import java.util.List;

/**
 * Single variable declaration AST node type. Single variable
 * declaration nodes are used in a limited number of places, including formal
 * parameter lists and catch clauses. They are not used for field declarations
 * and regular variable declaration statements.
 * For 2.0 (corresponding to JLS2):
 * <pre>
 * SingleVariableDeclaration:
 *    { Modifier } Type Identifier { <b>[</b><b>]</b> } [ <b>=</b> Expression ]
 * </pre>
 * For 3.0 (corresponding to JLS3), the modifier flags were replaced by
 * a list of modifier nodes (intermixed with annotations), and the variable arity
 * indicator was added:
 * <pre>
 * SingleVariableDeclaration:
 *    { ExtendedModifier } Type [ <b>...</b> ] Identifier { <b>[</b><b>]</b> } [ <b>=</b> Expression ]
 * </pre>
 * 
 * @since 2.0
 */
public class SingleVariableDeclaration extends VariableDeclaration {
	
	/**
	 * The "modifiers" structural property of this node type (2.0 API only).
	 * @since 3.0
	 * TODO (jeem) - @deprecated Replaced by {@link #MODIFIERS2_PROPERTY} in the 3.0 API.
	 */
	public static final SimplePropertyDescriptor MODIFIERS_PROPERTY = 
		new SimplePropertyDescriptor(SingleVariableDeclaration.class, "modifiers", int.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * The "modifiers" structural property of this node type (added in 3.0 API).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY = 
		new ChildListPropertyDescriptor(SingleVariableDeclaration.class, "modifiers", IExtendedModifier.class, CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "name" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(SingleVariableDeclaration.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "type" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor TYPE_PROPERTY = 
		new ChildPropertyDescriptor(SingleVariableDeclaration.class, "type", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "varargs" structural property of this node type (added in 3.0 API).
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor VARARGS_PROPERTY = 
		new SimplePropertyDescriptor(SingleVariableDeclaration.class, "varargs", boolean.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * @since 3.0
	 * @deprecated Renamed VARARGS_PROPERTY
	 * TODO (jeem) - Remove before M9
	 */
	public static final SimplePropertyDescriptor VARIABLE_ARITY_PROPERTY = VARARGS_PROPERTY;
	
	/**
	 * The "extraDimensions" structural property of this node type.
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor EXTRA_DIMENSIONS_PROPERTY = 
		new SimplePropertyDescriptor(SingleVariableDeclaration.class, "extraDimensions", int.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * The "initializer" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor INITIALIZER_PROPERTY = 
		new ChildPropertyDescriptor(SingleVariableDeclaration.class, "initializer", Expression.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		createPropertyList(SingleVariableDeclaration.class);
		addProperty(MODIFIERS_PROPERTY);
		addProperty(TYPE_PROPERTY);
		addProperty(NAME_PROPERTY);
		addProperty(EXTRA_DIMENSIONS_PROPERTY);
		addProperty(INITIALIZER_PROPERTY);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList();
		
		createPropertyList(SingleVariableDeclaration.class);
		addProperty(MODIFIERS2_PROPERTY);
		addProperty(TYPE_PROPERTY);
		addProperty(VARARGS_PROPERTY);
		addProperty(NAME_PROPERTY);
		addProperty(EXTRA_DIMENSIONS_PROPERTY);
		addProperty(INITIALIZER_PROPERTY);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList();
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.LEVEL_&ast;</code> constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.LEVEL_2_0) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
			
	/**
	 * The extended modifiers (element type: <code>IExtendedModifier</code>). 
	 * Null in 2.0. Added in 3.0; defaults to an empty list
	 * (see constructor).
	 * 
	 * @since 3.0
	 */
	private ASTNode.NodeList modifiers = null;
	
	/**
	 * The modifiers; bit-wise or of Modifier flags.
	 * Defaults to none. Not used in 3.0.
	 */
	private int modifierFlags = Modifier.NONE;
	
	/**
	 * The variable name; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private SimpleName variableName = null;

	/**
	 * The type; lazily initialized; defaults to a unspecified,
	 * legal type.
	 */
	private Type type = null;

	/**
	 * Indicates the last parameter of a variable arity method;
	 * defaults to false.
	 * 
	 * @since 3.0
	 */
	private boolean variableArity = false;

	/**
	 * The number of extra array dimensions that appear after the variable;
	 * defaults to 0.
	 * 
	 * @since 2.1
	 */
	private int extraArrayDimensions = 0;

	/**
	 * The initializer expression, or <code>null</code> if none;
	 * defaults to none.
	 */
	private Expression optionalInitializer = null;

	/**
	 * Creates a new AST node for a variable declaration owned by the given 
	 * AST. By default, the variable declaration has: no modifiers, an 
	 * unspecified (but legal) type, an unspecified (but legal) variable name, 
	 * 0 dimensions after the variable; no initializer; not variable arity.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	SingleVariableDeclaration(AST ast) {
		super(ast);
		if (ast.apiLevel >= AST.LEVEL_3_0) {
			this.modifiers = new ASTNode.NodeList(MODIFIERS2_PROPERTY);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int internalGetSetIntProperty(SimplePropertyDescriptor property, boolean get, int value) {
		if (property == MODIFIERS_PROPERTY) {
			if (get) {
				return getModifiers();
			} else {
				setModifiers(value);
				return 0;
			}
		}
		if (property == EXTRA_DIMENSIONS_PROPERTY) {
			if (get) {
				return getExtraDimensions();
			} else {
				setExtraDimensions(value);
				return 0;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetIntProperty(property, get, value);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == VARARGS_PROPERTY) {
			if (get) {
				return isVarargs();
			} else {
				setVarargs(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((SimpleName) child);
				return null;
			}
		}
		if (property == TYPE_PROPERTY) {
			if (get) {
				return getType();
			} else {
				setType((Type) child);
				return null;
			}
		}
		if (property == INITIALIZER_PROPERTY) {
			if (get) {
				return getInitializer();
			} else {
				setInitializer((Expression) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == MODIFIERS2_PROPERTY) {
			return modifiers();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SINGLE_VARIABLE_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		SingleVariableDeclaration result = new SingleVariableDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		if (this.ast.apiLevel == AST.LEVEL_2_0) {
			result.setModifiers(getModifiers());
		} else {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
			result.setVarargs(isVarargs());
		}
		result.setType((Type) getType().clone(target));
		result.setExtraDimensions(getExtraDimensions());
		result.setName((SimpleName) getName().clone(target));
		result.setInitializer(
			(Expression) ASTNode.copySubtree(target, getInitializer()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			if (this.ast.apiLevel >= AST.LEVEL_3_0) {
				acceptChildren(visitor, this.modifiers);
			}
			acceptChild(visitor, getType());
			acceptChild(visitor, getName());
			acceptChild(visitor, getInitializer());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of modifiers and annotations
	 * of this declaration (added in 3.0 API).
	 * <p>
	 * Note that the final modifier is the only meaningful modifier for local
	 * variable and formal parameter declarations.
	 * </p>
	 * <p>
	 * Note: Support for annotation metadata is an experimental language feature 
	 * under discussion in JSR-175 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return the live list of modifiers and annotations
	 *    (element type: <code>IExtendedModifier</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public List modifiers() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.modifiers == null) {
			unsupportedIn2();
		}
		return this.modifiers;
	}
	
	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 * In the 3.0 API, this method is a convenience method that
	 * computes these flags from <code>modifiers()</code>.
	 * </p>
	 * 
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see Modifier
	 */ 
	public int getModifiers() {
		// more efficient than checking getAST().API_LEVEL
		if (this.modifiers == null) {
			// 2.0 behavior - bona fide property
			return this.modifierFlags;
		} else {
			// 3.0 behavior - convenient method
			// performance could be improved by caching computed flags
			// but this would require tracking changes to this.modifiers
			int computedModifierFlags = Modifier.NONE;
			for (Iterator it = modifiers().iterator(); it.hasNext(); ) {
				Object x = it.next();
				if (x instanceof Modifier) {
					computedModifierFlags |= ((Modifier) x).getKeyword().toFlagValue();
				}
			}
			return computedModifierFlags;
		}
	}

	/**
	 * Sets the modifiers explicitly specified on this declaration (2.0 API only).
	 * <p>
	 * The following modifiers are meaningful for fields: public, private, protected,
	 * static, final, volatile, and transient. For local variable and formal
	 * parameter declarations, the only meaningful modifier is final.
	 * </p>
	 * 
	 * @param modifiers the given modifiers (bit-wise or of <code>Modifier</code> constants)
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * @see Modifier
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by 
	 * <code>modifiers()</code> which contains a list of 
	 * a <code>Modifier</code> nodes.
	 */ 
	public void setModifiers(int modifiers) {
	    supportedOnlyIn2();
		preValueChange(MODIFIERS_PROPERTY);
		this.modifierFlags = modifiers;
		postValueChange(MODIFIERS_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public SimpleName getName() {
		if (this.variableName == null) {
			preLazyInit();
			this.variableName = new SimpleName(this.ast);
			postLazyInit(this.variableName, NAME_PROPERTY);
		}
		return this.variableName;
	}
		
	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public void setName(SimpleName variableName) {
		if (variableName == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.variableName;
		preReplaceChild(oldChild, variableName, NAME_PROPERTY);
		this.variableName = variableName;
		postReplaceChild(oldChild, variableName, NAME_PROPERTY);
	}

	/**
	 * Returns the type of the variable declared in this variable declaration,
	 * exclusive of any extra array dimensions.
	 * 
	 * @return the type
	 */ 
	public Type getType() {
		if (this.type == null) {
			preLazyInit();
			this.type = this.ast.newPrimitiveType(PrimitiveType.INT);
			postLazyInit(this.type, TYPE_PROPERTY);
		}
		return this.type;
	}

	/**
	 * Sets the type of the variable declared in this variable declaration to 
	 * the given type, exclusive of any extra array dimensions.
	 * 
	 * @param type the new type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.type;
		preReplaceChild(oldChild, type, TYPE_PROPERTY);
		this.type = type;
		postReplaceChild(oldChild, type, TYPE_PROPERTY);
	}

	/**
	 * Returns whether this declaration declares the last parameter of
	 * a variable arity method (added in 3.0 API).
	 * <p>
	 * Note: Varible arity methods are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return <code>true</code> if this is a variable arity parameter declaration,
	 *    and <code>false</code> otherwise
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public boolean isVarargs() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.modifiers == null) {
			unsupportedIn2();
		}
		return this.variableArity;
	}
	
	/**
	 * Sets whether this declaration declares the last parameter of
	 * a variable arity method (added in 3.0 API).
	 * <p>
	 * Note: Varible arity methods are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @param variableArity <code>true</code> if this is a variable arity
	 *    parameter declaration, and <code>false</code> otherwise
	 * @since 3.0
	 */ 
	public void setVarargs(boolean variableArity) {
		// more efficient than just calling unsupportedIn2() to check
		if (this.modifiers == null) {
			unsupportedIn2();
		}
		preValueChange(VARARGS_PROPERTY);
		this.variableArity = variableArity;
		postValueChange(VARARGS_PROPERTY);
	}

	/**
	 * @since 3.0
	 * @deprecated Renamed isVarargs
	 * TODO (jeem) - Remove before M9
	 */ 
	public boolean isVariableArity() {
		return isVarargs();
	}
	
	/**
	 * @since 3.0
	 * @deprecated Renamed setVarargs
	 * TODO (jeem) - Remove before M9
	 */ 
	public void setVariableArity(boolean variableArity) {
		setVarargs(variableArity);
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 * @since 2.1
	 */ 
	public int getExtraDimensions() {
		return this.extraArrayDimensions;
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 * @since 2.1
	 */ 
	public void setExtraDimensions(int dimensions) {
		if (dimensions < 0) {
			throw new IllegalArgumentException();
		}
		preValueChange(EXTRA_DIMENSIONS_PROPERTY);
		this.extraArrayDimensions = dimensions;
		postValueChange(EXTRA_DIMENSIONS_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public Expression getInitializer() {
		return this.optionalInitializer;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public void setInitializer(Expression initializer) {
		// a SingleVariableDeclaration may occur inside an Expression 
		// must check cycles
		ASTNode oldChild = this.optionalInitializer;
		preReplaceChild(oldChild, initializer,INITIALIZER_PROPERTY);
		this.optionalInitializer = initializer;
		postReplaceChild(oldChild, initializer,INITIALIZER_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 7 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (this.modifiers == null ? 0 : this.modifiers.listSize())
			+ (this.type == null ? 0 : getType().treeSize())
			+ (this.variableName == null ? 0 : getName().treeSize())
			+ (this.optionalInitializer == null ? 0 : getInitializer().treeSize());
	}
}
