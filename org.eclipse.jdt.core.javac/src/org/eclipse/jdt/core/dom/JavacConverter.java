/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
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
import static com.sun.tools.javac.tree.JCTree.Tag.TYPEARRAY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ModuleModifier.ModuleModifierKeyword;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.javac.JavacProblemConverter;

import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.source.tree.ModuleTree.ModuleKind;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.parser.Tokens.Comment;
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
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position.LineMap;

/**
 * Deals with conversion of Javac domain into JDT DOM domain
 * @implNote Cannot move to another package as it uses some package protected methods
 */
@SuppressWarnings("unchecked")
class JavacConverter {

	public final AST ast;
	private final JCCompilationUnit javacCompilationUnit;
	private final Context context;
	final Map<ASTNode, JCTree> domToJavac = new HashMap<>();
	final String rawText;

	public JavacConverter(AST ast, JCCompilationUnit javacCompilationUnit, Context context, String rawText) {
		this.ast = ast;
		this.javacCompilationUnit = javacCompilationUnit;
		this.context = context;
		this.rawText = rawText;
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
		}
		if (javacCompilationUnit.getModule() != null) {
			res.setModule(convert(javacCompilationUnit.getModuleDecl()));
		}
		javacCompilationUnit.getImports().stream().map(jc -> convert(jc)).forEach(res.imports()::add);
		javacCompilationUnit.getTypeDecls().stream()
			.map(n -> convertBodyDeclaration(n, res))
			.filter(Objects::nonNull)
			.forEach(res.types()::add);
		res.accept(new FixPositions());
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
		if( (raw.endsWith("\n") && !raw.endsWith(";\n")) || (raw.endsWith("\r\n") && !raw.endsWith(";\r\n"))) {
			res.setFlags(ASTNode.MALFORMED);
		}
		return res;
	}

	private ModuleDeclaration convert(JCModuleDecl javac) {
		ModuleDeclaration res = this.ast.newModuleDeclaration();
		res.setName(toName(javac.getName()));
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
		List<IExtendedModifier> l = convert(javac.mods, res);
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
		Iterator<JCExpression> it = mods.iterator();
		while(it.hasNext()) {
			JCExpression jcpe = it.next();
			Expression e = convertExpression(jcpe);
			if( e != null )
				res.modules().add(e);
		}
		return res;
	}

	private OpensDirective convert(JCOpens javac) {
		OpensDirective res = this.ast.newOpensDirective();
		res.setName(toName(javac.getPackageName()));
		commonSettings(res, javac);
		List<JCExpression> mods = javac.getModuleNames();
		Iterator<JCExpression> it = mods.iterator();
		while(it.hasNext()) {
			JCExpression jcpe = it.next();
			Expression e = convertExpression(jcpe);
			if( e != null )
				res.modules().add(e);
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
		List modifiersToAdd = new ArrayList<>();
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
		modifiersToAdd.sort((a, b) -> ((ASTNode)a).getStartPosition() - ((ASTNode)b).getStartPosition());
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
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				res.setStatic(true);
			}
		}
		var select = javac.getQualifiedIdentifier();
		if (select.getIdentifier().contentEquals("*")) {
			res.setOnDemand(true);
			res.setName(toName(select.getExpression()));
		} else {
			res.setName(toName(select));
		}
		return res;
	}

	void commonSettings(ASTNode res, JCTree javac) {
		if( javac != null ) {
			if (javac.getStartPosition() >= 0) {
				int length = javac.getEndPosition(this.javacCompilationUnit.endPositions) - javac.getStartPosition();
				res.setSourceRange(javac.getStartPosition(), Math.max(0, length));
			}
			this.domToJavac.put(res, javac);
			setJavadocForNode(javac, res);
		}
	}

	public interface CommonSettingsOperator {
	    public void op(ASTNode res, JCTree javac);
	}
	private Name toName(JCTree expression) {
		return toName(expression, (a,b) -> commonSettings(a,b));
	}
	Name toName(JCTree expression, CommonSettingsOperator commonSettings ) {
		if (expression instanceof JCIdent ident) {
			Name res = convertName(ident.getName());
			commonSettings.op(res, ident);
			return res;
		}
		if (expression instanceof JCFieldAccess fieldAccess) {
			Name qualifier = toName(fieldAccess.getExpression());
			SimpleName n = (SimpleName)convertName(fieldAccess.getIdentifier());
			QualifiedName res = this.ast.newQualifiedName(qualifier, n);
			commonSettings.op(res, fieldAccess);
			return res;
		}
		if (expression instanceof JCAnnotatedType jcat) {
			return toName(jcat.underlyingType, commonSettings);
		}
		if (expression instanceof JCTypeApply jcta) {
			return toName(jcta.clazz, commonSettings);
		}
		throw new UnsupportedOperationException("toName for " + expression + " (" + expression.getClass().getName() + ")");
	}

	private AbstractTypeDeclaration convertClassDecl(JCClassDecl javacClassDecl, ASTNode parent) {
		if( javacClassDecl.getKind() == Kind.ANNOTATION_TYPE && this.ast.apiLevel == AST.JLS2_INTERNAL) {
			return null;
		}
		if( javacClassDecl.getKind() == Kind.ENUM && this.ast.apiLevel == AST.JLS2_INTERNAL) {
			return null;
		}
		if( javacClassDecl.getKind() == Kind.RECORD && this.ast.apiLevel < AST.JLS16_INTERNAL) {
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
			case CLASS -> this.ast.newTypeDeclaration();
			default -> throw new IllegalStateException();
		};
		return convertClassDecl(javacClassDecl, parent, res);
	}

	private AbstractTypeDeclaration convertClassDecl(JCClassDecl javacClassDecl, ASTNode parent, AbstractTypeDeclaration res) {
		commonSettings(res, javacClassDecl);
		SimpleName simpName = (SimpleName)convertName(javacClassDecl.getSimpleName());
		if( simpName != null )
			res.setName(simpName);
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
						.forEach(typeDeclaration.permittedTypes()::add);
				}
			}
			if (javacClassDecl.getMembers() != null) {
				List<JCTree> members = javacClassDecl.getMembers();
				ASTNode previous = null;
				for( int i = 0; i < members.size(); i++ ) {
					ASTNode decl = convertBodyDeclaration(members.get(i), res);
					if( decl != null ) {
						typeDeclaration.bodyDeclarations().add(decl);
						if( previous != null ) {
							int istart = decl.getStartPosition();
							int siblingEnds = previous.getStartPosition() + previous.getLength();
							if( siblingEnds > istart ) {
								previous.setSourceRange(previous.getStartPosition(), istart - previous.getStartPosition()-1);
								int z = 0; // help
							}
						}
					}
					previous = decl;
				}
			}
