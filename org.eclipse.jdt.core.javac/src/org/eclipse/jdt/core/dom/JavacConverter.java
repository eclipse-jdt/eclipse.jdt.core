/*******************************************************************************
 * Copyright (c) 2023, 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import static com.sun.tools.javac.code.Flags.VARARGS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javax.lang.model.type.TypeKind;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ModuleModifier.ModuleModifierKeyword;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;

import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.source.tree.ModuleTree.ModuleKind;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.tools.javac.tree.DCTree.DCDocComment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotatedType;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAnyPattern;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBindingPattern;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCaseLabel;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCConstantCaseLabel;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDefaultCaseLabel;
import com.sun.tools.javac.tree.JCTree.JCDirective;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExports;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCModuleDecl;
import com.sun.tools.javac.tree.JCTree.JCModuleImport;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCOpens;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPattern;
import com.sun.tools.javac.tree.JCTree.JCPatternCaseLabel;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCProvides;
import com.sun.tools.javac.tree.JCTree.JCRecordPattern;
import com.sun.tools.javac.tree.JCTree.JCRequires;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCSkip;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSwitchExpression;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeIntersection;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCTypeUnion;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCUses;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.JCYield;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import com.sun.tools.javac.util.Position.LineMap;

/**
 * Deals with conversion of Javac domain into JDT DOM domain
 * @implNote Cannot move to another package as it uses some package protected methods
 */
@SuppressWarnings("unchecked")
class JavacConverter {

	private static final String ERROR = "<error>";
	private static final String FAKE_IDENTIFIER = new String(RecoveryScanner.FAKE_IDENTIFIER);
	public final AST ast;
	final JCCompilationUnit javacCompilationUnit;
	private final Context context;
	final Map<ASTNode, JCTree> domToJavac = new HashMap<>();
	final String rawText;
	final Set<JCDiagnostic> javadocDiagnostics = new HashSet<>();
	private final List<JavadocConverter> javadocConverters = new ArrayList<>();
	final List<org.eclipse.jdt.core.dom.Comment> notAttachedComments = new ArrayList<>();
	private boolean buildJavadoc;
	private int focalPoint;

	private JavacConverter(AST ast, JCCompilationUnit javacCompilationUnit, Context context, String rawText, boolean buildJavadoc) {
		this.ast = ast;
		this.javacCompilationUnit = javacCompilationUnit;
		this.context = context;
		this.rawText = rawText;
		this.buildJavadoc = buildJavadoc;
		this.focalPoint = -1;
	}
	public JavacConverter(AST ast, JCCompilationUnit javacCompilationUnit, 
			Context context, String rawText, boolean buildJavadoc, int focalPoint) {
		this(ast, javacCompilationUnit, context, rawText, buildJavadoc);
		this.focalPoint = focalPoint;
	}

	CompilationUnit convertCompilationUnit() {
		return convertCompilationUnit(this.javacCompilationUnit);
	}

	CompilationUnit convertCompilationUnit(JCCompilationUnit javacCompilationUnit) {
		CompilationUnit res = this.ast.newCompilationUnit();
		populateCompilationUnit(res, javacCompilationUnit);
		return res;
	}

	void populateCompilationUnit(CompilationUnit res, JCCompilationUnit javacCompilationUnit) {
		commonSettings(res, javacCompilationUnit);
		res.setSourceRange(0, this.rawText.length());
		res.setLineEndTable(toLineEndPosTable(javacCompilationUnit.getLineMap(), res.getLength()));
		if (javacCompilationUnit.getPackage() != null) {
			res.setPackage(convert(javacCompilationUnit.getPackage()));
		} else if( javacCompilationUnit.defs != null && javacCompilationUnit.defs.size() > 0 && javacCompilationUnit.defs.get(0) instanceof JCErroneous jcer) {
			PackageDeclaration possible = convertMalformedPackageDeclaration(jcer);
			if( possible != null ) {
				res.setPackage(possible);
			}
		}
		if (javacCompilationUnit.getModule() != null) {
			res.setModule(convert(javacCompilationUnit.getModuleDecl()));
		}
		javacCompilationUnit.getImports().stream().filter(imp -> imp instanceof JCImport).map(jc -> convert((JCImport)jc)).forEach(res.imports()::add);
		if (this.ast.apiLevel >= AST.JLS23_INTERNAL) {
			javacCompilationUnit.getImports().stream().filter(imp -> imp instanceof JCModuleImport).map(jc -> convert((JCModuleImport)jc)).forEach(res.imports()::add);
		}
		javacCompilationUnit.getTypeDecls().stream()
			.map(n -> convertBodyDeclaration(n, res))
			.filter(Objects::nonNull)
			.forEach(res.types()::add);
		res.accept(new FixPositions());
	}

    private PackageDeclaration convertMalformedPackageDeclaration(JCErroneous jcer) {
    	if( jcer.errs != null && jcer.errs.size() > 0 && jcer.errs.get(0) instanceof JCModifiers) {
    		// Legitimate chance this is a misplaced modifier, private package, etc
    		int errEndPos = jcer.getEndPosition(this.javacCompilationUnit.endPositions);
    		String possiblePackageDecl = this.rawText.length() > (errEndPos + 7) ? this.rawText.substring(errEndPos, errEndPos + 7) : null;
    		if( "package".equals(possiblePackageDecl)) {
    			int newLine = this.rawText.indexOf("\n", errEndPos);
    			String decl = null;
    			if( newLine != -1 ) {
    				decl = this.rawText.substring(errEndPos, newLine).trim();
    			} else {
    				decl = this.rawText.substring(errEndPos);
    			}
    			String pkgName = decl.substring(7).trim();
    			if( pkgName.endsWith(";")) {
    				pkgName = pkgName.substring(0,pkgName.length()-1);
    			}
    			PackageDeclaration res = this.ast.newPackageDeclaration();
    			res.setName(toName(pkgName, 0, this.ast));
    			setJavadocForNode(jcer, res);
				res.setSourceRange(errEndPos, Math.max(0, pkgName.length()));
    			res.setFlags(res.getFlags() | ASTNode.MALFORMED);
    			return res;
    		}
    	}
		return null;
	}

	private int[] toLineEndPosTable(LineMap lineMap, int fileLength) {
		List<Integer> lineEnds = new ArrayList<>();
		int line = 1;
		try {
			do {
				lineEnds.add(lineMap.getStartPosition(line + 1) - 1);
				line++;
			} while (true);
		} catch (ArrayIndexOutOfBoundsException ex) {
			// expected
		}
		lineEnds.add(fileLength - 1);
		return lineEnds.stream().mapToInt(Integer::intValue).toArray();
	}

	private PackageDeclaration convert(JCPackageDecl javac) {
		PackageDeclaration res = this.ast.newPackageDeclaration();
		res.setName(toName(javac.getPackageName()));
		commonSettings(res, javac);
		Iterator<JCAnnotation> it = javac.annotations.iterator();
		while(it.hasNext()) {
			res.annotations().add(convert(it.next()));
		}
		String raw = this.rawText.substring(res.getStartPosition(), res.getStartPosition() + res.getLength());
		if( !raw.trim().endsWith(";")) {
			res.setFlags(res.getFlags() | ASTNode.MALFORMED);
		}
		return res;
	}

	private ModuleDeclaration convert(JCModuleDecl javac) {
		ModuleDeclaration res = this.ast.newModuleDeclaration();
		res.setName(toName(javac.getName()));
		this.domToJavac.put(res.getName(), javac);
		boolean isOpen = javac.getModuleType() == ModuleKind.OPEN;
		res.setOpen(isOpen);
		if (javac.getDirectives() != null) {
			List<JCDirective> directives = javac.getDirectives();
			for (int i = 0; i < directives.size(); i++) {
				JCDirective jcDirective = directives.get(i);
				res.moduleStatements().add(convert(jcDirective));
			}
		}
		commonSettings(res, javac);
		if( isOpen ) {
			int start = res.getStartPosition();
			if( !this.rawText.substring(start).trim().startsWith("open")) {
				// we are open but we don't start with open... so... gotta look backwards
				String prefix =  this.rawText.substring(0,start);
				if( prefix.trim().endsWith("open")) {
					// previous token is open
					int ind = new StringBuffer().append(prefix).reverse().toString().indexOf("nepo");
					if( ind != -1 ) {
						int gap = ind + 4;
						res.setSourceRange(res.getStartPosition() - gap, res.getLength() + gap);
					}
				}
			}
		}
		List<IExtendedModifier> l = convertModifierAnnotations(javac.mods, res);
		res.annotations().addAll(l);
		return res;
	}

	private ModuleDirective convert(JCDirective javac) {
		return switch (javac.getKind()) {
		case EXPORTS -> convert((JCExports)javac);
		case OPENS -> convert((JCOpens)javac);
		case PROVIDES -> convert((JCProvides)javac);
		case REQUIRES -> convert((JCRequires)javac);
		case USES -> convert((JCUses)javac);
		default -> throw new IllegalStateException();
		};
	}

	private ExportsDirective convert(JCExports javac) {
		ExportsDirective res = this.ast.newExportsStatement();
		res.setName(toName(javac.getPackageName()));
		commonSettings(res, javac);
		List<JCExpression> mods = javac.getModuleNames();
		if (mods != null) {
			Iterator<JCExpression> it = mods.iterator();
			while(it.hasNext()) {
				JCExpression jcpe = it.next();
				Expression e = convertExpression(jcpe);
				if( e != null )
					res.modules().add(e);
			}
		}
		return res;
	}

	private OpensDirective convert(JCOpens javac) {
		OpensDirective res = this.ast.newOpensDirective();
		res.setName(toName(javac.getPackageName()));
		commonSettings(res, javac);
		List<JCExpression> mods = javac.getModuleNames();
		if (mods != null) {
			Iterator<JCExpression> it = mods.iterator();
			while (it.hasNext()) {
				JCExpression jcpe = it.next();
				Expression e = convertExpression(jcpe);
				if (e != null)
					res.modules().add(e);
			}
		}
		return res;
	}

	private ProvidesDirective convert(JCProvides javac) {
		ProvidesDirective res = this.ast.newProvidesDirective();
		res.setName(toName(javac.getServiceName()));
		for (var jcName : javac.implNames) {
			res.implementations().add(toName(jcName));
		}
		commonSettings(res, javac);
		return res;
	}

	private RequiresDirective convert(JCRequires javac) {
		RequiresDirective res = this.ast.newRequiresDirective();
		res.setName(toName(javac.getModuleName()));
		int javacStart = javac.getStartPosition();
		List<ASTNode> modifiersToAdd = new ArrayList<>();
		if (javac.isTransitive()) {
			ModuleModifier trans = this.ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
			int transStart = this.rawText.substring(javacStart).indexOf(ModuleModifierKeyword.TRANSITIVE_KEYWORD.toString());
			if( transStart != -1 ) {
				int trueStart = javacStart + transStart;
				trans.setSourceRange(trueStart, ModuleModifierKeyword.TRANSITIVE_KEYWORD.toString().length());
			}
			modifiersToAdd.add(trans);
		}
		if (javac.isStatic()) {
			ModuleModifier stat = this.ast.newModuleModifier(ModuleModifierKeyword.STATIC_KEYWORD);
			int statStart = this.rawText.substring(javacStart).indexOf(ModuleModifierKeyword.STATIC_KEYWORD.toString());
			if( statStart != -1 ) {
				int trueStart = javacStart + statStart;
				stat.setSourceRange(trueStart, ModuleModifierKeyword.STATIC_KEYWORD.toString().length());
			}
			modifiersToAdd.add(stat);
		}
		modifiersToAdd.sort((a, b) -> a.getStartPosition() - b.getStartPosition());
		modifiersToAdd.stream().forEach(res.modifiers()::add);
		commonSettings(res, javac);
		return res;
	}

	private UsesDirective convert(JCUses javac) {
		UsesDirective res = this.ast.newUsesDirective();
		res.setName(toName(javac.getServiceName()));
		commonSettings(res, javac);
		return res;
	}

	private ImportDeclaration convert(JCImport javac) {
		ImportDeclaration res = this.ast.newImportDeclaration();
		commonSettings(res, javac);
		if (javac.isStatic()) {
			if (this.ast.apiLevel != AST.JLS2_INTERNAL) {
				res.setStatic(true);
			}
		}
		var select = javac.getQualifiedIdentifier();
		if (select.getIdentifier().contentEquals("*")) {
			res.setOnDemand(true);
			res.setName(toName(select.getExpression()));
		} else if (this.ast.apiLevel >= AST.JLS23_INTERNAL && select.selected.toString().equals("module") && select.name.toString().equals("<error>")) {
			// it's a broken module import
			var moduleModifier = this.ast.newModifier(ModifierKeyword.MODULE_KEYWORD);
			res.modifiers().add(moduleModifier);
			Name name = new SimpleName(this.ast);
			name.setSourceRange(res.getStartPosition() + res.getLength() + 1, 0);
			res.setName(name);
			res.setSourceRange(res.getStartPosition(), res.getLength() + 1);
		} else {
			res.setName(toName(select));
		}
		if (javac.isStatic() || javac.isModule()) {
			if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
				res.setFlags(res.getFlags() | ASTNode.MALFORMED);
			} else if (this.ast.apiLevel < AST.JLS23_INTERNAL) {
				if (!javac.isStatic()) {
					res.setFlags(res.getFlags() | ASTNode.MALFORMED);
				}
			} else {
				ModifierKeyword keyword = null;
				if (javac.isStatic()) {
					keyword = ModifierKeyword.STATIC_KEYWORD;
				}
				if (javac.isModule()) {
					keyword = ModifierKeyword.MODULE_KEYWORD;
				}
				if (keyword != null) {
					Modifier newModifier = this.ast.newModifier(keyword);
					newModifier.setSourceRange(javac.getStartPosition(), keyword.toString().length());
					res.modifiers().add(newModifier);
				} else {
					res.setFlags(res.getFlags() | ASTNode.MALFORMED);
				}
			}
		}
		return res;
	}
	
	private ImportDeclaration convert(JCModuleImport javac) {
		ImportDeclaration res = this.ast.newImportDeclaration();
		commonSettings(res, javac);
		var moduleModifier = this.ast.newModifier(ModifierKeyword.MODULE_KEYWORD);
		res.modifiers().add(moduleModifier);
		if (javac.isStatic()) {
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				res.setStatic(true);
			}
		}
		var select = javac.getQualifiedIdentifier();
		res.setName(toName(select));
		return res;
	}

	void commonSettings(ASTNode res, JCTree javac) {
		if( javac != null ) {
			int length = commonSettingsGetLength(res, javac);
			commonSettings(res, javac, length, true);
		}
	}
	
	int commonSettingsGetLength(ASTNode res, JCTree javac) {
		int length = -1;
		if( javac != null ) {
			int start = javac.getStartPosition();
			if (start >= 0) {
				int endPos = javac.getEndPosition(this.javacCompilationUnit.endPositions);
				if( endPos < 0 ) {
					endPos = start + javac.toString().length();
				}
				// workaround: some types appear to not keep the trailing semicolon in source range
				if (res instanceof Name || res instanceof FieldAccess || res instanceof SuperFieldAccess ) {
					while (endPos > start && this.rawText.length()>= endPos && this.rawText.charAt(endPos - 1) == ';') {
						endPos--;
					}
				}
				length = endPos - start;
				if (start + Math.max(0, length) > this.rawText.length()) {
					length = this.rawText.length() - start;
				}
			}
			return Math.max(0, length);
		}
		return length;
	}

	void commonSettings(ASTNode res, JCTree javac, int length, boolean removeWhitespace) {
		if( javac != null && length >= 0) {
			if (javac.getStartPosition() >= 0) {
				res.setSourceRange(javac.getStartPosition(), Math.max(0, length));
			}
			if( removeWhitespace ) {
				removeSurroundingWhitespaceFromRange(res);
			}
			this.domToJavac.put(res, javac);
			setJavadocForNode(javac, res);
		}
	}

	private void nameSettings(SimpleName name, JCMethodDecl javac, String selector, boolean isConstructor) {
		if ((selector.equals(ERROR) || selector.equals(FAKE_IDENTIFIER)))
			return;
		var start = javac.getPreferredPosition();
		if (start > -1) {
			// handle constructor length using type name instead of selector.
			var length = isConstructor ? name.toString().length() : selector.length();
			name.setSourceRange(start, length);
		}
	}

	private void nameSettings(SimpleName name, JCVariableDecl javac, String varName) {
		if (varName.equals(ERROR) || varName.equals(FAKE_IDENTIFIER))
			return;
		var start = javac.getPreferredPosition();
		if (start > -1) {
			name.setSourceRange(start, varName.length());
		}
	}

	private Name toName(JCTree expression) {
		return toName(expression, null);
	}
	
	Name toName(JCTree expression, BiConsumer<ASTNode, JCTree> extraSettings ) {
		if (expression instanceof JCIdent ident) {
			Name res = convertName(ident.getName());
			commonSettings(res, expression);
			if( extraSettings != null ) 
				extraSettings.accept(res, ident);
			return res;
		}
		if (expression instanceof JCFieldAccess fieldAccess) {
			JCExpression faExpression = fieldAccess.getExpression();
			SimpleName n = (SimpleName)convertName(fieldAccess.getIdentifier());
			if (n == null) {
				n = this.ast.newSimpleName(FAKE_IDENTIFIER);
				n.setFlags(ASTNode.RECOVERED);
			}
			commonSettings(n, fieldAccess);

			Name qualifier = toName(faExpression, extraSettings);
			QualifiedName res = this.ast.newQualifiedName(qualifier, n);
			commonSettings(res, fieldAccess);
			if( extraSettings != null ) 
				extraSettings.accept(res, fieldAccess);
			// don't calculate source range if the identifier is not valid.
			if (!fieldAccess.getIdentifier().contentEquals(FAKE_IDENTIFIER)
					&& !fieldAccess.getIdentifier().contentEquals(ERROR)) {
				// fix name position according to qualifier position
				int nameIndex = this.rawText.indexOf(fieldAccess.getIdentifier().toString(),
						qualifier.getStartPosition() + qualifier.getLength());
				if (nameIndex >= 0) {
					n.setSourceRange(nameIndex, fieldAccess.getIdentifier().toString().length());
				}
			}
			return res;
		}
		if (expression instanceof JCAnnotatedType jcat) {
			Name n = toName(jcat.underlyingType, extraSettings);
			commonSettings(n, jcat.underlyingType);
			return n;
		}
		if (expression instanceof JCTypeApply jcta) {
			Name n = toName(jcta.clazz, extraSettings);
			commonSettings(n, jcta.clazz);
			return n;
		}
		throw new UnsupportedOperationException("toName for " + expression + " (" + expression == null ? "null" : expression.getClass().getName() + ")");
	}

	private AbstractTypeDeclaration convertClassDecl(JCClassDecl javacClassDecl, ASTNode parent) {
		if( javacClassDecl.getKind() == Kind.ANNOTATION_TYPE &&
				(this.ast.apiLevel <= AST.JLS2_INTERNAL || this.ast.scanner.complianceLevel < ClassFileConstants.JDK1_5)) {
			return null;
		}
		if( javacClassDecl.getKind() == Kind.ENUM &&
				(this.ast.apiLevel <= AST.JLS2_INTERNAL || this.ast.scanner.complianceLevel < ClassFileConstants.JDK1_5)) {
			return null;
		}
		if( javacClassDecl.getKind() == Kind.RECORD &&
				(this.ast.apiLevel < AST.JLS16_INTERNAL || this.ast.scanner.complianceLevel < ClassFileConstants.JDK16)) {
			return null;
		}

		AbstractTypeDeclaration	res = switch (javacClassDecl.getKind()) {
			case ANNOTATION_TYPE -> this.ast.newAnnotationTypeDeclaration();
			case ENUM -> this.ast.newEnumDeclaration();
			case RECORD -> this.ast.newRecordDeclaration();
			case INTERFACE -> {
				TypeDeclaration decl = this.ast.newTypeDeclaration();
				decl.setInterface(true);
				yield decl;
			}
			case CLASS -> javacClassDecl.getModifiers() != null && (javacClassDecl.getModifiers().flags & Flags.IMPLICIT_CLASS) != 0  ?
					new ImplicitTypeDeclaration(this.ast) :
					this.ast.newTypeDeclaration();
			default -> throw new IllegalStateException();
		};
		return convertClassDecl(javacClassDecl, parent, res);
	}

	private AbstractTypeDeclaration convertClassDecl(JCClassDecl javacClassDecl, ASTNode parent, AbstractTypeDeclaration res) {
		commonSettings(res, javacClassDecl);
		SimpleName simpName = (SimpleName)convertName(javacClassDecl.getSimpleName());
		if(!(res instanceof ImplicitTypeDeclaration) && simpName != null) {
			res.setName(simpName);
			int searchNameFrom = javacClassDecl.getPreferredPosition();
			if (javacClassDecl.getModifiers() != null) {
				searchNameFrom = Math.max(searchNameFrom, TreeInfo.getEndPos(javacClassDecl.getModifiers(), this.javacCompilationUnit.endPositions));
			}
			int namePosition = this.rawText.indexOf(simpName.getIdentifier(), searchNameFrom);
			if (namePosition >= 0) {
				simpName.setSourceRange(namePosition, simpName.getIdentifier().length());
			}
		}
		if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
			res.modifiers().addAll(convert(javacClassDecl.mods, res));
		} else {
			int jls2Flags = getJLS2ModifiersFlags(javacClassDecl.mods);
			jls2Flags &= ~Flags.INTERFACE; // remove AccInterface flags, see ASTConverter
			res.internalSetModifiers(jls2Flags);
		}
		if (res instanceof TypeDeclaration typeDeclaration) {
			if (javacClassDecl.getExtendsClause() != null) {
				if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
					typeDeclaration.setSuperclassType(convertToType(javacClassDecl.getExtendsClause()));
				} else {
					JCExpression e = javacClassDecl.getExtendsClause();
					Name m = toName(e);
					if( m != null ) {
						typeDeclaration.setSuperclass(m);
					}
				}
			}
			if (javacClassDecl.getImplementsClause() != null) {
				if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
					javacClassDecl.getImplementsClause().stream()
						.map(this::convertToType)
						.filter(Objects::nonNull)
						.forEach(typeDeclaration.superInterfaceTypes()::add);
				} else {
					Iterator<JCExpression> it = javacClassDecl.getImplementsClause().iterator();
					while(it.hasNext()) {
						JCExpression next = it.next();
						Name m = toName(next);
						if( m != null ) {
							typeDeclaration.superInterfaces().add(m);
						}
					}
				}
			}

			if( javacClassDecl.getTypeParameters() != null ) {
				if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
					Iterator<JCTypeParameter> i = javacClassDecl.getTypeParameters().iterator();
					while(i.hasNext()) {
						JCTypeParameter next = i.next();
						typeDeclaration.typeParameters().add(convert(next));
					}
				}
			}

			if (javacClassDecl.getPermitsClause() != null) {
				if( this.ast.apiLevel >= AST.JLS17_INTERNAL) {
					javacClassDecl.getPermitsClause().stream()
						.map(this::convertToType)
						.filter(Objects::nonNull)
						.forEach(typeDeclaration.permittedTypes()::add);
					if (!javacClassDecl.getPermitsClause().isEmpty()) {
						int permitsOffset = this.rawText.substring(javacClassDecl.pos).indexOf("permits") + javacClassDecl.pos;
						typeDeclaration.setRestrictedIdentifierStartPosition(permitsOffset);
					}
				}
			}
			if (javacClassDecl.getMembers() != null) {
				List<JCTree> members = javacClassDecl.getMembers();
				ASTNode previous = null;
				for( int i = 0; i < members.size(); i++ ) {
					ASTNode decl = convertBodyDeclaration(members.get(i), res);
					if( decl != null ) {
						typeDeclaration.bodyDeclarations().add(decl);
						if (previous != null) {
							int istart = decl.getStartPosition();
							int siblingEnds = previous.getStartPosition() + previous.getLength();
							if(previous.getStartPosition() >= 0 && siblingEnds > istart && istart > previous.getStartPosition()) {
								previous.setSourceRange(previous.getStartPosition(), istart - previous.getStartPosition()-1);
							}
						}
						previous = decl;
					}
				}
			}
		} else if (res instanceof EnumDeclaration enumDecl) {
			List<EnumConstantDeclaration> enumStatements= enumDecl.enumConstants();
			if (javacClassDecl.getMembers() != null) {
				for(JCTree member : javacClassDecl.getMembers()) {
					EnumConstantDeclaration dec1 = convertEnumConstantDeclaration(member, parent, enumDecl);
					if( dec1 != null ) {
						enumStatements.add(dec1);
					} else {
						// body declaration
						ASTNode bodyDecl = convertBodyDeclaration(member, res);
						if( bodyDecl != null ) {
							res.bodyDeclarations().add(bodyDecl);
						}
					}
				}
			}
		} else if (res instanceof AnnotationTypeDeclaration annotDecl) {
			//setModifiers(annotationTypeMemberDeclaration2, annotationTypeMemberDeclaration);
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(annotDecl.typeName.toString()));
			res.setName(name);
			if( javacClassDecl.defs != null ) {
				for( Iterator<JCTree> i = javacClassDecl.defs.iterator(); i.hasNext(); ) {
					ASTNode converted = convertBodyDeclaration(i.next(), res);
					if( converted != null ) {
						res.bodyDeclarations.add(converted);
					}
				}
			}

