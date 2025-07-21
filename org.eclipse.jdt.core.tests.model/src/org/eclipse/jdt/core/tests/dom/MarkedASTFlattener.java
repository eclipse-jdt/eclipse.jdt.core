/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;

/**
 * Serialize the ast and decorate this ast with markers.
 * Compute also extra information about marked nodes.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MarkedASTFlattener extends NaiveASTFlattener {
	public static class DefaultMarkedNodeLabelProvider extends MarkedNodeLabelProvider implements DefaultMarkedNodeLabelProviderOptions {

		private final int options;

		public DefaultMarkedNodeLabelProvider(int options) {
			this.options = options;
		}

		private void appendBinding(ASTNode node, StringBuilder buffer) {
			buffer.append('[');
			try {
				IBinding binding = resolveBinding(node);
				if (binding != null) {
					boolean first = true;
					if ((this.options & BINDING_KIND) != 0) {
						if (!first) buffer.append(',');
						first = false;

						appendBindingKind(binding, buffer);
					}
					if ((this.options & BINDING_KEY) != 0) {
						if (!first) buffer.append(',');
						first = false;

						appendBindingKey(binding, buffer);
					}
					if ((this.options & BINDING_FLAGS) != 0) {
						if (!first) buffer.append(',');
						first = false;

						appendBindingFlags(binding, buffer);
					}
				} else {
					buffer.append("null");
				}
			} catch (IllegalArgumentException e) {
				buffer.append("N/A");
			}

			buffer.append(']');
		}

		private void appendBindingFlags(IBinding binding, StringBuilder buffer) {
			boolean firstFlag = true;
			if (binding.isDeprecated()) {
				if (!firstFlag) buffer.append('|');
				firstFlag = false;

				buffer.append("DEPRECATED");
			}
			if (binding.isSynthetic()) {
				if (!firstFlag) buffer.append('|');
				firstFlag = false;

				buffer.append("SYNTHETIC");
			}
			if (binding.isRecovered()) {
				if (!firstFlag) buffer.append('|');
				firstFlag = false;

				buffer.append("RECOVERED");
			}
		}

		private void appendBindingKey(IBinding binding, StringBuilder buffer) {
			buffer.append(binding.getKey());
		}

		private void appendBindingKind(IBinding binding, StringBuilder buffer) {
			switch (binding.getKind()) {
				case IBinding.ANNOTATION:
					buffer.append("ANNOTATION");break;
				case IBinding.MEMBER_VALUE_PAIR:
					buffer.append("MEMBER_VALUE_PAIR");break;
				case IBinding.METHOD:
					buffer.append("METHOD");break;
				case IBinding.PACKAGE:
					buffer.append("PACKAGE");break;
				case IBinding.TYPE:
					buffer.append("TYPE");break;
				case IBinding.VARIABLE:
					buffer.append("VARIABLE");break;
				default:
					buffer.append("UNKNOWN");break;
			}
		}

		private void appendFlags(ASTNode node, StringBuilder buffer) {
			boolean firstFlag = true;
			int flags = node.getFlags();
			if ((flags & ASTNode.MALFORMED) != 0) {
				if (!firstFlag) buffer.append('|');
				firstFlag = false;

				buffer.append("MALFORMED");
			}
			if ((flags & ASTNode.PROTECT) != 0) {
				if (!firstFlag) buffer.append('|');
				firstFlag = false;

				buffer.append("PROTECT");
			}
			if ((flags & ASTNode.RECOVERED) != 0) {
				if (!firstFlag) buffer.append('|');
				firstFlag = false;

				buffer.append("RECOVERED");
			}

			// All nodes are ORIGINAL by default. So an information is printed only when the node isn't ORIGINAL
			if ((flags & ASTNode.ORIGINAL) == 0) {
				if (!firstFlag) buffer.append('|');
				firstFlag = false;

				buffer.append("!ORIGINAL");
			}
		}

		private void appendNodeExtendedPosition(ASTNode node, StringBuilder buffer) {
			ASTNode root = node.getRoot();

			if (root.getNodeType() == ASTNode.COMPILATION_UNIT) {
				CompilationUnit cu = (CompilationUnit) root;

				int extendedStartPosition = cu.getExtendedStartPosition(node);
				int extendedLength = cu.getExtendedLength(node);
				if (extendedStartPosition != node.getStartPosition() ||
						extendedLength != node.getLength()) {
					buffer.append('[');
					buffer.append(cu.getExtendedStartPosition(node));
					buffer.append(',');
					buffer.append(cu.getExtendedLength(node));
					buffer.append(']');
				}

			} else {
				buffer.append("[N/A]");
			}

		}

		private void appendNodePosition(ASTNode node, StringBuilder buffer) {
			buffer.append('[');
			buffer.append(node.getStartPosition());
			buffer.append(',');
			buffer.append(node.getLength());
			buffer.append(']');
		}

		private void appendNodeType(ASTNode node, StringBuilder buffer) {
			switch (node.getNodeType()) {
				case ASTNode.ANNOTATION_TYPE_DECLARATION :
					buffer.append("ANNOTATION_TYPE_DECLARATION");break;
				case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
					buffer.append("ANNOTATION_TYPE_MEMBER_DECLARATION");break;
				case ASTNode.ANONYMOUS_CLASS_DECLARATION :
					buffer.append("ANONYMOUS_CLASS_DECLARATION");break;
				case ASTNode.ARRAY_ACCESS :
					buffer.append("ARRAY_ACCESS");break;
				case ASTNode.ARRAY_CREATION :
					buffer.append("ARRAY_CREATION");break;
				case ASTNode.ARRAY_INITIALIZER :
					buffer.append("ARRAY_INITIALIZER");break;
				case ASTNode.ARRAY_TYPE :
					buffer.append("ARRAY_TYPE");break;
				case ASTNode.ASSERT_STATEMENT :
					buffer.append("ASSERT_STATEMENT");break;
				case ASTNode.ASSIGNMENT :
					buffer.append("ASSIGNMENT");break;
				case ASTNode.BLOCK :
					buffer.append("BLOCK");break;
				case ASTNode.BLOCK_COMMENT :
					buffer.append("BLOCK_COMMENT");break;
				case ASTNode.BOOLEAN_LITERAL :
					buffer.append("BOOLEAN_LITERAL");break;
				case ASTNode.BREAK_STATEMENT :
					buffer.append("BREAK_STATEMENT");break;
				case ASTNode.CAST_EXPRESSION :
					buffer.append("CAST_EXPRESSION");break;
				case ASTNode.CATCH_CLAUSE :
					buffer.append("CATCH_CLAUSE");break;
				case ASTNode.CHARACTER_LITERAL :
					buffer.append("CHARACTER_LITERAL");break;
				case ASTNode.CLASS_INSTANCE_CREATION :
					buffer.append("CLASS_INSTANCE_CREATION");break;
				case ASTNode.COMPILATION_UNIT :
					buffer.append("COMPILATION_UNIT");break;
				case ASTNode.CONDITIONAL_EXPRESSION :
					buffer.append("CONDITIONAL_EXPRESSION");break;
				case ASTNode.CONSTRUCTOR_INVOCATION :
					buffer.append("CONSTRUCTOR_INVOCATION");break;
				case ASTNode.CONTINUE_STATEMENT :
					buffer.append("CONTINUE_STATEMENT");break;
				case ASTNode.DO_STATEMENT :
					buffer.append("DO_STATEMENT");break;
				case ASTNode.EMPTY_STATEMENT :
					buffer.append("EMPTY_STATEMENT");break;
				case ASTNode.ENHANCED_FOR_STATEMENT :
					buffer.append("ENHANCED_FOR_STATEMENT");break;
				case ASTNode.ENUM_CONSTANT_DECLARATION :
					buffer.append("ENUM_CONSTANT_DECLARATION");break;
				case ASTNode.ENUM_DECLARATION :
					buffer.append("ENUM_DECLARATION");break;
				case ASTNode.EXPRESSION_STATEMENT :
					buffer.append("EXPRESSION_STATEMENT");break;
				case ASTNode.EXPORTS_DIRECTIVE :
					buffer.append("EXPORTS_STATEMENT");break;
				case ASTNode.FIELD_ACCESS :
					buffer.append("FIELD_ACCESS");break;
				case ASTNode.FIELD_DECLARATION :
					buffer.append("FIELD_DECLARATION");break;
				case ASTNode.FOR_STATEMENT :
					buffer.append("FOR_STATEMENT");break;
				case ASTNode.IF_STATEMENT :
					buffer.append("IF_STATEMENT");break;
				case ASTNode.IMPORT_DECLARATION :
					buffer.append("IMPORT_DECLARATION");break;
				case ASTNode.INFIX_EXPRESSION :
					buffer.append("INFIX_EXPRESSION");break;
				case ASTNode.INITIALIZER :
					buffer.append("INITIALIZER");break;
				case ASTNode.INSTANCEOF_EXPRESSION :
					buffer.append("INSTANCEOF_EXPRESSION");break;
				case ASTNode.JAVADOC :
					buffer.append("JAVADOC");break;
				case ASTNode.LABELED_STATEMENT :
					buffer.append("LABELED_STATEMENT");break;
				case ASTNode.LINE_COMMENT :
					buffer.append("LINE_COMMENT");break;
				case ASTNode.MARKER_ANNOTATION :
					buffer.append("MARKER_ANNOTATION");break;
				case ASTNode.MEMBER_REF :
					buffer.append("MEMBER_REF");break;
				case ASTNode.MEMBER_VALUE_PAIR :
					buffer.append("MEMBER_VALUE_PAIR");break;
				case ASTNode.METHOD_DECLARATION :
					buffer.append("METHOD_DECLARATION");break;
				case ASTNode.METHOD_INVOCATION :
					buffer.append("METHOD_INVOCATION");break;
				case ASTNode.METHOD_REF :
					buffer.append("METHOD_REF");break;
				case ASTNode.METHOD_REF_PARAMETER :
					buffer.append("METHOD_REF_PARAMETER");break;
				case ASTNode.MODIFIER :
					buffer.append("MODIFIER");break;
				case ASTNode.MODULE_DECLARATION :
					buffer.append("MODULE_DECLARATION");break;
				case ASTNode.NORMAL_ANNOTATION :
					buffer.append("NORMAL_ANNOTATION");break;
				case ASTNode.NULL_LITERAL :
					buffer.append("NULL_LITERAL");break;
				case ASTNode.NUMBER_LITERAL :
					buffer.append("NUMBER_LITERAL");break;
				case ASTNode.OPENS_DIRECTIVE :
					buffer.append("OPENS_STATEMENT");break;
				case ASTNode.PACKAGE_DECLARATION :
					buffer.append("PACKAGE_DECLARATION");break;
				case ASTNode.PARAMETERIZED_TYPE :
					buffer.append("PARAMETERIZED_TYPE");break;
				case ASTNode.PARENTHESIZED_EXPRESSION :
					buffer.append("PARENTHESIZED_EXPRESSION");break;
				case ASTNode.POSTFIX_EXPRESSION :
					buffer.append("POSTFIX_EXPRESSION");break;
				case ASTNode.PREFIX_EXPRESSION :
					buffer.append("PREFIX_EXPRESSION");break;
				case ASTNode.PRIMITIVE_TYPE :
					buffer.append("PRIMITIVE_TYPE");break;
				case ASTNode.PROVIDES_DIRECTIVE :
					buffer.append("PROVIDES_STATEMENT");break;
				case ASTNode.QUALIFIED_NAME :
					buffer.append("QUALIFIED_NAME");break;
				case ASTNode.QUALIFIED_TYPE :
					buffer.append("QUALIFIED_TYPE");break;
				case ASTNode.REQUIRES_DIRECTIVE :
					buffer.append("REQUIRES_STATEMENT");break;
				case ASTNode.RETURN_STATEMENT :
					buffer.append("RETURN_STATEMENT");break;
				case ASTNode.SIMPLE_NAME :
					buffer.append("SIMPLE_NAME");break;
				case ASTNode.SIMPLE_TYPE :
					buffer.append("SIMPLE_TYPE");break;
				case ASTNode.SINGLE_MEMBER_ANNOTATION :
					buffer.append("SINGLE_MEMBER_ANNOTATION");break;
				case ASTNode.SINGLE_VARIABLE_DECLARATION :
					buffer.append("SINGLE_VARIABLE_DECLARATION");break;
				case ASTNode.STRING_LITERAL :
					buffer.append("STRING_LITERAL");break;
				case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
					buffer.append("SUPER_CONSTRUCTOR_INVOCATION");break;
				case ASTNode.SUPER_FIELD_ACCESS :
					buffer.append("SUPER_FIELD_ACCESS");break;
				case ASTNode.SUPER_METHOD_INVOCATION :
					buffer.append("SUPER_METHOD_INVOCATION");break;
				case ASTNode.SWITCH_CASE:
					buffer.append("SWITCH_CASE");break;
				case ASTNode.SWITCH_STATEMENT :
					buffer.append("SWITCH_STATEMENT");break;
				case ASTNode.SYNCHRONIZED_STATEMENT :
					buffer.append("SYNCHRONIZED_STATEMENT");break;
				case ASTNode.TAG_ELEMENT :
					buffer.append("TAG_ELEMENT");break;
				case ASTNode.TEXT_ELEMENT :
					buffer.append("TEXT_ELEMENT");break;
				case ASTNode.THIS_EXPRESSION :
					buffer.append("THIS_EXPRESSION");break;
				case ASTNode.THROW_STATEMENT :
					buffer.append("THROW_STATEMENT");break;
				case ASTNode.TRY_STATEMENT :
					buffer.append("TRY_STATEMENT");break;
				case ASTNode.TYPE_DECLARATION :
					buffer.append("TYPE_DECLARATION");break;
				case ASTNode.TYPE_DECLARATION_STATEMENT :
					buffer.append("TYPE_DECLARATION_STATEMENT");break;
				case ASTNode.TYPE_LITERAL :
					buffer.append("TYPE_LITERAL");break;
				case ASTNode.TYPE_PARAMETER :
					buffer.append("TYPE_PARAMETER");break;
				case ASTNode.USES_DIRECTIVE :
					buffer.append("USES_STATEMENT");break;
				case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
					buffer.append("VARIABLE_DECLARATION_EXPRESSION");break;
				case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
					buffer.append("VARIABLE_DECLARATION_FRAGMENT");break;
				case ASTNode.VARIABLE_DECLARATION_STATEMENT :
					buffer.append("VARIABLE_DECLARATION_STATEMENT");break;
				case ASTNode.WHILE_STATEMENT :
					buffer.append("WHILE_STATEMENT");break;
				case ASTNode.WILDCARD_TYPE :
					buffer.append("WILDCARD_TYPE");break;
				default:
					buffer.append("UNKNOWN");
			}
		}

		@Override
		public String getText(ASTNode node) {
			StringBuilder buffer = new StringBuilder();

			boolean first = true;

			if ((this.options & NODE_TYPE) != 0) {
				if (!first) buffer.append(',');
				first = false;

				appendNodeType(node, buffer);
			}
			if ((this.options & NODE_POSITION) != 0) {
				if (!first) buffer.append(',');
				first = false;

				appendNodePosition(node, buffer);
			}

			if ((this.options & NODE_EXTENDED_POSITION) != 0) {
				if (!first) buffer.append(',');
				first = false;

				appendNodeExtendedPosition(node, buffer);
			}

			if ((this.options & NODE_FLAGS) != 0) {
				if (!first) buffer.append(',');
				first = false;

				appendFlags(node, buffer);
			}

			if ((this.options & BINDING_OPTIONS) != 0) {
				if (!first) buffer.append(',');
				first = false;

				appendBinding(node, buffer);
			}

			return buffer.toString();
		}

		protected IBinding resolveBinding(ASTNode node) {
			switch (node.getNodeType()) {
				case ASTNode.PACKAGE_DECLARATION:
					return ((PackageDeclaration) node).resolveBinding();
				case ASTNode.TYPE_DECLARATION:
					return ((TypeDeclaration) node).resolveBinding();
				case ASTNode.ANONYMOUS_CLASS_DECLARATION:
					return ((AnonymousClassDeclaration) node).resolveBinding();
				case ASTNode.TYPE_DECLARATION_STATEMENT:
					return ((TypeDeclarationStatement) node).resolveBinding();
				case ASTNode.METHOD_DECLARATION:
					return ((MethodDeclaration) node).resolveBinding();
				case ASTNode.METHOD_INVOCATION:
					return ((MethodInvocation) node).resolveMethodBinding();
				case ASTNode.TYPE_PARAMETER:
					return ((TypeParameter) node).resolveBinding();
				case ASTNode.PARAMETERIZED_TYPE:
					return ((ParameterizedType) node).resolveBinding();
				case ASTNode.WILDCARD_TYPE:
					return ((WildcardType) node).resolveBinding();
				case ASTNode.SIMPLE_NAME:
					return ((SimpleName) node).resolveBinding();
				case ASTNode.ARRAY_TYPE:
					return ((ArrayType) node).resolveBinding();
				case ASTNode.ASSIGNMENT:
					return ((Assignment) node).getRightHandSide().resolveTypeBinding();
				case ASTNode.SIMPLE_TYPE:
					return ((SimpleType) node).resolveBinding();
				case ASTNode.QUALIFIED_NAME:
					return ((QualifiedName) node).resolveBinding();
				case ASTNode.MARKER_ANNOTATION:
					return ((MarkerAnnotation) node).resolveAnnotationBinding();
				case ASTNode.NORMAL_ANNOTATION:
					return ((NormalAnnotation) node).resolveAnnotationBinding();
				case ASTNode.SINGLE_MEMBER_ANNOTATION:
					return ((SingleMemberAnnotation) node).resolveAnnotationBinding();
				case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
					return ((VariableDeclarationFragment) node).resolveBinding();
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Compute extra information about a marked node
	 */
	public static abstract class MarkedNodeLabelProvider {
		public abstract String getText(ASTNode node);
	}

	private final static String AST_DELIMITER = "===== AST =====";
	private final static String DETAILS_DELIMITER = "===== Details =====";
	private final static String PROBLEMS_DELIMITER = "===== Problems =====";

	private final static String NO_PROBLEM = "No problem";
	private static final String NO_CORRESPONDING_NODE = "No corresponding node";

	// options
	private final boolean reportAST;
	private final boolean reportProblems;

	private String source;
	private CompilationUnit unit;
	private AbstractASTTests.MarkerInfo markerInfo;

	private Map markerFromNode;
	private Map nodeFromMarker;
	private Map markerPositonInBuffer;

	private boolean[] foundNodeFromMarker;
	private final StringBuilder markedNodesBuffer;

	private final MarkedNodeLabelProvider labelProvider;

	public MarkedASTFlattener(
			boolean reportAST,
			boolean reportProblems,
			MarkedNodeLabelProvider labelProvider) {

		this.reportAST = reportAST;
		this.reportProblems = reportProblems;

		this.markedNodesBuffer = new StringBuilder();
		this.labelProvider = labelProvider;
	}

	public String getResult() {
		StringBuilder resultBuffer = new StringBuilder();

		if (this.reportAST) {
			resultBuffer.append(AST_DELIMITER);
			resultBuffer.append('\n');
			resultBuffer.append(super.getResult());
			resultBuffer.append('\n');
		}

		resultBuffer.append(DETAILS_DELIMITER);
		resultBuffer.append(this.markedNodesBuffer);

		if (this.reportProblems) {
			resultBuffer.append('\n');
			resultBuffer.append(PROBLEMS_DELIMITER);
			resultBuffer.append('\n');

			StringBuilder problemBuffer = new StringBuilder();
			IProblem[] problems = this.unit.getProblems();
			int problemCount = problems.length;
			if (problemCount != 0) {
				for (int i = 0; i < problemCount; i++) {
					org.eclipse.jdt.core.tests.util.Util.appendProblem(problemBuffer, problems[i], this.source == null ? null : this.source.toCharArray() , i + 1);
				}
			} else {
				problemBuffer.append(NO_PROBLEM);
			}
			resultBuffer.append(Util.convertToIndependantLineDelimiter(problemBuffer.toString()));
		}

		return resultBuffer.toString();
	}

	public void postVisit(ASTNode node) {
		String markerName;
		if (this.reportAST && (markerName = (String)this.markerFromNode.get(node)) != null) {
			// add start marker
			int pos = ((Integer)this.markerPositonInBuffer.get(markerName)).intValue();
			while (Character.isWhitespace(this.buffer.charAt(pos))) {pos++;}
			this.buffer.insert(pos, this.markerInfo.markerStartStart + markerName + this.markerInfo.markerStartEnd);

			// add end marker
			pos = this.buffer.length() - 1;
			while (Character.isWhitespace(this.buffer.charAt(pos))) {pos--;}
			this.buffer.insert(pos + 1, this.markerInfo.markerEndStart + markerName + this.markerInfo.markerEndEnd);
		}
	}
	public void preVisit(ASTNode node) {
		String markerName = null;
		int index = -1;
		found : while ((index = this.markerInfo.indexOfASTStart(node.getStartPosition(), index + 1)) != -1) {
			if (node.getStartPosition() + node.getLength() == this.markerInfo.astEnds[index]) {
				markerName = String.valueOf(index + 1);

				if (this.nodeFromMarker.get(markerName) == null) {
					this.markerFromNode.put(node, markerName);
					this.nodeFromMarker.put(markerName, node);
					this.markerPositonInBuffer.put(markerName, Integer.valueOf(this.buffer.length()));
					this.foundNodeFromMarker[index] = true;
				}

				break found;
			}
		}

		if (markerName != null) {
			this.markedNodesBuffer.append('\n');
			this.markedNodesBuffer.append(markerName);
			this.markedNodesBuffer.append(':');
			this.markedNodesBuffer.append(this.labelProvider.getText(node));
		}
	}

	public void process(CompilationUnit cu, AbstractASTTests.MarkerInfo mf) {
		this.source = mf.source;
		this.unit = cu;
		this.markerInfo = mf;

		this.markerFromNode = new HashMap();
		this.nodeFromMarker = new HashMap();
		this.markerPositonInBuffer = new HashMap();
		int length = mf.astStarts.length;
		this.foundNodeFromMarker = new boolean[length];
		this.unit.accept(this);

		for (int i = 0; i < length; i++) {
			if (!this.foundNodeFromMarker[i]) {
				this.markedNodesBuffer.append('\n');
				this.markedNodesBuffer.append(String.valueOf(i + 1));
				this.markedNodesBuffer.append(':');
				this.markedNodesBuffer.append(NO_CORRESPONDING_NODE);
			}
		}
	}
}
