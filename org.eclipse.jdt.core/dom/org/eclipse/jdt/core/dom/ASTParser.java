/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.core.util.RecordedParsingInformation;

/**
 * A Java language parser for creating abstract syntax trees (ASTs).
 * <p>
 * Example: Create basic AST from source string
 * <pre>
 * char[] source = ...;
 * ASTParser parser = ASTParser.newParser(AST.JLS2);  // handles JLS2 (J2SE 1.4)
 * parser.setSource(source);
 * CompilationUnit result = (CompilationUnit) parser.createAST(null);
 * </pre>
 * Once a configured parser instance has been used to create an AST,
 * the settings are automicatically returned to their defaults,
 * ready for the parser instance to be reused.
 * </p>
 * <p>
 * There are a number of configurable features:
 * <ul>
 * <li>Source string from {@link #setSource(char[]) char[]},
 * {@link #setSource(ICompilationUnit) ICompilationUnit},
 * or {@link #setSource(IClassFile) IClassFile}, and limited
 * to a specified {@linkplain #setSourceRange(int,int) subrange}.</li>
 * <li>Whether {@linkplain #setResolveBindings(boolean) bindings} will be created.</li>
 * <li>Which {@linkplain #setWorkingCopyOwner(WorkingCopyOwner)
 * working set owner} to use when resolving bindings).</li>
 * <li>A hypothetical {@linkplain #setUnitName(String) compilation unit file name}
 * and {@linkplain #setProject(IJavaProject) Java project}
 * for locating a raw source string in the Java model (when
 * resolving bindings)</li>
 * <li>Which {@linkplain #setCompilerOptions(Map) compiler options}
 * to use.</li>
 * <li>Whether to parse just {@linkplain #setKind(int) an expression, statements,
 * or body declarations} rather than an entire compilation unit.</li>
 * <li>Whether to return a {@linkplain #setFocalPosition(int) abridged AST}
 * focused on the declaration containing a given source position.</li>
 * </ul>
 * </p>
 * 
 * @since 3.0
 */
public class ASTParser {

	/**
	 * Kind constant used to request that the source be parsed
     * as a single expression.
	 */
	public static final int K_EXPRESSION = 0x01;

	/**
	 * Kind constant used to request that the source be parsed
     * as a sequence of statements.
	 */
	public static final int K_STATEMENTS = 0x02;
	
	/**
	 * Kind constant used to request that the source be parsed
	 * as a sequence of class body declarations.
	 */
	public static final int K_CLASS_BODY_DECLARATIONS = 0x04;
	
	/**
	 * Kind constant used to request that the source be parsed
	 * as a compilation unit.
	 */
	public static final int K_COMPILATION_UNIT = 0x08;
	
	/**
	 * Creates a new object for creating a Java abstract syntax tree
     * (AST) following the specified set of API rules.
     * <p>
     * <b>NOTE:</b>In Eclipse 3.0, there is no parser support for
     * AST.JLS3. This support is planned for the follow-on release of
     * Eclipse which includes support for J2SE 1.5.
     * </p>
     *  
 	 * @param level the API level; one of the LEVEL constants
     * declared on <code>AST</code>
	 * @return new ASTParser instance
	 */
	public static ASTParser newParser(int level) {
		return new ASTParser(level);
	}

	/**
	 * Level of AST API desired.
	 */
	private final int apiLevel;

	/**
	 * Kind of parse requested. Defaults to an entire compilation unit.
	 */
	private int astKind;
	
	/**
	 * Compiler options. Defaults to JavaCore.getOptions().
	 */
	private Map compilerOptions;
	
	/**
	 * Request for bindings. Defaults to <code>false</code>.
     */
	private boolean resolveBindings;

	/**
	 * Request for a partial AST. Defaults to <code>false</code>.
     */
	private boolean partial = false;

	/**
	 * The focal point for a partial AST request.
     * Only used when <code>partial</code> is <code>true</code>.
     */
	private int focalPointPosition;

    /**
     * Source string. 
     */
    private char[] rawSource = null;
    
    /**
     * Java mode compilation unit supplying the source.
     */
    private ICompilationUnit compilationUnitSource = null;
    
