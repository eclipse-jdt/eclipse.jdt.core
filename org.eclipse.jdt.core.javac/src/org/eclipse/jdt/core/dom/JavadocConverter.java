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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;

import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
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
import com.sun.tools.javac.tree.DCTree.DCRawText;
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
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.JCDiagnostic;

import jdk.javadoc.internal.doclets.formats.html.taglets.snippet.Attribute;
import jdk.javadoc.internal.doclets.formats.html.taglets.snippet.MarkupParser;

class JavadocConverter {

	// Both copied from jdk.javadoc.internal.doclets.formats.html.taglets.snippet.Parser
	private static final Pattern JAVA_COMMENT = Pattern.compile(
			"^(?<payload>.*)//(?<markup>\\s*@\\s*\\w+.+?)$");
	private static final Pattern PROPERTIES_COMMENT = Pattern.compile(
			"^(?<payload>[ \t]*([#!].*)?)[#!](?<markup>\\s*@\\s*\\w+.+?)$");

	private final AST ast;
	private final JavacConverter javacConverter;
	private final DCDocComment docComment;
	private final int initialOffset;
	private final int endOffset;
	private boolean buildJavadoc;
	private final TreePath contextTreePath;
	private String rawContent;

	public final Map<ASTNode, DocTreePath> converted = new HashMap<>();

	final private Set<JCDiagnostic> diagnostics = new HashSet<>();

	private static final Pattern BEGIN_CHOPPER = Pattern.compile("(?:\\s+\\*)(.*)");

	private JavadocConverter(JavacConverter javacConverter, DCDocComment docComment, TreePath contextTreePath, int initialOffset, int endPos, boolean buildJavadoc) {
		this.javacConverter = javacConverter;
		this.ast = javacConverter.ast;
		this.docComment = docComment;
		this.contextTreePath = contextTreePath;
		this.buildJavadoc = buildJavadoc;
		this.initialOffset = initialOffset;
		this.endOffset = endPos;
	}

	JavadocConverter(JavacConverter javacConverter, DCDocComment docComment, int initialOffset, int endPos, boolean buildJavadoc) {
		this(javacConverter, docComment, null, initialOffset, endPos, buildJavadoc);
	}

	JavadocConverter(JavacConverter javacConverter, DCDocComment docComment, TreePath contextTreePath, boolean buildJavadoc) {
		this(javacConverter, docComment, contextTreePath, docComment.comment.getPos().getStartPosition(), docComment.comment.getPos().getEndPosition(javacConverter.javacCompilationUnit.endPositions), buildJavadoc);
	}

	private void commonSettings(ASTNode res, DCTree javac) {
		if (javac != null) {
			int startPosition = this.docComment.getSourcePosition(javac.getStartPosition());
			int endPosition = this.docComment.getSourcePosition(javac.getEndPosition());
			int length = endPosition - startPosition;
//			if (res instanceof TextElement) {
//				length++;
//			}
			if (startPosition >= 0 && length >= 0) {
				res.setSourceRange(startPosition, length);
			}
			if (this.contextTreePath != null) {
				this.converted.put(res, DocTreePath.getPath(this.contextTreePath, this.docComment, javac));
			}
		}
	}
	
	public String getRawContent() {
		if( this.rawContent == null ) 
			this.rawContent = this.javacConverter.rawText.substring(this.initialOffset, this.endOffset);
		return rawContent;
	}