//			org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference = annotDecl.get
//			if (typeReference != null) {
//				Type returnType = convertType(typeReference);
//				setTypeForMethodDeclaration(annotationTypeMemberDeclaration2, returnType, 0);
//			}
//			int declarationSourceStart = annotationTypeMemberDeclaration.declarationSourceStart;
//			int declarationSourceEnd = annotationTypeMemberDeclaration.bodyEnd;
//			annotationTypeMemberDeclaration2.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
//			// The javadoc comment is now got from list store in compilation unit declaration
//			convert(annotationTypeMemberDeclaration.javadoc, annotationTypeMemberDeclaration2);
//			org.eclipse.jdt.internal.compiler.ast.Expression memberValue = annotationTypeMemberDeclaration.defaultValue;
//			if (memberValue != null) {
//				annotationTypeMemberDeclaration2.setDefault(convert(memberValue));
//			}

		} else if (res instanceof RecordDeclaration recordDecl) {
			int start = javacClassDecl.getPreferredPosition();
			if( start != -1 ) {
				recordDecl.setRestrictedIdentifierStartPosition(start);
			}
			for (JCTree node : javacClassDecl.getMembers()) {
				if (node instanceof JCVariableDecl vd && !vd.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.STATIC)) {
					SingleVariableDeclaration vdd = (SingleVariableDeclaration)convertVariableDeclaration(vd);
					// Records cannot have modifiers
					vdd.modifiers().clear();
					// Add only annotation modifiers
					vdd.modifiers().addAll(convertModifierAnnotations(vd.getModifiers(), vdd));
					recordDecl.recordComponents().add(vdd);
				} else {
					ASTNode converted = convertBodyDeclaration(node, res);
					if( converted != null ) {
						res.bodyDeclarations.add(converted);
					}
				}
			}
		} else if (res instanceof ImplicitTypeDeclaration) {
			javacClassDecl.getMembers().stream()
				.map(member -> convertBodyDeclaration(member, res))
				.filter(Objects::nonNull)
				.forEach(res.bodyDeclarations()::add);
		}
		return res;
	}

	private TypeParameter convert(JCTypeParameter typeParameter) {
		final TypeParameter ret = new TypeParameter(this.ast);
		commonSettings(ret, typeParameter);
		final SimpleName simpleName = new SimpleName(this.ast);
		simpleName.internalSetIdentifier(typeParameter.getName().toString());
		int start = typeParameter.pos;
		int end = typeParameter.pos + typeParameter.getName().length();
		simpleName.setSourceRange(start, end - start);
		ret.setName(simpleName);
		List<JCExpression> bounds = typeParameter.getBounds();
		Iterator<JCExpression> i = bounds.iterator();
		while(i.hasNext()) {
			JCTree t = i.next();
			Type type = convertToType(t);
			ret.typeBounds().add(type);
			end = typeParameter.getEndPosition(this.javacCompilationUnit.endPositions);
		}
		if (typeParameter.getAnnotations() != null && this.ast.apiLevel() >= AST.JLS8_INTERNAL) {
			typeParameter.getAnnotations().stream()
				.map(this::convert)
				.forEach(ret.modifiers()::add);
		}
//		org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = typeParameter.annotations;
//		if (annotations != null) {
//			if (annotations[0] != null)
//				annotationsStart = annotations[0].sourceStart;
//			annotateTypeParameter(typeParameter2, typeParameter.annotations);
//		}
//		final TypeReference superType = typeParameter.type;
//		end = typeParameter.declarationSourceEnd;
//		if (superType != null) {
//			Type type = convertType(superType);
//			typeParameter2.typeBounds().add(type);
//			end = type.getStartPosition() + type.getLength() - 1;
//		}
//		TypeReference[] bounds = typeParameter.bounds;
//		if (bounds != null) {
//			Type type = null;
//			for (int index = 0, length = bounds.length; index < length; index++) {
//				type = convertType(bounds[index]);
//				typeParameter2.typeBounds().add(type);
//				end = type.getStartPosition() + type.getLength() - 1;
//			}
//		}
//		start = annotationsStart < typeParameter.declarationSourceStart ? annotationsStart : typeParameter.declarationSourceStart;
//		end = retrieveClosingAngleBracketPosition(end);
//		if (this.resolveBindings) {
//			recordName(simpleName, typeParameter);
//			recordNodes(typeParameter2, typeParameter);
//			typeParameter2.resolveBinding();
//		}
		ret.setSourceRange(start, end - start);
		return ret;
	}

	private ASTNode convertBodyDeclaration(JCTree tree, ASTNode parent) {
		if( parent instanceof AnnotationTypeDeclaration && tree instanceof JCMethodDecl methodDecl) {
			return convertMethodInAnnotationTypeDecl(methodDecl, parent);
		}
		if (tree instanceof JCMethodDecl methodDecl) {
			return convertMethodDecl(methodDecl, parent);
		}
		if (tree instanceof JCClassDecl jcClassDecl) {
			return convertClassDecl(jcClassDecl, parent);
		}
		if (tree instanceof JCVariableDecl jcVariableDecl) {
			return convertFieldDeclaration(jcVariableDecl, parent);
		}
		if (tree instanceof JCBlock block) {
			Initializer res = this.ast.newInitializer();
			commonSettings(res, tree);
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				// Now we have the tough task of going from a flags number to actual modifiers with source ranges
				res.modifiers().addAll(convertModifiersFromFlags(block.getStartPosition(), block.endpos, block.flags));
			} else {
				res.internalSetModifiers(getJLS2ModifiersFlags(block.flags));
			}
			boolean fillBlock = shouldFillBlock(block, this.focalPoint);
			if( fillBlock ) {
				res.setBody(convertBlock(block));
			} else {
				Block b = this.ast.newBlock();
				commonSettings(res, block);
				res.setBody(b);
			}
			return res;
		}
		if (tree instanceof JCErroneous || tree instanceof JCSkip) {
			return null;
		}
		ILog.get().error("Unsupported " + tree + " of type" + tree.getClass());
		Block substitute = this.ast.newBlock();
		commonSettings(substitute, tree);
		return substitute;
	}

	private ASTNode convertMethodInAnnotationTypeDecl(JCMethodDecl javac, ASTNode parent) {
		AnnotationTypeMemberDeclaration res = new AnnotationTypeMemberDeclaration(this.ast);
		commonSettings(res, javac);
		res.modifiers().addAll(convert(javac.getModifiers(), res));
		res.setType(convertToType(javac.getReturnType()));
		if( javac.defaultValue != null) {
			res.setDefault(convertExpression(javac.defaultValue));
		}
		if (convertName(javac.getName()) instanceof SimpleName simpleName) {
			res.setName(simpleName);
			int start = javac.getPreferredPosition();
			if (start > -1) {
				simpleName.setSourceRange(start, javac.getName().toString().length());
			}
		}
		return res;
	}

	private String getNodeName(ASTNode node) {
		if( node instanceof AbstractTypeDeclaration atd) {
			return atd.getName().toString();
		}
		if( node instanceof EnumDeclaration ed) {
			return ed.getName().toString();
		}
		return null;
	}

	private String getMethodDeclName(JCMethodDecl javac, ASTNode parent, boolean records) {
		String name = javac.getName().toString();
		boolean javacIsConstructor = Objects.equals(javac.getName(), Names.instance(this.context).init);
		if( javacIsConstructor) {
			// sometimes javac mistakes a method with no return type as a constructor
			String parentName = getNodeName(parent);
			String tmpString1 = this.rawText.substring(javac.pos);
			int openParen = tmpString1.indexOf("(");
			int openBrack = tmpString1.indexOf("{");
			int endPos = -1;
			if( openParen != -1 ) {
				endPos = openParen;
			}
			if( records && openBrack != -1 ) {
				endPos = endPos == -1 ? openBrack : Math.min(openBrack, endPos);
			}
			if( endPos != -1 ) {
				String methodName = tmpString1.substring(0, endPos).trim();
				if (!methodName.isEmpty() &&
					Character.isJavaIdentifierStart(methodName.charAt(0)) &&
					methodName.substring(1).chars().allMatch(Character::isJavaIdentifierPart) &&
					!methodName.equals(parentName)) {
					return methodName;
				}
			}
			return parentName;
		}
		return name;
	}

	private MethodDeclaration convertMethodDecl(JCMethodDecl javac, ASTNode parent) {
		if (TreeInfo.getEndPos(javac, this.javacCompilationUnit.endPositions) <= javac.getStartPosition()) {
			// not really existing, analysis sugar; let's skip
			return null;
		}
		MethodDeclaration res = this.ast.newMethodDeclaration();
		commonSettings(res, javac);
		if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
			res.modifiers().addAll(convert(javac.getModifiers(), res));
		} else {
			res.internalSetModifiers(getJLS2ModifiersFlags(javac.mods));
		}
		String javacName = javac.getName().toString();
		String methodDeclName = getMethodDeclName(javac, parent, parent instanceof RecordDeclaration);
		boolean methodDeclNameMatchesInit = Objects.equals(methodDeclName, Names.instance(this.context).init.toString());
		boolean javacNameMatchesInit = javacName.equals("<init>");
		boolean javacNameMatchesError = javacName.equals(ERROR);
		boolean javacNameMatchesInitAndMethodNameMatchesTypeName = javacNameMatchesInit && methodDeclName.equals(getNodeName(parent));
		boolean isConstructor = methodDeclNameMatchesInit || javacNameMatchesInitAndMethodNameMatchesTypeName;
		res.setConstructor(isConstructor);
		if (isConstructor && javac.getParameters().isEmpty()
			&& javac.getBody() != null && javac.getBody().endpos == Position.NOPOS) { // probably generated
			return null;
		}
		boolean isCompactConstructor = false;
		if(isConstructor && parent instanceof RecordDeclaration) {
			String postName = this.rawText.substring(javac.pos + methodDeclName.length()).trim();
			String firstChar = postName != null && postName.length() > 0 ? postName.substring(0,1) : null;
			isCompactConstructor = ("{".equals(firstChar));
			if( this.ast.apiLevel >= AST.JLS16_INTERNAL) {
				res.setCompactConstructor(isCompactConstructor);
			}
		}
		boolean malformed = false;
		if(isConstructor && !javacNameMatchesInitAndMethodNameMatchesTypeName) {
			malformed = true;
		}
		if( javacNameMatchesError || (javacNameMatchesInit && !isConstructor )) {
			malformed = true;
		}

		JCTree retTypeTree = javac.getReturnType();
		Type retType = null;
		if( !javacNameMatchesError) {
			var name = this.ast.newSimpleName(methodDeclName);
			nameSettings(name, javac, methodDeclName, isConstructor);
			res.setName(name);
		} else {
			// javac name is an error, so let's treat the return type as the name
			if (retTypeTree instanceof JCIdent jcid) {
				var name = this.ast.newSimpleName(jcid.getName().toString());
				nameSettings(name, javac, javacName, isConstructor);
				res.setName(name);
				retTypeTree = null;
				if (jcid.toString().equals(getNodeName(parent))) {
					res.setConstructor(true);
					isConstructor = true;
				}
			}
		}

		if( retTypeTree == null ) {
			if( isConstructor && this.ast.apiLevel == AST.JLS2_INTERNAL ) {
				retType = this.ast.newPrimitiveType(convert(TypeKind.VOID));
				// // TODO need to find the right range
				retType.setSourceRange(javac.mods.pos + getJLS2ModifiersFlagsAsStringLength(javac.mods.flags), 0);
			}
		} else {
			retType = convertToType(retTypeTree);
		}
		var dims = convertDimensionsAfterPosition(retTypeTree, javac.pos);
		if (!dims.isEmpty() && retTypeTree.pos > javac.pos ) {
			// The array dimensions are part of the variable name
			if( this.ast.apiLevel < AST.JLS8_INTERNAL) {
				res.setExtraDimensions(dims.size());
			} else {
				res.extraDimensions().addAll(dims);
			}
			retType = convertToType(unwrapDimensions(retTypeTree, dims.size()));
		}

		if( retType != null || isConstructor) {
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				res.setReturnType2(retType);
			} else {
				res.internalSetReturnType(retType);
			}
		} else {
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				res.setReturnType2(null);
			}
		}

		if( !isCompactConstructor) {
			// Compact constructor does not show the parameters even though javac finds them
			javac.getParameters().stream().map(this::convertVariableDeclaration).forEach(res.parameters()::add);
		}
		if (javac.getReceiverParameter() != null) {
			Type receiverType = convertToType(javac.getReceiverParameter().getType());
			if (receiverType instanceof AnnotatableType annotable) {
				javac.getReceiverParameter().getModifiers().getAnnotations().stream() //
					.map(this::convert)
					.forEach(annotable.annotations()::add);
			}
			if (receiverType != null) {
				res.setReceiverType(receiverType);
			}
			if (javac.getReceiverParameter().getNameExpression() instanceof JCFieldAccess qualifiedName) {
				res.setReceiverQualifier((SimpleName)toName(qualifiedName.getExpression()));
			}
		}

		if( javac.getTypeParameters() != null ) {
			Iterator<JCTypeParameter> i = javac.getTypeParameters().iterator();
			while(i.hasNext()) {
				JCTypeParameter next = i.next();
				if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
					res.typeParameters().add(convert(next));
				} else {
					// TODO
				}
			}
		}

		if (javac.getBody() != null
			&& javac.getBody().endpos > javac.getBody().getStartPosition()) { // otherwise, it's probably generated by lombok
			boolean fillBlock = shouldFillBlock(javac, this.focalPoint);
			if( fillBlock ) {
				Block b = convertBlock(javac.getBody());
				if (b != null) {
					AbstractTypeDeclaration td = findSurroundingTypeDeclaration(parent);
					boolean isInterface = td instanceof TypeDeclaration td1 && td1.isInterface();
					long modFlags = javac.getModifiers() == null ? 0 : javac.getModifiers().flags;
					boolean isAbstractOrNative = (modFlags & (Flags.ABSTRACT | Flags.NATIVE)) != 0;
					boolean isJlsBelow8 = this.ast.apiLevel < AST.JLS8_INTERNAL;
					boolean isJlsAbove8 = this.ast.apiLevel > AST.JLS8_INTERNAL;
					long flagsToCheckForAboveJLS8 = Flags.STATIC | Flags.DEFAULT | (isJlsAbove8 ? Flags.PRIVATE : 0);
					boolean notAllowed = (isAbstractOrNative || (isInterface && (isJlsBelow8 || (modFlags & flagsToCheckForAboveJLS8) == 0)));
					if (notAllowed) {
						res.setFlags(res.getFlags() | ASTNode.MALFORMED);
					}
					res.setBody(b);
					if( (b.getFlags() & ASTNode.MALFORMED) > 0 ) {
						malformed = true;
					}
				}
			} else {
				Block b = this.ast.newBlock();
				commonSettings(res, javac);
				res.setBody(b);
			}
		}

		for (JCExpression thrown : javac.getThrows()) {
			if (this.ast.apiLevel < AST.JLS8_INTERNAL) {
				res.thrownExceptions().add(toName(thrown));
			} else {
				Type type = convertToType(thrown);
				if (type != null) {
					res.thrownExceptionTypes().add(type);
				}
			}
		}
		if( malformed ) {
			res.setFlags(res.getFlags() | ASTNode.MALFORMED);
		}
		return res;
	}

	private boolean shouldFillBlock(JCTree tree, int focalPoint2) {
		int start = tree.getStartPosition();
		int endPos = tree.getEndPosition(this.javacCompilationUnit.endPositions);
		if( focalPoint == -1 || (focalPoint >= start && focalPoint <= endPos)) {
			return true;
		}
		return false;
	}
	private AbstractTypeDeclaration findSurroundingTypeDeclaration(ASTNode parent) {
		if( parent == null )
			return null;
		if( parent instanceof AbstractTypeDeclaration t) {
			return t;
		}
		return findSurroundingTypeDeclaration(parent.getParent());
	}

	private VariableDeclaration convertVariableDeclarationForLambda(JCVariableDecl javac) {
		if(javac.getType() == null && javac.getStartPosition() == javac.getPreferredPosition() /* check no "var" */) {
			return createVariableDeclarationFragment(javac);
		} else if (javac.getType() != null && javac.getType().getPreferredPosition() == Position.NOPOS) { // "virtual" node added for analysis, not part of AST
			return createVariableDeclarationFragment(javac);
		} else {
			return convertVariableDeclaration(javac);
		}
	}
	private VariableDeclaration convertVariableDeclaration(JCVariableDecl javac) {
		// if (singleDecl) {
		SingleVariableDeclaration res = this.ast.newSingleVariableDeclaration();
		commonSettings(res, javac);
		if (convertName(javac.getName()) instanceof SimpleName simpleName) {
			nameSettings(simpleName, javac, simpleName.toString());
			res.setName(simpleName);
		}
		if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
			res.modifiers().addAll(convert(javac.getModifiers(), res));
		} else {
			res.internalSetModifiers(getJLS2ModifiersFlags(javac.mods));
		}
		
		JCTree type = javac.getType();
		if (type instanceof JCAnnotatedType annotatedType) {
			annotatedType.getAnnotations().stream()
				.map(this::convert)
				.forEach(res.varargsAnnotations()::add);
			type = annotatedType.getUnderlyingType();
		}
		
		if ( (javac.mods.flags & VARARGS) != 0) {
			// We have varity
			if(type instanceof JCArrayTypeTree arr) {
				type = unwrapDimensions(arr, 1);
			}
			if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
				res.setVarargs(true);
			}
		}
		
		List<Dimension> dims = convertDimensionsAfterPosition(javac.getType(), javac.getPreferredPosition()); // +1 to exclude part of the type declared before name
		if(!dims.isEmpty() ) {
			// Some of the array dimensions are part of the variable name
			if( this.ast.apiLevel < AST.JLS8_INTERNAL) {
				res.setExtraDimensions(dims.size()); // the type is 1-dim array
			} else {
				res.extraDimensions().addAll(dims);
			}
			type = unwrapDimensions(type, dims.size());
		} 
		
		// the array dimensions are part of the type
		if (type != null) {
			if( !(type instanceof JCErroneous)) {
				Type converted = convertToType(type);
				if (converted != null) {
					res.setType(converted);
				}
			}
		} else if (javac.getStartPosition() != javac.getPreferredPosition()
					&& this.rawText.substring(javac.getStartPosition(), javac.getPreferredPosition()).matches("var(\\s)+")) {
			SimpleName varName = this.ast.newSimpleName("var");
			varName.setSourceRange(javac.getStartPosition(), varName.getIdentifier().length());
			Type varType = this.ast.newSimpleType(varName);
			varType.setSourceRange(varName.getStartPosition(), varName.getLength());
			res.setType(varType);
		}
		if (javac.getInitializer() != null) {
			res.setInitializer(convertExpression(javac.getInitializer()));
		}
		return res;
	}

	private int getJLS2ModifiersFlags(JCModifiers mods) {
		return getJLS2ModifiersFlags(mods.flags);
	}

	private VariableDeclarationFragment createVariableDeclarationFragment(JCVariableDecl javac) {
		VariableDeclarationFragment fragment = this.ast.newVariableDeclarationFragment();
		commonSettings(fragment, javac);
		int fragmentEnd = javac.getEndPosition(this.javacCompilationUnit.endPositions);
		int fragmentStart = javac.pos;
		int fragmentLength = fragmentEnd - fragmentStart; // ????  - 1;
		fragment.setSourceRange(fragmentStart, Math.max(0, fragmentLength));
		removeSurroundingWhitespaceFromRange(fragment);
		removeTrailingCharFromRange(fragment, new char[] {';', ','});
		removeSurroundingWhitespaceFromRange(fragment);
		if (convertName(javac.getName()) instanceof SimpleName simpleName) {
			fragment.setName(simpleName);
		}
		var dims = convertDimensionsAfterPosition(javac.getType(), fragmentStart);
		if( this.ast.apiLevel < AST.JLS8_INTERNAL) {
			fragment.setExtraDimensions(dims.size());
		} else {
			fragment.extraDimensions().addAll(dims);
		}
		if (javac.getInitializer() != null) {
			Expression initializer = convertExpression(javac.getInitializer());
			if( initializer != null ) {
				fragment.setInitializer(initializer);
				// we may receive range for `int i = 0;` (with semicolon and newline). If we
				// have an initializer, use it's endPos instead for the fragment
				int length = initializer.getStartPosition() + initializer.getLength() - fragment.getStartPosition();
				if (length >= 0) {
					fragment.setSourceRange(fragment.getStartPosition(), length);
				}
			}
		}
		return fragment;
	}

	private FieldDeclaration convertFieldDeclaration(JCVariableDecl javac, ASTNode parent) {
		VariableDeclarationFragment fragment = createVariableDeclarationFragment(javac);
		List<ASTNode> sameStartPosition = new ArrayList<>();
		if( parent instanceof AbstractTypeDeclaration decl) {
			decl.bodyDeclarations().stream().filter(x -> x instanceof FieldDeclaration)
					.filter(x -> ((FieldDeclaration)x).getType().getStartPosition() == javac.vartype.getStartPosition())
					.forEach(x -> sameStartPosition.add((ASTNode)x));
		}
		if( parent instanceof AnonymousClassDeclaration decl) {
			decl.bodyDeclarations().stream().filter(x -> x instanceof FieldDeclaration)
					.filter(x -> ((FieldDeclaration)x).getType().getStartPosition() == javac.vartype.getStartPosition())
					.forEach(x -> sameStartPosition.add((ASTNode)x));
		}
		if( sameStartPosition.size() >= 1 ) {
			FieldDeclaration fd = (FieldDeclaration)sameStartPosition.get(0);
			if( fd != null ) {
				fd.fragments().add(fragment);
				int newParentEnd = fragment.getStartPosition() + fragment.getLength();
				fd.setSourceRange(fd.getStartPosition(), newParentEnd - fd.getStartPosition() + 1);
			}
			return null;
		} else {
			FieldDeclaration res = this.ast.newFieldDeclaration(fragment);
			commonSettings(res, javac);
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				res.modifiers().addAll(convert(javac.getModifiers(), res));
			} else {
				res.internalSetModifiers(getJLS2ModifiersFlags(javac.mods));
			}

			Type resType = null;
			int count = fragment.getExtraDimensions();
			if( count > 0 ) {
				// must do simple type here
				JCTree t = javac.getType();
				if( t instanceof JCArrayTypeTree jcatt) {
					// unwrap the jcatt count times?
					JCTree working = jcatt;
					for( int i = 0; i < count; i++ ) {
						if( working instanceof JCArrayTypeTree work2) {
							working = work2.getType();
						}
					}
					resType = convertToType(working);
				} else {
					resType = convertToType(javac.getType());
				}
			} else {
				resType = convertToType(javac.getType());
			}
			if (resType != null) {
				res.setType(resType);
			}
			if( javac.getType() instanceof JCErroneous && resType instanceof SimpleType st && st.getName() instanceof SimpleName sn && sn.toString().equals(FAKE_IDENTIFIER)) {
				if( fragment.getName() instanceof SimpleName fragName && fragName.toString().equals(FAKE_IDENTIFIER)) {
					return null;
				}
			}

			return res;
		}
	}


	private void setJavadocForNode(JCTree javac, ASTNode node) {
		Comment c = this.javacCompilationUnit.docComments.getComment(javac);
		if(c != null && (c.getStyle() == Comment.CommentStyle.JAVADOC_BLOCK || c.getStyle() == CommentStyle.JAVADOC_LINE)) {
			org.eclipse.jdt.core.dom.Comment comment = convert(c, javac);
			if( !(comment instanceof Javadoc)) {
				return;
			}
			Javadoc javadoc = (Javadoc)comment;
			if (node instanceof BodyDeclaration bodyDeclaration) {
				bodyDeclaration.setJavadoc(javadoc);
				bodyDeclaration.setSourceRange(javadoc.getStartPosition(), bodyDeclaration.getStartPosition() + bodyDeclaration.getLength() - javadoc.getStartPosition());
			} else if (node instanceof ModuleDeclaration moduleDeclaration) {
				moduleDeclaration.setJavadoc(javadoc);
				moduleDeclaration.setSourceRange(javadoc.getStartPosition(), moduleDeclaration.getStartPosition() + moduleDeclaration.getLength() - javadoc.getStartPosition());
			} else if (node instanceof PackageDeclaration packageDeclaration) {
				if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
					packageDeclaration.setJavadoc(javadoc);
				} else {
					this.notAttachedComments.add(javadoc);
				}
				packageDeclaration.setSourceRange(javadoc.getStartPosition(), packageDeclaration.getStartPosition() + packageDeclaration.getLength() - javadoc.getStartPosition());
			} else {
				this.notAttachedComments.add(javadoc);
			}
		}
	}

	private Expression convertExpressionImpl(JCExpression javac) {
		if (javac instanceof JCIdent ident) {
			if (Objects.equals(ident.name, Names.instance(this.context)._this)) {
				ThisExpression res = this.ast.newThisExpression();
				commonSettings(res, javac);
				return res;
			}
			return toName(ident);
		}
		if (javac instanceof JCLiteral literal) {
			return convertLiteral(literal);
		}
		if (javac instanceof JCFieldAccess fieldAccess) {
			if (Objects.equals(Names.instance(this.context)._class, fieldAccess.getIdentifier())) {
				TypeLiteral res = this.ast.newTypeLiteral();
				commonSettings(res, javac);
				res.setType(convertToType(fieldAccess.getExpression()));
				return res;
			}
			if (Objects.equals(Names.instance(this.context)._this, fieldAccess.getIdentifier())) {
				ThisExpression res = this.ast.newThisExpression();
				commonSettings(res, javac);
				res.setQualifier(toName(fieldAccess.getExpression()));
				return res;
			}
			if (fieldAccess.getExpression() instanceof JCFieldAccess parentFieldAccess && Objects.equals(Names.instance(this.context)._super, parentFieldAccess.getIdentifier())) {
				SuperFieldAccess res = this.ast.newSuperFieldAccess();
				commonSettings(res, javac);
				res.setQualifier(toName(parentFieldAccess.getExpression()));
				res.setName((SimpleName)convertName(fieldAccess.getIdentifier()));
				return res;
			}
			if (fieldAccess.getExpression() instanceof JCIdent parentFieldAccess && Objects.equals(Names.instance(this.context)._super, parentFieldAccess.getName())) {
				SuperFieldAccess res = this.ast.newSuperFieldAccess();
				commonSettings(res, javac);
				res.setName((SimpleName)convertName(fieldAccess.getIdentifier()));
				return res;
			}
			if (fieldAccess.getExpression() instanceof JCIdent parentFieldAccess && Objects.equals(Names.instance(this.context)._this, parentFieldAccess.getName())) {
				FieldAccess res = this.ast.newFieldAccess();
				commonSettings(res, javac);
				res.setExpression(convertExpression(parentFieldAccess));
				if (convertName(fieldAccess.getIdentifier()) instanceof SimpleName name) {
					res.setName(name);
				}
				return res;
			}
			if (fieldAccess.getExpression() instanceof JCIdent qualifier) {
				Name qualifierName = convertName(qualifier.getName());
				commonSettings(qualifierName, qualifier);
				SimpleName qualifiedName = (SimpleName)convertName(fieldAccess.getIdentifier());
				if (qualifiedName == null) {
					// when there are syntax errors where the statement is not completed.
					qualifiedName = this.ast.newSimpleName(FAKE_IDENTIFIER);
					qualifiedName.setFlags(ASTNode.RECOVERED);
				}
				QualifiedName res = this.ast.newQualifiedName(qualifierName, qualifiedName);
				commonSettings(res, javac);
				return res;
			}
			useQualifiedName: if (fieldAccess.getExpression() instanceof JCFieldAccess parentFieldAccess) {
				JCFieldAccess cursor = parentFieldAccess;
				if (Objects.equals(Names.instance(this.context)._class, cursor.getIdentifier())
						|| Objects.equals(Names.instance(this.context)._this, cursor.getIdentifier())
						|| Objects.equals(Names.instance(this.context)._super, cursor.getIdentifier())) {
					break useQualifiedName;
				}
				while (cursor.getExpression() instanceof JCFieldAccess newParent) {
					cursor = newParent;
					if (Objects.equals(Names.instance(this.context)._class, cursor.getIdentifier())
							|| Objects.equals(Names.instance(this.context)._this, cursor.getIdentifier())
							|| Objects.equals(Names.instance(this.context)._super, cursor.getIdentifier())) {
						break useQualifiedName;
					}
				}

				if (cursor.getExpression() instanceof JCIdent oldestIdentifier
						&& !Objects.equals(Names.instance(this.context)._class, oldestIdentifier.getName())
						&& !Objects.equals(Names.instance(this.context)._this, oldestIdentifier.getName())
						&& !Objects.equals(Names.instance(this.context)._super, oldestIdentifier.getName())) {
					// all segments are simple names
					return convertQualifiedName(fieldAccess);
				}
			}
			FieldAccess res = this.ast.newFieldAccess();
			commonSettings(res, javac);
			res.setExpression(convertExpression(fieldAccess.getExpression()));
			if (convertName(fieldAccess.getIdentifier()) instanceof SimpleName name) {
				res.setName(name);
			}
			return res;
		}
		if (javac instanceof JCMethodInvocation methodInvocation) {
			JCExpression nameExpr = methodInvocation.getMethodSelect();
			if (nameExpr instanceof JCFieldAccess access) {
				// Handle super method calls first
				boolean superCall1 = access.getExpression() instanceof JCFieldAccess && Objects.equals(Names.instance(this.context)._super, ((JCFieldAccess)access.getExpression()).getIdentifier());
				boolean superCall2 = Objects.equals(Names.instance(this.context)._super.toString(), access.getExpression().toString());
				if (superCall1 || superCall2) {
					JCFieldAccess fa = superCall1 ? ((JCFieldAccess)access.getExpression()) : access;
					SuperMethodInvocation res2 = this.ast.newSuperMethodInvocation();
					commonSettings(res2, javac);
					methodInvocation.getArguments().stream().map(this::convertExpression).forEach(res2.arguments()::add);
					if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
						methodInvocation.getTypeArguments().stream()
							.map(this::convertToType)
							.filter(Objects::nonNull)
							.forEach(res2.typeArguments()::add);
					}
					if( superCall1 ) {
						res2.setQualifier(toName(fa.getExpression()));
					}
					res2.setName((SimpleName)convertName(access.getIdentifier()));
					return res2;
				}
			}

			MethodInvocation res = this.ast.newMethodInvocation();
			commonSettings(res, methodInvocation);
			if (nameExpr instanceof JCIdent ident) {
				if (Objects.equals(ident.getName(), Names.instance(this.context)._super)) {
					return convertSuperMethodInvocation(methodInvocation);
				}
				SimpleName name = (SimpleName)convertName(ident.getName());
				commonSettings(name, ident);
				res.setName(name);
			} else if (nameExpr instanceof JCFieldAccess access) {
				boolean superCall1 = access.getExpression() instanceof JCFieldAccess && Objects.equals(Names.instance(this.context)._super, ((JCFieldAccess)access.getExpression()).getIdentifier());
				boolean superCall2 = Objects.equals(Names.instance(this.context)._super.toString(), access.getExpression().toString());
				if (superCall1 || superCall2) {
					JCFieldAccess fa = superCall1 ? ((JCFieldAccess)access.getExpression()) : access;
					SuperMethodInvocation res2 = this.ast.newSuperMethodInvocation();
					commonSettings(res2, javac);
					methodInvocation.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);
					if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
						methodInvocation.getTypeArguments().stream()
							.map(this::convertToType)
							.filter(Objects::nonNull)
							.forEach(res.typeArguments()::add);
					}
					if( superCall1 ) {
						res2.setQualifier(toName(fa.getExpression()));
					}
					res2.setName((SimpleName)convertName(access.getIdentifier()));
					return res2;
				}
				if (convertName(access.getIdentifier()) instanceof SimpleName simpleName) {
					res.setName(simpleName);
					String asString = access.getIdentifier().toString();
					commonSettings(simpleName, access);
					int foundOffset = this.rawText.indexOf(asString, access.getPreferredPosition());
					if (foundOffset > 0) {
						simpleName.setSourceRange(foundOffset, asString.length());
					}
				}
				res.setExpression(convertExpression(access.getExpression()));
			}
			if (methodInvocation.getArguments() != null) {
				methodInvocation.getArguments().stream()
					.map(this::convertExpression)
					.forEach(res.arguments()::add);
			}
			if (methodInvocation.getTypeArguments() != null) {
				if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
					methodInvocation.getTypeArguments().stream()
						.map(this::convertToType)
						.filter(Objects::nonNull)
						.forEach(res.typeArguments()::add);
				}
			}
			return res;
		}
		if (javac instanceof JCNewClass newClass) {
			ClassInstanceCreation res = this.ast.newClassInstanceCreation();
			commonSettings(res, javac);
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				res.setType(convertToType(newClass.getIdentifier()));
			} else {
				Name n = toName(newClass.getIdentifier());
				if( n != null )
					res.setName(n);
			}
			if (newClass.getClassBody() != null && newClass.getClassBody() instanceof JCClassDecl javacAnon) {
				AnonymousClassDeclaration anon = createAnonymousClassDeclaration(javacAnon, res);
				res.setAnonymousClassDeclaration(anon);
			}
			if (newClass.getArguments() != null) {
				newClass.getArguments().stream()
					.map(this::convertExpression)
					.forEach(res.arguments()::add);
			}
			if (newClass.encl != null) {
				res.setExpression(convertExpression(newClass.encl));
			}
			if( newClass.getTypeArguments() != null && this.ast.apiLevel != AST.JLS2_INTERNAL) {
				Iterator<JCExpression> it = newClass.getTypeArguments().iterator();
				while(it.hasNext()) {
					Type e = convertToType(it.next());
					if( e != null ) {
						res.typeArguments().add(e);
					}
				}
			}
			return res;
		}
		if (javac instanceof JCBinary binary) {
			return handleInfixExpression(binary, javac);

		}
		if (javac instanceof JCUnary unary) {
			if (unary.getTag() != Tag.POSTINC && unary.getTag() != Tag.POSTDEC) {
				PrefixExpression res = this.ast.newPrefixExpression();
				commonSettings(res, javac);
				res.setOperand(convertExpression(unary.getExpression()));
				res.setOperator(switch (unary.getTag()) {
					case POS -> PrefixExpression.Operator.PLUS;
					case NEG -> PrefixExpression.Operator.MINUS;
					case NOT -> PrefixExpression.Operator.NOT;
					case COMPL -> PrefixExpression.Operator.COMPLEMENT;
					case PREINC -> PrefixExpression.Operator.INCREMENT;
					case PREDEC -> PrefixExpression.Operator.DECREMENT;
					default -> null;
				});
				return res;
			} else {
				PostfixExpression res = this.ast.newPostfixExpression();
				commonSettings(res, javac);
				res.setOperand(convertExpression(unary.getExpression()));
				res.setOperator(switch (unary.getTag()) {
					case POSTINC -> PostfixExpression.Operator.INCREMENT;
					case POSTDEC -> PostfixExpression.Operator.DECREMENT;
					default -> null;
				});
				return res;
			}
		}
		if (javac instanceof JCParens parens) {
			ParenthesizedExpression res = this.ast.newParenthesizedExpression();
			commonSettings(res, javac);
			res.setExpression(convertExpression(parens.getExpression()));
			return res;
		}
		if (javac instanceof JCAssign assign) {
			Assignment res = this.ast.newAssignment();
			commonSettings(res, javac);
			res.setLeftHandSide(convertExpression(assign.getVariable()));
			res.setRightHandSide(convertExpression(assign.getExpression()));
			return res;
		}
		if (javac instanceof JCAssignOp assignOp) {
			Assignment res = this.ast.newAssignment();
			commonSettings(res, javac);
			res.setLeftHandSide(convertExpression(assignOp.getVariable()));
			res.setRightHandSide(convertExpression(assignOp.getExpression()));
			res.setOperator(switch (assignOp.getTag()) {
				case PLUS_ASG -> Assignment.Operator.PLUS_ASSIGN;
				case BITOR_ASG -> Assignment.Operator.BIT_OR_ASSIGN;
				case BITXOR_ASG-> Assignment.Operator.BIT_XOR_ASSIGN;
				case BITAND_ASG-> Assignment.Operator.BIT_AND_ASSIGN;
				case SL_ASG-> Assignment.Operator.LEFT_SHIFT_ASSIGN;
				case SR_ASG-> Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN;
				case USR_ASG-> Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN;
				case MINUS_ASG-> Assignment.Operator.MINUS_ASSIGN;
				case MUL_ASG-> Assignment.Operator.TIMES_ASSIGN;
				case DIV_ASG-> Assignment.Operator.DIVIDE_ASSIGN;
				case MOD_ASG-> Assignment.Operator.REMAINDER_ASSIGN;
				default -> null;
			});
			return res;
		}
		if (javac instanceof JCInstanceOf jcInstanceOf) {
			JCPattern jcPattern = jcInstanceOf.getPattern();
			if (jcInstanceOf.getType() != null && jcPattern == null) {
				InstanceofExpression res = this.ast.newInstanceofExpression();
				commonSettings(res, javac);
				res.setLeftOperand(convertExpression(jcInstanceOf.getExpression()));
				res.setRightOperand(convertToType(jcInstanceOf.getType()));
				return res;
			}
			if (jcPattern instanceof JCAnyPattern) {
				InstanceofExpression res = this.ast.newInstanceofExpression();
				commonSettings(res, javac);
				res.setLeftOperand(convertExpression(jcInstanceOf.getExpression()));
				throw new UnsupportedOperationException("Right operand not supported yet");
//				return res;
			}
			PatternInstanceofExpression res = this.ast.newPatternInstanceofExpression();
			commonSettings(res, javac);
			res.setLeftOperand(convertExpression(jcInstanceOf.getExpression()));
			Pattern p = convert(jcPattern);
			if( p != null && this.ast.apiLevel >= AST.JLS20_INTERNAL)
				res.setPattern(p);
			else {
				res.setRightOperand(convertToSingleVarDecl(jcPattern));
			}
			return res;
		}
		if (javac instanceof JCArrayAccess jcArrayAccess) {
			ArrayAccess res = this.ast.newArrayAccess();
			commonSettings(res, javac);
			res.setArray(convertExpression(jcArrayAccess.getExpression()));
			res.setIndex(convertExpression(jcArrayAccess.getIndex()));
			return res;
		}
		if (javac instanceof JCTypeCast jcCast) {
			CastExpression res = this.ast.newCastExpression();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcCast.getExpression()));
			res.setType(convertToType(jcCast.getType()));
			return res;
		}
		if (javac instanceof JCMemberReference jcMemberReference) {
			JCExpression qualifierExpression = jcMemberReference.getQualifierExpression();
			if (Objects.equals(Names.instance(this.context).init, jcMemberReference.getName())) {
				CreationReference res = this.ast.newCreationReference();
				commonSettings(res, javac);
				res.setType(convertToType(qualifierExpression));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().stream()
						.map(this::convertToType)
						.filter(Objects::nonNull)
						.forEach(res.typeArguments()::add);
				}
				return res;
			} else if (qualifierExpression.getKind() == Kind.PARAMETERIZED_TYPE || qualifierExpression.getKind() == Kind.ARRAY_TYPE) {
				TypeMethodReference res = this.ast.newTypeMethodReference();
				commonSettings(res, javac);
				res.setType(convertToType(qualifierExpression));
				res.setName((SimpleName)convertName(jcMemberReference.getName()));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().stream()
						.map(this::convertToType)
						.filter(Objects::nonNull)
						.forEach(res.typeArguments()::add);
				}
				return res;
			} else if (qualifierExpression instanceof JCIdent ident
						&& Names.instance(this.context)._super.equals(ident.getName())) {
				SuperMethodReference res = this.ast.newSuperMethodReference();
				commonSettings(res, javac);
				res.setName((SimpleName)convertName(jcMemberReference.getName()));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().stream()
						.map(this::convertToType)
						.filter(Objects::nonNull)
						.forEach(res.typeArguments()::add);
				}
				return res;
			} else if (qualifierExpression instanceof JCFieldAccess fieldAccess
						&& Names.instance(this.context)._super.equals(fieldAccess.getIdentifier())) {
				SuperMethodReference res = this.ast.newSuperMethodReference();
				commonSettings(res, javac);
				res.setName((SimpleName)convertName(jcMemberReference.getName()));
				res.setQualifier(toName(fieldAccess.getExpression()));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().stream()
						.map(this::convertToType)
						.filter(Objects::nonNull)
						.forEach(res.typeArguments()::add);
				}
				return res;
			} else {
				ExpressionMethodReference res = this.ast.newExpressionMethodReference();
				commonSettings(res, javac);
				res.setExpression(convertExpression(jcMemberReference.getQualifierExpression()));
				res.setName((SimpleName)convertName(jcMemberReference.getName()));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().stream()
						.map(this::convertToType)
						.filter(Objects::nonNull)
						.forEach(res.typeArguments()::add);
				}
				return res;
			}
		}
		if (javac instanceof JCConditional jcCondition) {
			ConditionalExpression res = this.ast.newConditionalExpression();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcCondition.getCondition()));
			res.setThenExpression(convertExpression(jcCondition.getTrueExpression()));
			res.setElseExpression(convertExpression(jcCondition.getFalseExpression()));
			return res;
		}
		if (javac instanceof JCLambda jcLambda) {
			LambdaExpression res = this.ast.newLambdaExpression();
			commonSettings(res, javac);
			jcLambda.getParameters().stream()
				.filter(JCVariableDecl.class::isInstance)
				.map(JCVariableDecl.class::cast)
				.map(this::convertVariableDeclarationForLambda)
				.forEach(res.parameters()::add);
			int arrowIndex = this.rawText.indexOf("->", jcLambda.getStartPosition());
			int parenthesisIndex = this.rawText.indexOf(")", jcLambda.getStartPosition());
			res.setParentheses(parenthesisIndex >= 0 && parenthesisIndex < arrowIndex);
			ASTNode body = jcLambda.getBody() instanceof JCExpression expr ? convertExpression(expr) :
				jcLambda.getBody() instanceof JCStatement stmt ? convertStatement(stmt, res) :
				null;
			if( body != null )
				res.setBody(body);
			// TODO set parenthesis looking at the next non-whitespace char after the last parameter
			int endPos = jcLambda.getEndPosition(this.javacCompilationUnit.endPositions);
			res.setSourceRange(jcLambda.pos, endPos - jcLambda.pos);
			return res;
		}
		if (javac instanceof JCNewArray jcNewArray) {
			ArrayCreation res = this.ast.newArrayCreation();
			commonSettings(res, javac);
			if (jcNewArray.getType() == null) {
				// we have no type, we should return an initializer directly
				ArrayInitializer ret = createArrayInitializerFromJCNewArray(jcNewArray);
				return ret;
			}

			if (jcNewArray.getType() != null) {
				Type type = convertToType(jcNewArray.getType());
				ArrayType arrayType;
				if (type instanceof ArrayType childArrayType) {
					arrayType = childArrayType;
					if( this.ast.apiLevel >= AST.JLS8_INTERNAL) {
						var extraDimensions = jcNewArray.getDimAnnotations().stream()
							.map(annotations -> annotations.stream().map(this::convert).toList())
							.map(annotations -> {
								Dimension dim = this.ast.newDimension();
								dim.annotations().addAll(annotations);
								int startOffset = annotations.stream().mapToInt(Annotation::getStartPosition).min().orElse(-1);
								int endOffset = annotations.stream().mapToInt(ann -> ann.getStartPosition() + ann.getLength()).max().orElse(-1);
								dim.setSourceRange(startOffset, endOffset - startOffset);
								return dim;
							})
							.toList();
						if (arrayType.dimensions().isEmpty()) {
							arrayType.dimensions().addAll(extraDimensions);
						} else {
							var lastDimension = arrayType.dimensions().removeFirst();
							arrayType.dimensions().addAll(extraDimensions);
							arrayType.dimensions().add(lastDimension);
						}
						int totalRequiredDims = countDimensions(jcNewArray.getType()) + 1;
						int totalCreated = arrayType.dimensions().size();
						if( totalCreated < totalRequiredDims) {
							int endPos = jcNewArray.getEndPosition(this.javacCompilationUnit.endPositions);
							int startPos = jcNewArray.getStartPosition();
							String raw = this.rawText.substring(startPos, endPos);
							for( int i = 0; i < totalRequiredDims; i++ ) {
								int absoluteEndChar = startPos + ordinalIndexOf(raw, "]", i+1);
								int absoluteEnd = absoluteEndChar + 1;
								int absoluteStart = startPos + ordinalIndexOf(raw, "[", i+1);
								boolean found = false;
								if( absoluteEnd != -1 && absoluteStart != -1 ) {
									for( int j = 0; j < totalCreated && !found; j++ ) {
										Dimension d = (Dimension)arrayType.dimensions().get(j);
										if( d.getStartPosition() == absoluteStart && (d.getStartPosition() + d.getLength()) == absoluteEnd) {
											found = true;
										}
									}
									if( !found ) {
										// Need to make a new one
										Dimension d = this.ast.newDimension();
										d.setSourceRange(absoluteStart, absoluteEnd - absoluteStart);
										arrayType.dimensions().add(i, d);
										totalCreated++;
									}
								}
							}
						}
					} else {
						// JLS < 8, just wrap underlying type
						arrayType = this.ast.newArrayType(childArrayType);
					}
				} else if(jcNewArray.dims != null && jcNewArray.dims.size() > 0 ){
					// Child is not array type
					arrayType = this.ast.newArrayType(type);
					int dims = jcNewArray.dims.size();
					for( int i = 0; i < dims - 1; i++ ) {
						if( this.ast.apiLevel >= AST.JLS8_INTERNAL) {
							// TODO, this dimension needs source range
							arrayType.dimensions().addFirst(this.ast.newDimension());
						} else {
							// JLS < 8, wrap underlying
							arrayType = this.ast.newArrayType(arrayType);
						}
					}
				} else {
					// Child is not array type, and 0 dims for underlying
					arrayType = this.ast.newArrayType(type);
				}
				commonSettings(arrayType, jcNewArray.getType());
				res.setType(arrayType);
			}
			jcNewArray.getDimensions().map(this::convertExpression).forEach(res.dimensions()::add);
			if (jcNewArray.getInitializers() != null) {
				res.setInitializer(createArrayInitializerFromJCNewArray(jcNewArray));
			}
			return res;
		}
		if (javac instanceof JCAnnotation jcAnnot) {
			return convert(jcAnnot);
		}
		if (javac instanceof JCPrimitiveTypeTree primitiveTree) {
			SimpleName res = this.ast.newSimpleName(primitiveTree.getPrimitiveTypeKind().name());
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCSwitchExpression jcSwitch) {
			SwitchExpression res = this.ast.newSwitchExpression();
			commonSettings(res, javac);
			JCExpression switchExpr = jcSwitch.getExpression();
			if( switchExpr instanceof JCParens jcp) {
				switchExpr = jcp.getExpression();
			}
			res.setExpression(convertExpression(switchExpr));

			List<JCCase> cases = jcSwitch.getCases();
			Iterator<JCCase> it = cases.iterator();
			ArrayList<JCTree> bodyList = new ArrayList<>();
			while(it.hasNext()) {
				JCCase switchCase = it.next();
				bodyList.add(switchCase);
				if( switchCase.getCaseKind() == CaseKind.STATEMENT ) {
					if( switchCase.getStatements() != null && switchCase.getStatements().size() > 0 ) {
						bodyList.addAll(switchCase.getStatements());
					}
				} else {
					bodyList.add(switchCase.getBody());
				}
			}

			Iterator<JCTree> stmtIterator = bodyList.iterator();
			while(stmtIterator.hasNext()) {
				JCTree next = stmtIterator.next();
				if( next instanceof JCStatement jcs) {
					Statement s1 = convertStatement(jcs, res);
					if( s1 != null ) {
						res.statements().add(s1);
					}
				} else if( next instanceof JCExpression jce) {
					Expression s1 = convertExpression(jce);
					if( s1 != null ) {
						// make a yield statement out of it??
						YieldStatement r1 = this.ast.newYieldStatement();
						commonSettings(r1, jce);
						r1.setExpression(s1);
						res.statements().add(r1);
					}
				}
			}
			return res;
		}
		if (javac instanceof JCTree.JCArrayTypeTree arrayTypeTree) {
			Type type = convertToType(javac);
			TypeLiteral res = this.ast.newTypeLiteral();
			res.setType(type);
			commonSettings(res, arrayTypeTree);
			return res;
		}
		if (javac instanceof JCTypeApply parameterizedType) {
			// usually mapping from an error
			var recoveredType = convertToType(parameterizedType);
			// As we cannot directly map a type to a JDT expr, let's capture it anyway
			TypeLiteral decl = this.ast.newTypeLiteral();
			decl.setSourceRange(recoveredType.getStartPosition(), recoveredType.getLength());
			decl.setFlags(ASTNode.MALFORMED);
			decl.setType(recoveredType);
			return decl;
		}
		return null;
	}

	private SingleVariableDeclaration convertToSingleVarDecl(JCPattern jcPattern) {
		if( jcPattern instanceof JCBindingPattern jcbp && jcbp.var instanceof JCVariableDecl decl) {
			SingleVariableDeclaration vdd = (SingleVariableDeclaration)convertVariableDeclaration(decl);
			return vdd;
		}
		return null;
	}

	private List<JCExpression> consecutiveInfixExpressionsWithEqualOps(JCBinary binary, Tag opcode) {
		return consecutiveInfixExpressionsWithEqualOps(binary, opcode, new ArrayList<JCExpression>());
	}
	private List<JCExpression> consecutiveInfixExpressionsWithEqualOps(
			JCBinary binary, Tag opcode, List<JCExpression> consecutive) {

		if( opcode.equals(binary.getTag())) {
			if( consecutive != null ) {
				JCExpression left = binary.getLeftOperand();
				if( left instanceof JCBinary jcb) {
					consecutive = consecutiveInfixExpressionsWithEqualOps(jcb, opcode, consecutive);
				} else {
					consecutive.add(left);
				}
			}
			if( consecutive != null ) {
				JCExpression right = binary.getRightOperand();
				if( right instanceof JCBinary jcb) {
					consecutive = consecutiveInfixExpressionsWithEqualOps(jcb, opcode, consecutive);
				} else {
					consecutive.add(right);
				}
			}
			return consecutive;
		}
		return null;
	}

	private Expression handleInfixExpression(JCBinary binary, JCExpression javac) {
		List<JCExpression> conseq = consecutiveInfixExpressionsWithEqualOps(binary, binary.getTag());
		if( conseq != null && conseq.size() > 2 ) {
			return handleConsecutiveInfixExpression(binary, javac, conseq);
		}

		InfixExpression res = this.ast.newInfixExpression();
		commonSettings(res, javac);

		Expression left = convertExpression(binary.getLeftOperand());
		if (left != null) {
			res.setLeftOperand(left);
		}
		Expression right = convertExpression(binary.getRightOperand());
		if (right != null) {
			res.setRightOperand(right);
		}
		res.setOperator(binaryTagToInfixOperator(binary.getTag()));
		return res;
	}

	private Expression handleConsecutiveInfixExpression(JCBinary binary, JCExpression javac,
			List<JCExpression> conseq) {

		InfixExpression res = this.ast.newInfixExpression();
		commonSettings(res, javac);

		Expression left = convertExpression(conseq.get(0));
		if (left != null) {
			res.setLeftOperand(left);
		}
		Expression right = convertExpression(conseq.get(1));
		if (right != null) {
			res.setRightOperand(right);
		}
		for( int i = 2; i < conseq.size(); i++ ) {
			res.extendedOperands().add(convertExpression(conseq.get(i)));
		}

		res.setOperator(binaryTagToInfixOperator(binary.getTag()));
		return res;
	}

	private InfixExpression.Operator binaryTagToInfixOperator(Tag t) {
		return switch (t) {
			case OR -> InfixExpression.Operator.CONDITIONAL_OR;
			case AND -> InfixExpression.Operator.CONDITIONAL_AND;
			case BITOR -> InfixExpression.Operator.OR;
			case BITXOR -> InfixExpression.Operator.XOR;
			case BITAND -> InfixExpression.Operator.AND;
			case EQ -> InfixExpression.Operator.EQUALS;
			case NE -> InfixExpression.Operator.NOT_EQUALS;
			case LT -> InfixExpression.Operator.LESS;
			case GT -> InfixExpression.Operator.GREATER;
			case LE -> InfixExpression.Operator.LESS_EQUALS;
			case GE -> InfixExpression.Operator.GREATER_EQUALS;
			case SL -> InfixExpression.Operator.LEFT_SHIFT;
			case SR -> InfixExpression.Operator.RIGHT_SHIFT_SIGNED;
			case USR -> InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED;
			case PLUS -> InfixExpression.Operator.PLUS;
			case MINUS -> InfixExpression.Operator.MINUS;
			case MUL -> InfixExpression.Operator.TIMES;
			case DIV -> InfixExpression.Operator.DIVIDE;
			case MOD -> InfixExpression.Operator.REMAINDER;
			default -> null;
		};
	}


	/**
	 * precondition: you've checked all the segments are identifier that can be used in a qualified name
	 */
	private Name convertQualifiedName(JCFieldAccess fieldAccess) {
		JCExpression parent = fieldAccess.getExpression();
		Name parentName;
		if (parent instanceof JCFieldAccess parentFieldAccess) {
			parentName = convertQualifiedName(parentFieldAccess);
		} else if (parent instanceof JCIdent parentIdent) {
			parentName = convertName(parentIdent.getName());
		} else {
			throw new IllegalArgumentException("Unrecognized javac AST node type: " + parent.getClass().getCanonicalName());
		}
		commonSettings(parentName, parent);
		SimpleName segmentName = (SimpleName)convertName(fieldAccess.getIdentifier());
		int endPos = fieldAccess.getEndPosition(this.javacCompilationUnit.endPositions);
		int startPos = endPos - fieldAccess.getIdentifier().length();
		segmentName.setSourceRange(startPos, fieldAccess.getIdentifier().length());
		QualifiedName res = this.ast.newQualifiedName(parentName, segmentName);
		commonSettings(res, fieldAccess);
		return res;
	}

	private Expression convertExpression(JCExpression javac) {
		Expression ret = convertExpressionImpl(javac);
		if( ret != null )
			return ret;

		// Handle errors or default situation
		if (javac instanceof JCErroneous error) {
			if (error.getErrorTrees().size() == 1) {
				JCTree tree = error.getErrorTrees().get(0);
				if (tree instanceof JCExpression nestedExpr) {
					try {
						return convertExpression(nestedExpr);
					} catch (Exception ex) {
						// pass-through: do not break when attempting such reconcile
					}
				}
			}
		}
		if( shouldRecoverWithSimpleName(javac)) {
			var res = this.ast.newSimpleName(FAKE_IDENTIFIER);
			res.setFlags(ASTNode.RECOVERED);
			commonSettings(res, javac);
			return res;
		}
		return null;
	}

	private boolean shouldRecoverWithSimpleName(JCExpression javac) {
		if( javac instanceof JCNewClass)
			return false;
		return true;
	}
	private Pattern convert(JCPattern jcPattern) {
		if (this.ast.apiLevel >= AST.JLS21_INTERNAL) {
			if (jcPattern instanceof JCBindingPattern jcBindingPattern) {
				TypePattern jdtPattern = this.ast.newTypePattern();
				commonSettings(jdtPattern, jcBindingPattern);
				jdtPattern.setPatternVariable((SingleVariableDeclaration)convertVariableDeclaration(jcBindingPattern.var));
				return jdtPattern;
			} else if (jcPattern instanceof JCRecordPattern jcRecordPattern) {
				RecordPattern jdtPattern = this.ast.newRecordPattern();
				commonSettings(jdtPattern, jcRecordPattern);
				jdtPattern.setPatternType(convertToType(jcRecordPattern.deconstructor));
				for (JCPattern nestedJcPattern : jcRecordPattern.nested) {
					jdtPattern.patterns().add(convert(nestedJcPattern));
				}
				return jdtPattern;
			} else if (jcPattern instanceof JCAnyPattern jcAnyPattern) {
				TypePattern jdtPattern = this.ast.newTypePattern();
				commonSettings(jdtPattern, jcAnyPattern);
				VariableDeclarationFragment variable = this.ast.newVariableDeclarationFragment();
				commonSettings(variable, jcAnyPattern);
				variable.setName(this.ast.newSimpleName("_"));
				jdtPattern.setPatternVariable(variable);
				return jdtPattern;
			}
		}
		return null;
	}

	private ArrayInitializer createArrayInitializerFromJCNewArray(JCNewArray jcNewArray) {
		ArrayInitializer initializer = this.ast.newArrayInitializer();
		commonSettings(initializer, jcNewArray);
		if (!jcNewArray.getInitializers().isEmpty()) {
			jcNewArray.getInitializers().stream().map(this::convertExpression).filter(Objects::nonNull).forEach(initializer.expressions()::add);
			int start = ((Expression)initializer.expressions().getFirst()).getStartPosition() - 1;
			while (start >= 0 && this.rawText.charAt(start) != '{') {
				start--;
			}
			Expression lastExpr = (Expression)initializer.expressions().getLast();
			int end = lastExpr.getStartPosition() + lastExpr.getLength();
			while (end < this.rawText.length() && this.rawText.charAt(end) != '}') {
				end++;
			}
			initializer.setSourceRange(start, end - start + 1);
		}
		return initializer;
	}

	private AnonymousClassDeclaration createAnonymousClassDeclaration(JCClassDecl javacAnon, ASTNode parent) {
		AnonymousClassDeclaration anon = this.ast.newAnonymousClassDeclaration();
		commonSettings(anon, javacAnon);
		if (javacAnon.getMembers() != null) {
			List<JCTree> members = javacAnon.getMembers();
			for( int i = 0; i < members.size(); i++ ) {
				ASTNode decl = convertBodyDeclaration(members.get(i), anon);
				if( decl != null ) {
					anon.bodyDeclarations().add(decl);
				}
			}
		}
		return anon;
	}

	/**
	 *
	 * @param tree
	 * @param pos
	 * @return a list of dimensions for the given type. If target < JLS8, then
	 *         it returns a list of null objects, of the right size for the dimensions
	 */
	private List<Dimension> convertDimensionsAfterPosition(JCTree tree, int pos) {
		if (tree == null) {
			return List.of();
		}
		List<Dimension> res = new ArrayList<>();
		JCTree elem = tree;
		do {
			if( elem.pos >= pos) {
				if (elem instanceof JCArrayTypeTree arrayType) {
					if (this.ast.apiLevel < AST.JLS8_INTERNAL) {
						res.add(null);
					} else {
						Dimension dimension = this.ast.newDimension();
						res.add(dimension);
						// Would be better to use a Tokenizer here that is capable of skipping comments
						int startPosition = this.rawText.indexOf('[', arrayType.pos);
						int endPosition = this.rawText.indexOf(']', startPosition);
						dimension.setSourceRange(startPosition, endPosition - startPosition + 1);
					}
					elem = arrayType.getType();
				} else if (elem instanceof JCAnnotatedType annotated && annotated.getUnderlyingType() instanceof JCArrayTypeTree arrayType) {
					if (this.ast.apiLevel < AST.JLS8_INTERNAL) {
						res.add(null);
					} else {
						Dimension dimension = this.ast.newDimension();
						annotated.getAnnotations().stream()
							.map(this::convert)
							.forEach(dimension.annotations()::add);
						// Would be better to use a Tokenizer here that is capable of skipping comments
						int startPosition = this.rawText.indexOf('[', arrayType.pos);
						int endPosition = this.rawText.indexOf(']', startPosition);
						dimension.setSourceRange(startPosition, endPosition - startPosition + 1);
						res.add(dimension);
					}
					elem = arrayType.getType();
				} else {
					elem = null;
				}
			} else {
				elem = null;
			}
		} while (elem != null);
		return res;
	}

	private JCTree unwrapDimensions(JCTree tree, int count) {
		JCTree elem = tree;
		while (count > 0) {
			if (elem instanceof JCArrayTypeTree arrayTree) {
				elem = arrayTree.getType();
				count--;
			} else if (elem instanceof JCAnnotatedType annotated && annotated.getUnderlyingType() instanceof JCArrayTypeTree arrayType) {
				elem = arrayType.getType();
				count--;
			} else {
				count = 0;
			}

		}
		return elem;
	}

	private int countDimensions(JCTree tree) {
		JCTree elem = tree;
		int count = 0;
		boolean done = false;
		while (!done) {
			if (elem instanceof JCArrayTypeTree arrayTree) {
				elem = arrayTree.getType();
				count++;
			} else if (elem instanceof JCAnnotatedType annotated && annotated.getUnderlyingType() instanceof JCArrayTypeTree arrayType) {
				elem = arrayType.getType();
				count++;
			} else {
				done = true;
			}
		}
		return count;
	}


	private SuperMethodInvocation convertSuperMethodInvocation(JCMethodInvocation javac) {
		SuperMethodInvocation res = this.ast.newSuperMethodInvocation();
		commonSettings(res, javac);
		javac.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);

		//res.setFlags(javac.getFlags() | ASTNode.MALFORMED);
		if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
			javac.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
		}
		return res;
	}

	private SuperConstructorInvocation convertSuperConstructorInvocation(JCMethodInvocation javac) {
		SuperConstructorInvocation res = this.ast.newSuperConstructorInvocation();
		commonSettings(res, javac);
		ensureTrailingSemicolonInRange(res);
		javac.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);

		//res.setFlags(javac.getFlags() | ASTNode.MALFORMED);
		if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
			javac.getTypeArguments().stream()
				.map(this::convertToType)
				.filter(Objects::nonNull)
				.forEach(res.typeArguments()::add);
		}
		if( javac.getMethodSelect() instanceof JCFieldAccess jcfa && jcfa.selected != null ) {
			res.setExpression(convertExpression(jcfa.selected));
		}
		return res;
	}


	private ConstructorInvocation convertThisConstructorInvocation(JCMethodInvocation javac) {
		ConstructorInvocation res = this.ast.newConstructorInvocation();
		commonSettings(res, javac);
		// add the trailing `;`
		// it's always there, since this is always a statement, since this is always `this();` or `super();`
		// (or equivalent with type parameters)
		res.setSourceRange(res.getStartPosition(), res.getLength() + 1);
		javac.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);
		if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
			javac.getTypeArguments().stream()
				.map(this::convertToType)
				.filter(Objects::nonNull)
				.forEach(res.typeArguments()::add);
		}
		return res;
	}

	private Expression convertLiteral(JCLiteral literal) {
		Object value = literal.getValue();
		if (value instanceof Number) {
			// to check if the literal is actually a prefix expression of it is a hex
			// negative value we need to check the source char value.
			char firstChar = this.rawText.substring(literal.getStartPosition(), literal.getStartPosition() + 1)
					.charAt(0);

			if( firstChar != '-' ) {
				NumberLiteral res = this.ast.newNumberLiteral();
				commonSettings(res, literal);
				String fromSrc = this.rawText.substring(res.getStartPosition(), res.getStartPosition() + res.getLength());
				try {
					res.setToken(fromSrc);
				} catch (IllegalArgumentException ex) {
					// probably some lombok oddity, let's ignore
				}
				return res;
			} else {
				PrefixExpression res = this.ast.newPrefixExpression();
				commonSettings(res, literal);

				String fromSrc = this.rawText.substring(res.getStartPosition()+1, res.getStartPosition() + res.getLength());
				NumberLiteral operand = this.ast.newNumberLiteral();
				commonSettings(operand, literal);
				operand.setToken(fromSrc);

				res.setOperand(operand);
				res.setOperator(Operator.MINUS);
				return res;
			}
		}
		if (value instanceof String string) {
			boolean malformed = false;
			if (this.rawText.charAt(literal.pos) == '"'
					&& this.rawText.charAt(literal.pos + 1) == '"'
					&& this.rawText.charAt(literal.pos + 2) == '"') {
				if (this.ast.apiLevel() > AST.JLS14) {
					TextBlock res = this.ast.newTextBlock();
					commonSettings(res, literal);
					String rawValue = this.rawText.substring(literal.pos, literal.getEndPosition(this.javacCompilationUnit.endPositions));
					res.internalSetEscapedValue(rawValue, string);
					return res;
				}
				malformed = true;
			}
			StringLiteral res = this.ast.newStringLiteral();
			commonSettings(res, literal);
			int startPos = res.getStartPosition();
			int len = res.getLength();
			if( string.length() != len && len > 2) {
				try {
					string = this.rawText.substring(startPos, startPos + len);
					if (!string.startsWith("\"")) {
						string = '"' + string;
					}
					if (!string.endsWith("\"")) {
						string = string + '"';
					}
					res.internalSetEscapedValue(string);
				} catch(IndexOutOfBoundsException ignore) {
					res.setLiteralValue(string);  // TODO: we want the token here
				}
			} else {
				res.setLiteralValue(string);  // TODO: we want the token here
			}
			if (malformed) {
				res.setFlags(res.getFlags() | ASTNode.MALFORMED);
			}
			return res;
		}
		if (value instanceof Boolean string) {
			BooleanLiteral res = this.ast.newBooleanLiteral(string.booleanValue());
			commonSettings(res, literal);
			return res;
		}
		if (value == null) {
			NullLiteral res = this.ast.newNullLiteral();
			commonSettings(res, literal);
			return res;
		}
		if (value instanceof Character v) {
			CharacterLiteral res = this.ast.newCharacterLiteral();
			commonSettings(res, literal);
			res.setCharValue(v.charValue());
			return res;
		}
		throw new UnsupportedOperationException("Not supported yet " + literal + "\n of type" + literal.getClass().getName());
	}

	private Statement convertStatement(JCStatement javac, ASTNode parent) {
		int endPos = TreeInfo.getEndPos(javac, this.javacCompilationUnit.endPositions);
		int preferredPos = javac.getPreferredPosition();
		if (endPos < preferredPos) {
			return null;
		}
		if (javac instanceof JCReturn returnStatement) {
			ReturnStatement res = this.ast.newReturnStatement();
			commonSettings(res, javac);
			if (returnStatement.getExpression() != null) {
				res.setExpression(convertExpression(returnStatement.getExpression()));
			}
			return res;
		}
		if (javac instanceof JCBlock block) {
			return convertBlock(block);
		}
		if (javac instanceof JCExpressionStatement jcExpressionStatement) {
			JCExpression jcExpression = jcExpressionStatement.getExpression();
			if (jcExpression instanceof JCMethodInvocation jcMethodInvocation
				&& jcMethodInvocation.getMethodSelect() instanceof JCIdent methodName
				&& Objects.equals(methodName.getName(), Names.instance(this.context)._this)) {
				return convertThisConstructorInvocation(jcMethodInvocation);
			}
			if (jcExpressionStatement.getExpression() == null) {
				return null;
			}
			if (jcExpressionStatement.getExpression() instanceof JCErroneous jcError) {
				if (jcError.getErrorTrees().size() == 1) {
					JCTree tree = jcError.getErrorTrees().get(0);
					if (tree instanceof JCStatement nestedStmt) {
						try {
							Statement stmt = convertStatement(nestedStmt, parent);
							if( stmt != null )
								stmt.setFlags(stmt.getFlags() | ASTNode.RECOVERED);
							return stmt;
						} catch (Exception ex) {
							// pass-through: do not break when attempting such reconcile
						}
					}
					if (tree instanceof JCExpression expr) {
						Expression expression = convertExpression(expr);
						ExpressionStatement res = this.ast.newExpressionStatement(expression);
						commonSettings(res, javac);
						return res;
					}
				}
				parent.setFlags(parent.getFlags() | ASTNode.MALFORMED);
				return null;
			}
			boolean uniqueCaseFound = false;
			if (jcExpressionStatement.getExpression() instanceof JCMethodInvocation methodInvocation) {
				JCExpression nameExpr = methodInvocation.getMethodSelect();
				if (nameExpr instanceof JCIdent ident) {
					if (Objects.equals(ident.getName(), Names.instance(this.context)._super)) {
						uniqueCaseFound = true;
					}
				}
				if (nameExpr instanceof JCFieldAccess jcfa) {
					if (Objects.equals(jcfa.getIdentifier(), Names.instance(this.context)._super)) {
						uniqueCaseFound = true;
					}
				}
			}
			if( uniqueCaseFound ) {
				return convertSuperConstructorInvocation((JCMethodInvocation)jcExpressionStatement.getExpression());
			}
			ExpressionStatement res = this.ast.newExpressionStatement(convertExpression(jcExpressionStatement.getExpression()));
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCVariableDecl jcVariableDecl) {
			VariableDeclarationFragment fragment = createVariableDeclarationFragment(jcVariableDecl);
			List<ASTNode> sameStartPosition = new ArrayList<>();
 			if (parent instanceof Block decl && jcVariableDecl.vartype != null) {
				decl.statements().stream().filter(x -> x instanceof VariableDeclarationStatement)
				.filter(x -> ((VariableDeclarationStatement)x).getType().getStartPosition() == jcVariableDecl.vartype.getStartPosition())
				.forEach(x -> sameStartPosition.add((ASTNode)x));
			} else if( parent instanceof ForStatement decl && jcVariableDecl.vartype != null) {
				// TODO somehow doubt this will work as expected
				decl.initializers().stream().filter(x -> x instanceof VariableDeclarationExpression)
				.filter(x -> ((VariableDeclarationExpression)x).getType().getStartPosition() == jcVariableDecl.vartype.getStartPosition())
				.forEach(x -> sameStartPosition.add((ASTNode)x));
			}
			if( sameStartPosition.size() >= 1 ) {
				Object obj0 = sameStartPosition.get(0);
				if( obj0 instanceof VariableDeclarationStatement fd ) {
					fd.fragments().add(fragment);
					int newParentEnd = fragment.getStartPosition() + fragment.getLength();
					fd.setSourceRange(fd.getStartPosition(), newParentEnd - fd.getStartPosition() + 1);
					removeSurroundingWhitespaceFromRange(fd);
				} else if( obj0 instanceof VariableDeclarationExpression fd ) {
					fd.fragments().add(fragment);
					int newParentEnd = fragment.getStartPosition() + fragment.getLength();
					fd.setSourceRange(fd.getStartPosition(), newParentEnd - fd.getStartPosition() + 1);
					removeTrailingSemicolonFromRange(fd);
					removeSurroundingWhitespaceFromRange(fd);
				}
				return null;
			}
			VariableDeclarationStatement res = this.ast.newVariableDeclarationStatement(fragment);
			commonSettings(res, javac);

			if (jcVariableDecl.vartype != null) {
				if( jcVariableDecl.vartype instanceof JCArrayTypeTree jcatt) {
					int extraDims = 0;
					if( fragment.extraArrayDimensions > 0 ) {
						extraDims = fragment.extraArrayDimensions;
					} else if( this.ast.apiLevel > AST.JLS4_INTERNAL && fragment.extraDimensions() != null && fragment.extraDimensions().size() > 0 ) {
						extraDims = fragment.extraDimensions().size();
					}
					res.setType(convertToType(unwrapDimensions(jcatt, extraDims)));
				} else {
					res.setType(convertToType(findBaseType(jcVariableDecl.vartype)));
				}
			} else if( jcVariableDecl.declaredUsingVar() ) {
				SimpleType st = this.ast.newSimpleType(this.ast.newSimpleName("var"));
				st.setSourceRange(javac.getStartPosition(), 3);
				res.setType(st);
			}
			if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
				res.modifiers().addAll(convert(jcVariableDecl.getModifiers(), res));
			} else {
				JCModifiers mods = jcVariableDecl.getModifiers();
				int[] total = new int[] {0};
				mods.getFlags().forEach(x -> {total[0] += modifierToFlagVal(x);});
				res.internalSetModifiers(total[0]);
			}
			return res;
		}
		if (javac instanceof JCIf ifStatement) {
			return convertIfStatement(ifStatement);
		}
		if (javac instanceof JCThrow throwStatement) {
			ThrowStatement res = this.ast.newThrowStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(throwStatement.getExpression()));
			return res;
		}
		if (javac instanceof JCTry tryStatement) {
			return convertTryStatement(tryStatement, parent);
		}
		if (javac instanceof JCSynchronized jcSynchronized) {
			SynchronizedStatement res = this.ast.newSynchronizedStatement();
			commonSettings(res, javac);
			JCExpression syncExpr = jcSynchronized.getExpression();
			if( syncExpr instanceof JCParens jcp) {
				syncExpr = jcp.getExpression();
			}
			res.setExpression(convertExpression(syncExpr));
			res.setBody(convertBlock(jcSynchronized.getBlock()));
			return res;
		}
		if (javac instanceof JCForLoop jcForLoop) {
			ForStatement res = this.ast.newForStatement();
			commonSettings(res, javac);
			Statement stmt = convertStatement(jcForLoop.getStatement(), res);
			if( stmt != null )
				res.setBody(stmt);
			var initializerIt = jcForLoop.getInitializer().iterator();
			while(initializerIt.hasNext()) {
				Expression expr = convertStatementToExpression(initializerIt.next(), res);
				if( expr != null )
					res.initializers().add(expr);
			}
			if (jcForLoop.getCondition() != null) {
				Expression expr = convertExpression(jcForLoop.getCondition());
				if( expr != null )
					res.setExpression(expr);
			}

			Iterator<JCExpressionStatement> updateIt = jcForLoop.getUpdate().iterator();
			while(updateIt.hasNext()) {
				Expression expr = convertStatementToExpression(updateIt.next(), res);
				if( expr != null )
					res.updaters().add(expr);
			}
			return res;
		}
		if (javac instanceof JCEnhancedForLoop jcEnhancedForLoop) {
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				EnhancedForStatement res = this.ast.newEnhancedForStatement();
				commonSettings(res, javac);
				res.setParameter((SingleVariableDeclaration)convertVariableDeclaration(jcEnhancedForLoop.getVariable()));
				Expression expr = convertExpression(jcEnhancedForLoop.getExpression());
				if( expr != null )
					res.setExpression(expr);
				Statement stmt = convertStatement(jcEnhancedForLoop.getStatement(), res);
				if( stmt != null )
					res.setBody(stmt);
				return res;
			} else {
				EmptyStatement res = this.ast.newEmptyStatement();
				commonSettings(res, javac);
				return res;
			}
		}
		if (javac instanceof JCBreak jcBreak) {
			BreakStatement res = this.ast.newBreakStatement();
			commonSettings(res, javac);
			if (jcBreak.getLabel() != null) {
				res.setLabel((SimpleName)convertName(jcBreak.getLabel()));
			}
			return res;
		}
		if (javac instanceof JCSwitch jcSwitch) {
			SwitchStatement res = this.ast.newSwitchStatement();
			commonSettings(res, javac);
			JCExpression switchExpr = jcSwitch.getExpression();
			if( switchExpr instanceof JCParens jcp) {
				switchExpr = jcp.getExpression();
			}
			res.setExpression(convertExpression(switchExpr));
			jcSwitch.getCases().stream()
				.flatMap(switchCase -> {
					List<JCStatement> stmts = new ArrayList<>();
					switch(switchCase.getCaseKind()) {
						case CaseKind.STATEMENT: {
							int numStatements = switchCase.getStatements() != null ? switchCase.getStatements().size()
									: 0;
							stmts.add(switchCase);
							if (numStatements > 0) {
								stmts.addAll(switchCase.getStatements());
							}
							return stmts.stream();
						}
						case CaseKind.RULE: {
							stmts.add(switchCase);
							JCTree body = switchCase.getBody();
							if (body instanceof JCExpressionStatement stmt) {
								stmts.add(stmt);
							}
						}
					}
					return stmts.stream();
				}).map(x -> convertStatement(x, res))
				.filter(x -> x != null)
				.forEach(res.statements()::add);
			return res;
		}
		if (javac instanceof JCCase jcCase) {
			return convertSwitchCase(jcCase);
		}
		if (javac instanceof JCWhileLoop jcWhile) {
			WhileStatement res = this.ast.newWhileStatement();
			commonSettings(res, javac);
			JCExpression expr = jcWhile.getCondition();
			if( expr instanceof JCParens jcp) {
				expr = jcp.getExpression();
			}
			res.setExpression(convertExpression(expr));
			Statement body = convertStatement(jcWhile.getStatement(), res);
			if( body != null )
				res.setBody(body);
			return res;
		}
		if (javac instanceof JCDoWhileLoop jcDoWhile) {
			DoStatement res = this.ast.newDoStatement();
			commonSettings(res, javac);
			JCExpression expr = jcDoWhile.getCondition();
			if( expr instanceof JCParens jcp) {
				expr = jcp.getExpression();
			}
			Expression expr1 = convertExpression(expr);
			if( expr != null )
				res.setExpression(expr1);

			Statement body = convertStatement(jcDoWhile.getStatement(), res);
			if( body != null )
				res.setBody(body);
			return res;
		}
		if (javac instanceof JCYield jcYield) {
			YieldStatement res = this.ast.newYieldStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcYield.getValue()));
			return res;
		}
		if (javac instanceof JCContinue jcContinue) {
			ContinueStatement res = this.ast.newContinueStatement();
			commonSettings(res, javac);
			if (jcContinue.getLabel() != null) {
				res.setLabel((SimpleName)convertName(jcContinue.getLabel()));
			}
			return res;
		}
		if (javac instanceof JCLabeledStatement jcLabel) {
			LabeledStatement res = this.ast.newLabeledStatement();
			commonSettings(res, javac);
			res.setLabel((SimpleName)convertName(jcLabel.getLabel()));
			Statement stmt = convertStatement(jcLabel.getStatement(), res);
			if( stmt != null )
				res.setBody(stmt);
			return res;
		}
		if (javac instanceof JCAssert jcAssert) {
			AssertStatement res =this.ast.newAssertStatement();
			commonSettings(res, javac);
			Expression expr = convertExpression(jcAssert.getCondition());
			if( expr != null )
				res.setExpression(expr);
			if( jcAssert.getDetail() != null ) {
				Expression det = convertExpression(jcAssert.getDetail());
				if( det != null )
					res.setMessage(det);
			}
			return res;
		}
		if (javac instanceof JCClassDecl jcclass) {
			TypeDeclarationStatement res = this.ast.newTypeDeclarationStatement(convertClassDecl(jcclass, parent));
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCSkip) {
			EmptyStatement res = this.ast.newEmptyStatement();
			commonSettings(res, javac);
			return res;
		}
		throw new UnsupportedOperationException("Missing support to convert " + javac + "of type " + javac.getClass().getName());
	}

	private Statement convertSwitchCase(JCCase jcCase) {
		SwitchCase res = this.ast.newSwitchCase();
		commonSettings(res, jcCase);
		if( this.ast.apiLevel >= AST.JLS14_INTERNAL) {
			if (jcCase.getGuard() != null && (jcCase.getLabels().size() > 1 || jcCase.getLabels().get(0) instanceof JCPatternCaseLabel)) {
				GuardedPattern guardedPattern = this.ast.newGuardedPattern();
				guardedPattern.setExpression(convertExpression(jcCase.getGuard()));
				guardedPattern.setRestrictedIdentifierStartPosition(jcCase.guard.getStartPosition() - 5); // javac gives start position without "when " while jdt expects it with
				if (jcCase.getLabels().length() > 1) {
					int start = Integer.MAX_VALUE;
					int end = Integer.MIN_VALUE;
					EitherOrMultiPattern eitherOrMultiPattern = this.ast.newEitherOrMultiPattern();
					for (JCCaseLabel label : jcCase.getLabels()) {
						if (label.pos < start) {
							start = label.pos;
						}
						if (end < label.getEndPosition(this.javacCompilationUnit.endPositions)) {
							end = label.getEndPosition(this.javacCompilationUnit.endPositions);
						}
						if (label instanceof JCPatternCaseLabel jcPattern) {
							eitherOrMultiPattern.patterns().add(convert(jcPattern.getPattern()));
						}
						// skip over any constants, they are not valid anyways
					}
					eitherOrMultiPattern.setSourceRange(start, end - start);
					guardedPattern.setPattern(eitherOrMultiPattern);
				} else if (jcCase.getLabels().length() == 1) {
					if (jcCase.getLabels().get(0) instanceof JCPatternCaseLabel jcPattern) {
						guardedPattern.setPattern(convert(jcPattern.getPattern()));
					} else {
						// see same above note regarding guarded case labels using constants
						throw new UnsupportedOperationException("cannot convert case label: " + jcCase.getLabels().get(0));
					}
				}
				int start = guardedPattern.getPattern().getStartPosition();
				int end = guardedPattern.getExpression().getStartPosition() + guardedPattern.getExpression().getLength();
				guardedPattern.setSourceRange(start, end - start);
				res.expressions().add(guardedPattern);
			} else {
				if (jcCase.getLabels().length() == 1 && jcCase.getLabels().get(0) instanceof JCPatternCaseLabel jcPattern) {
					Pattern p = convert(jcPattern.getPattern());
					if( p != null ) {
						int start = jcPattern.getStartPosition();
						p.setSourceRange(start, jcPattern.getEndPosition(this.javacCompilationUnit.endPositions)-start);
						res.expressions().add(p);
					}
				} else {
					// Override length to just be `case blah:`
					for (JCCaseLabel jcLabel : jcCase.getLabels()) {
						switch (jcLabel) {
						case JCConstantCaseLabel constantLabel: {
							if (constantLabel.expr.toString().equals("null")) {
								res.expressions().add(this.ast.newNullLiteral());
							}
							break;
						}
						case JCDefaultCaseLabel defaultCase: {
							if (jcCase.getLabels().size() != 1) {
								res.expressions().add(this.ast.newCaseDefaultExpression());
							}
							break;
						}
						default: {
							break;
						}
						}
					}
					int start1 = res.getStartPosition();
					int colon = this.rawText.indexOf(":", start1);
					if( colon != -1 ) {
						res.setSourceRange(start1, colon - start1 + 1);
					}
				}
				jcCase.getExpressions().stream().map(this::convertExpression).forEach(res.expressions()::add);
			}
			res.setSwitchLabeledRule(jcCase.getCaseKind() == CaseKind.RULE);
		} else {
			// Override length to just be `case blah:`
			int start1 = res.getStartPosition();
			int colon = this.rawText.indexOf(":", start1);
			if( colon != -1 ) {
				res.setSourceRange(start1, colon - start1 + 1);
			}
			List<JCExpression> l = jcCase.getExpressions();
			if( l.size() == 1 ) {
				res.setExpression(convertExpression(l.get(0)));
			} else if( l.size() == 0 ) {
				res.setExpression(null);
			}
		}
		// jcCase.getStatements is processed as part of JCSwitch conversion
		return res;
	}

	private Expression convertStatementToExpression(JCStatement javac, ASTNode parent) {
		if (javac instanceof JCExpressionStatement jcExpressionStatement) {
			return convertExpression(jcExpressionStatement.getExpression());
		}
		Statement javacStatement = convertStatement(javac, parent);
		if (javacStatement instanceof VariableDeclarationStatement decl && decl.fragments().size() == 1) {
			javacStatement.delete();
			VariableDeclarationFragment fragment = (VariableDeclarationFragment)decl.fragments().get(0);
			fragment.delete();
			VariableDeclarationExpression jdtVariableDeclarationExpression = this.ast.newVariableDeclarationExpression(fragment);
			commonSettings(jdtVariableDeclarationExpression, javac);
			if (javac instanceof JCVariableDecl jcvd && jcvd.vartype != null) {
				if( jcvd.vartype instanceof JCArrayTypeTree jcatt) {
					int extraDims = 0;
					if( fragment.extraArrayDimensions > 0 ) {
						extraDims = fragment.extraArrayDimensions;
					} else if( this.ast.apiLevel > AST.JLS4_INTERNAL && fragment.extraDimensions() != null && fragment.extraDimensions().size() > 0 ) {
						extraDims = fragment.extraDimensions().size();
					}
					jdtVariableDeclarationExpression.setType(convertToType(unwrapDimensions(jcatt, extraDims)));
				} else {
					jdtVariableDeclarationExpression.setType(convertToType(findBaseType(jcvd.vartype)));
				}
			}
			return jdtVariableDeclarationExpression;
		}
		return null;
	}

	private JCTree findBaseType(JCExpression vartype) {
		if( vartype instanceof JCArrayTypeTree jcatt) {
			return findBaseType(jcatt.elemtype);
		}
		return vartype;
	}

	private Block convertBlock(JCBlock javac) {
		Block res = this.ast.newBlock();
		commonSettings(res, javac);
		if (javac.getStatements() != null) {
			for (JCStatement next : javac.getStatements()) {
				Statement s = convertStatement(next, res);
				if( s != null ) {
					res.statements().add(s);
				}
			}
		}
		return res;
	}

	private TryStatement convertTryStatement(JCTry javac, ASTNode parent) {
		TryStatement res = this.ast.newTryStatement();
		commonSettings(res, javac);
		res.setBody(convertBlock(javac.getBlock()));
		if (javac.finalizer != null) {
			res.setFinally(convertBlock(javac.getFinallyBlock()));
		}

		if( this.ast.apiLevel >= AST.JLS8_INTERNAL) {
			if( javac.getResources().size() > 0) {
				Iterator<JCTree> it = javac.getResources().iterator();
				while(it.hasNext()) {
					ASTNode working = convertTryResource(it.next(), parent);
					if( working instanceof VariableDeclarationExpression) {
						res.resources().add(working);
					} else if( this.ast.apiLevel >= AST.JLS9_INTERNAL && working instanceof Name){
						res.resources().add(working);
					} else {
						res.setFlags(res.getFlags() | ASTNode.MALFORMED);
					}
				}
			} else {
				res.setFlags(res.getFlags() | ASTNode.MALFORMED);
			}
		}
		javac.getCatches().stream().map(this::convertCatcher).forEach(res.catchClauses()::add);
		return res;
	}

	private Expression convertTryResource(JCTree javac, ASTNode parent) {
		if (javac instanceof JCVariableDecl decl) {
			var converted = convertVariableDeclaration(decl);
			final VariableDeclarationFragment fragment;
			if (converted instanceof VariableDeclarationFragment f) {
				fragment = f;
			} else if (converted instanceof SingleVariableDeclaration single) {
				single.delete();
				this.domToJavac.remove(single);
				fragment = this.ast.newVariableDeclarationFragment();
				commonSettings(fragment, javac);
				fragment.setFlags(single.getFlags());
				SimpleName name = (SimpleName)single.getName().clone(this.ast);
				fragment.setName(name);
				Expression initializer = single.getInitializer();
				if (initializer != null) {
					initializer.delete();
					fragment.setInitializer(initializer);
				}
				if (parent.getAST().apiLevel() > AST.JLS4) {
					for (Dimension extraDimension : (List<Dimension>)single.extraDimensions()) {
						extraDimension.delete();
						fragment.extraDimensions().add(extraDimension);
					}
				}
			} else {
				fragment = this.ast.newVariableDeclarationFragment();
			}
			VariableDeclarationExpression res = this.ast.newVariableDeclarationExpression(fragment);
			commonSettings(res, javac);
			removeTrailingSemicolonFromRange(res);
			res.setType(convertToType(decl.getType()));
			if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
				res.modifiers().addAll(convert(decl.getModifiers(), res));
			} else {
				JCModifiers mods = decl.getModifiers();
				int[] total = new int[] {0};
				mods.getFlags().forEach(x -> {total[0] += modifierToFlagVal(x);});
				res.internalSetModifiers(total[0]);
			}
			return res;
		}
		if (javac instanceof JCExpression jcExpression) {
			return convertExpression(jcExpression);
		}
		return null;
	}

	private void removeTrailingSemicolonFromRange(ASTNode res) {
		removeTrailingCharFromRange(res, new char[] {';'});
	}
	private void ensureTrailingSemicolonInRange(ASTNode res) {
		int end = res.getStartPosition() + res.getLength();
		if( end < this.rawText.length() && this.rawText.charAt(end-1) != ';' && this.rawText.charAt(end) == ';') {
			// jdt expects semicolon to be part of the range
			res.setSourceRange(res.getStartPosition(), res.getLength() + 1);
		}
	}

	private void removeSurroundingWhitespaceFromRange(ASTNode res) {
		int start = res.getStartPosition();
		if (start >= 0 && start < this.rawText.length()) {
			String rawSource = this.rawText.substring(start, start + res.getLength());
			int trimLeading = rawSource.length() - rawSource.stripLeading().length();
			int trimTrailing = rawSource.length() - rawSource.stripTrailing().length();
			if( (trimLeading != 0 || trimTrailing != 0) && res.getLength() > trimLeading + trimTrailing ) {
				//String newContent = this.rawText.substring(start+trimLeading, start+trimLeading+res.getLength()-trimLeading-trimTrailing);
				res.setSourceRange(start+trimLeading, res.getLength() - trimLeading - trimTrailing);
			}
		}
	}

	private void removeTrailingCharFromRange(ASTNode res, char[] possible) {
		int endPos = res.getStartPosition() + res.getLength();
		char lastChar = this.rawText.charAt(endPos-1);
		boolean found = false;
		for( int i = 0; i < possible.length; i++ ) {
			if( lastChar == possible[i]) {
				found = true;
			}
		}
		if( found ) {
			res.setSourceRange(res.getStartPosition(), res.getLength() - 1);
		}
	}

	private CatchClause convertCatcher(JCCatch javac) {
		CatchClause res = this.ast.newCatchClause();
		commonSettings(res, javac);
		res.setBody(convertBlock(javac.getBlock()));
		res.setException((SingleVariableDeclaration)convertVariableDeclaration(javac.getParameter()));
		return res;
	}

	private IfStatement convertIfStatement(JCIf javac) {
		IfStatement res = this.ast.newIfStatement();
		commonSettings(res, javac);
		if (javac.getCondition() != null) {
			JCExpression expr = javac.getCondition();
			if( expr instanceof JCParens jpc) {
				res.setExpression(convertExpression(jpc.getExpression()));
			} else {
				res.setExpression(convertExpression(expr));
			}
		}
		if (javac.getThenStatement() != null) {
			Statement stmt = convertStatement(javac.getThenStatement(), res);
			if( stmt != null )
				res.setThenStatement(stmt);
		}
		if (javac.getElseStatement() != null) {
			Statement stmt = convertStatement(javac.getElseStatement(), res);
			if( stmt != null )
				res.setElseStatement(stmt);
		}
		return res;
	}

	/**
	 * ⚠️ node position in JCTree must be absolute
	 * @param javac
	 * @return
	 */
	Type convertToType(JCTree javac) {
		if (javac instanceof JCIdent ident) {
			Name name = convertName(ident.name);
			int len = FAKE_IDENTIFIER.equals(name.toString()) ? 0 : ident.name.length();
			name.setSourceRange(ident.getStartPosition(), len);
			SimpleType res = this.ast.newSimpleType(name);
			commonSettings(res, ident);
			commonSettings(name, ident);
			return res;
		}
		if (javac instanceof JCFieldAccess qualified) {
			try {
				if( qualified.getExpression() == null ) {
					Name qn = toName(qualified);
					commonSettings(qn, javac);
					SimpleType res = this.ast.newSimpleType(qn);
					commonSettings(res, qualified);
					return res;
				}
			} catch (Exception ex) {
			}
			// case of not translatable name, eg because of generics
			// TODO find a better check instead of relying on exception
			Type qualifierType = convertToType(qualified.getExpression());
			SimpleName simpleName = (SimpleName)convertName(qualified.getIdentifier());
			int simpleNameStart = this.rawText.indexOf(simpleName.getIdentifier(), qualifierType.getStartPosition() + qualifierType.getLength());
			if (simpleNameStart > 0) {
				simpleName.setSourceRange(simpleNameStart, simpleName.getIdentifier().length());
			} else if (simpleName.getIdentifier().isEmpty()){
				// the name second segment is invalid
				simpleName.delete();
				return qualifierType;
			} else {
				// lombok case
				// or empty (eg `test.`)
				simpleName.setSourceRange(qualifierType.getStartPosition(), 0);
			}
			if(qualifierType instanceof SimpleType simpleType && (ast.apiLevel() < AST.JLS8 || simpleType.annotations().isEmpty())) {
				simpleType.delete();
				Name parentName = simpleType.getName();
				parentName.setParent(null, null);
				QualifiedName name = this.ast.newQualifiedName(simpleType.getName(), simpleName);
				commonSettings(name, javac);
				int length = simpleType.getName().getLength() + 1 + simpleName.getLength();
				if (name.getStartPosition() >= 0) {
					name.setSourceRange(name.getStartPosition(), Math.max(0, length));
				}
				SimpleType res = this.ast.newSimpleType(name);
				commonSettings(res, javac);
				if (name.getStartPosition() >= 0) {
					res.setSourceRange(name.getStartPosition(), Math.max(0, length));
				}
				return res;
			} else {
				QualifiedType res = this.ast.newQualifiedType(qualifierType, simpleName);
				commonSettings(res, qualified);
				return res;
			}
		}
		if (javac instanceof JCPrimitiveTypeTree primitiveTypeTree) {
			PrimitiveType res = this.ast.newPrimitiveType(convert(primitiveTypeTree.getPrimitiveTypeKind()));
			commonSettings(res, primitiveTypeTree);
			return res;
		}
		if (javac instanceof JCTypeUnion union) {
			if (this.ast.apiLevel() > AST.JLS3) {
				UnionType res = this.ast.newUnionType();
				commonSettings(res, javac);
				union.getTypeAlternatives().stream()
					.map(this::convertToType)
					.filter(Objects::nonNull)
					.forEach(res.types()::add);
				return res;
			} else {
				Optional<Type> lastType = union.getTypeAlternatives().reverse().stream().map(this::convertToType).filter(Objects::nonNull).findFirst();
				lastType.ifPresent(a -> a.setFlags(a.getFlags() | ASTNode.MALFORMED));
				return lastType.get();
			}
		}
		if (javac instanceof JCArrayTypeTree jcArrayType) {
			Type t = convertToType(jcArrayType.getType());
			if (t == null) {
				return null;
			}
			ArrayType res;
			if (t instanceof ArrayType childArrayType && this.ast.apiLevel > AST.JLS4_INTERNAL) {
				res = childArrayType;
				res.dimensions().addFirst(this.ast.newDimension());
				commonSettings(res, jcArrayType.getType());
			} else {
				int dims = countDimensions(jcArrayType);
				res = this.ast.newArrayType(t);
				if( dims == 0 ) {
					commonSettings(res, jcArrayType);
				} else {
					int endPos = jcArrayType.getEndPosition(this.javacCompilationUnit.endPositions);
					if (endPos == -1) {
						endPos = jcArrayType.pos;
					}
					int startPos = jcArrayType.getStartPosition();
					try {
						String raw = this.rawText.substring(startPos, endPos);
						int ordinalEnd = ordinalIndexOf(raw, "]", dims);
						int ordinalStart = ordinalIndexOf(raw, "[", dims);
						if( ordinalEnd != -1 ) {
							commonSettings(res, jcArrayType, ordinalEnd + 1, true);
							if( this.ast.apiLevel >= AST.JLS8_INTERNAL ) {
								if( res.dimensions().size() > 0 ) {
									((Dimension)res.dimensions().get(0)).setSourceRange(startPos + ordinalStart, ordinalEnd - ordinalStart + 1);
								}
							}
							return res;
						}
					} catch( Throwable tErr) {
					}
					commonSettings(res, jcArrayType);
				}
			}
			return res;
		}
		if (javac instanceof JCTypeApply jcTypeApply) {
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				ParameterizedType res = this.ast.newParameterizedType(convertToType(jcTypeApply.getType()));
				commonSettings(res, javac);
				jcTypeApply.getTypeArguments().stream()
					.map(this::convertToType)
					.filter(Objects::nonNull)
					.forEach(res.typeArguments()::add);
				return res;
			} else {
				return convertToType(jcTypeApply.clazz);
			}
		}
		if (javac instanceof JCWildcard wc) {
			WildcardType res = this.ast.newWildcardType();
			if( wc.kind.kind == BoundKind.SUPER) {
				final Type bound = convertToType(wc.inner);
				res.setBound(bound, false);
			} else if( wc.kind.kind == BoundKind.EXTENDS) {
				final Type bound = convertToType(wc.inner);
				res.setBound(bound, true);
			}
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCTypeIntersection jcTypeIntersection) {
			IntersectionType res = this.ast.newIntersectionType();
			commonSettings(res, javac);
			jcTypeIntersection.getBounds().stream()
				.map(this::convertToType)
				.filter(Objects::nonNull)
				.forEach(res.types()::add);
			return res;
		}
		if (javac instanceof JCAnnotatedType jcAnnotatedType) {
			Type res = null;
			JCExpression jcpe = jcAnnotatedType.getUnderlyingType();
			if( jcAnnotatedType.getAnnotations() != null //
				&& !jcAnnotatedType.getAnnotations().isEmpty() //
				&& this.ast.apiLevel >= AST.JLS8_INTERNAL
				&& !(jcpe instanceof JCWildcard)) {
				if( jcpe instanceof JCFieldAccess jcfa2) {
					if( jcfa2.selected instanceof JCAnnotatedType || jcfa2.selected instanceof JCTypeApply) {
						QualifiedType nameQualifiedType = new QualifiedType(this.ast);
						commonSettings(nameQualifiedType, javac);
						nameQualifiedType.setQualifier(convertToType(jcfa2.selected));
						nameQualifiedType.setName(this.ast.newSimpleName(jcfa2.name.toString()));
						res = nameQualifiedType;
					} else {
						NameQualifiedType nameQualifiedType = new NameQualifiedType(this.ast);
						commonSettings(nameQualifiedType, javac);
						nameQualifiedType.setQualifier(toName(jcfa2.selected));
						nameQualifiedType.setName(this.ast.newSimpleName(jcfa2.name.toString()));
						res = nameQualifiedType;
					}
				} else if (jcpe instanceof JCIdent simpleType) {
					res = this.ast.newSimpleType(convertName(simpleType.getName()));
					commonSettings(res, javac);
				}
			}
			if (res == null) { // nothing specific
				res = convertToType(jcAnnotatedType.getUnderlyingType());
			}
			if (res instanceof AnnotatableType annotatableType && this.ast.apiLevel() >= AST.JLS8_INTERNAL) {
				for (JCAnnotation annotation : jcAnnotatedType.getAnnotations()) {
					annotatableType.annotations().add(convert(annotation));
				}
			} else if (res instanceof ArrayType arrayType) {
				if (this.ast.apiLevel() >= AST.JLS8 && !arrayType.dimensions().isEmpty()) {
					for (JCAnnotation annotation : jcAnnotatedType.getAnnotations()) {
						((Dimension)arrayType.dimensions().get(0)).annotations().add(convert(annotation));
					}
				}
			}
			return res;
		}
		if (javac instanceof JCErroneous || javac == null /* when there are syntax errors */) {
			// returning null could result in upstream errors, so return a fake type
			var res = this.ast.newSimpleType(this.ast.newSimpleName(FAKE_IDENTIFIER));
			if (javac instanceof JCErroneous err) {
				res.setSourceRange(err.getStartPosition(), 0);
			}
			res.setFlags(ASTNode.RECOVERED);
			return res;
		}
		ILog.get().warn("Not supported yet, converting to type type " + javac + " of class" + javac.getClass());
		return null;
	}
	public static int ordinalIndexOf(String str, String substr, int n) {
		int pos = str.indexOf(substr);
		while (--n > 0 && pos != -1) {
			pos = str.indexOf(substr, pos + 1);
		}
		return pos;
	}
	private Code convert(TypeKind javac) {
		return switch(javac) {
			case BOOLEAN -> PrimitiveType.BOOLEAN;
			case BYTE -> PrimitiveType.BYTE;
			case SHORT -> PrimitiveType.SHORT;
			case INT -> PrimitiveType.INT;
			case LONG -> PrimitiveType.LONG;
			case CHAR -> PrimitiveType.CHAR;
			case FLOAT -> PrimitiveType.FLOAT;
			case DOUBLE -> PrimitiveType.DOUBLE;
			case VOID -> PrimitiveType.VOID;
			default -> throw new IllegalArgumentException(javac.toString());
		};
	}

	private Annotation convert(JCAnnotation javac) {
		int startPos = javac.getStartPosition();
		int length = javac.getEndPosition(this.javacCompilationUnit.endPositions) - startPos;
		String content = this.rawText.substring(startPos, startPos+length);
		boolean mustUseNormalAnnot = content != null && content.contains("(");
		if( javac.getArguments().size() == 0 && !mustUseNormalAnnot) {
			MarkerAnnotation res = this.ast.newMarkerAnnotation();
			commonSettings(res, javac);
			res.setTypeName(toName(javac.getAnnotationType()));
			return res;
		} else if( javac.getArguments().size() == 1 && !(javac.getArguments().get(0) instanceof JCAssign)) {
			SingleMemberAnnotation result= ast.newSingleMemberAnnotation();
			commonSettings(result, javac);
			result.setTypeName(toName(javac.annotationType));
			JCTree value = javac.getArguments().get(0);
			if (value != null) {
				if( value instanceof JCExpression jce) {
					result.setValue(convertExpression(jce));
				} else {
					result.setValue(toName(value));
				}
			}
			return result;
		} else if (javac.getArguments().size() == 1
				&& javac.getArguments().get(0) instanceof JCAssign namedArg
				&& (namedArg.getVariable().getPreferredPosition() == Position.NOPOS
				    || namedArg.getVariable().getPreferredPosition() == namedArg.getExpression().getPreferredPosition())) {
			// actually a @Annotation(value), but returned as a @Annotation(field = value)
			SingleMemberAnnotation result= ast.newSingleMemberAnnotation();
			commonSettings(result, javac);
			result.setTypeName(toName(javac.annotationType));
			JCTree value = namedArg.getExpression();
			if (value != null) {
				if( value instanceof JCExpression jce) {
					result.setValue(convertExpression(jce));
				} else {
					result.setValue(toName(value));
				}
			}
			return result;
		} else {
			NormalAnnotation res = this.ast.newNormalAnnotation();
			commonSettings(res, javac);
			res.setTypeName(toName(javac.getAnnotationType()));
			Iterator<JCExpression> it = javac.getArguments().iterator();
			while(it.hasNext()) {
				JCExpression expr = it.next();
				if( expr instanceof JCAssign jcass) {
					if( jcass.lhs instanceof JCIdent jcid ) {
						final MemberValuePair pair = new MemberValuePair(this.ast);
						commonSettings(pair, jcass);
						final SimpleName simpleName = new SimpleName(this.ast);
						commonSettings(simpleName, jcid);
						simpleName.internalSetIdentifier(new String(jcid.getName().toString()));
						int start = jcid.pos;
						int end = start + jcid.getName().toString().length();
						simpleName.setSourceRange(start, end - start );
						pair.setName(simpleName);
						Expression value = null;
						if (jcass.rhs instanceof JCNewArray jcNewArray) {
							ArrayInitializer initializer = this.ast.newArrayInitializer();
							commonSettings(initializer, javac);
							jcNewArray.getInitializers().stream().map(this::convertExpression).forEach(initializer.expressions()::add);
							value = initializer;
						} else {
							value = convertExpression(jcass.rhs);
						}
						commonSettings(value, jcass.rhs);
						pair.setValue(value);
						start = value.getStartPosition();
						end = value.getStartPosition() + value.getLength() - 1;
						pair.setSourceRange(start, end - start + 1);
						res.values().add(pair);
					}
				}
			}
			return res;
		}
	}