    /**
     * Java model class file supplying the source.
     */
    private IClassFile classFileSource = null;
    
    /**
     * Character-based offset into the source string where parsing is to
     * begin. Defaults to 0.
     */
	private int sourceOffset = 0;
	
    /**
     * Character-based length limit, or -1 if unlimited.
     * All characters in the source string between <code>offset</code>
     * and <code>offset+length-1</code> inclusive are parsed. Defaults to -1, 
     * which means the rest of the source string.
     */
	private int sourceLength = -1;

    /**
     * Working copy owner. Defaults to primary owner.
     */
	private WorkingCopyOwner workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
	
    /**
	 * Java project used to resolve names, or <code>null</code> if none.
     * Defaults to none.
     */
	private IJavaProject project = null;
	
    /**
	 * Name of the compilation unit for resolving bindings, or 
	 * <code>null</code> if none. Defaults to none.
     */
	private String unitName = null; 

 	/**
	 * Creates a new AST parser for the given API level.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param level the API level; one of the LEVEL constants
     * declared on <code>AST</code>
	 */
	ASTParser(int level) {
		if ((level != AST.JLS2)
			&& (level != AST.JLS3)) {
			throw new IllegalArgumentException();
		}
		this.apiLevel = level;
	   	initializeDefaults();
	}

	/**
	 * Sets all the setting to their default values.
	 */
	private void initializeDefaults() {
	   this.astKind = K_COMPILATION_UNIT;
	   this.rawSource = null;
	   this.classFileSource = null;
	   this.compilationUnitSource = null;
	   this.resolveBindings = false;
	   this.sourceLength = -1;
	   this.sourceOffset = 0;
	   this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
	   this.unitName = null;
	   this.project = null;
	   this.partial = false;
	   this.compilerOptions = JavaCore.getOptions();
	}
	   
	/**
	 * Sets the compiler options to be used when parsing.
     * <p>
     * The compiler options default to {@link JavaCore#getOptions()}.
     * </p>
	 * 
	 * @param options the table of options (key type: <code>String</code>;
	 * value type: <code>String</code>), or <code>null</code>
     * to set it back to the default
	 */
	public void setCompilerOptions(Map options) {
	   if (options == null) {
	      this.compilerOptions = JavaCore.getOptions();
	   }
	   this.compilerOptions = options;
	}
	
	/**
	 * Requests that the compiler should provide binding information for
     * the AST nodes it creates.
     * <p>
     * Default to <code>false</code> (no bindings).
     * </p>
	 * <p>
	 * If <code>setResolveBindings(true)</code>, the various names
	 * and types appearing in the AST can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings 
	 * draw connections between the different parts of a program, and 
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a 
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the 
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Bindings are resolved at the time the AST is created. Subsequent
	 * modifications to the AST do not affect the bindings returned by
	 * <code>resolveBinding</code> methods in any way; these methods return the
	 * same binding as before the AST was modified (including modifications
	 * that rearrange subtrees by reparenting nodes).
	 * If <code>setResolveBindings(false)</code> (the default), the analysis 
	 * does not go beyond parsing and building the tree, and all 
	 * <code>resolveBinding</code> methods return <code>null</code> from the 
	 * outset.
	 * </p>
	 * <p>
	 * When bindings are requested, instead of considering compilation units on disk only
	 * one can supply a <code>WorkingCopyOwner</code>. Working copies owned 
	 * by this owner take precedence over the underlying compilation units when looking
	 * up names and drawing the connections.
	 * </p>
	 * <p>
     * Binding information is obtained from the Java model.
     * This means that the compilation unit must be located relative to the
     * Java model. This happens automatically when the source code comes from
     * either {@link #setSource(ICompilationUnit) setSource(ICompilationUnit)}
     * or {@link #setSource(IClassFile) setSource(IClassFile)}.
     * When source is supplied by {@link #setSource(char[]) setSource(char[])},
     * the location must be extablished explicitly by calling 
     * {@link #setProject(IJavaProject)} and  {@link #setUnitName(String)}.
	 * Note that the compiler options that affect doc comment checking may also
	 * affect whether any bindings are resolved for nodes within doc comments.
	 * </p>
	 * 
	 * @param bindings <code>true</code> if bindings are wanted, 
	 *   and <code>false</code> if bindings are not of interest
	 */
	public void setResolveBindings(boolean bindings) {
	  this.resolveBindings = bindings;
	}
	
