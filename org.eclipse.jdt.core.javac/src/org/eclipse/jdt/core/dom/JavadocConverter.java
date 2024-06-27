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
package org.eclipse.jdt.core.dom;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;

import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.parser.UnicodeReader;
import com.sun.tools.javac.tree.DCTree;
import com.sun.tools.javac.tree.DCTree.DCAuthor;
import com.sun.tools.javac.tree.DCTree.DCBlockTag;
import com.sun.tools.javac.tree.DCTree.DCComment;
import com.sun.tools.javac.tree.DCTree.DCDeprecated;
import com.sun.tools.javac.tree.DCTree.DCDocComment;
import com.sun.tools.javac.tree.DCTree.DCEndElement;
import com.sun.tools.javac.tree.DCTree.DCEntity;
import com.sun.tools.javac.tree.DCTree.DCErroneous;
import com.sun.tools.javac.tree.DCTree.DCIdentifier;
import com.sun.tools.javac.tree.DCTree.DCInheritDoc;
import com.sun.tools.javac.tree.DCTree.DCLink;
import com.sun.tools.javac.tree.DCTree.DCLiteral;
import com.sun.tools.javac.tree.DCTree.DCParam;
import com.sun.tools.javac.tree.DCTree.DCReference;
import com.sun.tools.javac.tree.DCTree.DCReturn;
import com.sun.tools.javac.tree.DCTree.DCSee;
import com.sun.tools.javac.tree.DCTree.DCSince;
import com.sun.tools.javac.tree.DCTree.DCSnippet;
import com.sun.tools.javac.tree.DCTree.DCStartElement;
import com.sun.tools.javac.tree.DCTree.DCText;
import com.sun.tools.javac.tree.DCTree.DCThrows;
import com.sun.tools.javac.tree.DCTree.DCUnknownBlockTag;
import com.sun.tools.javac.tree.DCTree.DCUnknownInlineTag;
import com.sun.tools.javac.tree.DCTree.DCUses;
import com.sun.tools.javac.tree.DCTree.DCValue;
import com.sun.tools.javac.tree.DCTree.DCVersion;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.JCDiagnostic;

class JavadocConverter {

	private final AST ast;
	private final JavacConverter javacConverter;
	private final DCDocComment docComment;
	private final int initialOffset;
	private final int endOffset;
	private boolean buildJavadoc;
	private final TreePath contextTreePath;

	public final Map<ASTNode, DocTreePath> converted = new HashMap<>();

	final private Set<JCDiagnostic> diagnostics = new HashSet<>();

	private static Field UNICODE_READER_CLASS_OFFSET_FIELD = null;
	static {
		try {
			Class<UnicodeReader> unicodeReaderClass = (Class<UnicodeReader>) Class.forName("com.sun.tools.javac.parser.UnicodeReader");
			UNICODE_READER_CLASS_OFFSET_FIELD = unicodeReaderClass.getDeclaredField("offset");
			UNICODE_READER_CLASS_OFFSET_FIELD.setAccessible(true);
		} catch (Exception e) {
			// do nothing, leave null
		}
	}

	JavadocConverter(JavacConverter javacConverter, DCDocComment docComment, TreePath contextTreePath, boolean buildJavadoc) {
		this.javacConverter = javacConverter;
		this.ast = javacConverter.ast;
		this.docComment = docComment;
		this.contextTreePath = contextTreePath;
		this.buildJavadoc = buildJavadoc;

		int startPos = -1;
		if (UNICODE_READER_CLASS_OFFSET_FIELD != null) {
			try {
				startPos = UNICODE_READER_CLASS_OFFSET_FIELD.getInt(docComment.comment);
			} catch (Exception e) {
				ILog.get().warn("could not reflexivly access doc comment offset");
			}
		} else {
			startPos = docComment.getSourcePosition(0) >= 0 ? docComment.getSourcePosition(0) : docComment.comment.getSourcePos(0);
		}

		if (startPos < 0) {
			throw new IllegalArgumentException("Doc comment has no start position");
		}
		this.initialOffset = startPos;
		this.endOffset = startPos + this.javacConverter.rawText.substring(startPos).indexOf("*/") + "*/".length();
	}