//
//	public Annotation addAnnotation(IAnnotationBinding annotation, AST ast, ImportRewriteContext context) {
//		Type type = addImport(annotation.getAnnotationType(), ast, context, TypeLocation.OTHER);
//		Name name;
//		if (type instanceof SimpleType) {
//			SimpleType simpleType = (SimpleType) type;
//			name = simpleType.getName();
//			// cut 'name' loose from its parent, so that it can be reused
//			simpleType.setName(ast.newName("a")); //$NON-NLS-1$
//		} else {
//			name = ast.newName("invalid"); //$NON-NLS-1$
//		}
//
//		IMemberValuePairBinding[] mvps= annotation.getDeclaredMemberValuePairs();
//		if (mvps.length == 0) {
//			MarkerAnnotation result = ast.newMarkerAnnotation();
//			result.setTypeName(name);
//			return result;
//		} else if (mvps.length == 1 && "value".equals(mvps[0].getName())) { //$NON-NLS-1$
//			SingleMemberAnnotation result= ast.newSingleMemberAnnotation();
//			result.setTypeName(name);
//			Object value = mvps[0].getValue();
//			if (value != null)
//				result.setValue(addAnnotation(ast, value, context));
//			return result;
//		} else {
//			NormalAnnotation result = ast.newNormalAnnotation();
//			result.setTypeName(name);
//			for (int i= 0; i < mvps.length; i++) {
//				IMemberValuePairBinding mvp = mvps[i];
//				MemberValuePair mvpNode = ast.newMemberValuePair();
//				mvpNode.setName(ast.newSimpleName(mvp.getName()));
//				Object value = mvp.getValue();
//				if (value != null)
//					mvpNode.setValue(addAnnotation(ast, value, context));
//				result.values().add(mvpNode);
//			}
//			return result;
//		}
//	}

	private List<IExtendedModifier> convert(JCModifiers modifiers, ASTNode parent) {
		List<IExtendedModifier> res = new ArrayList<>();
		convertModifiers(modifiers, parent, res);
		convertModifierAnnotations(modifiers, parent, res);
		sortModifierNodesByPosition(res);
		return res;
	}

	private void sortModifierNodesByPosition(List<IExtendedModifier> l) {
		l.sort((o1, o2) -> {
			ASTNode a1 = (ASTNode)o1;
			ASTNode a2 = (ASTNode)o2;
			return a1.getStartPosition() - a2.getStartPosition();
		});
	}

	private void convertModifiers(JCModifiers modifiers, ASTNode parent, List<IExtendedModifier> res) {
		Iterator<javax.lang.model.element.Modifier> mods = modifiers.getFlags().iterator();
		while(mods.hasNext()) {
			Modifier converted = convert(mods.next(), modifiers.pos, modifiers.getEndPosition(this.javacCompilationUnit.endPositions) + 1);
			if (converted.getStartPosition() >= 0) {
				// some modifiers are added to the list without being really part of
				// the text/DOM. JDT doesn't like it, so we filter out the "implicit"
				// modifiers
				res.add(converted);
			}
		}
	}


	private List<IExtendedModifier>  convertModifierAnnotations(JCModifiers modifiers, ASTNode parent ) {
		List<IExtendedModifier> res = new ArrayList<>();
		convertModifierAnnotations(modifiers, parent, res);
		sortModifierNodesByPosition(res);
		return res;
	}

	private void convertModifierAnnotations(JCModifiers modifiers, ASTNode parent, List<IExtendedModifier> res) {
		modifiers.getAnnotations().stream().map(this::convert).forEach(res::add);
	}

	private List<IExtendedModifier> convertModifiersFromFlags(int startPos, int endPos, long oflags) {
		String rawTextSub = this.rawText.substring(startPos, endPos);
		List<IExtendedModifier> res = new ArrayList<>();
		ModifierKeyword[] ops = {
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.PUBLIC_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.PROTECTED_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.PRIVATE_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.STATIC_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.ABSTRACT_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.FINAL_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.NATIVE_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.TRANSIENT_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.VOLATILE_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.STRICTFP_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.DEFAULT_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.SEALED_KEYWORD,
				org.eclipse.jdt.core.dom.Modifier.ModifierKeyword.NON_SEALED_KEYWORD
			};
		for( int i = 0; i < ops.length; i++ ) {
			ModifierKeyword k = ops[i];
			int flagVal = k.toFlagValue();
			if( (oflags & flagVal) > 0 ) {
				Modifier m = this.ast.newModifier(k);
				String asStr = k.toString();
				int foundLoc = rawTextSub.indexOf(asStr);
				if( foundLoc != -1 ) {
					m.setSourceRange(startPos + foundLoc, asStr.length());
				}
				res.add(m);
			}
		}
		return res;
	}

	private int getJLS2ModifiersFlags(long oflags) {
		int flags = 0;
		if( (oflags & Flags.PUBLIC) > 0) flags += Flags.PUBLIC;
		if( (oflags & Flags.PRIVATE) > 0) flags += Flags.PRIVATE;
		if( (oflags & Flags.PROTECTED) > 0) flags += Flags.PROTECTED;
		if( (oflags & Flags.STATIC) > 0) flags += Flags.STATIC;
		if( (oflags & Flags.FINAL) > 0) flags += Flags.FINAL;
		if( (oflags & Flags.SYNCHRONIZED) > 0) flags += Flags.SYNCHRONIZED;
		if( (oflags & Flags.VOLATILE) > 0) flags += Flags.VOLATILE;
		if( (oflags & Flags.TRANSIENT) > 0) flags += Flags.TRANSIENT;
		if( (oflags & Flags.NATIVE) > 0) flags += Flags.NATIVE;
		if( (oflags & Flags.INTERFACE) > 0) flags += Flags.INTERFACE;
		if( (oflags & Flags.ABSTRACT) > 0) flags += Flags.ABSTRACT;
		if( (oflags & Flags.STRICTFP) > 0) flags += Flags.STRICTFP;
		return flags;
	}

	private int getJLS2ModifiersFlagsAsStringLength(long flags) {
		int len = 0;
		if( (flags & Flags.PUBLIC) > 0) len += 6 + 1;
		if( (flags & Flags.PRIVATE) > 0) len += 7 + 1;
		if( (flags & Flags.PROTECTED) > 0) len += 9 + 1;
		if( (flags & Flags.STATIC) > 0) len += 5 + 1;
		if( (flags & Flags.FINAL) > 0) len += 5 + 1;
		if( (flags & Flags.SYNCHRONIZED) > 0) len += 12 + 1;
		if( (flags & Flags.VOLATILE) > 0) len += 8 + 1;
		if( (flags & Flags.TRANSIENT) > 0) len += 9 + 1;
		if( (flags & Flags.NATIVE) > 0) len += 6 + 1;
		if( (flags & Flags.INTERFACE) > 0) len += 9 + 1;
		if( (flags & Flags.ABSTRACT) > 0) len += 8 + 1;
		if( (flags & Flags.STRICTFP) > 0) len += 8 + 1;
		return len;
	}


	private ModifierKeyword modifierToKeyword(javax.lang.model.element.Modifier javac) {
		return switch (javac) {
			case PUBLIC -> ModifierKeyword.PUBLIC_KEYWORD;
			case PROTECTED -> ModifierKeyword.PROTECTED_KEYWORD;
			case PRIVATE -> ModifierKeyword.PRIVATE_KEYWORD;
			case ABSTRACT -> ModifierKeyword.ABSTRACT_KEYWORD;
			case DEFAULT -> ModifierKeyword.DEFAULT_KEYWORD;
			case STATIC -> ModifierKeyword.STATIC_KEYWORD;
			case SEALED -> ModifierKeyword.SEALED_KEYWORD;
			case NON_SEALED -> ModifierKeyword.NON_SEALED_KEYWORD;
			case FINAL -> ModifierKeyword.FINAL_KEYWORD;
			case TRANSIENT -> ModifierKeyword.TRANSIENT_KEYWORD;
			case VOLATILE -> ModifierKeyword.VOLATILE_KEYWORD;
			case SYNCHRONIZED -> ModifierKeyword.SYNCHRONIZED_KEYWORD;
			case NATIVE -> ModifierKeyword.NATIVE_KEYWORD;
			case STRICTFP -> ModifierKeyword.STRICTFP_KEYWORD;
		};
	}
	private Modifier modifierToDom(javax.lang.model.element.Modifier javac) {
		return this.ast.newModifier(modifierToKeyword(javac));
	}
	private int modifierToFlagVal(javax.lang.model.element.Modifier javac) {
		ModifierKeyword m = modifierToKeyword(javac);
		if( m != null ) {
			return m.toFlagValue();
		}
		return 0;
	}


	private Modifier convert(javax.lang.model.element.Modifier javac, int startPos, int endPos) {
		Modifier res = modifierToDom(javac);
		if (startPos >= 0 && endPos >= startPos && endPos <= this.rawText.length()) {
			int indOf = this.rawText.indexOf(res.getKeyword().toString(), startPos, endPos);
			if( indOf != -1 ) {
				res.setSourceRange(indOf, res.getKeyword().toString().length());
			}
		}
		return res;
	}


	private Name convertName(com.sun.tools.javac.util.Name javac) {
		if (javac == null || Objects.equals(javac, Names.instance(this.context).error)) {
			var res = this.ast.newSimpleName(FAKE_IDENTIFIER);
			res.setFlags(ASTNode.RECOVERED);
			return res;
		}
		if (Objects.equals(javac, Names.instance(this.context).empty)) {
			return this.ast.newSimpleName("_");
		}
		String nameString = javac.toString();
		int lastDot = nameString.lastIndexOf(".");
		if (lastDot < 0) {
			try {
				return this.ast.newSimpleName(nameString);
			} catch (IllegalArgumentException ex) { // invalid name: super, this...
				var res = this.ast.newSimpleName(FAKE_IDENTIFIER);
				res.setFlags(ASTNode.RECOVERED);
				return res;
			}
		} else {
			return this.ast.newQualifiedName(convertName(javac.subName(0, lastDot)), (SimpleName)convertName(javac.subName(lastDot + 1, javac.length() - 1)));
		}
		// position is set later, in FixPositions, as computing them depends on the sibling
	}


	public org.eclipse.jdt.core.dom.Comment convert(Comment javac, JCTree context) {
		CommentStyle style = javac.getStyle();
		if ((style == CommentStyle.JAVADOC_BLOCK || style == CommentStyle.JAVADOC_LINE) && context != null) {
			var docCommentTree = this.javacCompilationUnit.docComments.getCommentTree(context);
			if (docCommentTree instanceof DCDocComment dcDocComment) {
				JavadocConverter javadocConverter = new JavadocConverter(this, dcDocComment, TreePath.getPath(this.javacCompilationUnit, context), this.buildJavadoc);
				String raw = javadocConverter.getRawContent();
				if( !"/**/".equals(raw)) {
					this.javadocConverters.add(javadocConverter);
					Javadoc javadoc = javadocConverter.convertJavadoc();
					if (this.ast.apiLevel() >= AST.JLS23) {
						javadoc.setMarkdown(javac.getStyle() == CommentStyle.JAVADOC_LINE);
					}
					this.javadocDiagnostics.addAll(javadocConverter.getDiagnostics());
					return javadoc;
				} else {
					style = CommentStyle.BLOCK;
				}
			}
		}
		org.eclipse.jdt.core.dom.Comment jdt = switch (style) {
			case LINE -> this.ast.newLineComment();
			case BLOCK -> this.ast.newBlockComment();
			case JAVADOC_BLOCK -> this.ast.newJavadoc();
			case JAVADOC_LINE -> this.ast.newJavadoc();
		};
		javac.isDeprecated(); javac.getText(); // initialize docComment
		int startPos = javac.getPos().getStartPosition();
		int endPos = javac.getPos().getEndPosition(this.javacCompilationUnit.endPositions);
		jdt.setSourceRange(startPos, endPos-startPos);
		return jdt;
	}

	public org.eclipse.jdt.core.dom.Comment convert(Comment javac, int pos, int endPos) {
		// testBug113108b expects /// comments to be Line comments, not Javadoc comments
		if (javac.getStyle() == CommentStyle.JAVADOC_BLOCK || javac.getStyle() == CommentStyle.JAVADOC_LINE) {
			var parser = new com.sun.tools.javac.parser.DocCommentParser(ParserFactory.instance(this.context), Log.instance(this.context).currentSource(), javac);
			JavadocConverter javadocConverter = new JavadocConverter(this, parser.parse(), pos, endPos, this.buildJavadoc);
			this.javadocConverters.add(javadocConverter);
			Javadoc javadoc = javadocConverter.convertJavadoc();
			if (this.ast.apiLevel() >= AST.JLS23) {
				javadoc.setMarkdown(javac.getStyle() == CommentStyle.JAVADOC_LINE);
			}
			this.javadocDiagnostics.addAll(javadocConverter.getDiagnostics());
			return javadoc;
		}
		org.eclipse.jdt.core.dom.Comment jdt = switch (javac.getStyle()) {
			case LINE -> this.ast.newLineComment();
			case BLOCK -> this.ast.newBlockComment();
			case JAVADOC_BLOCK -> this.ast.newJavadoc();
			case JAVADOC_LINE -> this.ast.newLineComment();
		};
		javac.isDeprecated(); javac.getText(); // initialize docComment
		jdt.setSourceRange(pos, endPos - pos);
		return jdt;
	}

	class FixPositions extends ASTVisitor {
		private final String contents;

		FixPositions() {
			super(true);
			String s = null;
			try {
				s = JavacConverter.this.javacCompilationUnit.getSourceFile().getCharContent(true).toString();
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			this.contents = s;
		}

		@Override
		public boolean visit(QualifiedName node) {
			if (node.getStartPosition() < 0) {
				int foundOffset = findPositionOfText(node.getFullyQualifiedName(), node.getParent(), siblingsOf(node));
				if (foundOffset >= 0) {
					node.setSourceRange(foundOffset, node.getFullyQualifiedName().length());
				}
			}
			return true;
		}

		@Override
		public void endVisit(QualifiedName node) {
			if (node.getName().getStartPosition() >= 0) {
				node.setSourceRange(node.getQualifier().getStartPosition(), node.getName().getStartPosition() + node.getName().getLength() - node.getQualifier().getStartPosition());
			}
		}

		@Override
		public boolean visit(SimpleName name) {
			if (name.getStartPosition() < 0 && ! FAKE_IDENTIFIER.equals(name.getIdentifier())) {
				int foundOffset = findPositionOfText(name.getIdentifier(), name.getParent(), siblingsOf(name));
				if (foundOffset >= 0) {
					name.setSourceRange(foundOffset, name.getIdentifier().length());
				}
			}
			return false;
		}

		@Override
		public boolean visit(Modifier modifier) {
			int parentStart = modifier.getParent().getStartPosition();
			int relativeStart = this.contents.substring(parentStart, parentStart + modifier.getParent().getLength()).indexOf(modifier.getKeyword().toString());
			if (relativeStart >= 0 && relativeStart < modifier.getParent().getLength()) {
				modifier.setSourceRange(parentStart + relativeStart, modifier.getKeyword().toString().length());
			}
			return true;
		}

		@Override
		public void endVisit(TagElement tagElement) {
			if (tagElement.getStartPosition() < 0) {
				OptionalInt start = ((List<ASTNode>)tagElement.fragments()).stream()
					.filter(node -> node.getStartPosition() >= 0 && node.getLength() >= 0)
					.mapToInt(ASTNode::getStartPosition)
					.min();
				OptionalInt end = ((List<ASTNode>)tagElement.fragments()).stream()
					.filter(node -> node.getStartPosition() >= 0 && node.getLength() >= 0)
					.mapToInt(node -> node.getStartPosition() + node.getLength())
					.max();
				if (start.isPresent() && end.isPresent()) {
					if (JavadocConverter.isInline(tagElement)) {
						// include some extra wrapping chars ( `{...}` or `[...]`)
						// current heuristic is very approximative as it will fail with whitespace
						tagElement.setSourceRange(start.getAsInt() - 1, end.getAsInt() - start.getAsInt() + 2);
					} else {
						tagElement.setSourceRange(start.getAsInt(), end.getAsInt() - start.getAsInt());
					}
				}
			}
			if (TagElement.TAG_DEPRECATED.equals(tagElement.getTagName())
				&& tagElement.getParent() instanceof Javadoc javadoc
				&& javadoc.getParent() != null) {
				javadoc.getParent().setFlags(javadoc.getParent().getFlags() | ClassFileConstants.AccDeprecated);
			}
		}

		private int findPositionOfText(String text, ASTNode in, List<ASTNode> excluding) {
			int current = in.getStartPosition();
			PriorityQueue<ASTNode> excluded = new PriorityQueue<>(Comparator.comparing(ASTNode::getStartPosition));
			if( current == -1 ) {
				return -1;
			}
			if (excluded.isEmpty()) {
				int position = this.contents.indexOf(text, current, current + in.getLength());
				if (position >= 0) {
					return position;
				}
			} else {
				ASTNode currentExclusion = null;
				while ((currentExclusion = excluded.poll()) != null) {
					if (currentExclusion.getStartPosition() >= current) {
						int rangeEnd = currentExclusion.getStartPosition();
						int position = this.contents.indexOf(text, current, rangeEnd);
						if (position >= 0) {
							return position;
						}
						current = rangeEnd + currentExclusion.getLength();
					}
				}
			}
			return -1;
		}
	}

	private EnumConstantDeclaration convertEnumConstantDeclaration(JCTree var, ASTNode parent, EnumDeclaration enumDecl) {
		EnumConstantDeclaration enumConstantDeclaration = null;
		String enumName = null;
		if( var instanceof JCVariableDecl enumConstant && (enumConstant.getModifiers().flags & Flags.ENUM) != 0 ) {
			if( enumConstant.getType() instanceof JCIdent jcid) {
				String o = jcid.getName().toString();
				String o2 = enumDecl.getName().toString();
				if( o.equals(o2)) {
					enumConstantDeclaration = new EnumConstantDeclaration(this.ast);
					commonSettings(enumConstantDeclaration, enumConstant);
					final SimpleName typeName = new SimpleName(this.ast);
					enumName = enumConstant.getName().toString();
					typeName.internalSetIdentifier(enumName);
					typeName.setSourceRange(enumConstant.getStartPosition(), Math.max(0, enumName.length()));
					enumConstantDeclaration.setName(typeName);
					if (enumConstant.getModifiers() != null && enumConstant.getPreferredPosition() != Position.NOPOS) {
						enumConstantDeclaration.modifiers()
								.addAll(convert(enumConstant.getModifiers(), enumConstantDeclaration));
					}
				}
				if( enumConstant.init instanceof JCNewClass jcnc ) {
					if( jcnc.def instanceof JCClassDecl jccd) {
						int blockStarts = jcnc.getStartPosition() + (enumName == null ? 0 : enumName.length());
						if(jcnc.getArguments() != null && !jcnc.getArguments().isEmpty() && jcnc.getArguments().get(jcnc.getArguments().length()-1) instanceof JCTree lastArg) {
							blockStarts = lastArg.getEndPosition(this.javacCompilationUnit.endPositions);
						}
						int endPos = jcnc.getEndPosition(this.javacCompilationUnit.endPositions);
						AnonymousClassDeclaration e = createAnonymousClassDeclaration(jccd, enumConstantDeclaration);
						if( e != null ) {
							String tmp = this.rawText.substring(blockStarts);
							int bracket = tmp.indexOf("{");
							if( bracket != -1 ) {
								blockStarts += bracket;
							}
							e.setSourceRange(blockStarts, endPos - blockStarts);
							enumConstantDeclaration.setAnonymousClassDeclaration(e);
						}
					}
					if( jcnc.getArguments() != null ) {
						Iterator<JCExpression> it = jcnc.getArguments().iterator();
						while(it.hasNext()) {
							Expression e = convertExpression(it.next());
							if( e != null ) {
								enumConstantDeclaration.arguments().add(e);
							}
						}
					}
				}
			}
		}
		return enumConstantDeclaration;
	}

	private static List<ASTNode> siblingsOf(ASTNode node) {
		return childrenOf(node.getParent());
	}

	public static Name toName(String val, int startPosition, AST ast) {
		try {
			String stripped = val.stripLeading();
			int strippedAmt = val.length() - stripped.length();
			int lastDot = stripped.lastIndexOf(".");
			if( lastDot == -1 ) {
				SimpleName sn = ast.newSimpleName(stripped); // TODO error here, testBug51600
				sn.setSourceRange(startPosition + strippedAmt, stripped.length());
				return sn;
			} else {
				SimpleName sn = ast.newSimpleName(stripped.substring(lastDot+1));
				sn.setSourceRange(startPosition + strippedAmt + lastDot+1, sn.getIdentifier().length());
				
				QualifiedName qn = ast.newQualifiedName(toName(stripped.substring(0,lastDot), startPosition + strippedAmt, ast), sn);
				qn.setSourceRange(startPosition + strippedAmt, stripped.length());
				return qn;
			}
		} catch(IllegalArgumentException iae) {
			return null;
		}
		//return null;
	}
	private static List<ASTNode> childrenOf(ASTNode node) {
		return ((Collection<Object>)node.properties().values()).stream()
			.filter(ASTNode.class::isInstance)
			.map(ASTNode.class::cast)
			.filter(Predicate.not(node::equals))
			.toList();
	}

	public DocTreePath findDocTreePath(ASTNode node) {
		return this.javadocConverters.stream()
			.map(javadocConverter -> javadocConverter.converted.get(node))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	public DocTreePath[] searchRelatedDocTreePath(MethodRef ref) {
		ArrayList<ASTNode> possibleNodes = new ArrayList<>();
		this.javadocConverters.forEach(x -> possibleNodes.addAll(x.converted.keySet()));
		DocTreePath[] r = possibleNodes.stream().filter(x -> x != ref && x instanceof MethodRef mr
				&& mr.getName().toString().equals(ref.getName().toString())
				&& Objects.equals(mr.getQualifier() == null ? null : mr.getQualifier().toString(),
						ref.getQualifier() == null ? null : ref.getQualifier().toString()))
				.map(x -> findDocTreePath(x))
				.toArray(size -> new DocTreePath[size]);
		return r;
	}


}