	/**
     * Requests an abridged abstract syntax tree. 
     * By default, complete ASTs are returned.
     *
     * When <code>true</code> the resulting AST does not have nodes for
     * the entire compilation unit. Rather, the AST is only fleshed out
     * for the node that include the given source position. This kind of limited
     * AST is sufficient for certain purposes but totally unsuitable for others.
     * In places where it can be used, the limited AST offers the advantage of
     * being smaller and faster to construct.
	 * </p>
	 * <p>
	 * The AST will include nodes for all of the compilation unit's
	 * package, import, and top-level type declarations. It will also always contain
	 * nodes for all the body declarations for those top-level types, as well
	 * as body declarations for any member types. However, some of the body
	 * declarations may be abridged. In particular, the statements ordinarily
	 * found in the body of a method declaration node will not be included
	 * (the block will be empty) unless the source position falls somewhere
	 * within the source range of that method declaration node. The same is true
	 * for initializer declarations; the statements ordinarily found in the body
	 * of initializer node will not be included unless the source position falls
	 * somewhere within the source range of that initializer declaration node.
	 * Field declarations are never abridged. Note that the AST for the body of
	 * that one unabridged method (or initializer) is 100% complete; it has all
	 * its statements, including any local or anonymous type declarations 
	 * embedded within them. When the the given position is not located within
	 * the source range of any body declaration of a top-level type, the AST
	 * returned will be a skeleton that includes nodes for all and only the major
	 * declarations; this kind of AST is still quite useful because it contains
	 * all the constructs that introduce names visible to the world outside the
	 * compilation unit.
	 * </p>
	 * 
	 * @param position a position into the corresponding body declaration
	 */
	public void setFocalPosition(int position) {
		this.partial = true;
		this.focalPointPosition = position;
	}
	
	/**
	 * Sets the kind of constructs to be parsed from the source.
     * Defaults to an entire compilation unit.
	 * <p>
	 * When the parse is successful the result returned includes the ASTs for the
	 * requested source:
	 * <ul>
	 * <li>{@link #K_COMPILATION_UNIT}: The result node
	 * is a {@link CompilationUnit}.</li>
	 * <li>{@link #K_CLASS_BODY_DECLARATIONS}: The result node
	 * is a {@link TypeDeclaration} whose
	 * {@link TypeDeclaration#bodyDeclarations() bodyDeclarations}
	 * are the new trees. Other aspects of the type declaration are unspecified.</li>
	 * <li>{@link #K_STATEMENTS}: The result node is a
	 * {@link Block Block} whose {@link Block#statements() statements}
	 * are the new trees. Other aspects of the block are unspecified.</li>
	 * <li>{@link #K_EXPRESSION}: The result node is a subclass of
	 * {@link Expression Expression}. Other aspects of the expression are unspecified.</li>
	 * </ul>
	 * The resulting AST node is rooted under (possibly contrived)
	 * {@link CompilationUnit CompilationUnit} node, to allow the
	 * client to retrieve the following pieces of information 
	 * available there:
	 * <ul>
	 * <li>{@linkplain CompilationUnit#lineNumber(int) Line number map}. Line
	 * numbers start at 1 and only cover the subrange scanned
	 * (<code>source[offset]</code> through <code>source[offset+length-1]</code>).</li>
	 * <li>{@linkplain CompilationUnit#getMessages() Compiler messages}
	 * and {@linkplain CompilationUnit#getProblems() detailed problem reports}.
	 * Character positions are relative to the start of 
	 * <code>source</code>; line positions are for the subrange scanned.</li>
	 * <li>{@linkplain CompilationUnit#getCommentList() Comment list}
	 * for the subrange scanned.</li>
	 * </ul>
	 * The contrived nodes do not have source positions. Other aspects of the
	 * {@link CompilationUnit CompilationUnit} node are unspecified, including
	 * the exact arrangment of intervening nodes.
	 * </p>
	 * <p>
	 * Lexical or syntax errors detected while parsing can result in
	 * a result node being marked as {@link ASTNode#MALFORMED MALFORMED}.
	 * In more severe failure cases where the parser is unable to
	 * recognize the input, this method returns 
	 * a {@link CompilationUnit CompilationUnit} node with at least the
	 * compiler messages.
	 * </p>
	 * <p>Each node in the subtree (other than the contrived nodes) 
	 * carries source range(s) information relating back
	 * to positions in the given source (the given source itself
	 * is not remembered with the AST). 
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * </p>
	 * <p>
	 * Binding information is only computed when <code>kind</code> is 
     * <code>K_COMPILATION_UNIT</code>.
	 * </p>
	 *  
	 * @param kind the kind of construct to parse: one of 
	 * {@link #K_COMPILATION_UNIT},
	 * {@link #K_CLASS_BODY_DECLARATIONS},
	 * {@link #K_EXPRESSION},
	 * {@link #K_STATEMENTS}
	 */
	public void setKind(int kind) {
	    if ((kind != K_COMPILATION_UNIT)
		    && (kind != K_CLASS_BODY_DECLARATIONS)
		    && (kind != K_EXPRESSION)
		    && (kind != K_STATEMENTS)) {
	    	throw new IllegalArgumentException();
	    }
		this.astKind = kind;
	}
	
