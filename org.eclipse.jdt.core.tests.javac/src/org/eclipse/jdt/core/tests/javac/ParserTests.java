/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.javac;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.JavacCompilationUnitResolver;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.junit.Test;

public class ParserTests {

	@Test
	public void testParseStream() throws Exception {
		JavacCompilationUnitResolver resolver = new JavacCompilationUnitResolver();
		BasicCompilationUnit unit = new BasicCompilationUnit("""
				/**
				 * A sequence of elements supporting sequential and parallel aggregate
				 * operations.  The following example illustrates an aggregate operation using
				 * {@link Stream} and {@link IntStream}:
				 *
				 * <pre>{@code
				 *     int sum = widgets.stream()
				 *                      .filter(w -> w.getColor() == RED)
				 *                      .mapToInt(w -> w.getWeight())
				 *                      .sum();
				 * }</pre>
				 *
				 * In this example, {@code widgets} is a {@code Collection<Widget>}.  We create
				 * a stream of {@code Widget} objects via {@link Collection#stream Collection.stream()},
				 * filter it to produce a stream containing only the red widgets, and then
				 * transform it into a stream of {@code int} values representing the weight of
				 * each red widget. Then this stream is summed to produce a total weight.
				 *
				 * <p>In addition to {@code Stream}, which is a stream of object references,
				 * there are primitive specializations for {@link IntStream}, {@link LongStream},
				 * and {@link DoubleStream}, all of which are referred to as "streams" and
				 * conform to the characteristics and restrictions described here.
				 *
				 * <p>To perform a computation, stream
				 * <a href="package-summary.html#StreamOps">operations</a> are composed into a
				 * <em>stream pipeline</em>.  A stream pipeline consists of a source (which
				 * might be an array, a collection, a generator function, an I/O channel,
				 * etc), zero or more <em>intermediate operations</em> (which transform a
				 * stream into another stream, such as {@link Stream#filter(Predicate)}), and a
				 * <em>terminal operation</em> (which produces a result or side-effect, such
				 * as {@link Stream#count()} or {@link Stream#forEach(Consumer)}).
				 * Streams are lazy; computation on the source data is only performed when the
				 * terminal operation is initiated, and source elements are consumed only
				 * as needed.
				 *
				 * <p>A stream implementation is permitted significant latitude in optimizing
				 * the computation of the result.  For example, a stream implementation is free
				 * to elide operations (or entire stages) from a stream pipeline -- and
				 * therefore elide invocation of behavioral parameters -- if it can prove that
				 * it would not affect the result of the computation.  This means that
				 * side-effects of behavioral parameters may not always be executed and should
				 * not be relied upon, unless otherwise specified (such as by the terminal
				 * operations {@code forEach} and {@code forEachOrdered}). (For a specific
				 * example of such an optimization, see the API note documented on the
				 * {@link #count} operation.  For more detail, see the
				 * <a href="package-summary.html#SideEffects">side-effects</a> section of the
				 * stream package documentation.)
				 *
				 * <p>Collections and streams, while bearing some superficial similarities,
				 * have different goals.  Collections are primarily concerned with the efficient
				 * management of, and access to, their elements.  By contrast, streams do not
				 * provide a means to directly access or manipulate their elements, and are
				 * instead concerned with declaratively describing their source and the
				 * computational operations which will be performed in aggregate on that source.
				 * However, if the provided stream operations do not offer the desired
				 * functionality, the {@link #iterator()} and {@link #spliterator()} operations
				 * can be used to perform a controlled traversal.
				 *
				 * <p>A stream pipeline, like the "widgets" example above, can be viewed as
				 * a <em>query</em> on the stream source.  Unless the source was explicitly
				 * designed for concurrent modification (such as a {@link ConcurrentHashMap}),
				 * unpredictable or erroneous behavior may result from modifying the stream
				 * source while it is being queried.
				 *
				 * <p>Most stream operations accept parameters that describe user-specified
				 * behavior, such as the lambda expression {@code w -> w.getWeight()} passed to
				 * {@code mapToInt} in the example above.  To preserve correct behavior,
				 * these <em>behavioral parameters</em>:
				 * <ul>
				 * <li>must be <a href="package-summary.html#NonInterference">non-interfering</a>
				 * (they do not modify the stream source); and</li>
				 * <li>in most cases must be <a href="package-summary.html#Statelessness">stateless</a>
				 * (their result should not depend on any state that might change during execution
				 * of the stream pipeline).</li>
				 * </ul>
				 *
				 * <p>Such parameters are always instances of a
				 * <a href="../function/package-summary.html">functional interface</a> such
				 * as {@link java.util.function.Function}, and are often lambda expressions or
				 * method references.  Unless otherwise specified these parameters must be
				 * <em>non-null</em>.
				 *
				 * <p>A stream should be operated on (invoking an intermediate or terminal stream
				 * operation) only once.  This rules out, for example, "forked" streams, where
				 * the same source feeds two or more pipelines, or multiple traversals of the
				 * same stream.  A stream implementation may throw {@link IllegalStateException}
				 * if it detects that the stream is being reused. However, since some stream
				 * operations may return their receiver rather than a new stream object, it may
				 * not be possible to detect reuse in all cases.
				 *
				 * <p>Streams have a {@link #close()} method and implement {@link AutoCloseable}.
				 * Operating on a stream after it has been closed will throw {@link IllegalStateException}.
				 * Most stream instances do not actually need to be closed after use, as they
				 * are backed by collections, arrays, or generating functions, which require no
				 * special resource management. Generally, only streams whose source is an IO channel,
				 * such as those returned by {@link Files#lines(Path)}, will require closing. If a
				 * stream does require closing, it must be opened as a resource within a try-with-resources
				 * statement or similar control structure to ensure that it is closed promptly after its
				 * operations have completed.
				 *
				 * <p>Stream pipelines may execute either sequentially or in
				 * <a href="package-summary.html#Parallelism">parallel</a>.  This
				 * execution mode is a property of the stream.  Streams are created
				 * with an initial choice of sequential or parallel execution.  (For example,
				 * {@link Collection#stream() Collection.stream()} creates a sequential stream,
				 * and {@link Collection#parallelStream() Collection.parallelStream()} creates
				 * a parallel one.)  This choice of execution mode may be modified by the
				 * {@link #sequential()} or {@link #parallel()} methods, and may be queried with
				 * the {@link #isParallel()} method.
				 *
				 * @param <T> the type of the stream elements
				 * @since 1.8
				 * @see IntStream
				 * @see LongStream
				 * @see DoubleStream
				 * @see <a href="package-summary.html">java.util.stream</a>
				 */
				public interface Stream<T> extends BaseStream<T, Stream<T>> {
				}
				""".toCharArray(), new char[][] {}, "Stream.java", StandardCharsets.UTF_8.toString());
		CompilationUnit compilationUnit = resolver.toCompilationUnit(unit, false, null, null, 0, AST.getJLSLatest(), Map.of(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED), null, null, 0, null);
		var javadoc = ((AbstractTypeDeclaration)compilationUnit.types().get(0)).getJavadoc();
		Set<ASTNode> errors = new HashSet<>();
		javadoc.accept(new ASTVisitor(true) {
			int previousEnd = 0;
			@Override
			public void preVisit(ASTNode node) {
				super.preVisit(node);
				if (node.getStartPosition() < this.previousEnd) {
					errors.add(node);
				}
				if (node.getStartPosition() < node.getParent().getStartPosition()) {
					errors.add(node);
				}
			}
			@Override
			public void postVisit(ASTNode node) {
				super.postVisit(node);
				this.previousEnd = node.getStartPosition() + node.getLength();
			}
		});
		assertEquals(Set.of(), errors);
	}