//
//			Javadoc doc = this.ast.newJavadoc();
//			TagElement tagElement = this.ast.newTagElement();
//			TextElement textElement = this.ast.newTextElement();
//			textElement.setText("Hello");
//			tagElement.fragments().add(textElement);
//			doc.tags().add(tagElement);
//			res.setJavadoc(doc);
		} else if (res instanceof EnumDeclaration enumDecl) {
	        List enumStatements= enumDecl.enumConstants();
			if (javacClassDecl.getMembers() != null) {
		        for( Iterator<JCTree> i = javacClassDecl.getMembers().iterator(); i.hasNext(); ) {
		        	JCTree iNext = i.next();
		        	EnumConstantDeclaration dec1 = convertEnumConstantDeclaration(iNext, parent, enumDecl);
		        	if( dec1 != null ) {
		        		enumStatements.add(dec1);
		        	} else {
		        		// body declaration
		        		ASTNode bodyDecl = convertBodyDeclaration(iNext, res);
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

		}
		// TODO Javadoc
		return res;
	}

	private TypeParameter convert(JCTypeParameter typeParameter) {
		final TypeParameter ret = new TypeParameter(this.ast);
		final SimpleName simpleName = new SimpleName(this.ast);
		simpleName.internalSetIdentifier(typeParameter.getName().toString());
		int start = typeParameter.pos;
		int end = typeParameter.pos + typeParameter.getName().length();
		simpleName.setSourceRange(start, end - start);
		ret.setName(simpleName);
		int annotationsStart = start;
		List bounds = typeParameter.bounds;
		Iterator i = bounds.iterator();
		while(i.hasNext()) {
			JCTree t = (JCTree)i.next();
			Type type = convertToType(t);
			ret.typeBounds().add(type);
			end = typeParameter.getEndPosition(this.javacCompilationUnit.endPositions);
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
			res.setBody(convertBlock(block));
			return res;
		}
		if (tree instanceof JCErroneous erroneous) {
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

	private String getMethodDeclName(JCMethodDecl javac, ASTNode parent) {
		String name = javac.getName().toString();
		boolean javacIsConstructor = Objects.equals(javac.getName(), Names.instance(this.context).init);
		if( javacIsConstructor) {
			// sometimes javac mistakes a method with no return type as a constructor
			String parentName = getNodeName(parent);
			String tmpString1 = this.rawText.substring(javac.pos);
			int openParen = tmpString1.indexOf("(");
			if( openParen != -1 ) {
				String methodName = tmpString1.substring(0, openParen).trim();
				if( !methodName.equals(parentName)) {
					return methodName;
				}
			}
			return parentName;
		}
		return name;
	}

	private MethodDeclaration convertMethodDecl(JCMethodDecl javac, ASTNode parent) {
		MethodDeclaration res = this.ast.newMethodDeclaration();
		commonSettings(res, javac);
		if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
			res.modifiers().addAll(convert(javac.getModifiers(), res));
		} else {
			res.internalSetModifiers(getJLS2ModifiersFlags(javac.mods));
		}

		String javacName = javac.getName().toString();
		String methodDeclName = getMethodDeclName(javac, parent);
		boolean methodDeclNameMatchesInit = Objects.equals(methodDeclName, Names.instance(this.context).init.toString());
		boolean javacNameMatchesInit = javacName.equals("<init>");
		boolean javacNameMatchesError = javacName.equals("<error>");
		boolean javacNameMatchesInitAndMethodNameMatchesTypeName = javacNameMatchesInit && methodDeclName.equals(getNodeName(parent));
		boolean isConstructor = methodDeclNameMatchesInit || javacNameMatchesInitAndMethodNameMatchesTypeName;
		res.setConstructor(isConstructor);
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
			res.setName(this.ast.newSimpleName(methodDeclName));
		} else {
			// javac name is an error, so let's treat the return type as the name
			if( retTypeTree instanceof JCIdent jcid) {
				res.setName(this.ast.newSimpleName(jcid.getName().toString()));
				retTypeTree = null;
				if( jcid.toString().equals(getNodeName(parent))) {
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
		if( retTypeTree instanceof JCArrayTypeTree jcatt && retTypeTree.pos > javac.pos ) {
			// The array dimensions are part of the variable name
			if (jcatt.getType() != null) {
				int dims = countDimensionsAfterPosition(jcatt, javac.pos);
				if( this.ast.apiLevel < AST.JLS8_INTERNAL) {
					res.setExtraDimensions(dims);
				} else {
					// TODO might be buggy
					for( int i = 0; i < dims; i++ ) {
						Dimension d = this.ast.newDimension();
						d.setSourceRange(jcatt.pos + (2*i), 2);
						res.extraDimensions().add(d);
						if( jcatt.getType() instanceof JCArrayTypeTree jcatt2) {
							jcatt = jcatt2;
						}
					}
				}
				retType = convertToType(unwrapDimensions(jcatt, dims));
			}
		}
		
		if( retType != null || isConstructor) {
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				res.setReturnType2(retType);
			} else {
				res.internalSetReturnType(retType);
			}
		} 

		javac.getParameters().stream().map(this::convertVariableDeclaration).forEach(res.parameters()::add);

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

		if (javac.getBody() != null) {
			Block b = convertBlock(javac.getBody());
			if (b != null) {
				AbstractUnnamedTypeDeclaration td = findSurroundingTypeDeclaration(parent);
				boolean isInterface = td instanceof TypeDeclaration td1 && td1.isInterface();
				long modFlags = javac.getModifiers() == null ? 0 : javac.getModifiers().flags;
				boolean isAbstractOrNative = (modFlags & (Flags.ABSTRACT | Flags.NATIVE)) != 0;
				boolean isJlsBelow8 = this.ast.apiLevel < AST.JLS8_INTERNAL;
				boolean isJlsAbove8 = this.ast.apiLevel > AST.JLS8_INTERNAL;
				long flagsToCheckForAboveJLS8 = Flags.STATIC | Flags.DEFAULT | (isJlsAbove8 ? Flags.PRIVATE : 0);
				boolean notAllowed = (isAbstractOrNative || (isInterface && (isJlsBelow8 || (modFlags & flagsToCheckForAboveJLS8) == 0)));
				if (notAllowed) {
					res.setFlags(res.getFlags() | ASTNode.MALFORMED);
					Block b1 = this.ast.newBlock();
					commonSettings(b1, javac);
					res.setBody(b1);
				} else {
					res.setBody(b);
				}
			}

			if( (b.getFlags() & ASTNode.MALFORMED) > 0 ) {
				malformed = true;
			}
		}

		for (JCExpression thrown : javac.getThrows()) {
			if (this.ast.apiLevel < AST.JLS8_INTERNAL) {
				res.thrownExceptions().add(toName(thrown));
			} else {
				res.thrownExceptionTypes().add(convertToType(thrown));
			}
		}
		if( malformed ) {
			res.setFlags(res.getFlags() | ASTNode.MALFORMED);
		}
		return res;
	}
	
	private AbstractUnnamedTypeDeclaration findSurroundingTypeDeclaration(ASTNode parent) {
		if( parent == null )
			return null;
		if( parent instanceof AbstractUnnamedTypeDeclaration t) {
			return t;
		}
		return findSurroundingTypeDeclaration(parent.getParent());
	}

	private VariableDeclaration convertVariableDeclarationForLambda(JCVariableDecl javac) {
		if( javac.type == null ) {
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
			res.setName(simpleName);
		}
		if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
			res.modifiers().addAll(convert(javac.getModifiers(), res));
		} else {
			res.internalSetModifiers(getJLS2ModifiersFlags(javac.mods));
		}
		if( javac.getType() instanceof JCArrayTypeTree jcatt && javac.vartype.pos > javac.pos ) {
			// The array dimensions are part of the variable name
			if (jcatt.getType() != null) {
				int dims = countDimensionsAfterPosition(jcatt, javac.vartype.pos);
				if( this.ast.apiLevel < AST.JLS8_INTERNAL) {
					res.setExtraDimensions(dims);
				} else {
					// TODO might be buggy
					for( int i = 0; i < dims; i++ ) {
						Dimension d = this.ast.newDimension();
						d.setSourceRange(jcatt.pos + (2*i), 2);
						res.extraDimensions().add(d);
						if( jcatt.getType() instanceof JCArrayTypeTree jcatt2) {
							jcatt = jcatt2;
						}
					}
				}
				res.setType(convertToType(unwrapDimensions(jcatt, dims)));
			}
		} else if ( (javac.mods.flags & VARARGS) != 0) {
			// We have varity
			if( javac.getType() instanceof JCArrayTypeTree arr) {
				res.setType(convertToType(arr.elemtype));
			}
			if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
				res.setVarargs(true);
			}
		} else {
			// the array dimensions are part of the type
			if (javac.getType() != null) {
				if( !(javac.getType() instanceof JCErroneous)) {
					res.setType(convertToType(javac.getType()));
				}
			}
		}
		if (javac.getInitializer() != null) {
			res.setInitializer(convertExpression(javac.getInitializer()));
		}
		return res;
	}

	private int getJLS2ModifiersFlags(JCModifiers mods) {
		return getJLS2ModifiersFlags(mods.flags);
	}

	private FieldDeclaration convertFieldDeclaration(JCVariableDecl javac) {
		return convertFieldDeclaration(javac, null);
	}

	private VariableDeclarationFragment createVariableDeclarationFragment(JCVariableDecl javac) {
		VariableDeclarationFragment fragment = this.ast.newVariableDeclarationFragment();
		commonSettings(fragment, javac);
		int fragmentEnd = javac.getEndPosition(this.javacCompilationUnit.endPositions);
		int fragmentStart = javac.pos;
		int fragmentLength = fragmentEnd - fragmentStart; // ????  - 1;
		char c = this.rawText.charAt(fragmentEnd-1);
		if( c == ';' || c == ',') {
			fragmentLength--;
		}
		fragment.setSourceRange(fragmentStart, Math.max(0, fragmentLength));

		if (convertName(javac.getName()) instanceof SimpleName simpleName) {
			fragment.setName(simpleName);
		}
		if( javac.getType() instanceof JCArrayTypeTree jcatt && javac.vartype.pos > javac.pos ) {
			// The array dimensions are part of the variable name
			if (jcatt.getType() != null) {
				int dims = countDimensionsAfterPosition(jcatt, fragmentStart);
				if( this.ast.apiLevel < AST.JLS8_INTERNAL) {
					fragment.setExtraDimensions(dims);
				} else {
					// TODO might be buggy
					for( int i = 0; i < dims; i++ ) {
						Dimension d = this.ast.newDimension();
						d.setSourceRange(jcatt.pos, 2);
						fragment.extraDimensions().add(d);
					}
				}
			}
		}
		if (javac.getInitializer() != null) {
			fragment.setInitializer(convertExpression(javac.getInitializer()));
		}
		return fragment;
	}

	private FieldDeclaration convertFieldDeclaration(JCVariableDecl javac, ASTNode parent) {
		VariableDeclarationFragment fragment = createVariableDeclarationFragment(javac);
		List<ASTNode> sameStartPosition = new ArrayList<>();
		if( parent instanceof TypeDeclaration decl) {
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
					Type type = convertToType(working);
					if (type != null) {
						res.setType(type);
					}
				} else {
					Type type = convertToType(javac.getType());
					if (type != null) {
						res.setType(type);
					}
				}
			} else {
				Type type = convertToType(javac.getType());
				if (type != null) {
					res.setType(type);
				}
			}
			return res;
		}
	}


	private void setJavadocForNode(JCTree javac, ASTNode node) {
		Comment c = this.javacCompilationUnit.docComments.getComment(javac);
		if( c != null && c.getStyle() == Comment.CommentStyle.JAVADOC) {
			var docCommentTree = this.javacCompilationUnit.docComments.getCommentTree(javac);
			Javadoc javadoc = new JavadocConverter(this, docCommentTree).convertJavadoc();
			if (node instanceof BodyDeclaration bodyDeclaration) {
				bodyDeclaration.setJavadoc(javadoc);
				bodyDeclaration.setSourceRange(javadoc.getStartPosition(), bodyDeclaration.getStartPosition() + bodyDeclaration.getLength() - javadoc.getStartPosition());
			} else if (node instanceof ModuleDeclaration moduleDeclaration) {
				moduleDeclaration.setJavadoc(javadoc);
				moduleDeclaration.setSourceRange(javadoc.getStartPosition(), moduleDeclaration.getStartPosition() + moduleDeclaration.getLength() - javadoc.getStartPosition());
			} else if (node instanceof PackageDeclaration packageDeclaration) {
				if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
					packageDeclaration.setJavadoc(javadoc);
				}
				packageDeclaration.setSourceRange(javadoc.getStartPosition(), packageDeclaration.getStartPosition() + packageDeclaration.getLength() - javadoc.getStartPosition());
			}
		}
	}

	private Expression convertExpression(JCExpression javac) {
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
				boolean superCall2 = access instanceof JCFieldAccess && Objects.equals(Names.instance(this.context)._super.toString(), access.getExpression().toString());
				if (superCall1 || superCall2) {
					JCFieldAccess fa = superCall1 ? ((JCFieldAccess)access.getExpression()) : access;
					SuperMethodInvocation res2 = this.ast.newSuperMethodInvocation();
					commonSettings(res2, javac);
					methodInvocation.getArguments().stream().map(this::convertExpression).forEach(res2.arguments()::add);
					if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
						methodInvocation.getTypeArguments().stream().map(this::convertToType).forEach(res2.typeArguments()::add);
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
				boolean superCall2 = access instanceof JCFieldAccess && Objects.equals(Names.instance(this.context)._super.toString(), access.getExpression().toString());
				if (superCall1 || superCall2) {
					JCFieldAccess fa = superCall1 ? ((JCFieldAccess)access.getExpression()) : access;
					SuperMethodInvocation res2 = this.ast.newSuperMethodInvocation();
					commonSettings(res2, javac);
					methodInvocation.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);
					if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
						methodInvocation.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
					}
					if( superCall1 ) {
						res2.setQualifier(toName(fa.getExpression()));
					}
					res2.setName((SimpleName)convertName(access.getIdentifier()));
					return res2;
				}
				if (convertName(access.getIdentifier()) instanceof SimpleName simpleName) {
					res.setName(simpleName);
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
					methodInvocation.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
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
				Name n = toName(newClass.clazz);
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
			ParenthesizedExpression substitute = this.ast.newParenthesizedExpression();
			commonSettings(substitute, error);
			return substitute;
		}
		if (javac instanceof JCBinary binary) {
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
			res.setOperator(switch (binary.getTag()) {
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
			});
			return res;
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
			if (jcInstanceOf.getType() != null) {
				InstanceofExpression res = this.ast.newInstanceofExpression();
				commonSettings(res, javac);
				res.setLeftOperand(convertExpression(jcInstanceOf.getExpression()));
				res.setRightOperand(convertToType(jcInstanceOf.getType()));
				return res;
			}
			JCPattern jcPattern = jcInstanceOf.getPattern();
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
			res.setPattern(convert(jcPattern));
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
			if (Objects.equals(Names.instance(this.context).init, jcMemberReference.getName())) {
				CreationReference res = this.ast.newCreationReference();
				commonSettings(res, javac);
				res.setType(convertToType(jcMemberReference.getQualifierExpression()));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().map(this::convertToType).forEach(res.typeArguments()::add);
				}
				return res;
			} else {
				ExpressionMethodReference res = this.ast.newExpressionMethodReference();
				commonSettings(res, javac);
				res.setExpression(convertExpression(jcMemberReference.getQualifierExpression()));
				res.setName((SimpleName)convertName(jcMemberReference.getName()));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().map(this::convertToType).forEach(res.typeArguments()::add);
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
			jcLambda.getParameters().stream()
				.filter(JCVariableDecl.class::isInstance)
				.map(JCVariableDecl.class::cast)
				.map(this::convertVariableDeclarationForLambda)
				.forEach(res.parameters()::add);
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
						arrayType.dimensions().addFirst(this.ast.newDimension());
					} else {
						arrayType = this.ast.newArrayType(childArrayType);
					}
				} else {
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
		ILog.get().error("Unsupported " + javac + " of type" + (javac == null ? "null" : javac.getClass()));
		ParenthesizedExpression substitute = this.ast.newParenthesizedExpression();
		commonSettings(substitute, javac);
		return substitute;
	}

	private Pattern convert(JCPattern jcPattern) {
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
		}
		throw new UnsupportedOperationException("Missing support to convert '" + jcPattern);
	}

	private ArrayInitializer createArrayInitializerFromJCNewArray(JCNewArray jcNewArray) {
		ArrayInitializer initializer = this.ast.newArrayInitializer();
		commonSettings(initializer, jcNewArray);
		if( jcNewArray.getInitializers().size() > 0 ) {
			commonSettings(initializer, jcNewArray.getInitializers().get(0));
		}
		jcNewArray.getInitializers().stream().map(this::convertExpression).forEach(initializer.expressions()::add);
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

	private int countDimensions(JCArrayTypeTree tree) {
		return countDimensionsAfterPosition(tree, 0);
	}
	
	private int countDimensionsAfterPosition(JCArrayTypeTree tree, int pos) {
		int ret = 0;
        JCTree elem = tree;
        while (elem != null && elem.hasTag(TYPEARRAY)) {
        	if( elem.pos > pos)
        		ret++;
            elem = ((JCArrayTypeTree)elem).elemtype;
        }
        return ret;
	}
	
	private JCTree unwrapDimensions(JCArrayTypeTree tree, int count) {
        JCTree elem = tree;
        while (elem != null && elem.hasTag(TYPEARRAY) && count > 0) {
            elem = ((JCArrayTypeTree)elem).elemtype;
            count--;
        }
        return elem;
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
		int end = res.getStartPosition() + res.getLength();
		if( end < this.rawText.length() && this.rawText.charAt(end-1) != ';' && this.rawText.charAt(end) == ';') {
			// jdt expects semicolon to be part of the range
			res.setSourceRange(res.getStartPosition(), res.getLength() + 1);
		}
		javac.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);

		//res.setFlags(javac.getFlags() | ASTNode.MALFORMED);
		if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
			javac.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
		}
		if( javac.getMethodSelect() instanceof JCFieldAccess jcfa && jcfa.selected != null ) {
			res.setExpression(convertExpression(jcfa.selected));
		}
		return res;
	}


	private ConstructorInvocation convertThisConstructorInvocation(JCMethodInvocation javac) {
		ConstructorInvocation res = this.ast.newConstructorInvocation();
		commonSettings(res, javac);
		javac.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);
		if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
			javac.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
		}
		return res;
	}

	private Expression convertLiteral(JCLiteral literal) {
		Object value = literal.getValue();
		if (value instanceof Number number) {
			char firstChar = number.toString().charAt(0);
			if( firstChar != '-' ) {
				NumberLiteral res = this.ast.newNumberLiteral();
				commonSettings(res, literal);
				String fromSrc = this.rawText.substring(res.getStartPosition(), res.getStartPosition() + res.getLength());
				res.setToken(fromSrc);
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
			StringLiteral res = this.ast.newStringLiteral();
			commonSettings(res, literal);
			res.setLiteralValue(string);  // TODO: we want the token here
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
				Block substitute = this.ast.newBlock();
				commonSettings(substitute, jcError);
				parent.setFlags(parent.getFlags() | ASTNode.MALFORMED);
				return substitute;
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
			VariableDeclarationStatement res = this.ast.newVariableDeclarationStatement(fragment);
			commonSettings(res, javac);
			if (jcVariableDecl.vartype != null) {
				Type t = convertToType(jcVariableDecl.vartype);
				if( t != null )
					res.setType(t);
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
			return convertTryStatement(tryStatement);
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
				Expression expr = convertStatementToExpression((JCStatement)initializerIt.next(), res);
				if( expr != null )
					res.initializers().add(expr);
			}
			if (jcForLoop.getCondition() != null) {
				Expression expr = convertExpression(jcForLoop.getCondition());
				if( expr != null )
					res.setExpression(expr);
			}

			Iterator updateIt = jcForLoop.getUpdate().iterator();
			while(updateIt.hasNext()) {
				Expression expr = convertStatementToExpression((JCStatement)updateIt.next(), res);
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
					int numStatements = switchCase.getStatements() != null ? switchCase.getStatements().size() : 0;
					List<JCStatement> stmts = new ArrayList<>(numStatements + 1);
					stmts.add(switchCase);
					if (numStatements > 0) {
						stmts.addAll(switchCase.getStatements());
					}
					return stmts.stream();
				}).map(x -> convertStatement(x, res))
				.filter(x -> x != null)
				.forEach(res.statements()::add);
			return res;
		}
		if (javac instanceof JCCase jcCase) {
			SwitchCase res = this.ast.newSwitchCase();
			commonSettings(res, javac);
			if( this.ast.apiLevel >= AST.JLS14_INTERNAL) {
				if (jcCase.getGuard() != null && (jcCase.getLabels().size() > 1 || jcCase.getLabels().get(0) instanceof JCPatternCaseLabel)) {
					GuardedPattern guardedPattern = this.ast.newGuardedPattern();
					guardedPattern.setExpression(convertExpression(jcCase.getGuard()));
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
					jcCase.getExpressions().stream().map(this::convertExpression).forEach(res.expressions()::add);
				}
				res.setSwitchLabeledRule(jcCase.getCaseKind() == CaseKind.RULE);
			} else {
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
				if( fragment.extraArrayDimensions > 0 ) {
					jdtVariableDeclarationExpression.setType(convertToType(findBaseType(jcvd.vartype)));
				} else if( this.ast.apiLevel > AST.JLS4_INTERNAL && fragment.extraDimensions().size() > 0 ) {
					jdtVariableDeclarationExpression.setType(convertToType(findBaseType(jcvd.vartype)));
				}
			}
			return jdtVariableDeclarationExpression;
		}
		throw new UnsupportedOperationException(javac + " of type" + javac.getClass());
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
			for( Iterator i = javac.getStatements().iterator(); i.hasNext();) {
				JCStatement next = (JCStatement)i.next();
				Statement s = convertStatement(next, res);
				if( s != null ) {
					res.statements().add(s);
				}
			}
		}
		return res;
	}

	private TryStatement convertTryStatement(JCTry javac) {
		TryStatement res = this.ast.newTryStatement();
		commonSettings(res, javac);
		res.setBody(convertBlock(javac.getBlock()));
		if (javac.finalizer != null) {
			res.setFinally(convertBlock(javac.getFinallyBlock()));
		}

		if( this.ast.apiLevel >= AST.JLS4_INTERNAL) {
			javac.getResources().stream().map(this::convertTryResource).forEach(res.resources()::add);
		}
		javac.getCatches().stream().map(this::convertCatcher).forEach(res.catchClauses()::add);
		return res;
	}

	private ASTNode /*VariableDeclarationExpression or Name*/ convertTryResource(JCTree javac) {
		throw new UnsupportedOperationException("Not implemented yet");
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

	private Type convertToType(JCTree javac) {
		if (javac instanceof JCIdent ident) {
			SimpleType res = this.ast.newSimpleType(convertName(ident.name));
			commonSettings(res, ident);
			return res;
		}
		if (javac instanceof JCFieldAccess qualified) {
			try {
				if( qualified.getExpression() == null ) {
					Name qn = toName(qualified);
					SimpleType res = this.ast.newSimpleType(qn);
					commonSettings(res, qualified);
					return res;
				}
			} catch (Exception ex) {
			}
			// case of not translatable name, eg because of generics
			// TODO find a better check instead of relying on exception
			if( this.ast.apiLevel > AST.JLS2_INTERNAL) {
				QualifiedType res = this.ast.newQualifiedType(convertToType(qualified.getExpression()), (SimpleName)convertName(qualified.getIdentifier()));
				commonSettings(res, qualified);
				return res;
			} else {
				SimpleType res = this.ast.newSimpleType(toName(qualified));
				commonSettings(res, javac);
				return res;
			}
		}
		if (javac instanceof JCPrimitiveTypeTree primitiveTypeTree) {
			PrimitiveType res = this.ast.newPrimitiveType(convert(primitiveTypeTree.getPrimitiveTypeKind()));
			commonSettings(res, primitiveTypeTree);
			return res;
		}
		if (javac instanceof JCTypeUnion union) {
			UnionType res = this.ast.newUnionType();
			commonSettings(res, javac);
			union.getTypeAlternatives().stream().map(this::convertToType).forEach(res.types()::add);
			return res;
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
			} else {
				res = this.ast.newArrayType(t);
			}
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCTypeApply jcTypeApply) {
			if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
				ParameterizedType res = this.ast.newParameterizedType(convertToType(jcTypeApply.getType()));
				commonSettings(res, javac);
				jcTypeApply.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
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
			jcTypeIntersection.getBounds().stream().map(this::convertToType).forEach(res.types()::add);
			return res;
		}
		if (javac instanceof JCAnnotatedType jcAnnotatedType) {
			boolean createNameQualifiedType = jcAnnotatedType.getAnnotations() != null && jcAnnotatedType.getAnnotations().size() > 0;
			Type res = null;
			if( createNameQualifiedType && this.ast.apiLevel >= AST.JLS8_INTERNAL) {
				JCExpression jcpe = jcAnnotatedType.underlyingType;
				if( jcpe instanceof JCFieldAccess jcfa2) {
					if( jcfa2.selected instanceof JCAnnotatedType) {
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
				}
			} else {
				convertToType(jcAnnotatedType.getUnderlyingType());
			}
			if (res instanceof AnnotatableType annotatableType) {
				for (JCAnnotation annotation : jcAnnotatedType.getAnnotations()) {
					annotatableType.annotations.add(convert(annotation));
				}
			} else if (res instanceof ArrayType arrayType) {
				if (!arrayType.dimensions().isEmpty()) {
					for (JCAnnotation annotation : jcAnnotatedType.getAnnotations()) {
						((Dimension)arrayType.dimensions().get(0)).annotations().add(convert(annotation));
					}
				}
			}
			return res;
		}
		if (javac instanceof JCErroneous erroneous) {
			return null;
		}
		throw new UnsupportedOperationException("Not supported yet, type " + javac + " of class" + javac.getClass());
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
						final SimpleName simpleName = new SimpleName(this.ast);
						simpleName.internalSetIdentifier(new String(jcid.getName().toString()));
						int start = jcid.pos;
						int end = start + jcid.getName().toString().length();
						simpleName.setSourceRange(start, end - start + 1);
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
		modifiers.getAnnotations().stream().map(this::convert).forEach(res::add);
		Iterator<javax.lang.model.element.Modifier> mods = modifiers.getFlags().iterator();
		while(mods.hasNext()) {
			res.add(convert(mods.next(), modifiers.pos, parent.getStartPosition() + parent.getLength()));
		}
		res.sort((o1, o2) -> {
			ASTNode a1 = (ASTNode)o1;
			ASTNode a2 = (ASTNode)o2;
			return a1.getStartPosition() - a2.getStartPosition();
		});
		return res;
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
		if (startPos >= 0) {
			// This needs work... It's not a great solution.
			String sub = this.rawText.substring(startPos, endPos);
			int indOf = sub.indexOf(res.getKeyword().toString());
			if( indOf != -1 ) {
				res.setSourceRange(startPos+indOf, res.getKeyword().toString().length());
			}
		}
		return res;
	}


	private Name convertName(com.sun.tools.javac.util.Name javac) {
		if (javac == null || Objects.equals(javac, Names.instance(this.context).error) || Objects.equals(javac, Names.instance(this.context).empty)) {
			return null;
		}
		String nameString = javac.toString();
		int lastDot = nameString.lastIndexOf(".");
		if (lastDot < 0) {
			return this.ast.newSimpleName(nameString);
		} else {
			return this.ast.newQualifiedName(convertName(javac.subName(0, lastDot)), (SimpleName)convertName(javac.subName(lastDot + 1, javac.length() - 1)));
		}
		// position is set later, in FixPositions, as computing them depends on the sibling
	}

	private Name convert(com.sun.tools.javac.util.Name javac, String selected) {
		if (javac == null || Objects.equals(javac, Names.instance(this.context).error) || Objects.equals(javac, Names.instance(this.context).empty)) {
			return null;
		}
		if (selected == null) {
			return this.ast.newSimpleName(javac.toString());
		} else {
			return this.ast.newQualifiedName(this.ast.newName(selected), this.ast.newSimpleName(javac.toString()));
		}
		// position is set later, in FixPositions, as computing them depends on the sibling
	}

	public org.eclipse.jdt.core.dom.Comment convert(Comment javac, int pos, int endPos) {
		org.eclipse.jdt.core.dom.Comment jdt = switch (javac.getStyle()) {
			case LINE -> this.ast.newLineComment();
			case BLOCK -> this.ast.newBlockComment();
			case JAVADOC -> this.ast.newJavadoc();
		};
		jdt.setSourceRange(pos, endPos - pos);
		return jdt;
	}

	static IProblem convertDiagnostic(Diagnostic<? extends JavaFileObject> javacDiagnostic) {
		return JavacProblemConverter.createJavacProblem(javacDiagnostic);
	}

	class FixPositions extends ASTVisitor {
		private final String contents;

		FixPositions() {
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
			if (name.getStartPosition() < 0) {
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
			if (relativeStart >= 0) {
				modifier.setSourceRange(parentStart + relativeStart, modifier.getKeyword().toString().length());
			} else {
				ILog.get().warn("Couldn't compute position of " + modifier);
			}
			return true;
		}

		private int findPositionOfText(String text, ASTNode in, List<ASTNode> excluding) {
			int current = in.getStartPosition();
			PriorityQueue<ASTNode> excluded = new PriorityQueue<>(Comparator.comparing(ASTNode::getStartPosition));
			if( current == -1 ) {
				return -1;
			} if (excluded.isEmpty()) {
				String subText = this.contents.substring(current, current + in.getLength());
				int foundInSubText = subText.indexOf(text);
				if (foundInSubText >= 0) {
					return current + foundInSubText;
				}
			} else {
				ASTNode currentExclusion = null;
				while ((currentExclusion = excluded.poll()) != null) {
					if (currentExclusion.getStartPosition() >= current) {
						int rangeEnd = currentExclusion.getStartPosition();
						String subText = this.contents.substring(current, rangeEnd);
						int foundInSubText = subText.indexOf(text);
						if (foundInSubText >= 0) {
							return current + foundInSubText;
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
		if( var instanceof JCVariableDecl enumConstant ) {
			if( enumConstant.getType() instanceof JCIdent jcid) {
				String o = jcid.getName().toString();
				String o2 = enumDecl.getName().toString();
				if( o.equals(o2)) {
					enumConstantDeclaration = new EnumConstantDeclaration(this.ast);
					final SimpleName typeName = new SimpleName(this.ast);
					typeName.internalSetIdentifier(enumConstant.getName().toString());
					int start = enumConstant.getStartPosition();
					int end = enumConstant.getEndPosition(this.javacCompilationUnit.endPositions);
					enumConstantDeclaration.setSourceRange(start, end-start);
					enumConstantDeclaration.setName(typeName);
				}
				if( enumConstant.init instanceof JCNewClass jcnc ) {
					if( jcnc.def instanceof JCClassDecl jccd) {
						AnonymousClassDeclaration e = createAnonymousClassDeclaration(jccd, enumConstantDeclaration);
						if( e != null ) {
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

	private BodyDeclaration convertEnumFieldOrMethodDeclaration(JCTree var, BodyDeclaration parent, EnumDeclaration enumDecl) {
		if( var instanceof JCVariableDecl field ) {
			if( !(field.getType() instanceof JCIdent jcid)) {
				return convertFieldDeclaration(field);
			}
			String o = jcid.getName().toString();
			String o2 = enumDecl.getName().toString();
			if( !o.equals(o2)) {
				return convertFieldDeclaration(field);
			}
		}
		if( var instanceof JCMethodDecl method) {
			return convertMethodDecl(method, parent);
		}

		return null;
	}

	private static List<ASTNode> siblingsOf(ASTNode node) {
		return childrenOf(node.getParent());
	}

	private static List<ASTNode> childrenOf(ASTNode node) {
		return ((Collection<Object>)node.properties().values()).stream()
			.filter(ASTNode.class::isInstance)
			.map(ASTNode.class::cast)
			.filter(Predicate.not(node::equals))
			.toList();
	}


}