	/**
     * Sets the source code to be parsed.
     *
	 * @param source the source string to be parsed,
     * or <code>null</code> if none
     */
	public void setSource(char[] source) {
		this.rawSource = source;
		// clear the others
		this.compilationUnitSource = null;
		this.classFileSource = null;
	}

	/**
     * Sets the source code to be parsed.
     * This method automatically sets the project (and compiler
     * options) based on the given compilation unit, in a manner
     * equivalent to <code>setProject(source.getJavaProject())</code>
     *
	 * @param source the Java model compilation unit whose source code
     * is to be parsed, or <code>null</code> if none
      */
	public void setSource(ICompilationUnit source) {
		this.compilationUnitSource = source;
		// clear the others
		this.rawSource = null;
		this.classFileSource = null;
		if (source != null) {
			this.project = source.getJavaProject();
			this.compilerOptions = this.project.getOptions(true);
		}
	}
	
	/**
     * Sets the source code to be parsed.
     * This method automatically sets the project (and compiler
     * options) based on the given compilation unit, in a manner
     * equivalent to <code>setProject(source.getJavaProject())</code>
     *
	 * @param source the Java model class file whose corresponding source code
     * is to be parsed, or <code>null</code> if none
     */
	public void setSource(IClassFile source) {
		this.classFileSource = source;
		// clear the others
		this.rawSource = null;
		this.compilationUnitSource = null;
		if (source != null) {
			this.project = source.getJavaProject();
			this.compilerOptions = this.project.getOptions(true);
		}
	}
	
	/**
     * Sets the subrange of the source code to be parsed.
     * By default, the entire source string will be parsed
     * (<code>offset</code> 0 and <code>length</code> -1).
     *
     * @param offset the index of the first character to parse
     * @param length the number of characters to parse, or -1 if
     * the remainder of the source string is 
     */
	public void setSourceRange(int offset, int length) {
		if (offset < 0 || length < -1) {
			throw new IllegalArgumentException();
		}
		this.sourceOffset = offset;
		this.sourceLength = length;
	}
	
    /**
     * Sets the working copy owner using when resolving bindings, where
     * <code>null</code> means the primary owner. Defaults to the primary owner.
     *
	 * @param owner the owner of working copies that take precedence over underlying 
	 *   compilation units, or <code>null</code> if the primary owner should be used
     */
	public void setWorkingCopyOwner(WorkingCopyOwner owner) {
	    if (owner == null) {
			this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
		} else {
			this.workingCopyOwner = owner;
	 	}
	}