	@Test
	public void testParseICompilationUnit() throws Exception {
		JavacCompilationUnitResolver resolver = new JavacCompilationUnitResolver();
		BasicCompilationUnit unit = new BasicCompilationUnit("""
			/*******************************************************************************
			 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
			 *     IBM Corporation - added J2SE 1.5 support
			 *     Microsoft Corporation - support custom options at compilation unit level
			 *******************************************************************************/
			package org.eclipse.jdt.core;

			import java.util.Collections;
			import java.util.Map;
			import org.eclipse.core.runtime.IProgressMonitor;
			import org.eclipse.jdt.core.dom.AST;
			import org.eclipse.jdt.core.dom.ASTParser;
			import org.eclipse.jdt.core.dom.CompilationUnit;
			import org.eclipse.jdt.core.dom.IBinding;
			import org.eclipse.text.edits.TextEdit;
			import org.eclipse.text.edits.UndoEdit;


			/**
			 * Represents an entire Java compilation unit (source file with one of the
			 * {@link JavaCore#getJavaLikeExtensions() Java-like extensions}).
			 * Compilation unit elements need to be opened before they can be navigated or manipulated.
			 * The children are of type {@link IPackageDeclaration},
			 * {@link IImportContainer}, and {@link IType},
			 * and appear in the order in which they are declared in the source.
			 * If a source file cannot be parsed, its structure remains unknown.
			 * Use {@link IJavaElement#isStructureKnown} to determine whether this is
			 * the case.
			 *
			 * @noimplement This interface is not intended to be implemented by clients.
			 */
			@SuppressWarnings("deprecation")
			public interface ICompilationUnit extends ITypeRoot, IWorkingCopy, ISourceManipulation {
			/**
			 * Constant indicating that a reconcile operation should not return an AST.
			 * @since 3.0
			 */
			public static final int NO_AST = 0;

			/**
			 * Constant indicating that a reconcile operation should recompute the problems
			 * even if the source hasn't changed.
			 * @since 3.3
			 */
			public static final int FORCE_PROBLEM_DETECTION = 0x01;

			/**
			 * Constant indicating that a reconcile operation should enable the statements recovery.
			 * @see ASTParser#setStatementsRecovery(boolean)
			 * @since 3.3
			 */
			public static final int ENABLE_STATEMENTS_RECOVERY = 0x02;

			/**
			 * Constant indicating that a reconcile operation should enable the bindings recovery
			 * @see ASTParser#setBindingsRecovery(boolean)
			 * @see IBinding#isRecovered()
			 * @since 3.3
			 */
			public static final int ENABLE_BINDINGS_RECOVERY = 0x04;

			/**
			 * Constant indicating that a reconcile operation could ignore to parse the method bodies.
			 * @see ASTParser#setIgnoreMethodBodies(boolean)
			 * @since 3.5.2
			 */
			public static final int IGNORE_METHOD_BODIES = 0x08;


			/**
			 * Applies a text edit to the compilation unit's buffer.
			 * <p>
			 * Note that the edit is simply applied to the compilation unit's buffer.
			 * In particular the undo edit is not grouped with previous undo edits
			 * if the buffer doesn't implement {@link IBuffer.ITextEditCapability}.
			 * If it does, the exact semantics for grouping undo edit depends
			 * on how {@link IBuffer.ITextEditCapability#applyTextEdit(TextEdit, IProgressMonitor)}
			 * is implemented.
			 * </p>
			 *
			 * @param edit the edit to apply
			 * @param monitor the progress monitor to use or <code>null</code> if no progress should be reported
			 * @return the undo edit
			 * @throws JavaModelException if this edit can not be applied to the compilation unit's buffer. Reasons include:
			 * <ul>
			 * <li>This compilation unit does not exist ({@link IJavaModelStatusConstants#ELEMENT_DOES_NOT_EXIST}).</li>
			 * <li>The provided edit can not be applied as there is a problem with the text edit locations ({@link IJavaModelStatusConstants#BAD_TEXT_EDIT_LOCATION}).</li>
			 * </ul>
			 *
			 * @since 3.4
			 */
			public UndoEdit applyTextEdit(TextEdit edit, IProgressMonitor monitor) throws JavaModelException;

			/**
			 * Changes this compilation unit handle into a working copy. A new {@link IBuffer} is
			 * created using this compilation unit handle's owner. Uses the primary owner if none was
			 * specified when this compilation unit handle was created.
			 * <p>
			 * When switching to working copy mode, problems are reported to given
			 * {@link IProblemRequestor}. Note that once in working copy mode, the given
			 * {@link IProblemRequestor} is ignored. Only the original {@link IProblemRequestor}
			 * is used to report subsequent problems.
			 * </p>
			 * <p>
			 * Once in working copy mode, changes to this compilation unit or its children are done in memory.
			 * Only the new buffer is affected. Using {@link #commitWorkingCopy(boolean, IProgressMonitor)}
			 * will bring the underlying resource in sync with this compilation unit.
			 * </p>
			 * <p>
			 * If this compilation unit was already in working copy mode, an internal counter is incremented and no
			 * other action is taken on this compilation unit. To bring this compilation unit back into the original mode
			 * (where it reflects the underlying resource), {@link #discardWorkingCopy} must be call as many
			 * times as {@link #becomeWorkingCopy(IProblemRequestor, IProgressMonitor)}.
			 * </p>
			 *
			 * @param problemRequestor a requestor which will get notified of problems detected during
			 * 	reconciling as they are discovered. The requestor can be set to <code>null</code> indicating
			 * 	that the client is not interested in problems.
			 * @param monitor a progress monitor used to report progress while opening this compilation unit
			 * 	or <code>null</code> if no progress should be reported
			 * @throws JavaModelException if this compilation unit could not become a working copy.
			 * @see #discardWorkingCopy()
			 * @since 3.0
			 *
			 * @deprecated Use {@link #becomeWorkingCopy(IProgressMonitor)} instead.
			 * 	Note that if this deprecated method is used, problems will be reported to the given problem requestor
			 * 	as well as the problem requestor returned by the working copy owner (if not null).
			 */
			void becomeWorkingCopy(IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException;
			/**
			 * Changes this compilation unit handle into a working copy. A new {@link IBuffer} is
			 * created using this compilation unit handle's owner. Uses the primary owner if none was
			 * specified when this compilation unit handle was created.
			 * <p>
			 * When switching to working copy mode, problems are reported to the {@link IProblemRequestor
			 * problem requestor} of the {@link WorkingCopyOwner working copy owner}.
			 * </p><p>
			 * Once in working copy mode, changes to this compilation unit or its children are done in memory.
			 * Only the new buffer is affected. Using {@link #commitWorkingCopy(boolean, IProgressMonitor)}
			 * will bring the underlying resource in sync with this compilation unit.
			 * </p><p>
			 * If this compilation unit was already in working copy mode, an internal counter is incremented and no
			 * other action is taken on this compilation unit. To bring this compilation unit back into the original mode
			 * (where it reflects the underlying resource), {@link #discardWorkingCopy} must be call as many
			 * times as {@link #becomeWorkingCopy(IProblemRequestor, IProgressMonitor)}.
			 * </p>
			 *
			 * @param monitor a progress monitor used to report progress while opening this compilation unit
			 * 	or <code>null</code> if no progress should be reported
			 * @throws JavaModelException if this compilation unit could not become a working copy.
			 * @see #discardWorkingCopy()
			 * @since 3.3
			 */
			void becomeWorkingCopy(IProgressMonitor monitor) throws JavaModelException;
			/**
			 * Commits the contents of this working copy to its underlying resource.
			 *
			 * <p>It is possible that the contents of the original resource have changed
			 * since this working copy was created, in which case there is an update conflict.
			 * The value of the <code>force</code> parameter affects the resolution of
			 * such a conflict:<ul>
			 * <li> <code>true</code> - in this case the contents of this working copy are applied to
			 * 	the underlying resource even though this working copy was created before
			 *		a subsequent change in the resource</li>
			 * <li> <code>false</code> - in this case a {@link JavaModelException} is thrown</li>
			 * </ul>
			 * <p>
			 * Since 2.1, a working copy can be created on a not-yet existing compilation
			 * unit. In particular, such a working copy can then be committed in order to create
			 * the corresponding compilation unit.
			 * </p>
			 * @param force a flag to handle the cases when the contents of the original resource have changed
			 * since this working copy was created
			 * @param monitor the given progress monitor
			 * @throws JavaModelException if this working copy could not commit. Reasons include:
			 * <ul>
			 * <li> A {@link org.eclipse.core.runtime.CoreException} occurred while updating an underlying resource
			 * <li> This element is not a working copy (INVALID_ELEMENT_TYPES)
			 * <li> A update conflict (described above) (UPDATE_CONFLICT)
			 * </ul>
			 * @since 3.0
			 */
			void commitWorkingCopy(boolean force, IProgressMonitor monitor) throws JavaModelException;
			/**
			 * Creates and returns an non-static import declaration in this compilation unit
			 * with the given name. This method is equivalent to
			 * <code>createImport(name, Flags.AccDefault, sibling, monitor)</code>.
			 *
			 * @param name the name of the import declaration to add as defined by JLS2 7.5. (For example: <code>"java.io.File"</code> or
			 *  <code>"java.awt.*"</code>)
			 * @param sibling the existing element which the import declaration will be inserted immediately before (if
			 *	<code> null </code>, then this import will be inserted as the last import declaration.
			 * @param monitor the progress monitor to notify
			 * @return the newly inserted import declaration (or the previously existing one in case attempting to create a duplicate)
			 *
			 * @throws JavaModelException if the element could not be created. Reasons include:
			 * <ul>
			 * <li> This Java element does not exist or the specified sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
			 * <li> A {@link org.eclipse.core.runtime.CoreException} occurred while updating an underlying resource
			 * <li> The specified sibling is not a child of this compilation unit (INVALID_SIBLING)
			 * <li> The name is not a valid import name (INVALID_NAME)
			 * </ul>
			 * @see #createImport(String, IJavaElement, int, IProgressMonitor)
			 */
			IImportDeclaration createImport(String name, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException;

			/**
			 * Creates and returns an import declaration in this compilation unit
			 * with the given name.
			 * <p>
			 * Optionally, the new element can be positioned before the specified
			 * sibling. If no sibling is specified, the element will be inserted
			 * as the last import declaration in this compilation unit.
			 * <p>
			 * If the compilation unit already includes the specified import declaration,
			 * the import is not generated (it does not generate duplicates).
			 * Note that it is valid to specify both a single-type import and an on-demand import
			 * for the same package, for example <code>"java.io.File"</code> and
			 * <code>"java.io.*"</code>, in which case both are preserved since the semantics
			 * of this are not the same as just importing <code>"java.io.*"</code>.
			 * Importing <code>"java.lang.*"</code>, or the package in which the compilation unit
			 * is defined, are not treated as special cases.  If they are specified, they are
			 * included in the result.
			 * <p>
			 * Note: This API element is only needed for dealing with Java code that uses
			 * new language features of J2SE 5.0.
			 * </p>
			 *
			 * @param name the name of the import declaration to add as defined by JLS2 7.5. (For example: <code>"java.io.File"</code> or
			 *  <code>"java.awt.*"</code>)
			 * @param sibling the existing element which the import declaration will be inserted immediately before (if
			 *	<code> null </code>, then this import will be inserted as the last import declaration.
			 * @param flags {@link Flags#AccStatic} for static imports, or
			 * {@link Flags#AccDefault} for regular imports; other modifier flags
			 * are ignored
			 * @param monitor the progress monitor to notify
			 * @return the newly inserted import declaration (or the previously existing one in case attempting to create a duplicate)
			 *
			 * @throws JavaModelException if the element could not be created. Reasons include:
			 * <ul>
			 * <li> This Java element does not exist or the specified sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
			 * <li> A {@link org.eclipse.core.runtime.CoreException} occurred while updating an underlying resource
			 * <li> The specified sibling is not a child of this compilation unit (INVALID_SIBLING)
			 * <li> The name is not a valid import name (INVALID_NAME)
			 * </ul>
			 * @see Flags
			 * @since 3.0
			 */
			IImportDeclaration createImport(String name, IJavaElement sibling, int flags, IProgressMonitor monitor) throws JavaModelException;

			/**
			 * Creates and returns a package declaration in this compilation unit
			 * with the given package name.
			 *
			 * <p>If the compilation unit already includes the specified package declaration,
			 * it is not generated (it does not generate duplicates).
			 *
			 * @param name the name of the package declaration to add as defined by JLS2 7.4. (For example, <code>"java.lang"</code>)
			 * @param monitor the progress monitor to notify
			 * @return the newly inserted package declaration (or the previously existing one in case attempting to create a duplicate)
			 *
			 * @throws JavaModelException if the element could not be created. Reasons include:
			 * <ul>
			 * <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
			 * <li> A {@link org.eclipse.core.runtime.CoreException} occurred while updating an underlying resource
			 * <li> The name is not a valid package name (INVALID_NAME)
			 * </ul>
			 */
			 IPackageDeclaration createPackageDeclaration(String name, IProgressMonitor monitor) throws JavaModelException;
			/**
			 * Creates and returns a type in this compilation unit with the
			 * given contents. If this compilation unit does not exist, one
			 * will be created with an appropriate package declaration.
			 * <p>
			 * Optionally, the new type can be positioned before the specified
			 * sibling. If <code>sibling</code> is <code>null</code>, the type will be appended
			 * to the end of this compilation unit.
			 *
			 * <p>It is possible that a type with the same name already exists in this compilation unit.
			 * The value of the <code>force</code> parameter affects the resolution of
			 * such a conflict:<ul>
			 * <li> <code>true</code> - in this case the type is created with the new contents</li>
			 * <li> <code>false</code> - in this case a {@link JavaModelException} is thrown</li>
			 * </ul>
			 *
			 * @param contents the source contents of the type declaration to add.
			 * @param sibling the existing element which the type will be inserted immediately before (if
			 *	<code>null</code>, then this type will be inserted as the last type declaration.
			 * @param force a <code>boolean</code> flag indicating how to deal with duplicates
			 * @param monitor the progress monitor to notify
			 * @return the newly inserted type
			 *
			 * @throws JavaModelException if the element could not be created. Reasons include:
			 * <ul>
			 * <li>The specified sibling element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
			 * <li> A {@link org.eclipse.core.runtime.CoreException} occurred while updating an underlying resource
			 * <li> The specified sibling is not a child of this compilation unit (INVALID_SIBLING)
			 * <li> The contents could not be recognized as a type declaration (INVALID_CONTENTS)
			 * <li> There was a naming collision with an existing type (NAME_COLLISION)
			 * </ul>
			 */
			IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException;
			/**
			 * Changes this compilation unit in working copy mode back to its original mode.
			 * <p>
			 * This has no effect if this compilation unit was not in working copy mode.
			 * </p>
			 * <p>
			 * If {@link #becomeWorkingCopy(IProgressMonitor)} method was called several
			 * times on this compilation unit, {@link #discardWorkingCopy()} must be called
			 * as many times before it switches back to the original mode. Same as
			 * for method {@link #getWorkingCopy(IProgressMonitor)}.
			 * </p>
			 *
			 * @throws JavaModelException if this working copy could not return in its original mode.
			 * @see #becomeWorkingCopy(IProblemRequestor, IProgressMonitor)
			 * @since 3.0
			 */
			void discardWorkingCopy() throws JavaModelException;
			/**
			 * Finds the elements in this compilation unit that correspond to
			 * the given element.
			 * An element A corresponds to an element B if:
			 * <ul>
			 * <li>A has the same element name as B.
			 * <li>If A is a method, A must have the same number of arguments as
			 *     B and the simple names of the argument types must be equals.
			 * <li>The parent of A corresponds to the parent of B recursively up to
			 *     their respective compilation units.
			 * <li>A exists.
			 * </ul>
			 * Returns <code>null</code> for the following cases:
			 * <ul>
			 * <li>if no such java elements can be found or if the given element is not included in this compilation unit</li>
			 * <li>the element is a lambda expression, i.e. calling {@link IType#isLambda()} returns true</li>
			 * <li>the element is an {@link ILocalVariable}</li>
			 * </ul>
			 *
			 * @param element the given element
			 * @return the found elements in this compilation unit that correspond to the given element
			 * @since 3.0
			 */
			@Override
			IJavaElement[] findElements(IJavaElement element);
			/**
			 * Finds the working copy for this compilation unit, given a {@link WorkingCopyOwner}.
			 * If no working copy has been created for this compilation unit associated with this
			 * working copy owner, returns <code>null</code>.
			 * <p>
			 * Users of this method must not destroy the resulting working copy.
			 *
			 * @param owner the given {@link WorkingCopyOwner}
			 * @return the found working copy for this compilation unit, <code>null</code> if none
			 * @see WorkingCopyOwner
			 * @since 3.0
			 */
			ICompilationUnit findWorkingCopy(WorkingCopyOwner owner);
			/**
			 * Returns all types declared in this compilation unit in the order
			 * in which they appear in the source.
			 * This includes all top-level types and nested member types.
			 * It does NOT include local types (types defined in methods).
			 *
			 * @return the array of top-level and member types defined in a compilation unit, in declaration order.
			 * @throws JavaModelException if this element does not exist or if an
			 *		exception occurs while accessing its corresponding resource
			 */
			IType[] getAllTypes() throws JavaModelException;
			/**
			 * Returns the first import declaration in this compilation unit with the given name.
			 * This is a handle-only method. The import declaration may or may not exist. This
			 * is a convenience method - imports can also be accessed from a compilation unit's
			 * import container.
			 *
			 * @param name the name of the import to find as defined by JLS2 7.5. (For example: <code>"java.io.File"</code>
			 * 	or <code>"java.awt.*"</code>)
			 * @return a handle onto the corresponding import declaration. The import declaration may or may not exist.
			 */
			IImportDeclaration getImport(String name) ;
			/**
			 * Returns the import container for this compilation unit.
			 * This is a handle-only method. The import container may or
			 * may not exist. The import container can used to access the
			 * imports.
			 * @return a handle onto the corresponding import container. The
			 *		import contain may or may not exist.
			 */
			IImportContainer getImportContainer();
			/**
			 * Returns the import declarations in this compilation unit
			 * in the order in which they appear in the source. This is
			 * a convenience method - import declarations can also be
			 * accessed from a compilation unit's import container.
			 *
			 * @return the import declarations in this compilation unit
			 * @throws JavaModelException if this element does not exist or if an
			 *		exception occurs while accessing its corresponding resource
			 */
			IImportDeclaration[] getImports() throws JavaModelException;
			/**
			 * Returns the primary compilation unit (whose owner is the primary owner)
			 * this working copy was created from, or this compilation unit if this a primary
			 * compilation unit.
			 * <p>
			 * Note that the returned primary compilation unit can be in working copy mode.
			 * </p>
			 *
			 * @return the primary compilation unit this working copy was created from,
			 * or this compilation unit if it is primary
			 * @since 3.0
			 */
			ICompilationUnit getPrimary();
			/**
			 * Returns <code>null</code> if this <code>ICompilationUnit</code> is the primary
			 * working copy, or this <code>ICompilationUnit</code> is not a working copy,
			 * otherwise the <code>WorkingCopyOwner</code>
			 *
			 * @return <code>null</code> if this <code>ICompilationUnit</code> is the primary
			 * working copy, or this <code>ICompilationUnit</code> is not a working copy,
			 * otherwise the <code>WorkingCopyOwner</code>
			 * @since 3.0
			 */
			WorkingCopyOwner getOwner();
			/**
			 * Returns the first package declaration in this compilation unit with the given package name
			 * (there normally is at most one package declaration).
			 * This is a handle-only method. The package declaration may or may not exist.
			 *
			 * @param name the name of the package declaration as defined by JLS2 7.4. (For example, <code>"java.lang"</code>)
			 * @return the first package declaration in this compilation unit with the given package name
			 */
			IPackageDeclaration getPackageDeclaration(String name);
			/**
			 * Returns the package declarations in this compilation unit
			 * in the order in which they appear in the source.
			 * There normally is at most one package declaration.
			 *
			 * @return an array of package declaration (normally of size one)
			 *
			 * @throws JavaModelException if this element does not exist or if an
			 *		exception occurs while accessing its corresponding resource
			 */
			IPackageDeclaration[] getPackageDeclarations() throws JavaModelException;
			/**
			 * Returns the top-level type declared in this compilation unit with the given simple type name.
			 * The type name has to be a valid compilation unit name.
			 * This is a handle-only method. The type may or may not exist.
			 *
			 * @param name the simple name of the requested type in the compilation unit
			 * @return a handle onto the corresponding type. The type may or may not exist.
			 * @see JavaConventions#validateCompilationUnitName(String name, String sourceLevel, String complianceLevel)
			 */
			IType getType(String name);
			/**
			 * Returns the top-level types declared in this compilation unit
			 * in the order in which they appear in the source.
			 *
			 * @return the top-level types declared in this compilation unit
			 * @throws JavaModelException if this element does not exist or if an
			 *		exception occurs while accessing its corresponding resource
			 */
			IType[] getTypes() throws JavaModelException;
			/**
			 * Returns a new working copy of this compilation unit if it is a primary compilation unit,
			 * or this compilation unit if it is already a non-primary working copy.
			 * <p>
			 * Note: if intending to share a working copy amongst several clients, then
			 * {@link #getWorkingCopy(WorkingCopyOwner, IProblemRequestor, IProgressMonitor)}
			 * should be used instead.
			 * </p><p>
			 * When the working copy instance is created, an ADDED IJavaElementDelta is
			 * reported on this working copy.
			 * </p><p>
			 * Once done with the working copy, users of this method must discard it using
			 * {@link #discardWorkingCopy()}.
			 * </p><p>
			 * Since 2.1, a working copy can be created on a not-yet existing compilation
			 * unit. In particular, such a working copy can then be committed in order to create
			 * the corresponding compilation unit.
			 * </p>
			 * @param monitor a progress monitor used to report progress while opening this compilation unit
			 *                 or <code>null</code> if no progress should be reported
			 * @throws JavaModelException if the contents of this element can
			 *   not be determined.
			 * @return a new working copy of this element if this element is not
			 * a working copy, or this element if this element is already a working copy
			 * @since 3.0
			 */
			ICompilationUnit getWorkingCopy(IProgressMonitor monitor) throws JavaModelException;
			/**
			 * Returns a shared working copy on this compilation unit using the given working copy owner to create
			 * the buffer, or this compilation unit if it is already a non-primary working copy.
			 * This API can only answer an already existing working copy if it is based on the same
			 * original compilation unit AND was using the same working copy owner (that is, as defined by {@link Object#equals}).
			 * <p>
			 * The life time of a shared working copy is as follows:
			 * <ul>
			 * <li>The first call to {@link #getWorkingCopy(WorkingCopyOwner, IProblemRequestor, IProgressMonitor)}
			 * 	creates a new working copy for this element</li>
			 * <li>Subsequent calls increment an internal counter.</li>
			 * <li>A call to {@link #discardWorkingCopy()} decrements the internal counter.</li>
			 * <li>When this counter is 0, the working copy is discarded.
			 * </ul>
			 * So users of this method must discard exactly once the working copy.
			 * <p>
			 * Note that the working copy owner will be used for the life time of this working copy, that is if the
			 * working copy is closed then reopened, this owner will be used.
			 * The buffer will be automatically initialized with the original's compilation unit content
			 * upon creation.
			 * <p>
			 * When the shared working copy instance is created, an ADDED IJavaElementDelta is reported on this
			 * working copy.
			 * </p><p>
			 * Since 2.1, a working copy can be created on a not-yet existing compilation
			 * unit. In particular, such a working copy can then be committed in order to create
			 * the corresponding compilation unit.
			 * </p>
			 * @param owner the working copy owner that creates a buffer that is used to get the content
			 * 				of the working copy
			 * @param problemRequestor a requestor which will get notified of problems detected during
			 * 	reconciling as they are discovered. The requestor can be set to <code>null</code> indicating
			 * 	that the client is not interested in problems.
			 * @param monitor a progress monitor used to report progress while opening this compilation unit
			 *                 or <code>null</code> if no progress should be reported
			 * @throws JavaModelException if the contents of this element can
			 *   not be determined.
			 * @return a new working copy of this element using the given factory to create
			 * the buffer, or this element if this element is already a working copy
			 * @since 3.0
			 * @deprecated Use {@link ITypeRoot#getWorkingCopy(WorkingCopyOwner, IProgressMonitor)} instead.
			 * 	Note that if this deprecated method is used, problems will be reported on the passed problem requester
			 * 	as well as on the problem requestor returned by the working copy owner (if not null).
			 */
			ICompilationUnit getWorkingCopy(WorkingCopyOwner owner, IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException;
			/**
			 * Returns whether the resource of this working copy has changed since the
			 * inception of this working copy.
			 * Returns <code>false</code> if this compilation unit is not in working copy mode.
			 *
			 * @return whether the resource has changed
			 * @since 3.0
			 */
			public boolean hasResourceChanged();
			/**
			 * Returns whether this element is a working copy.
			 *
			 * @return true if this element is a working copy, false otherwise
			 * @since 3.0
			 */
			@Override
			boolean isWorkingCopy();

			/**
			 * Sets the ICompilationUnit custom options. All and only the options explicitly included in the given table
			 * are remembered; all previous option settings are forgotten, including ones not explicitly
			 * mentioned.
			 * <p>
			 * For a complete description of the configurable options, see <code>JavaCore#getDefaultOptions</code>.
			 * </p>
			 *
			 * @param newOptions the new custom options for this compilation unit
			 * @see JavaCore#setOptions(java.util.Hashtable)
			 * @since 3.25
			 */
			default void setOptions(Map<String, String> newOptions) {
				// Do nothing by default
			}

			/**
			 * Returns the table of the current custom options for this ICompilationUnit. If there is no <code>setOptions</code> called
			 * for the ICompliationUnit, then return an empty table.
			 *
			 * @return the table of the current custom options for this ICompilationUnit
			 * @since 3.25
			 */
			default Map<String, String> getCustomOptions() {
				return Collections.emptyMap();
			}

			/**
			 * Returns the table of the options for this ICompilationUnit, which includes its custom options and options
			 * inherited from its parent JavaProject. The boolean argument <code>inheritJavaCoreOptions</code> allows
			 * to directly merge the global ones from <code>JavaCore</code>.
			 * <p>
			 * For a complete description of the configurable options, see <code>JavaCore#getDefaultOptions</code>.
			 * </p>
			 *
			 * @param inheritJavaCoreOptions - boolean indicating whether the JavaCore options should be inherited as well
			 * @return table of current settings of all options
			 * @see JavaCore#getDefaultOptions()
			 * @since 3.25
			 */
			default Map<String, String> getOptions(boolean inheritJavaCoreOptions) {
				IJavaProject parentProject = getJavaProject();
				Map<String, String> options = parentProject == null ? JavaCore.getOptions() : parentProject.getOptions(inheritJavaCoreOptions);
				Map<String, String> customOptions = getCustomOptions();
				if (customOptions != null) {
					options.putAll(customOptions);
				}

				return options;
			}

			/**
			 * Reconciles the contents of this working copy, sends out a Java delta
			 * notification indicating the nature of the change of the working copy since
			 * the last time it was either reconciled or made consistent
			 * ({@link IOpenable#makeConsistent(IProgressMonitor)}), and returns a
			 * compilation unit AST if requested.
			 * <p>
			 * It performs the reconciliation by locally caching the contents of
			 * the working copy, updating the contents, then creating a delta
			 * over the cached contents and the new contents, and finally firing
			 * this delta.
			 * <p>
			 * The boolean argument allows to force problem detection even if the
			 * working copy is already consistent.
			 * </p>
			 * <p>
			 * This functionality allows to specify a working copy owner which is used
			 * during problem detection. All references contained in the working copy are
			 * resolved against other units; for which corresponding owned working copies
			 * are going to take precedence over their original compilation units. If
			 * <code>null</code> is passed in, then the primary working copy owner is used.
			 * </p>
			 * <p>
			 * Compilation problems found in the new contents are notified through the
			 * {@link IProblemRequestor} interface which was passed at
			 * creation, and no longer as transient markers.
			 * </p>
			 * <p>
			 * Note: Since 3.0, added/removed/changed inner types generate change deltas.
			 * </p>
			 * <p>
			 * If requested, a DOM AST representing the compilation unit is returned.
			 * Its bindings are computed only if the problem requestor is active.
			 * This method returns <code>null</code> if one of the following conditions is true:
			 * <ul>
			 *   <li>the creation of the DOM AST is not requested</li>
			 *   <li>the requested level of AST API is not supported</li>
			 *   <li>the working copy was already consistent and problem detection is not forced</li>
			 * </ul>
			 * <p>
			 * This method doesn't perform statements recovery. To recover statements with syntax
			 * errors, {@link #reconcile(int, boolean, boolean, WorkingCopyOwner, IProgressMonitor)} must be use.
			 * </p>
			 *
			 * @param astLevel either {@link #NO_AST} if no AST is wanted,
			 * or the {@linkplain AST#newAST(int, boolean) AST API level} of the AST if one is wanted
			 * @param forceProblemDetection boolean indicating whether problem should be
			 *   recomputed even if the source hasn't changed
			 * @param owner the owner of working copies that take precedence over the
			 *   original compilation units, or <code>null</code> if the primary working
			 *   copy owner should be used
			 * @param monitor a progress monitor
			 * @return the compilation unit AST or <code>null</code> if one of the following conditions is true:
			 * <ul>
			 *   <li>the creation of the DOM AST is not requested</li>
			 *   <li>the requested level of AST API is not supported</li>
			 *   <li>the working copy was already consistent and problem detection is not forced</li>
			 * </ul>
			 * @throws JavaModelException if the contents of the original element
			 *		cannot be accessed. Reasons include:
			 * <ul>
			 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
			 * </ul>
			 * @since 3.0
			 */
			CompilationUnit reconcile(int astLevel, boolean forceProblemDetection, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException;

			/**
			 * Reconciles the contents of this working copy, sends out a Java delta
			 * notification indicating the nature of the change of the working copy since
			 * the last time it was either reconciled or made consistent
			 * ({@link IOpenable#makeConsistent(IProgressMonitor)}), and returns a
			 * compilation unit AST if requested.
			 * <p>
			 * It performs the reconciliation by locally caching the contents of
			 * the working copy, updating the contents, then creating a delta
			 * over the cached contents and the new contents, and finally firing
			 * this delta.
			 * <p>
			 * The boolean argument allows to force problem detection even if the
			 * working copy is already consistent.
			 * </p>
			 * <p>
			 * This functionality allows to specify a working copy owner which is used
			 * during problem detection. All references contained in the working copy are
			 * resolved against other units; for which corresponding owned working copies
			 * are going to take precedence over their original compilation units. If
			 * <code>null</code> is passed in, then the primary working copy owner is used.
			 * </p>
			 * <p>
			 * Compilation problems found in the new contents are notified through the
			 * {@link IProblemRequestor} interface which was passed at
			 * creation, and no longer as transient markers.
			 * </p>
			 * <p>
			 * Note: Since 3.0, added/removed/changed inner types generate change deltas.
			 * </p>
			 * <p>
			 * If requested, a DOM AST representing the compilation unit is returned.
			 * Its bindings are computed only if the problem requestor is active.
			 * This method returns <code>null</code> if one of the following conditions is true:
			 * <ul>
			 *   <li>the creation of the DOM AST is not requested</li>
			 *   <li>the requested level of AST API is not supported</li>
			 *   <li>the working copy was already consistent and problem detection is not forced</li>
			 * </ul>
			 *
			 * <p>
			 * If statements recovery is enabled then this method tries to rebuild statements
			 * with syntax error. Otherwise statements with syntax error won't be present in
			 * the returning DOM AST.
			 * </p>
			 *
			 * @param astLevel either {@link #NO_AST} if no AST is wanted,
			 * or the {@linkplain AST#newAST(int, boolean) AST API level} of the AST if one is wanted
			 * @param forceProblemDetection boolean indicating whether problem should be
			 *   recomputed even if the source hasn't changed
			 * @param enableStatementsRecovery if <code>true</code> statements recovery is enabled.
			 * @param owner the owner of working copies that take precedence over the
			 *   original compilation units, or <code>null</code> if the primary working
			 *   copy owner should be used
			 * @param monitor a progress monitor
			 * @return the compilation unit AST or <code>null</code> if one of the following conditions is true:
			 * <ul>
			 *   <li>the creation of the DOM AST is not requested</li>
			 *   <li>the requested level of AST API is not supported</li>
			 *   <li>the working copy was already consistent and problem detection is not forced</li>
			 * </ul>
			 * @throws JavaModelException if the contents of the original element
			 *		cannot be accessed. Reasons include:
			 * <ul>
			 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
			 * </ul>
			 * @since 3.2
			 */
			CompilationUnit reconcile(int astLevel, boolean forceProblemDetection, boolean enableStatementsRecovery, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException;

			/**
			 * Reconciles the contents of this working copy, sends out a Java delta
			 * notification indicating the nature of the change of the working copy since
			 * the last time it was either reconciled or made consistent
			 * ({@link IOpenable#makeConsistent(IProgressMonitor)}), and returns a
			 * compilation unit AST if requested.
			 *
			 * <p>
			 * If the problem detection is forced by passing the {@link #FORCE_PROBLEM_DETECTION} bit in the given reconcile flag,
			 * problem detection is run even if the working copy is already consistent.
			 * </p>
			 *
			 * <p>
			 * It performs the reconciliation by locally caching the contents of
			 * the working copy, updating the contents, then creating a delta
			 * over the cached contents and the new contents, and finally firing
			 * this delta.</p>
			 *
			 * <p>
			 * This functionality allows to specify a working copy owner which is used
			 * during problem detection. All references contained in the working copy are
			 * resolved against other units; for which corresponding owned working copies
			 * are going to take precedence over their original compilation units. If
			 * <code>null</code> is passed in, then the primary working copy owner is used.
			 * </p>
			 * <p>
			 * Compilation problems found in the new contents are notified through the
			 * {@link IProblemRequestor} interface which was passed at
			 * creation, and no longer as transient markers.
			 * </p>
			 * <p>
			 * Note: Since 3.0, added/removed/changed inner types generate change deltas.
			 * </p>
			 * <p>
			 * If requested, a DOM AST representing the compilation unit is returned.
			 * Its bindings are computed only if the problem requestor is active.
			 * This method returns <code>null</code> if one of the following conditions is true:
			 * <ul>
			 *   <li>the creation of the DOM AST is not requested</li>
			 *   <li>the requested level of AST API is not supported</li>
			 *   <li>the working copy was already consistent and problem detection is not forced</li>
			 * </ul>
			 *
			 * <p>
			 * If statements recovery is enabled by passing the {@link #ENABLE_STATEMENTS_RECOVERY} bit in the given reconcile flag
			 * then this method tries to rebuild statements with syntax error. Otherwise statements with syntax error won't be
			 * present in the returning DOM AST.</p>
			 * <p>
			 * If bindings recovery is enabled by passing the {@link #ENABLE_BINDINGS_RECOVERY} bit in the given reconcile flag
			 * then this method tries to resolve bindings even if the type resolution contains errors.</p>
			 * <p>
			 * The given reconcile flags is a bit-mask of the different constants ({@link #ENABLE_BINDINGS_RECOVERY},
			 * {@link #ENABLE_STATEMENTS_RECOVERY}, {@link #FORCE_PROBLEM_DETECTION}). Unspecified values are left for future use.
			 * </p>
			 *
			 * @param astLevel either {@link #NO_AST} if no AST is wanted,
			 * or the {@linkplain AST#newAST(int, boolean) AST API level} of the AST if one is wanted
			 * @param reconcileFlags the given reconcile flags
			 * @param owner the owner of working copies that take precedence over the
			 *   original compilation units, or <code>null</code> if the primary working
			 *   copy owner should be used
			 * @param monitor a progress monitor
			 * @return the compilation unit AST or <code>null</code> if one of the following conditions is true:
			 * <ul>
			 *   <li>the creation of the DOM AST is not requested</li>
			 *   <li>the requested level of AST API is not supported</li>
			 *   <li>the working copy was already consistent and problem detection is not forced</li>
			 * </ul>
			 * @throws JavaModelException if the contents of the original element
			 *		cannot be accessed. Reasons include:
			 * <ul>
			 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
			 * </ul>
			 * @see #FORCE_PROBLEM_DETECTION
			 * @see #ENABLE_BINDINGS_RECOVERY
			 * @see #ENABLE_STATEMENTS_RECOVERY
			 * @since 3.3
			 */
			CompilationUnit reconcile(int astLevel, int reconcileFlags, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException;

			/**
			 * Restores the contents of this working copy to the current contents of
			 * this working copy's original element. Has no effect if this element
			 * is not a working copy.
			 *
			 * <p>Note: This is the inverse of committing the content of the
			 * working copy to the original element with {@link #commitWorkingCopy(boolean, IProgressMonitor)}.
			 *
			 * @throws JavaModelException if the contents of the original element
			 *		cannot be accessed.  Reasons include:
			 * <ul>
			 * <li> The original Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
			 * </ul>
			 * @since 3.0
			 */
			@Override
			void restore() throws JavaModelException;
			}

			""".toCharArray(), new char[][] {}, "ICompilationUnit.java", StandardCharsets.UTF_8.toString());
		CompilationUnit compilationUnit = resolver.toCompilationUnit(unit, false, null, null, 0, AST.getJLSLatest(), Map.of(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED), null, null, 0, null);
		var javadoc = ((AbstractTypeDeclaration)compilationUnit.types().get(0)).getJavadoc();
		Set<ASTNode> errors = new HashSet<>();
		javadoc.accept(new ASTVisitor(true) {
			int previousEnd = 0;
			@Override
			public void preVisit(ASTNode node) {
				super.preVisit(node);
				if (node.getStartPosition() < this.previousEnd) {
					errors.add(node);
				}
				if (node.getStartPosition() < node.getParent().getStartPosition()) {
					errors.add(node);
				}
			}
			@Override
			public void postVisit(ASTNode node) {
				super.postVisit(node);
				this.previousEnd = node.getStartPosition() + node.getLength();
			}
		});
		assertEquals(Set.of(), errors);
	}
}
