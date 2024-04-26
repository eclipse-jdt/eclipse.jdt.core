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
import java.util.Locale;
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
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.tree.JCTree;
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
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
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
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPattern;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
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
	private String rawText;

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
		return res;
	}

	private ImportDeclaration convert(JCImport javac) {
		ImportDeclaration res = this.ast.newImportDeclaration();
		commonSettings(res, javac);
		if (javac.isStatic()) {
			res.setStatic(true);
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

	private void commonSettings(ASTNode res, JCTree javac) {
		if (javac.getStartPosition() >= 0) {
			int length = javac.getEndPosition(this.javacCompilationUnit.endPositions) - javac.getStartPosition();
			res.setSourceRange(javac.getStartPosition(), Math.max(0, length));
		}
		this.domToJavac.put(res, javac);
		setJavadocForNode(javac, res);
	}


	private Name toName(JCTree expression) {
		if (expression instanceof JCIdent ident) {
			Name res = convert(ident.getName());
			commonSettings(res, ident);
			return res;
		}
		if (expression instanceof JCFieldAccess fieldAccess) {
			QualifiedName res = this.ast.newQualifiedName(toName(fieldAccess.getExpression()), (SimpleName)convert(fieldAccess.getIdentifier()));
			commonSettings(res, fieldAccess);
			return res;
		}
		throw new UnsupportedOperationException("toName for " + expression + " (" + expression.getClass().getName() + ")");
	}

	private AbstractTypeDeclaration convertClassDecl(JCClassDecl javacClassDecl, ASTNode parent) {
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

//	private AbstractTypeDeclaration convertClassDecl(JCClassDecl javacClassDecl, ASTNode parent, AbstractTypeDeclaration res) {
	private AbstractTypeDeclaration convertClassDecl(JCClassDecl javacClassDecl, ASTNode parent, AbstractTypeDeclaration res) {
		commonSettings(res, javacClassDecl);
		SimpleName simpName = (SimpleName)convert(javacClassDecl.getSimpleName());
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
					if( e instanceof JCFieldAccess jcfa) {
						String pack = jcfa.selected == null ? null : jcfa.selected.toString();
						typeDeclaration.setSuperclass(convert(jcfa.name, pack));
					} else if( e instanceof JCIdent jcid) {
						typeDeclaration.setSuperclass(convert(jcid.name, null));
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
						if( next instanceof JCFieldAccess jcfa ) {
							String pack = jcfa.selected == null ? null : jcfa.selected.toString();
							typeDeclaration.superInterfaces().add(convert(jcfa.name, pack));
						}
					}
				}
			}

			if( javacClassDecl.getTypeParameters() != null ) {
				Iterator<JCTypeParameter> i = javacClassDecl.getTypeParameters().iterator();
				while(i.hasNext()) {
					JCTypeParameter next = i.next();
					typeDeclaration.typeParameters().add(convert(next));
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
		        	EnumConstantDeclaration dec1 = convertEnumConstantDeclaration(i.next(), parent, enumDecl);
		        	if( dec1 != null ) {
		        		enumStatements.add(dec1);
		        	}
		        }
			}

			List bodyDecl = enumDecl.bodyDeclarations();
			if (javacClassDecl.getMembers() != null) {
		        for( Iterator<JCTree> i = javacClassDecl.getMembers().iterator(); i.hasNext(); ) {
		        	BodyDeclaration bd = convertEnumFieldOrMethodDeclaration(i.next(), res, enumDecl);
		        	if( bd != null ) {
		        		bodyDecl.add(bd);
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
			end = type.getStartPosition() + type.getLength() - 1;
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
		if (tree instanceof JCErroneous) {
			return null;
		}
		throw new UnsupportedOperationException("Unsupported " + tree + " of type" + tree.getClass());
	}

	private ASTNode convertMethodInAnnotationTypeDecl(JCMethodDecl javac, ASTNode parent) {
		AnnotationTypeMemberDeclaration res = new AnnotationTypeMemberDeclaration(this.ast);
		commonSettings(res, javac);
		res.modifiers().addAll(convert(javac.getModifiers(), res));
		res.setType(convertToType(javac.getReturnType()));
		if (convert(javac.getName()) instanceof SimpleName simpleName) {
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
		boolean javacNameMatchesInitAndMethodNameMatchesTypeName = javacNameMatchesInit && methodDeclName.equals(getNodeName(parent));
		boolean isConstructor = methodDeclNameMatchesInit || javacNameMatchesInitAndMethodNameMatchesTypeName;
		res.setConstructor(isConstructor);
		boolean malformed = false;
		if(isConstructor && !javacNameMatchesInitAndMethodNameMatchesTypeName) {
			malformed = true;
		}
		if( javacNameMatchesInit && !isConstructor ) {
			malformed = true;
		}

		res.setName(this.ast.newSimpleName(methodDeclName));
		JCTree retTypeTree = javac.getReturnType();
		Type retType = null;
		if( retTypeTree == null ) {
			if( isConstructor && this.ast.apiLevel == AST.JLS2_INTERNAL ) {
				retType = this.ast.newPrimitiveType(convert(TypeKind.VOID));
				// // TODO need to find the right range
				retType.setSourceRange(javac.mods.pos + getJLS2ModifiersFlagsAsStringLength(javac.mods.flags), 0); 
			}
		} else {
			retType = convertToType(retTypeTree);
		}
		
		if( this.ast.apiLevel != AST.JLS2_INTERNAL) {
			res.setReturnType2(retType);
		} else {
			if (retType != null) {
				res.internalSetReturnType(retType);
			}
		}

		javac.getParameters().stream().map(this::convertVariableDeclaration).forEach(res.parameters()::add);
		if (javac.getBody() != null) {
			Block b = convertBlock(javac.getBody());
			res.setBody(b);
			if( (b.getFlags() & ASTNode.MALFORMED) > 0 ) {
				malformed = true;
			}
		}

		List throwing = javac.getThrows();
		for( Iterator i = throwing.iterator(); i.hasNext(); ) {
			if( this.ast.apiLevel < AST.JLS8_INTERNAL) {
				JCIdent id = (JCIdent)i.next();
				Name r = convert(id.getName());
				res.thrownExceptions().add(r);
			} else {
				JCIdent id = (JCIdent)i.next();
				res.thrownExceptionTypes().add(convertToType(id));
			}
		}
		if( malformed ) {
			res.setFlags(res.getFlags() | ASTNode.MALFORMED);
		}
		return res;
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
		if (convert(javac.getName()) instanceof SimpleName simpleName) {
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
				int dims = countDimensions(jcatt);
				res.setType(convertToType(jcatt.getType()));
				if( this.ast.apiLevel < AST.JLS8_INTERNAL) {
					res.setExtraDimensions(dims);
				} else {
					// TODO might be buggy
					for( int i = 0; i < dims; i++ ) {
						Dimension d = this.ast.newDimension();
						d.setSourceRange(jcatt.pos, 2);
						res.extraDimensions().add(d);
					}
				}
			}
		} else if ( (javac.mods.flags & VARARGS) != 0) {
			// We have varity
			if( javac.getType() instanceof JCArrayTypeTree arr) {
				res.setType(convertToType(arr.elemtype));
			}
			res.setVarargs(true);
		} else {
			// the array dimensions are part of the type
			if (javac.getType() != null) {
				res.setType(convertToType(javac.getType()));
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

		if (convert(javac.getName()) instanceof SimpleName simpleName) {
			fragment.setName(simpleName);
		}
		if( javac.getType() instanceof JCArrayTypeTree jcatt && javac.vartype.pos > javac.pos ) {
			// The array dimensions are part of the variable name
			if (jcatt.getType() != null) {
				int dims = countDimensions(jcatt);
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
					.filter(x -> ((FieldDeclaration)x).getStartPosition() == javac.getStartPosition())
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
					// unwrap the jcatt?
					JCTree working = jcatt;
					while(working instanceof JCArrayTypeTree work2) {
						working = work2.getType();
					}
					res.setType(convertToType(working));
				} else {
					res.setType(convertToType(javac.getType()));
				}
			} else {
				res.setType(convertToType(javac.getType()));
			}
			return res;
		}
	}
	

	private void setJavadocForNode(JCTree javac, ASTNode node) {
		Comment c = this.javacCompilationUnit.docComments.getComment(javac);
		if( c != null && c.getStyle() == Comment.CommentStyle.JAVADOC) {
			String textVal = c.getText(); // initialize
			int start = c.getSourcePos(0);
			String prefix = new StringBuilder(this.rawText.substring(0, start)).reverse().toString();
			int ind = prefix.indexOf("**/");
			if( ind != -1 ) {
				start -= (ind + 3);
				int len = this.rawText.substring(start).indexOf("*/");
				if( len != -1 ) {
					len += 2;
					Javadoc jd = (Javadoc)convert(c, start, start + len);
					String jdString = this.rawText.substring(start, start + len);
					if( this.ast.apiLevel == AST.JLS2_INTERNAL) {
						jd.setComment(jdString);
					}
					int nodeStartPosition = Math.min(jd.getStartPosition(), node.getStartPosition());
					int nodeEndPosition = Math.max(jd.getStartPosition() + jd.getLength(), node.getStartPosition() + node.getLength());
					int nodeFinalLength = nodeEndPosition - nodeStartPosition;
					node.setSourceRange(nodeStartPosition, nodeFinalLength);
					
					if( node instanceof BodyDeclaration bd) {
						bd.setJavadoc(jd);
						int contentsStart = nodeStartPosition + 3;
						int contentsEnd = jd.getStartPosition() + jd.getLength() - 2;
						int contentsLength = contentsEnd - contentsStart;
						String jdStringContents = this.rawText.substring(contentsStart, contentsStart + contentsLength);
						String stripLeading = jdStringContents.stripLeading();
						int leadingStripped = jdStringContents.length() - stripLeading.length();
						contentsStart += leadingStripped;
						contentsLength = contentsEnd - contentsStart;
						jdStringContents = this.rawText.substring(contentsStart, contentsStart + contentsLength);
								
						String[] split = jdStringContents.split("\n");
						int runningTally = 0;
						TagElement previousTag = null;
						// TODO Now split by line? TODO there's much more to do here
						for( int i = 0; i < split.length; i++ ) {
							String line = split[i];
							int leadingTrimmedFromLine = line.length() - trimLeadingWhiteAndStars(line).length();
							int trailingTrimmedFromLine = line.length() - trimLeadingWhiteAndStars(new StringBuffer(line).reverse().toString()).length();
							int lineStart = contentsStart + runningTally;
							int lineTrimmedStart = contentsStart + leadingTrimmedFromLine + runningTally;
							int lineTrimmedEnd = lineStart + line.length() - trailingTrimmedFromLine;
							int lineTrimmedLength = lineTrimmedEnd - lineTrimmedStart;
							if( lineTrimmedLength > 0 ) {
								String lineTrimmedContent = this.rawText.substring(lineTrimmedStart, lineTrimmedEnd);
								if( lineTrimmedContent.startsWith("@")) {
									previousTag = null;
								}
								TextElement text = this.ast.newTextElement();
								text.setText(lineTrimmedContent);
								text.setSourceRange(lineTrimmedStart, lineTrimmedLength);
								
								if( previousTag == null ) {
									previousTag = this.ast.newTagElement();
									previousTag.setSourceRange(lineTrimmedStart, lineTrimmedEnd - lineTrimmedStart);
									jd.tags().add(previousTag);
								} else {
									previousTag.setSourceRange(previousTag.getStartPosition(), lineTrimmedEnd - previousTag.getStartPosition());
								}
								previousTag.fragments().add(text);
							} else {
								previousTag = null;
							}
							runningTally += line.length() + 1;
						}
					}
				}
			}
		}
	}
	
	private String trimLeadingWhiteAndStars(String line) {
		int length = line.length();
		for( int i = 0; i < length; i++ ) {
			if( !Character.isWhitespace(line.charAt(i)) && line.charAt(i) != '*') {
				return line.substring(i);
			}
		}
		return "";
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
			if (fieldAccess.getExpression() instanceof JCFieldAccess parentFieldAccess && Objects.equals(Names.instance(this.context)._super, parentFieldAccess.getIdentifier())) {
				SuperFieldAccess res = this.ast.newSuperFieldAccess();
				commonSettings(res, javac);
				res.setQualifier(toName(parentFieldAccess.getExpression()));
				res.setName((SimpleName)convert(fieldAccess.getIdentifier()));
				return res;
			}
			FieldAccess res = this.ast.newFieldAccess();
			commonSettings(res, javac);
			res.setExpression(convertExpression(fieldAccess.getExpression()));
			if (convert(fieldAccess.getIdentifier()) instanceof SimpleName name) {
				res.setName(name);
			}
			return res;
		}
		if (javac instanceof JCMethodInvocation methodInvocation) {
			MethodInvocation res = this.ast.newMethodInvocation();
			commonSettings(res, methodInvocation);
			JCExpression nameExpr = methodInvocation.getMethodSelect();
			if (nameExpr instanceof JCIdent ident) {
				if (Objects.equals(ident.getName(), Names.instance(this.context)._super)) {
					return convertSuperMethodInvocation(methodInvocation);
				}
				SimpleName name = (SimpleName)convert(ident.getName());
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
					methodInvocation.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
					if( superCall1 ) {
						res2.setQualifier(toName(fa.getExpression()));
					}
					res2.setName((SimpleName)convert(access.getIdentifier()));
					return res2;
				}
				res.setName((SimpleName)convert(access.getIdentifier()));
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
				res.setName(toName(newClass.clazz));
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
			return res;
		}
		if (javac instanceof JCErroneous error) {
			if (error.getErrorTrees().size() == 1) {
				JCTree tree = error.getErrorTrees().get(0);
				if (tree instanceof JCExpression nestedExpr) {
					return convertExpression(nestedExpr);
				}
			} else {
				ParenthesizedExpression substitute = this.ast.newParenthesizedExpression();
				commonSettings(substitute, error);
				return substitute;
			}
			return null;
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
			if (jcPattern instanceof JCBindingPattern jcBindingPattern) {
				TypePattern jdtPattern = this.ast.newTypePattern();
				commonSettings(jdtPattern, jcBindingPattern);
				jdtPattern.setPatternVariable((SingleVariableDeclaration)convertVariableDeclaration(jcBindingPattern.var));
				res.setPattern(jdtPattern);
			} else {
				throw new UnsupportedOperationException("Missing support to convert '" + jcPattern + "' of type " + javac.getClass().getSimpleName());
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
				res.setName((SimpleName)convert(jcMemberReference.getName()));
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
			res.setBody(
					jcLambda.getBody() instanceof JCExpression expr ? convertExpression(expr) :
					jcLambda.getBody() instanceof JCStatement stmt ? convertStatement(stmt, res) :
					null);
			// TODO set parenthesis looking at the next non-whitespace char after the last parameter
			int endPos = jcLambda.getEndPosition(this.javacCompilationUnit.endPositions);
			res.setSourceRange(jcLambda.pos, endPos - jcLambda.pos);
			return res;
		}
		if (javac instanceof JCNewArray jcNewArray) {
			ArrayCreation res = this.ast.newArrayCreation();
			commonSettings(res, javac);
			if (jcNewArray.getType() != null) {
				Type type = convertToType(jcNewArray.getType());
				ArrayType arrayType = this.ast.newArrayType(type);
				commonSettings(arrayType, jcNewArray.getType());
				res.setType(arrayType);
			}
			jcNewArray.getDimensions().map(this::convertExpression).forEach(res.dimensions()::add);
			if (jcNewArray.getInitializers() != null) {
				ArrayInitializer initializer = this.ast.newArrayInitializer();
				commonSettings(initializer, javac);
				jcNewArray.getInitializers().stream().map(this::convertExpression).forEach(initializer.expressions()::add);
			}
			return res;
		}
		if (javac instanceof JCAnnotation jcAnnot) {
			return convert(jcAnnot);
		}
		throw new UnsupportedOperationException("Missing support to convert '" + javac + "' of type " + javac.getClass().getSimpleName());
	}

	private AnonymousClassDeclaration createAnonymousClassDeclaration(JCClassDecl javacAnon, ASTNode parent) {
		AnonymousClassDeclaration anon = this.ast.newAnonymousClassDeclaration();
		commonSettings(anon, javacAnon);
		if (javacAnon.getMembers() != null) {
			List<JCTree> members = javacAnon.getMembers();
			for( int i = 0; i < members.size(); i++ ) {
				ASTNode decl = convertBodyDeclaration(members.get(i), parent);
				if( decl != null ) {
					anon.bodyDeclarations().add(decl);
				}
			}
		}
		return anon;
	}

	private int countDimensions(JCArrayTypeTree tree) {
		int ret = 0;
        JCTree elem = tree;
        while (elem != null && elem.hasTag(TYPEARRAY)) {
        	ret++;
            elem = ((JCArrayTypeTree)elem).elemtype;
        }
        return ret;
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

	private ConstructorInvocation convertThisConstructorInvocation(JCMethodInvocation javac) {
		ConstructorInvocation res = this.ast.newConstructorInvocation();
		commonSettings(res, javac);
		javac.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);
		javac.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
		return res;
	}

	private Expression convertLiteral(JCLiteral literal) {
		Object value = literal.getValue();
		if (value instanceof Number number) {
			NumberLiteral res = this.ast.newNumberLiteral();
			commonSettings(res, literal);
			res.setToken(literal.value.toString()); // TODO: we want the token here
			return res;
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
		if (value instanceof Character) {
			CharacterLiteral res = this.ast.newCharacterLiteral();
			commonSettings(res, literal);
			res.setCharValue(res.charValue());
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
						Statement stmt = convertStatement(nestedStmt, parent);
						stmt.setFlags(stmt.getFlags() | ASTNode.RECOVERED);
						return stmt;
					}
				} else {
					Block substitute = this.ast.newBlock();
					commonSettings(substitute, jcError);
					parent.setFlags(parent.getFlags() | ASTNode.MALFORMED);
					return substitute;
				}
			}
			ExpressionStatement res = this.ast.newExpressionStatement(convertExpression(jcExpressionStatement.getExpression()));
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCVariableDecl jcVariableDecl) {
			VariableDeclarationFragment fragment = createVariableDeclarationFragment(jcVariableDecl);
			VariableDeclarationStatement res = this.ast.newVariableDeclarationStatement(fragment);
			commonSettings(res, javac);
			res.setType(convertToType(jcVariableDecl.vartype));
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
			res.setExpression(convertExpression(jcSynchronized.getExpression()));
			res.setBody(convertBlock(jcSynchronized.getBlock()));
			return res;
		}
		if (javac instanceof JCForLoop jcForLoop) {
			ForStatement res = this.ast.newForStatement();
			commonSettings(res, javac);
			res.setBody(convertStatement(jcForLoop.getStatement(), res));
			Iterator initializerIt = jcForLoop.getInitializer().iterator();
			while(initializerIt.hasNext()) {
				res.initializers().add(convertStatementToExpression((JCStatement)initializerIt.next(), res));
			}
			res.setExpression(convertExpression(jcForLoop.getCondition()));

			Iterator updateIt = jcForLoop.getUpdate().iterator();
			while(updateIt.hasNext()) {
				res.updaters().add(convertStatementToExpression((JCStatement)updateIt.next(), res));
			}
			return res;
		}
		if (javac instanceof JCEnhancedForLoop jcEnhancedForLoop) {
			EnhancedForStatement res = this.ast.newEnhancedForStatement();
			commonSettings(res, javac);
			res.setParameter((SingleVariableDeclaration)convertVariableDeclaration(jcEnhancedForLoop.getVariable()));
			res.setExpression(convertExpression(jcEnhancedForLoop.getExpression()));
			res.setBody(convertStatement(jcEnhancedForLoop.getStatement(), res));
			return res;
		}
		if (javac instanceof JCBreak jcBreak) {
			BreakStatement res = this.ast.newBreakStatement();
			commonSettings(res, javac);
			if (jcBreak.getLabel() != null) {
				res.setLabel((SimpleName)convert(jcBreak.getLabel()));
			}
			return res;
		}
		if (javac instanceof JCSwitch jcSwitch) {
			SwitchStatement res = this.ast.newSwitchStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcSwitch.getExpression()));
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
				.forEach(res.statements()::add);
			return res;
		}
		if (javac instanceof JCCase jcCase) {
			SwitchCase res = this.ast.newSwitchCase();
			commonSettings(res, javac);
			res.setSwitchLabeledRule(jcCase.getCaseKind() == CaseKind.RULE);
			jcCase.getExpressions().stream().map(this::convertExpression).forEach(res.expressions()::add);
			// jcCase.getStatements is processed as part of JCSwitch conversion
			return res;
		}
		if (javac instanceof JCWhileLoop jcWhile) {
			WhileStatement res = this.ast.newWhileStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcWhile.getCondition()));
			res.setBody(convertStatement(jcWhile.getStatement(), res));
			return res;
		}
		if (javac instanceof JCDoWhileLoop jcDoWhile) {
			DoStatement res = this.ast.newDoStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcDoWhile.getCondition()));
			res.setBody(convertStatement(jcDoWhile.getStatement(), res));
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
				res.setLabel((SimpleName)convert(jcContinue.getLabel()));
			}
			return res;
		}
		if (javac instanceof JCLabeledStatement jcLabel) {
			LabeledStatement res = this.ast.newLabeledStatement();
			commonSettings(res, javac);
			res.setLabel((SimpleName)convert(jcLabel.getLabel()));
			return res;
		}
		if (javac instanceof JCAssert jcAssert) {
			AssertStatement res =this.ast.newAssertStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcAssert.getCondition()));
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
			return jdtVariableDeclarationExpression;
		}
		throw new UnsupportedOperationException(javac + " of type" + javac.getClass());
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
			res.setExpression(convertExpression(javac.getCondition()));
		}
		if (javac.getThenStatement() != null) {
			res.setThenStatement(convertStatement(javac.getThenStatement(), res));
		}
		if (javac.getElseStatement() != null) {
			res.setElseStatement(convertStatement(javac.getElseStatement(), res));
		}
		return res;
	}

	private Type convertToType(JCTree javac) {
		if (javac instanceof JCIdent ident) {
			SimpleType res = this.ast.newSimpleType(convert(ident.name));
			commonSettings(res, ident);
			return res;
		}
		if (javac instanceof JCFieldAccess qualified) {
			if( this.ast.apiLevel != AST.JLS2_INTERNAL ) {
				// TODO need more logic here, but, the common case is a simple type
				Name qn = toName(qualified);  
				SimpleType res = this.ast.newSimpleType(qn);
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
			UnionType res = this.ast.newUnionType();
			commonSettings(res, javac);
			union.getTypeAlternatives().stream().map(this::convertToType).forEach(res.types()::add);
			return res;
		}
		if (javac instanceof JCArrayTypeTree jcArrayType) {
			ArrayType res = this.ast.newArrayType(convertToType(jcArrayType.getType()));
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCTypeApply jcTypeApply) {
			ParameterizedType res = this.ast.newParameterizedType(convertToType(jcTypeApply.getType()));
			commonSettings(res, javac);
			jcTypeApply.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
			return res;
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
		// TODO this needs more work, see below
		String asString = javac.toString();
		if( !asString.contains("(")) {
			MarkerAnnotation res = this.ast.newMarkerAnnotation();
			commonSettings(res, javac);
			res.setTypeName(toName(javac.getAnnotationType()));
			return res;
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

	
	private Modifier convert(javax.lang.model.element.Modifier javac, int startPos, int endPos) {
		Modifier res = this.ast.newModifier(switch (javac) {
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
		});
		// This needs work... It's not a great solution. 
		String sub = this.rawText.substring(startPos, endPos);
		int indOf = sub.indexOf(res.getKeyword().toString());
		if( indOf != -1 ) {
			res.setSourceRange(startPos+indOf, res.getKeyword().toString().length());
		}
		return res;
	}

	
	private Name convert(com.sun.tools.javac.util.Name javac) {
		if (javac == null || Objects.equals(javac, Names.instance(this.context).error) || Objects.equals(javac, Names.instance(this.context).empty)) {
			return null;
		}
		String nameString = javac.toString();
		int lastDot = nameString.lastIndexOf(".");
		if (lastDot < 0) {
			return this.ast.newSimpleName(nameString);
		} else {
			return this.ast.newQualifiedName(convert(javac.subName(0, lastDot)), (SimpleName)convert(javac.subName(lastDot + 1, javac.length() - 1)));
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
		// TODO use a problem factory? Map code to category...?
		return new DefaultProblem(
			javacDiagnostic.getSource().getName().toCharArray(),
			javacDiagnostic.getMessage(Locale.getDefault()),
			toProblemId(javacDiagnostic.getCode()), // TODO probably use `getCode()` here
			null,
			switch (javacDiagnostic.getKind()) {
				case ERROR -> ProblemSeverities.Error;
				case WARNING, MANDATORY_WARNING -> ProblemSeverities.Warning;
				case NOTE -> ProblemSeverities.Info;
				default -> ProblemSeverities.Error;
			},
			(int)Math.min(javacDiagnostic.getPosition(), javacDiagnostic.getStartPosition()),
			(int)javacDiagnostic.getEndPosition(),
			(int)javacDiagnostic.getLineNumber(),
			(int)javacDiagnostic.getColumnNumber());
	}

	private static int toProblemId(String javacDiagnosticCode) {
		// better use a Map<String, IProblem> if there is a 1->0..1 mapping
		return switch (javacDiagnosticCode) {
			case "compiler.warn.raw.class.use" -> IProblem.RawTypeReference;
			// TODO complete mapping list; dig in https://github.com/openjdk/jdk/blob/master/src/jdk.compiler/share/classes/com/sun/tools/javac/resources/compiler.properties
			// for an exhaustive (but polluted) list, unless a better source can be found (spec?)
			default -> 0;
		};
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
			if (excluded.isEmpty()) {
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
				if( enumConstant.init instanceof JCNewClass jcnc && jcnc.def instanceof JCClassDecl jccd) {
					AnonymousClassDeclaration e = createAnonymousClassDeclaration(jccd, enumConstantDeclaration);
					if( e != null ) {
						enumConstantDeclaration.setAnonymousClassDeclaration(e);
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