	Javadoc convertJavadoc() {
		Javadoc res = this.ast.newJavadoc();
		res.setSourceRange(this.initialOffset, this.endOffset - this.initialOffset);
		try {
			if( this.javacConverter.ast.apiLevel == AST.JLS2_INTERNAL) {
				String rawContent = getRawContent();
				try {
					res.setComment(rawContent);
				} catch( IllegalArgumentException iae) {
					// Ignore
				}
			}
			if (this.buildJavadoc) {
				List<DCTree> treeElements = Stream.of(docComment.preamble, docComment.fullBody, docComment.postamble, docComment.tags)
						.flatMap(List::stream).toList();
				List<IDocElement> elements2 = convertElementCombiningNodes(treeElements);
				List<IDocElement> elements = convertNestedTagElements(elements2);
				
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
						} else if (docElement instanceof ASTNode extraNode && extraNode.getStartPosition() >= 0 && extraNode.getLength() >= 0){
							host.setSourceRange(host.getStartPosition(), extraNode.getStartPosition() + extraNode.getLength() - host.getStartPosition());
						}
						
						host.fragments().add(docElement);
					}
				}
				if (host != null) {
					res.tags().add(host);
				}
			}
		} catch (Exception ex) {
			ILog.get().error("Failed to convert Javadoc", ex);
		}
		return res;
	}

	private List<IDocElement> convertNestedTagElements(List<IDocElement> elements2) {
		return elements2.stream().map(x -> {
			if( x instanceof TextElement te) {
				String s = te.getText();
				if( s != null && s.startsWith("{@") && s.trim().endsWith("}")) {
					String txt = this.javacConverter.rawText.substring(te.getStartPosition(), te.getStartPosition() + te.getLength());
					TextElement innerMost = this.ast.newTextElement();
					innerMost.setSourceRange(te.getStartPosition()+2, te.getLength()-3);
					innerMost.setText(txt.substring(2, txt.length() - 1));
					
					TagElement nested = this.ast.newTagElement();
					int atLoc = txt.indexOf("@");
					String name = atLoc == -1 ? txt : ("@" + txt.substring(atLoc + 1)).split("\\s+")[0];
					nested.setTagName(name);
					nested.setSourceRange(te.getStartPosition(), te.getLength());
					nested.fragments().add(innerMost);
					
					TagElement wrapper = this.ast.newTagElement();
					wrapper.setSourceRange(te.getStartPosition(), te.getLength());
					wrapper.fragments().add(nested);
					return wrapper;
				}
			}
			return x;
		}).toList();
	}

	Set<JCDiagnostic> getDiagnostics() {
        return diagnostics;
    }

	static boolean isInline(TagElement tag) {
		return tag.getTagName() == null || switch (tag.getTagName()) {
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
			convertElementCombiningNodes(author.name.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else if (javac instanceof DCSince since) {
			res.setTagName(TagElement.TAG_SINCE);
			convertElementCombiningNodes(since.body.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else if (javac instanceof DCVersion version) {
		    res.setTagName(TagElement.TAG_VERSION);
			convertElementCombiningNodes(version.body.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		}  else if (javac instanceof DCSee see) {
			res.setTagName(TagElement.TAG_SEE);
			convertElementCombiningNodes(see.reference.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else if (javac instanceof DCDeprecated deprecated) {
			res.setTagName(TagElement.TAG_DEPRECATED);
			convertElementCombiningNodes(deprecated.body.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else if (javac instanceof DCParam param) {
			res.setTagName(TagElement.TAG_PARAM);
			int tagNameEnds = javac.getStartPosition() + res.getTagName().length();
			if( param.isTypeParameter()) {
				int stopSearchRelative = param.getEndPosition();
				if( param.description != null && param.description.size() > 0 ) { 
					stopSearchRelative = param.description.get(0).getEndPosition();
				}
				int stopSearchAbsolute = this.docComment.getSourcePosition(stopSearchRelative);
				int start = this.docComment.getSourcePosition(param.getStartPosition());
				int ltRaw = this.javacConverter.rawText.indexOf("<", start, stopSearchAbsolute);
				int gtRaw = this.javacConverter.rawText.indexOf(">", start, stopSearchAbsolute);
				if( ltRaw != -1 ) {
					int ltStart = this.docComment.getSourcePosition(tagNameEnds+1);
					// must include spaces
					Region r = new Region(ltStart, 1 + (ltRaw - ltStart));
					res.fragments().add(toTextElement(r));
					res.fragments().addAll(convertElement(param.name).toList());
				} else {
					res.fragments().addAll(convertElement(param.name).toList());
				}
				if( gtRaw != -1 ) {
					Region r = new Region(gtRaw, 1);
					res.fragments().add(toTextElement(r));
				}
				} else {
					res.fragments().addAll(convertElement(param.name).toList());
				}
			convertElementCombiningNodes(param.description.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else if (javac instanceof DCReturn ret) {
			res.setTagName(TagElement.TAG_RETURN);
			convertElementCombiningNodes(ret.description.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else if (javac instanceof DCThrows thrown) {
			String tagName = thrown.kind == Kind.THROWS ? TagElement.TAG_THROWS : TagElement.TAG_EXCEPTION;
			res.setTagName(tagName);
			res.fragments().addAll(convertElement(thrown.name).toList());
			convertElementCombiningNodes(thrown.description.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else if (javac instanceof DCUses uses) {
			res.setTagName(TagElement.TAG_USES);
			// According to SemanticTokensHandlerTest.testSemanticTokens_Modules,
			// we want directly a TextElement rather than a name here
			//res.fragments().addAll(convertElement(uses.serviceType).toList());
			TextElement serviceName = this.ast.newTextElement();
			serviceName.setText(uses.serviceType.getSignature());
			commonSettings(serviceName, uses.serviceType);
			res.fragments().add(serviceName);
			convertElementCombiningNodes(uses.description.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else if (javac instanceof DCUnknownBlockTag unknown) {
			res.setTagName("@" + unknown.getTagName());
			convertElementCombiningNodes(unknown.content.stream().filter(x -> x != null).toList()).forEach(res.fragments::add);
		} else {
			return Optional.empty();
		}
		if( res != null ) {
			if( res.fragments().size() != 0 ) {
				// Make sure the tag wrapper has a proper source range
				ASTNode lastFrag = ((ASTNode)res.fragments().get(res.fragments().size() - 1));
				int trueEnd = lastFrag.getStartPosition() + lastFrag.getLength();
				if( trueEnd > (res.getStartPosition() + res.getLength())) {
					res.setSourceRange(res.getStartPosition(), trueEnd - res.getStartPosition());
				}
			}
		}
		return Optional.of(res);
	}

	
	
	private Stream<IDocElement> convertInlineTag(DCTree javac) {
		ArrayList<IDocElement> collector = new ArrayList<>();
		TagElement res = this.ast.newTagElement();
		commonSettings(res, javac);
		collector.add(res);
		if (javac instanceof DCLiteral literal) {
			res.setTagName(switch (literal.getKind()) {
				case CODE -> TagElement.TAG_CODE;
				case LITERAL -> TagElement.TAG_LITERAL;
				default -> TagElement.TAG_LITERAL;
			});
			res.fragments().addAll(convertElement(literal.body).toList());
		} else if (javac instanceof DCLink link) {
			res.setTagName(switch (link.getKind()) {
				case LINK -> TagElement.TAG_LINK;
				case LINK_PLAIN -> TagElement.TAG_LINKPLAIN;
				default -> TagElement.TAG_LINK;
			});
			res.fragments().addAll(convertElement(link.ref).toList());
			link.label.stream().flatMap(this::convertElement).forEach(res.fragments()::add);
		} else if (javac instanceof DCValue dcv) {
			res.setTagName(TagElement.TAG_VALUE);
			res.fragments().addAll(convertElement(dcv.ref).toList());
		} else if (javac instanceof DCInheritDoc inheritDoc) {
			res.setTagName(TagElement.TAG_INHERITDOC);
		} else if (javac instanceof DCSnippet snippet) {
			res.setTagName(TagElement.TAG_SNIPPET);
			res.setProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID, true);
			res.fragments().addAll(splitLines(snippet.body, true)
					.map(this::toSnippetFragment)
					.toList());
		} else if (javac instanceof DCUnknownInlineTag unknown) {
			res.fragments().add(toDefaultTextElement(unknown));
		} else {
			return Stream.empty();
		}
		return collector.stream();
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
		String suggestedText = this.javacConverter.rawText.substring(line.startOffset, line.startOffset + line.length);
		String strippedLeading = suggestedText.stripLeading();
		int leadingWhitespace = suggestedText.length() - strippedLeading.length();
		res.setSourceRange(line.startOffset + leadingWhitespace, line.length - leadingWhitespace);
		res.setText(strippedLeading);
		return res;
	}
	
	private TextElement toTextElementPreserveWhitespace(Region line) {
		TextElement res = this.ast.newTextElement();	
		res.setSourceRange(line.startOffset, line.length);
		res.setText(line.getContents());
		return res;
	}

	private Stream<Region> splitLines(DCText text, boolean keepWhitespaces) {
		return splitLines(text.getBody(), text.getStartPosition(), text.getEndPosition(), keepWhitespaces);
	}
	
	private Stream<Region> splitLines(String body, int startPos, int endPos, boolean keepWhitespaces) {
		String[] bodySplit = body.split("\n");
		ArrayList<Region> regions = new ArrayList<>();
		int workingIndexWithinComment = startPos;
		for( int i = 0; i < bodySplit.length; i++ ) {
			int lineStart = this.docComment.getSourcePosition(workingIndexWithinComment);
			int lineEnd = this.docComment.getSourcePosition(workingIndexWithinComment + bodySplit[i].length());
			if (!keepWhitespaces) {
				String tmp = this.javacConverter.rawText.substring(lineStart, lineEnd);
				int leadingWhite = tmp.length() - tmp.stripLeading().length();
				Region r = new Region(lineStart + leadingWhite, lineEnd - lineStart - leadingWhite);
				regions.add(r);
			} else {
//				if (lineEnd < this.javacConverter.rawText.length() && this.javacConverter.rawText.charAt(lineEnd) == '\n') {
//					lineEnd++;
//				}
				regions.add(new Region(lineStart, lineEnd - lineStart));
			}
			workingIndexWithinComment += bodySplit[i].length() + 1;
		}
		return regions.stream();
	}

	private Stream<Region> splitLines(DCTree[] allPositions) {
		if( allPositions.length > 0 ) {
			int[] startPosition = { this.docComment.getSourcePosition(allPositions[0].getStartPosition()) };
			int lastNodeStart = this.docComment.getSourcePosition(allPositions[allPositions.length - 1].getStartPosition());
			int endPosition = this.docComment.getSourcePosition(allPositions[allPositions.length - 1].getEndPosition());
			if( allPositions[allPositions.length-1] instanceof DCText dct) {
				String lastText = dct.text;
				String lastTextFromSrc = this.javacConverter.rawText.substring(lastNodeStart, endPosition);
				if( !lastTextFromSrc.equals(lastText)) {
					// We need to fix this. There might be unicode in here
					String convertedText = Convert.escapeUnicode(lastText);
					if( convertedText.startsWith(lastTextFromSrc)) {
						endPosition = lastNodeStart + convertedText.length();
					}
				}
			}
			String sub = this.javacConverter.rawText.substring(startPosition[0], endPosition);
			String[] split = sub.split("(\r)?\n\\s*[*][ \t]*");
			List<Region> regions = new ArrayList<>();
			for( int i = 0; i < split.length; i++ ) {
				int index = this.javacConverter.rawText.indexOf(split[i], startPosition[0]);
				if (index >= 0) {
					regions.add(new Region(index, split[i].length()));
					startPosition[0] = index + split[i].length();
				}
			}
			return regions.stream();
		}
		return Stream.empty();
	}

	private IDocElement /* TextElement or TagElement for highlight/link... */ toSnippetFragment(Region region) {
		TextElement defaultElement = toTextElementPreserveWhitespace(region);
		if (!defaultElement.getText().endsWith("\n")) {
			defaultElement.setText(defaultElement.getText() + '\n');
		}
		String line = region.getContents();
		Matcher markedUpLine = JAVA_COMMENT.matcher(line);
		if (!markedUpLine.matches()) {
			return defaultElement;
		}
		int markupStart = markedUpLine.start("markup");
		String markup = line.substring(markupStart);
		MarkupParser markupParser = new MarkupParser(null);
		try {
			List<?> tags = markupParser.parse(markup);
			if (tags.isEmpty()) {
				return defaultElement;
			}
			TextElement initialTextElement = this.ast.newTextElement();
			initialTextElement.setSourceRange(region.startOffset, markupStart - 2 /* 2 is length of `//` */);
			initialTextElement.setText(line.substring(0, markupStart - 2) + '\n');
			IDocElement currentElement = initialTextElement;
			Class<? extends Object> tagClass = tags.getFirst().getClass();
			Field nameField = tagClass.getDeclaredField("name"); //$NON-NLS-1$
			nameField.setAccessible(true);
			Field attributesFields = tagClass.getDeclaredField("attributes"); //$NON-NLS-1$
			attributesFields.setAccessible(true);
			for (Object tag : tags) {
				String name = (String)nameField.get(tag);
				List<Attribute> attributes = (List<Attribute>)attributesFields.get(tag);
				TagElement newElement = this.ast.newTagElement();
				newElement.setSourceRange(region.startOffset, region.length);
				newElement.setTagName('@' + name);
				newElement.setProperty(TagProperty.TAG_PROPERTY_SNIPPET_INLINE_TAG_COUNT, 1); // TODO what?
				attributes.stream().map(this::toTagProperty).forEach(newElement.tagProperties()::add);
				newElement.fragments().add(currentElement);
				currentElement = newElement;
			}
			return currentElement;
		} catch (Exception ex) {
			ILog.get().error("While trying to build snippet line " + line + ": " + ex.getMessage(), ex);
		}
		return defaultElement;
	}
	private TagProperty toTagProperty(Attribute snippetMarkupAttribute) {
		TagProperty res = this.ast.newTagProperty();
		try {
			Field name = Attribute.class.getDeclaredField("name"); //$NON-NLS-1$
			name.setAccessible(true);
			res.setName((String)name.get(snippetMarkupAttribute));
			Field value = snippetMarkupAttribute.getClass().getDeclaredField("value"); //$NON-NLS-1$
			value.setAccessible(true);
			res.setStringValue((String)value.get(snippetMarkupAttribute));
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return res;
	}

	private Stream<IDocElement> convertElementGroup(DCTree[] javac) {
		return splitLines(javac).filter(x -> x.length != 0).flatMap(this::toTextOrTag);
	}
	private Stream<IDocElement> toTextOrTag(Region line) {
		String suggestedText = this.javacConverter.rawText.substring(line.startOffset, line.startOffset + line.length);
		TextElement postElement = null;
		if( suggestedText.startsWith("{@")) {
			int closeBracket = suggestedText.indexOf("}");
			int firstWhite = findFirstWhitespace(suggestedText);
			if( closeBracket > firstWhite && firstWhite != -1 ) {
				Region postRegion = new Region(line.startOffset + closeBracket + 1, line.length - closeBracket - 1);
				if( postRegion.length > 0 )
					postElement = toTextElement(postRegion);
				String tagName = suggestedText.substring(1, firstWhite).trim();
				TagElement res = this.ast.newTagElement();
				res.setTagName(tagName);
				res.fragments.add(toTextElementPreserveWhitespace(new Region(line.startOffset + firstWhite, closeBracket - firstWhite)));
				res.setSourceRange(line.startOffset, closeBracket + 1);
				if( postElement == null )
					return Stream.of(res);
				else
					return Stream.of(res, postElement);
			}
		} 
		
		return Stream.of(toTextElement(line));
	}
	
	private int findFirstWhitespace(String s) {
		int len = s.length();
		for (int index = 0; index < len; index++) {
		   if (Character.isWhitespace(s.charAt(index))) { 
		     return index;
		   }
		}
		return -1;
	}
	
	private List<IDocElement> convertElementCombiningNodes(List<DCTree> treeElements) {
		List<IDocElement> elements = new ArrayList<>();
		List<DCTree> combinable = new ArrayList<>();
		int size = treeElements.size();
		DCTree prev = null;
		for( int i = 0; i < size; i++ ) {
			boolean shouldCombine = false;
			boolean lineBreakBefore = false;
			DCTree oneTree = treeElements.get(i);
			if(oneTree instanceof DCText || oneTree instanceof DCStartElement || oneTree instanceof DCEndElement || oneTree instanceof DCEntity) {
				shouldCombine = true;
				if((oneTree instanceof DCText dct && dct.text.startsWith("\n"))
						|| (oneTree instanceof DCRawText raw && raw.getContent().endsWith("\n"))) {
					lineBreakBefore = true;
				}
			} else if( oneTree instanceof DCErroneous derror) {
				Stream<IDocElement> de = convertDCErroneousElement(derror);
				if( de == null ) {
					shouldCombine = true;
					if( derror.body.startsWith("{@")) {
						lineBreakBefore = true;
					}
				}
			}
			
			if( lineBreakBefore || !shouldCombine) {
				if( combinable.size() > 0 ) {
					elements.addAll(convertElementGroup(combinable.toArray(new DCTree[0])).toList());
					combinable.clear();
				}
			}
			
			if( shouldCombine ) {
				combinable.add(oneTree);
			} else {
				elements.addAll(convertElement(oneTree).toList());
			}
			prev = oneTree;
		}
		if( combinable.size() > 0 ) 
			elements.addAll(convertElementGroup(combinable.toArray(new DCTree[0])).toList());
		return elements;
	}
	private Stream<? extends IDocElement> convertElement(DCTree javac) {
		if (javac instanceof DCText text) {
			return splitLines(text, false).map(this::toTextElement);
		} else if (javac instanceof DCRawText rawText) {
			TextElement element = this.ast.newTextElement();
			commonSettings(element, javac);
			element.setText(rawText.getContent());
			return Stream.of(element);
		} else if (javac instanceof DCIdentifier identifier) {
			Name res = this.ast.newName(identifier.getName().toString());
			commonSettings(res, javac);
			return Stream.of(res);
		} else if (javac instanceof DCReference reference) {
			return convertReference(javac, reference);
		} else if (javac instanceof DCStartElement || javac instanceof DCEndElement || javac instanceof DCEntity) {
			return Stream.of(toDefaultTextElement(javac));
		} else if (javac instanceof DCBlockTag || javac instanceof DCReturn) {
			Optional<Stream<TagElement>> blockTag = convertBlockTag(javac).map(Stream::of);
			if (blockTag.isPresent()) {
				return blockTag.get();
			}
		} else if (javac instanceof DCErroneous erroneous) {
			Stream<IDocElement> docE = convertDCErroneousElement(erroneous);
			if( docE != null ) {
				return docE;
			}
			TextElement res = this.ast.newTextElement();
			commonSettings(res, erroneous);
			res.setText(erroneous.body);
			diagnostics.add(erroneous.diag);
			return Stream.of(res);
		} else if (javac instanceof DCComment comment) {
            TextElement res = this.ast.newTextElement();
            commonSettings(res, comment);
            res.setText(res.text);
            return Stream.of(res);
		} else {
			Stream<IDocElement> inlineTag = convertInlineTag(javac);
			return inlineTag;
		}
		var message = "💥🐛 Not supported yet conversion of " + javac.getClass().getSimpleName() + " to element";
		ILog.get().error(message);
		JavaDocTextElement res = this.ast.newJavaDocTextElement();
		commonSettings(res, javac);
		res.setText(this.docComment.comment.getText().substring(javac.getStartPosition(), javac.getEndPosition()) + System.lineSeparator() + message);
		return Stream.of(res);
	}

	private Stream<? extends IDocElement> convertReference(DCTree javac, DCReference reference) {
		String signature = reference.getSignature();
		if (reference.memberName == null) {
			if( reference.qualifierExpression != null ) {
				Name res = convertReferenceToNameOnly(reference);
				return Stream.of(res);
			}
		} else if (reference.memberName != null) {
			if (signature.charAt(signature.length() - 1) == ')') {
				return Stream.of(convertMemberReferenceWithParens(reference));
			} else {
				return Stream.of(convertReferenceToMemberRef(reference));
			}
		} 
		// just return it as text
		int startPosition = this.docComment.getSourcePosition(reference.getPreferredPosition());
		TextElement res = this.ast.newTextElement();
		res.setText(signature);
		res.setSourceRange(startPosition, reference.getEndPos() - reference.pos);
		return Stream.of(res);	
	}

	private IDocElement convertMemberReferenceWithParens(DCReference reference) {
		MethodRef res = this.ast.newMethodRef();
		commonSettings(res, reference);
		int currentOffset = this.docComment.getSourcePosition(reference.getStartPosition());
		
		// TODO missing module reference
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
		return res;
	}

	private IDocElement convertReferenceToMemberRef(DCReference reference) {
		MemberRef res = this.ast.newMemberRef();
		commonSettings(res, reference);
		if (this.contextTreePath != null) {
			this.converted.put(res, DocTreePath.getPath(this.contextTreePath, this.docComment, reference));
		}

		int startPos = this.docComment.getSourcePosition(reference.pos);
		int memberNameStart = startPos;

		Name qualifier = convertReferenceToNameOnly(reference);
		if( qualifier != null ) {
			res.setQualifier(qualifier);
			memberNameStart = qualifier.getStartPosition() + qualifier.getLength() + 1;
		}
		if( reference.memberName != null ) {
			SimpleName name = this.ast.newSimpleName(reference.memberName.toString());
			name.setSourceRange(memberNameStart, Math.max(0, reference.memberName.toString().length()));
			res.setName(name);
		}
		return res;
	}
	
	private Name convertReferenceToNameOnly(DCReference reference) {
		int startPos = this.docComment.getSourcePosition(reference.getStartPosition());
		if (reference.qualifierExpression != null) {
			int moduleQualifierLen = 0;
			if( reference.moduleName != null) {
				moduleQualifierLen = this.javacConverter.commonSettingsGetLength(null, reference.moduleName) + 1;
			}
			Name qualifierExpressionName = toName(reference.qualifierExpression, startPos + moduleQualifierLen);
			int len = Math.max(0, reference.qualifierExpression.toString().length());
			qualifierExpressionName.setSourceRange(startPos + moduleQualifierLen, len);
			if( reference.moduleName == null ) {
				return qualifierExpressionName;
			} else {
				ModuleQualifiedName mqn = new ModuleQualifiedName(this.ast);
				Name moduleName = toName(reference.moduleName, startPos);
				moduleName.setSourceRange(startPos, moduleQualifierLen);
				mqn.setModuleQualifier(moduleName);
				mqn.setName(qualifierExpressionName);
				mqn.setSourceRange(startPos, qualifierExpressionName.getStartPosition() + qualifierExpressionName.getLength() - startPos);
				return mqn;
			}
		}
		return null;
	}

	// Return a stream, or null if empty
	private Stream<IDocElement> convertDCErroneousElement(DCErroneous erroneous) {
		String body = erroneous.body;
		MethodRef match = null;
		try {
			match = matchesMethodReference(erroneous, body);
		} catch(Exception e) {
			// ignore
		}
		int start = this.docComment.getSourcePosition(erroneous.getStartPosition());
		int endInd = erroneous.getEndPosition();
		int endPosition = this.docComment.getSourcePosition(endInd);
		if( match != null) {
			TagElement res = this.ast.newTagElement();
			res.setTagName(TagElement.TAG_SEE);
			res.fragments.add(match);
			res.setSourceRange(start, endPosition - start);
			return Stream.of(res);
		} else if( body.startsWith("@")) {
			TagElement res = this.ast.newTagElement();
			String tagName = body.split("\\s+")[0];
			res.setTagName(tagName);
			int newStart = erroneous.getStartPosition() + tagName.length();
			List<TextElement> l = splitLines(body.substring(tagName.length()), newStart, endInd, false).map(x -> toTextElement(x)).toList();
			res.fragments.addAll(l);
			TextElement lastFragment = l.size() == 0 ? null : l.get(l.size() - 1);
			int newEnd = lastFragment == null ? tagName.length() : (lastFragment.getStartPosition() + lastFragment.getLength());
			res.setSourceRange(start, endPosition - start);
			return Stream.of(res);
//		} else if( body.startsWith("{@")) {
//			return convertElementGroup(new DCTree[] {erroneous});
		}
		return null;
	}
	
	private MethodRef matchesMethodReference(DCErroneous tree, String body) {
		if( body.startsWith("@see")) {
			String value = body.substring(4);
			int hash = value.indexOf("#");
			if( hash != -1 ) {
				int startPosition = this.docComment.getSourcePosition(tree.getStartPosition()) + 4;
				String prefix = value.substring(0, hash);
				int link = prefix.indexOf("@link");
				if (link != -1) {
					prefix = prefix.substring(link + 5);
					startPosition = startPosition + link + 5;
				}
				MethodRef ref = this.ast.newMethodRef();
				if( prefix != null && !prefix.isBlank()) {
					Name n = toName(prefix, startPosition);
					ref.setQualifier(n);
				}
				String suffix = hash+1 > value.length() ? "" : value.substring(hash+1);
				if( suffix.indexOf("(") != -1 ) {
					String qualifiedMethod = suffix.substring(0, suffix.indexOf("("));
					int methodNameStart = qualifiedMethod.lastIndexOf(".") + 1;
					String methodName = qualifiedMethod.substring(methodNameStart);
					SimpleName sn = (SimpleName)toName(methodName, startPosition + prefix.length() + 1 + methodNameStart);
					ref.setName(sn);
					commonSettings(ref, tree);
					diagnostics.add(tree.diag);
					return ref;
				}
			}
		}
		return null;
	}
	private Name toName(String val, int startPosition) {
		return JavacConverter.toName(val, startPosition, this.ast);
	}
	

	private TextElement toDefaultTextElement(DCTree javac) {
		TextElement res = this.ast.newTextElement();
		commonSettings(res, javac);
		String r = this.docComment.comment.getText();
		String s1 = r.substring(javac.getStartPosition(), javac.getEndPosition());
		res.setText(s1);
		return res;
	}

	private MethodRefParameter toMethodRefParam(JCTree type, Region range, int paramListOffset) {
		MethodRefParameter res = this.ast.newMethodRefParameter();
		res.setSourceRange(
				range != null ? range.startOffset : paramListOffset + type.getStartPosition(),
				range != null ? range.length : type.toString().length());
		// Temporary replace relative positons for types in javadoc by absolute of convertToType is bogus
		new TreeScanner() {
			@Override
			public void scan(JCTree tree) {
				tree.setPos(tree.pos + paramListOffset);
				super.scan(tree);
			}
		}.scan(type);

		String[] segments = Stream.of(range.getContents().split("\n"))
				.map(t -> {
					Matcher m = BEGIN_CHOPPER.matcher(t);
					if (m.find()) {
						return m.group(1);
					}
					return t;
				})
				.map(String::trim)
				.flatMap(t -> Stream.of(t.split("\s")))
				.filter(t -> !t.isEmpty())
				.toArray(String[]::new);
		
		Type jdtType = null;
		if( segments.length > 0 && segments[segments.length-1].endsWith("...")) {
			res.setVarargs(true);
			if( type instanceof JCArrayTypeTree att) {
				jdtType = this.javacConverter.convertToType(att.getType());
			}
		} 
		if( jdtType == null ) {
			jdtType = this.javacConverter.convertToType(type);
		}
		res.setType(jdtType);
		// revert position types to origin as some nodes might be shared and reused
		new TreeScanner() {
			@Override
			public void scan(JCTree tree) {
				tree.setPos(tree.pos - paramListOffset);
				super.scan(tree);
			}
		}.scan(type);

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