	/**
     * Sets the name of the compilation unit that would hypothetically contains
     * the source string. This is used in conjunction with
     * <code>setSource(char[])</code> and <code>setProject</code> to locate the
     * compilation unit relative to a Java project.
     * Defaults to none (<code>null</code>).
	 * <p>
	 * The name of the compilation unit must be supplied for resolving bindings.
	 * This name should include the ".java" suffix and match the name of the main
	 * (public) class or interface declared in the source. For example, if the source
	 * declares a public class named "Foo", the name of the compilation should be
	 * "Foo.java".
	 * </p>
     *
	 * @param unitName the name of the compilation unit that would contain the source
	 *    string, or <code>null</code> if none
     */
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	/**
     * Sets the Java project used when resolving bindings.
     * This method automatically sets the compiler
     * options based on the given project:
     * <pre>
     * setCompilerOptions(project.getOptions(true));
     * </pre>
     * This setting is used in conjunction with <code>setSource(char[])</code>.
     * For the purposes of resolving bindings, types declared in the
	 * source string will hide types by the same name available
     * through the classpath of the given project.
     * Defaults to none (<code>null</code>).
     * 
	 * @param project the Java project used to resolve names, or 
	 *    <code>null</code> if none
     */
	public void setProject(IJavaProject project) {
		this.project = project;
		if (project != null) {
			this.compilerOptions = project.getOptions(true);
		}
	}
	
	/**
     * Creates an abstract syntax tree.
     * <p>
     * A successful call to this method returns all settings to their
     * default values so the object is ready to be reused.
     * </p>
     * 
	 * @param monitor the progress monitor used to report progress and request cancelation,
	 *   or <code>null</code> if none
	 * @return an AST node whose type depends on the kind of parse
	 *  requested, with a fallback to a <code>CompilationUnit</code>
	 *  in the case of severe parsing errors
	 * @exception IllegalStateException if the settings provided
	 * are insufficient, contradictory, or otherwise unsupported
     */
	public ASTNode createAST(IProgressMonitor monitor) {
	   ASTNode result = null;
		try {
			if ((this.rawSource == null)
		   	  && (this.compilationUnitSource == null)
		   	  && (this.classFileSource == null)) {
		   	  throw new IllegalStateException("source not specified"); //$NON-NLS-1$
		   }
	   		result = internalCreateAST(monitor);
		} finally {
	   	   // re-init defaults to allow reuse (and avoid leaking)
	   	   initializeDefaults();
		}
   	   return result;
	}
	
	private ASTNode internalCreateAST(IProgressMonitor monitor) {
		boolean needToResolveBindings = this.resolveBindings;
		switch(this.astKind) {
			case K_CLASS_BODY_DECLARATIONS :
			case K_EXPRESSION :
			case K_STATEMENTS :
				if (this.rawSource != null) {
					if (this.sourceOffset + this.sourceLength > this.rawSource.length) {
					    throw new IllegalStateException();
					}
					return internalCreateASTForKind();
				}
				break;
			case K_COMPILATION_UNIT :
				CompilationUnitDeclaration compilationUnitDeclaration = null;
				try {
					char[] source = null;
					NodeSearcher searcher = null;
					char[][] packageName = null;
					String fileName = null;
					if (this.compilationUnitSource != null) {
						try {
							source = this.compilationUnitSource.getSource().toCharArray();
						} catch(JavaModelException e) {
							// no source, then we cannot build anything
							throw new IllegalStateException();
						}
						IPackageFragment packageFragment = (IPackageFragment)this.compilationUnitSource.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
						if (packageFragment != null){
							packageName = CharOperation.splitOn('.', packageFragment.getElementName().toCharArray());
						}
						fileName = this.compilationUnitSource.getElementName();
					} else if (this.classFileSource != null) {
						String sourceString = null;
						try {
							sourceString = this.classFileSource.getSource();
						} catch (JavaModelException e) {
							// nothing to do
						}
					
						if (sourceString == null) {
							throw new IllegalStateException();
						}
						source = sourceString.toCharArray();
						try {
							packageName = CharOperation.splitOn('.', this.classFileSource.getType().getPackageFragment().getElementName().toCharArray());
							StringBuffer buffer = new StringBuffer(SuffixConstants.SUFFIX_STRING_java);
							String classFileName = this.classFileSource.getElementName(); // this includes the trailing .class
							buffer.insert(0, classFileName.toCharArray(), 0, classFileName.indexOf('.'));
							fileName = String.valueOf(buffer);
						} catch(JavaModelException e) {
							needToResolveBindings = false;
						}
					} else if (this.rawSource != null) {
						source = this.rawSource;
						if (this.unitName == null || this.project == null || this.compilerOptions == null) {
							needToResolveBindings = false;
						} else {
							fileName = this.unitName;
							needToResolveBindings = true;
						}
					}
					if (source == null) {
						throw new IllegalStateException();
					}
					if (this.partial) {
						searcher = new NodeSearcher(this.focalPointPosition);
					}
					if (needToResolveBindings && this.project != null) {
						try {
							// parse and resolve
							compilationUnitDeclaration = 
								CompilationUnitResolver.resolve(
									source,
									packageName,
									fileName,
									this.project,
									searcher,
									this.compilerOptions,
									false,
									this.workingCopyOwner,
									monitor);
						} catch (JavaModelException e) {
							compilationUnitDeclaration = CompilationUnitResolver.parse(
									source,
									searcher,
									this.compilerOptions);
							needToResolveBindings = false;
						}
					} else {
						compilationUnitDeclaration = CompilationUnitResolver.parse(
								source,
								searcher,
								this.compilerOptions);
						needToResolveBindings = false;
					}
					return convert(monitor, compilationUnitDeclaration, source, needToResolveBindings);
				} finally {
					if (compilationUnitDeclaration != null && this.resolveBindings) {
						compilationUnitDeclaration.cleanUp();
					}
				}					
		}
		throw new IllegalStateException();
	}