	JavadocConverter(JavacConverter javacConverter, DCDocComment docComment, int initialOffset, int endPos, boolean buildJavadoc) {
		this.javacConverter = javacConverter;
		this.ast = javacConverter.ast;
		this.docComment = docComment;
		this.contextTreePath = null;
		this.buildJavadoc = buildJavadoc;
		this.initialOffset = initialOffset;
		this.endOffset = endPos;
	}

	private void commonSettings(ASTNode res, DCTree javac) {
		if (javac != null) {
			int startPosition = this.docComment.getSourcePosition(javac.getStartPosition());
			int endPosition = this.docComment.getSourcePosition(javac.getEndPosition());
			int length = endPosition - startPosition;
			if (res instanceof TextElement) {
				length++;
			}
			res.setSourceRange(startPosition, length);
			if (this.contextTreePath != null) {
				this.converted.put(res, DocTreePath.getPath(this.contextTreePath, this.docComment, javac));
			}
		}
	}

	Javadoc convertJavadoc() {
		Javadoc res = this.ast.newJavadoc();
		res.setSourceRange(this.initialOffset, this.endOffset - this.initialOffset);
		if( this.javacConverter.ast.apiLevel == AST.JLS2_INTERNAL) {
			String rawContent = this.javacConverter.rawText.substring(this.initialOffset, this.endOffset);
			res.setComment(rawContent);
		}
		if (this.buildJavadoc) {
			List<? extends IDocElement> elements = Stream.of(docComment.preamble, docComment.fullBody, docComment.postamble, docComment.tags)
				.flatMap(List::stream)
				.flatMap(this::convertElement)
				.toList();
			TagElement host = null;
			for (IDocElement docElement : elements) {
				if (docElement instanceof TagElement tag && !isInline(tag)) {
					if (host != null) {
						res.tags().add(host);
						host = null;
					}
					res.tags().add(tag);
				} else {
					if (host == null) {
						host = this.ast.newTagElement();
						if(docElement instanceof ASTNode astn) {
							host.setSourceRange(astn.getStartPosition(), astn.getLength());
						}
					} else if (docElement instanceof ASTNode extraNode){
						host.setSourceRange(host.getStartPosition(), extraNode.getStartPosition() + extraNode.getLength() - host.getStartPosition());
					}
					host.fragments().add(docElement);
				}
			}
			if (host != null) {
				res.tags().add(host);
			}
		}
		return res;
	}

	Set<JCDiagnostic> getDiagnostics() {
        return diagnostics;
    }

	private boolean isInline(TagElement tag) {
		return tag.getTagName() != null && switch (tag.getTagName()) {
			case TagElement.TAG_CODE,
				TagElement.TAG_DOCROOT,
				TagElement.TAG_INHERITDOC,
				TagElement.TAG_LINK,
				TagElement.TAG_LINKPLAIN,
				TagElement.TAG_LITERAL,
				TagElement.TAG_SNIPPET,
				TagElement.TAG_VALUE -> true;
			default -> false;
		};
	}

