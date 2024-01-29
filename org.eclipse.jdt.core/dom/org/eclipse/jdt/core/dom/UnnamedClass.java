package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.37
 */
public class UnnamedClass extends AbstractUnnamedTypeDeclaration {

	@Deprecated
	public static final SimplePropertyDescriptor MODIFIERS_PROPERTY =
			internalModifiersPropertyFactory(UnnamedClass.class);

	public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY =
			internalModifiers2PropertyFactory(UnnamedClass.class);

	public static final ChildPropertyDescriptor JAVADOC_PROPERTY =
			internalJavadocPropertyFactory(UnnamedClass.class);

	public static final ChildListPropertyDescriptor BODY_DECLARATIONS_PROPERTY =
			internalBodyDeclarationPropertyFactory(UnnamedClass.class);

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List<Object> PROPERTY_DESCRIPTORS_2_0;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.1
	 */
	private static final List<Object> PROPERTY_DESCRIPTORS_3_0;

	static {
		List<Object> propertyList = new ArrayList<>(8);
		createPropertyList(UnnamedClass.class, propertyList);
		addProperty(BODY_DECLARATIONS_PROPERTY, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(MODIFIERS_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);

		propertyList = new ArrayList<>(8);
		createPropertyList(UnnamedClass.class, propertyList);
		addProperty(BODY_DECLARATIONS_PROPERTY, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(MODIFIERS2_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}

	UnnamedClass(AST ast) {
		super(ast);
	}

	@Override
	SimplePropertyDescriptor internalModifiersProperty() {
		return MODIFIERS_PROPERTY;
	}

	@Override
	ChildListPropertyDescriptor internalModifiers2Property() {
		return MODIFIERS2_PROPERTY;
	}

	@Override
	ChildPropertyDescriptor internalJavadocProperty() {
		return JAVADOC_PROPERTY;
	}

	@Override
	List internalStructuralPropertiesForType(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return PROPERTY_DESCRIPTORS_2_0;
		}
		return PROPERTY_DESCRIPTORS_3_0;
	}

	@Override
	int getNodeType0() {
		return UNNAMED_CLASS;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		UnnamedClass result = new UnnamedClass(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.bodyDeclarations().addAll(
			ASTNode.copySubtrees(target, bodyDeclarations()));
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getJavadoc());
			acceptChildren(visitor, this.bodyDeclarations);
		}
		visitor.endVisit(this);
	}

	@Override
	int treeSize() {
		return memSize() + this.bodyDeclarations.listSize();
	}

	@Override
	final ChildListPropertyDescriptor internalBodyDeclarationsProperty() {
		return BODY_DECLARATIONS_PROPERTY;
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == JAVADOC_PROPERTY) {
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == MODIFIERS2_PROPERTY) {
			return modifiers();
		}
		if (property == BODY_DECLARATIONS_PROPERTY) {
			return bodyDeclarations();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	final int internalGetSetIntProperty(SimplePropertyDescriptor property, boolean get, int value) {
		if (property == MODIFIERS_PROPERTY) {
			if (get) {
				return getModifiers();
			} else {
				internalSetModifiers(value);
				return 0;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetIntProperty(property, get, value);
	}

}