	/**
	 * @param monitor
	 * @param compilationUnitDeclaration
	 * @param source
	 * @return
	 */
	private ASTNode convert(IProgressMonitor monitor, CompilationUnitDeclaration compilationUnitDeclaration, char[] source, boolean needToResolveBindings) {
		BindingResolver resolver = null;
		AST ast = AST.newAST(this.apiLevel);
		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
		CompilationUnit compilationUnit = null;
		if (AST.JLS2 == this.apiLevel) {
			ASTConverter converter = new ASTConverter(this.compilerOptions, needToResolveBindings, monitor);
			if (needToResolveBindings) {
				resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope);
			} else {
				resolver = new BindingResolver();
			}
			ast.setBindingResolver(resolver);
			converter.setAST(ast);
			compilationUnit = converter.convert(compilationUnitDeclaration, source);
			compilationUnit.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
		} else {
			throw new RuntimeException("J2SE 1.5 parser not implemented yet"); //$NON-NLS-1$
		}
		ast.setDefaultNodeFlag(0);
		ast.setOriginalModificationCount(ast.modificationCount());
		return compilationUnit;
	}

	/**
	 * Parses the given source between the bounds specified by the given offset (inclusive)
	 * and the given length and creates and returns a corresponding abstract syntax tree.
	 * <p>
	 * When the parse is successful the result returned includes the ASTs for the
	 * requested source:
	 * <ul>
	 * <li>{@link #K_CLASS_BODY_DECLARATIONS K_CLASS_BODY_DECLARATIONS}: The result node
	 * is a {@link TypeDeclaration TypeDeclaration} whose
	 * {@link TypeDeclaration#bodyDeclarations() bodyDeclarations}
	 * are the new trees. Other aspects of the type declaration are unspecified.</li>
	 * <li>{@link #K_STATEMENTS K_STATEMENTS}: The result node is a
	 * {@link Block Block} whose {@link Block#statements() statements}
	 * are the new trees. Other aspects of the block are unspecified.</li>
	 * <li>{@link #K_EXPRESSION K_EXPRESSION}: The result node is a subclass of
	 * {@link Expression Expression}. Other aspects of the expression are unspecified.</li>
	 * </ul>
	 * The resulting AST node is rooted under an contrived
	 * {@link CompilationUnit CompilationUnit} node, to allow the
	 * client to retrieve the following pieces of information 
	 * available there:
	 * <ul>
	 * <li>{@linkplain CompilationUnit#lineNumber(int) Line number map}. Line
	 * numbers start at 1 and only cover the subrange scanned
	 * (<code>source[offset]</code> through <code>source[offset+length-1]</code>).</li>
	 * <li>{@linkplain CompilationUnit#getMessages() Compiler messages}
	 * and {@linkplain CompilationUnit#getProblems() detailed problem reports}.
	 * Character positions are relative to the start of 
	 * <code>source</code>; line positions are for the subrange scanned.</li>
	 * <li>{@linkplain CompilationUnit#getCommentList() Comment list}
	 * for the subrange scanned.</li>
	 * </ul>
	 * The contrived nodes do not have source positions. Other aspects of the
	 * {@link CompilationUnit CompilationUnit} node are unspecified, including
	 * the exact arrangment of intervening nodes.
	 * </p>
	 * <p>
	 * Lexical or syntax errors detected while parsing can result in
	 * a result node being marked as {@link ASTNode#MALFORMED MALFORMED}.
	 * In more severe failure cases where the parser is unable to
	 * recognize the input, this method returns 
	 * a {@link CompilationUnit CompilationUnit} node with at least the
	 * compiler messages.
	 * </p>
	 * <p>Each node in the subtree (other than the contrived nodes) 
	 * carries source range(s) information relating back
	 * to positions in the given source (the given source itself
	 * is not remembered with the AST). 
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * </p>
	 * <p>
	 * This method does not compute binding information; all <code>resolveBinding</code>
	 * methods applied to nodes of the resulting AST return <code>null</code>.
	 * </p>
	 * 
	 * @return an AST node whose type depends on the kind of parse
	 *  requested, with a fallback to a <code>CompilationUnit</code>
	 *  in the case of severe parsing errors
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 */
	private ASTNode internalCreateASTForKind() {
		ASTConverter converter = new ASTConverter(this.compilerOptions, false, null);
		converter.compilationUnitSource = this.rawSource;
		converter.scanner.setSource(this.rawSource);
		
		AST ast = AST.newAST(this.apiLevel);
		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
		ast.setBindingResolver(new BindingResolver());
		converter.setAST(ast);
		CodeSnippetParsingUtil codeSnippetParsingUtil = new CodeSnippetParsingUtil();
		CompilationUnit compilationUnit = ast.newCompilationUnit();
		switch(this.astKind) {
			case K_STATEMENTS :
				ConstructorDeclaration constructorDeclaration = codeSnippetParsingUtil.parseStatements(this.rawSource, this.sourceOffset, this.sourceLength, this.compilerOptions, true);
				RecordedParsingInformation recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
				int[][] comments = recordedParsingInformation.commentPositions;
				if (comments != null) {
					converter.buildCommentsTable(compilationUnit, comments);
				}
				compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
				if (constructorDeclaration != null) {
					Block block = ast.newBlock();
					Statement[] statements = constructorDeclaration.statements;
					if (statements != null) {
						int statementsLength = statements.length;
						for (int i = 0; i < statementsLength; i++) {
							block.statements().add(converter.convert(statements[i]));
						}
					}
					rootNodeToCompilationUnit(ast, compilationUnit, block, recordedParsingInformation);
					ast.setDefaultNodeFlag(0);
					ast.setOriginalModificationCount(ast.modificationCount());
					return block;
				} else {
					IProblem[] problems = recordedParsingInformation.problems;
					if (problems != null) {
						compilationUnit.setProblems(problems);
					}
					ast.setDefaultNodeFlag(0);
					ast.setOriginalModificationCount(ast.modificationCount());
					return compilationUnit;
				}
			case K_EXPRESSION :
				org.eclipse.jdt.internal.compiler.ast.Expression expression = codeSnippetParsingUtil.parseExpression(this.rawSource, this.sourceOffset, this.sourceLength, this.compilerOptions, true);
				recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
				comments = recordedParsingInformation.commentPositions;
				if (comments != null) {
					converter.buildCommentsTable(compilationUnit, comments);
				}
				compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
				if (expression != null) {
					Expression expression2 = converter.convert(expression);
					rootNodeToCompilationUnit(expression2.getAST(), compilationUnit, expression2, codeSnippetParsingUtil.recordedParsingInformation);
					ast.setDefaultNodeFlag(0);
					ast.setOriginalModificationCount(ast.modificationCount());
					return expression2;
				} else {
					IProblem[] problems = recordedParsingInformation.problems;
					if (problems != null) {
						compilationUnit.setProblems(problems);
					}
					ast.setDefaultNodeFlag(0);
					ast.setOriginalModificationCount(ast.modificationCount());
					return compilationUnit;
				}
			case K_CLASS_BODY_DECLARATIONS :
				final org.eclipse.jdt.internal.compiler.ast.ASTNode[] nodes = codeSnippetParsingUtil.parseClassBodyDeclarations(this.rawSource, this.sourceOffset, this.sourceLength, this.compilerOptions, true);
				recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
				comments = recordedParsingInformation.commentPositions;
				if (comments != null) {
					converter.buildCommentsTable(compilationUnit, comments);
				}
				compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
				if (nodes != null) {
					TypeDeclaration typeDeclaration = converter.convert(nodes);
					rootNodeToCompilationUnit(typeDeclaration.getAST(), compilationUnit, typeDeclaration, codeSnippetParsingUtil.recordedParsingInformation);
					ast.setDefaultNodeFlag(0);
					ast.setOriginalModificationCount(ast.modificationCount());
					return typeDeclaration;
				} else {
					IProblem[] problems = recordedParsingInformation.problems;
					if (problems != null) {
						compilationUnit.setProblems(problems);
					}
					ast.setDefaultNodeFlag(0);
					ast.setOriginalModificationCount(ast.modificationCount());
					return compilationUnit;
				}
		}
		throw new IllegalStateException();
	}

	private void propagateErrors(ASTNode astNode, IProblem[] problems) {
		ASTSyntaxErrorPropagator syntaxErrorPropagator = new ASTSyntaxErrorPropagator(problems);
		astNode.accept(syntaxErrorPropagator);
	}
	
	private void rootNodeToCompilationUnit(AST ast, CompilationUnit compilationUnit, ASTNode node, RecordedParsingInformation recordedParsingInformation) {
		final int problemsCount = recordedParsingInformation.problemsCount;
		switch(node.getNodeType()) {
			case ASTNode.BLOCK :
				{
					Block block = (Block) node;
					if (problemsCount != 0) {
						// propagate and record problems
						final IProblem[] problems = recordedParsingInformation.problems;
						for (int i = 0, max = block.statements().size(); i < max; i++) {
							propagateErrors((ASTNode) block.statements().get(i), problems);
						}
						compilationUnit.setProblems(problems);
					}
					TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
					Initializer initializer = ast.newInitializer();
					initializer.setBody(block);
					typeDeclaration.bodyDeclarations().add(initializer);
					compilationUnit.types().add(typeDeclaration);
				}
				break;
			case ASTNode.TYPE_DECLARATION :
				{
					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
					if (problemsCount != 0) {
						// propagate and record problems
						final IProblem[] problems = recordedParsingInformation.problems;
						for (int i = 0, max = typeDeclaration.bodyDeclarations().size(); i < max; i++) {
							propagateErrors((ASTNode) typeDeclaration.bodyDeclarations().get(i), problems);
						}
						compilationUnit.setProblems(problems);
					}
					compilationUnit.types().add(typeDeclaration);
				}
				break;
			default :
				if (node instanceof Expression) {
					Expression expression = (Expression) node;
					if (problemsCount != 0) {
						// propagate and record problems
						final IProblem[] problems = recordedParsingInformation.problems;
						propagateErrors(expression, problems);
						compilationUnit.setProblems(problems);
					}
					ExpressionStatement expressionStatement = ast.newExpressionStatement(expression);
					Block block = ast.newBlock();
					block.statements().add(expressionStatement);
					Initializer initializer = ast.newInitializer();
					initializer.setBody(block);
					TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
					typeDeclaration.bodyDeclarations().add(initializer);
					compilationUnit.types().add(typeDeclaration);
				}
		}
	}
}