	private Optional<TagElement> convertBlockTag(DCTree javac) {
		TagElement res = this.ast.newTagElement();
		commonSettings(res, javac);
		if (javac instanceof DCAuthor author) {
			res.setTagName(TagElement.TAG_AUTHOR);
			author.name.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else if (javac instanceof DCSince since) {
			res.setTagName(TagElement.TAG_SINCE);
			since.body.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else if (javac instanceof DCVersion version) {
		    res.setTagName(TagElement.TAG_VERSION);
		    version.body.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		}  else if (javac instanceof DCSee see) {
			res.setTagName(TagElement.TAG_SEE);
			see.reference.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else if (javac instanceof DCDeprecated deprecated) {
			res.setTagName(TagElement.TAG_DEPRECATED);
			deprecated.body.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else if (javac instanceof DCParam param) {
			res.setTagName(TagElement.TAG_PARAM);
			res.fragments().addAll(convertElement(param.name).toList());
			param.description.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else if (javac instanceof DCReturn ret) {
			res.setTagName(TagElement.TAG_RETURN);
			ret.description.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else if (javac instanceof DCThrows thrown) {
			res.setTagName(TagElement.TAG_THROWS);
			res.fragments().addAll(convertElement(thrown.name).toList());
			thrown.description.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else if (javac instanceof DCUses uses) {
			res.setTagName(TagElement.TAG_USES);
			res.fragments().addAll(convertElement(uses.serviceType).toList());
			uses.description.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else if (javac instanceof DCUnknownBlockTag unknown) {
			res.setTagName(unknown.getTagName());
			unknown.content.stream().flatMap(this::convertElement).forEach(res.fragments::add);
		} else {
			return Optional.empty();
		}
		return Optional.of(res);
	}

	private Optional<TagElement> convertInlineTag(DCTree javac) {
		TagElement res = this.ast.newTagElement();
		commonSettings(res, javac);
//		res.setSourceRange(res.getStartPosition(), res.getLength() + 1); // include `@` prefix
		if (javac instanceof DCLiteral literal) {
			res.setTagName(switch (literal.getKind()) {
				case CODE -> TagElement.TAG_CODE;
				case LITERAL -> TagElement.TAG_LITERAL;
				default -> TagElement.TAG_LITERAL;
			});
			res.fragments().addAll(convertElement(literal.body).toList());
		} else if (javac instanceof DCLink link) {
			res.setTagName(TagElement.TAG_LINK);
			res.fragments().addAll(convertElement(link.ref).toList());
			link.label.stream().flatMap(this::convertElement).forEach(res.fragments()::add);
		} else if (javac instanceof DCValue) {
            res.setTagName(TagElement.TAG_VALUE);
		} else if (javac instanceof DCInheritDoc inheritDoc) {
			res.setTagName(TagElement.TAG_INHERITDOC);
		} else if (javac instanceof DCSnippet snippet) {
			res.setTagName(TagElement.TAG_SNIPPET);
			// TODO hardcoded value
			res.setProperty(TagProperty.TAG_PROPERTY_SNIPPET_ERROR, false);
			// TODO hardcoded value
			res.setProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID, true);
			// TODO attributes
			res.fragments().addAll(convertElement(snippet.body).toList());
		} else if (javac instanceof DCUnknownInlineTag unknown) {
			res.fragments().add(toDefaultTextElement(unknown));
		} else {
			return Optional.empty();
		}
		return Optional.of(res);
	}

	private Name toName(JCTree expression, int parentOffset) {
		Name n = this.javacConverter.toName(expression, (dom, javac) -> {
			int start = parentOffset + javac.getStartPosition();
			int length = javac.toString().length();
			dom.setSourceRange(start, Math.max(0,length));
			this.javacConverter.domToJavac.put(dom, javac);
		});
		// We need to clean all the sub-names
		if( n instanceof QualifiedName qn ) {
			SimpleName sn = qn.getName();
			if( sn.getStartPosition() == 0 || sn.getStartPosition() == -1) {
				int qnEnd = qn.getStartPosition() + qn.getLength();
				int start = qnEnd - sn.toString().length();
				sn.setSourceRange(start, qnEnd-start);
			}
			cleanNameQualifierLocations(qn);
		}
		return n;
	}

	private void cleanNameQualifierLocations(QualifiedName qn) {
		Name qualifier = qn.getQualifier();
		if( qualifier != null ) {
			qualifier.setSourceRange(qn.getStartPosition(), qualifier.toString().length());
			if( qualifier instanceof QualifiedName qn2) {
				cleanNameQualifierLocations(qn2);
			}
		}
	}

	private class Region {
		final int startOffset;
		final int length;

		Region(int startOffset, int length) {
			this.startOffset = startOffset;
			this.length = length;
		}

		String getContents() {
			return JavadocConverter.this.javacConverter.rawText.substring(this.startOffset, this.startOffset + this.length);
		}

		public int endPosition() {
			return this.startOffset + this.length;
		}
	}

	private TextElement toTextElement(Region line) {
		TextElement res = this.ast.newTextElement();
		res.setSourceRange(line.startOffset, line.length);
		res.setText(this.javacConverter.rawText.substring(line.startOffset, line.startOffset + line.length));
		return res;
	}

	private Stream<Region> splitLines(DCText text) {
		int[] startPosition = { this.docComment.getSourcePosition(text.getStartPosition()) };
		int endPosition = this.docComment.getSourcePosition(text.getEndPosition());
		return Arrays.stream(this.javacConverter.rawText.substring(startPosition[0], endPosition).split("(\r)?\n\\s*\\*\\s")) //$NON-NLS-1$
			.map(string -> {
				int index = this.javacConverter.rawText.indexOf(string, startPosition[0]);
				if (index < 0) {
					return null;
				}
				startPosition[0] = index + string.length();
				return new Region(index, string.length());
			}).filter(Objects::nonNull);
	}

	private Stream<? extends IDocElement> convertElement(DCTree javac) {
		if (javac instanceof DCText text) {
			return splitLines(text).map(this::toTextElement);
		} else if (javac instanceof DCIdentifier identifier) {
			Name res = this.ast.newName(identifier.getName().toString());
			commonSettings(res, javac);
			return Stream.of(res);
		} else if (javac instanceof DCReference reference) {
			String signature = reference.getSignature();
			if (reference.memberName != null) {
				if (signature.charAt(signature.length() - 1) == ')') {
					MethodRef res = this.ast.newMethodRef();
					commonSettings(res, javac);
					int currentOffset = this.docComment.getSourcePosition(reference.getStartPosition());
					if (reference.qualifierExpression != null) {
						Name qualifierExpressionName = toName(reference.qualifierExpression, res.getStartPosition());
						qualifierExpressionName.setSourceRange(currentOffset, Math.max(0, reference.qualifierExpression.toString().length()));
						res.setQualifier(qualifierExpressionName);
						currentOffset += qualifierExpressionName.getLength();
					}
					currentOffset++; // #
					SimpleName name = this.ast.newSimpleName(reference.memberName.toString());
					name.setSourceRange(currentOffset, Math.max(0, reference.memberName.toString().length()));
					currentOffset += name.getLength();
					res.setName(name);
					if (this.contextTreePath != null) {
						this.converted.put(name, DocTreePath.getPath(this.contextTreePath, this.docComment, reference));
					}
					currentOffset++; // (
					final int paramListOffset = currentOffset;
					List<Region> params = new ArrayList<>();
					int separatorOffset = currentOffset;
					while (separatorOffset < res.getStartPosition() + res.getLength()
							&& this.javacConverter.rawText.charAt(separatorOffset) != ')') {
						while (separatorOffset < res.getStartPosition() + res.getLength()
							&& this.javacConverter.rawText.charAt(separatorOffset) != ')'
							&& this.javacConverter.rawText.charAt(separatorOffset) != ',') {
							separatorOffset++;
						}
						params.add(new Region(currentOffset, separatorOffset - currentOffset));
						separatorOffset++; // consume separator
						currentOffset = separatorOffset;
					}
					for (int i = 0; i < reference.paramTypes.size(); i++) {
						JCTree type = reference.paramTypes.get(i);
						Region range = i < params.size() ? params.get(i) : null;
						res.parameters().add(toMethodRefParam(type, range, paramListOffset));
					}
					return Stream.of(res);
				} else {
					MemberRef res = this.ast.newMemberRef();
					commonSettings(res, javac);
					SimpleName name = this.ast.newSimpleName(reference.memberName.toString());
					name.setSourceRange(this.docComment.getSourcePosition(javac.getStartPosition()), Math.max(0, reference.memberName.toString().length()));
					if (this.contextTreePath != null) {
						this.converted.put(res, DocTreePath.getPath(this.contextTreePath, this.docComment, reference));
					}
					res.setName(name);
					if (reference.qualifierExpression != null) {
						Name qualifierExpressionName = toName(reference.qualifierExpression, res.getStartPosition());
						qualifierExpressionName.setSourceRange(this.docComment.getSourcePosition(reference.pos), Math.max(0, reference.qualifierExpression.toString().length()));
						res.setQualifier(qualifierExpressionName);
					}
					return Stream.of(res);
				}
			} else if (!signature.contains("#")) {
				Name res = this.javacConverter.toName(reference.qualifierExpression, (dom, javacNode) -> {
					int startPosition = this.docComment.getSourcePosition(reference.getPreferredPosition()) + javacNode.getStartPosition();
					dom.setSourceRange(startPosition, dom.getLength());
					if (this.contextTreePath != null) {
						this.converted.put(dom, DocTreePath.getPath(this.contextTreePath, this.docComment, javac));
					}
				});
//				res.accept(new ASTVisitor() {
//					@Override
//					public void preVisit(ASTNode node) {
//						JavadocConverter.this.converted.put(node, DocTreePath.getPath(JavadocConverter.this.contextTreePath, JavadocConverter.this.docComment, reference));
//					}
//				});
				return Stream.of(res);
			}
		} else if (javac instanceof DCStartElement || javac instanceof DCEndElement || javac instanceof DCEntity) {
			return Stream.of(toDefaultTextElement(javac));
		} else if (javac instanceof DCBlockTag || javac instanceof DCReturn) {
			Optional<Stream<TagElement>> blockTag = convertBlockTag(javac).map(Stream::of);
			if (blockTag.isPresent()) {
				return blockTag.get();
			}
		} else if (javac instanceof DCErroneous erroneous) {
		    JavaDocTextElement res = this.ast.newJavaDocTextElement();
	        commonSettings(res, erroneous);
	        res.setText(res.text);
	        diagnostics.add(erroneous.diag);
	        return Stream.of(res);
		} else if (javac instanceof DCComment comment) {
            TextElement res = this.ast.newTextElement();
            commonSettings(res, comment);
            res.setText(res.text);
            return Stream.of(res);
		} else {
			Optional<Stream<TagElement>> inlineTag = convertInlineTag(javac).map(Stream::of);
			if (inlineTag.isPresent()) {
				return inlineTag.get();
			}
		}
		var message = "💥🐛 Not supported yet conversion of " + javac.getClass().getSimpleName() + " to element";
		ILog.get().error(message);
		JavaDocTextElement res = this.ast.newJavaDocTextElement();
		commonSettings(res, javac);
		res.setText(this.docComment.comment.getText().substring(javac.getStartPosition(), javac.getEndPosition()) + System.lineSeparator() + message);
		return Stream.of(res);
	}

	private JavaDocTextElement toDefaultTextElement(DCTree javac) {
		JavaDocTextElement res = this.ast.newJavaDocTextElement();
		commonSettings(res, javac);
		res.setText(this.docComment.comment.getText().substring(javac.getStartPosition(), javac.getEndPosition()));
		return res;
	}

	private MethodRefParameter toMethodRefParam(JCTree type, Region range, int paramListOffset) {
		MethodRefParameter res = this.ast.newMethodRefParameter();
		res.setSourceRange(
				range != null ? range.startOffset : paramListOffset + type.getStartPosition(),
				range != null ? range.length : type.toString().length());
		// Make positons absolute
		var fixPositions = new TreeScanner() {
			@Override
			public void scan(JCTree tree) {
				tree.setPos(tree.pos + paramListOffset);
				super.scan(tree);
			}
		};
		fixPositions.scan(type);
		Type jdtType = this.javacConverter.convertToType(type);
		res.setType(jdtType);
		// some lengths may be missing
		jdtType.accept(new ASTVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				if (node.getLength() == 0 && node.getStartPosition() >= 0) {
					node.setSourceRange(node.getStartPosition(), node.toString().length());
				}
				super.preVisit(node);
			}
		});
		if (jdtType.getStartPosition() + jdtType.getLength() < res.getStartPosition() + res.getLength()) {
			String[] segments = range.getContents().trim().split("\s");
			if (segments.length > 1) {
				String nameSegment = segments[segments.length - 1];
				SimpleName name = this.ast.newSimpleName(nameSegment);
				name.setSourceRange(this.javacConverter.rawText.lastIndexOf(nameSegment, range.endPosition()), nameSegment.length());
				res.setName(name);
			}
		}
		return res;
	}
}
