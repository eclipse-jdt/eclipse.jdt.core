package test.wksp.eclipse;

public class X18 {

	/**
	 * Sets the kind of constructs to be parsed from the source. Defaults to an
	 * entire compilation unit.
	 * <p>
	 * When the parse is successful the result returned includes the ASTs for
	 * the requested source:
	 * <ul>
	 * <li>{@link #K_COMPILATION_UNIT}: The result node is a
	 * {@link CompilationUnit}.</li>
	 * <li>{@link #K_CLASS_BODY_DECLARATIONS}: The result node is a
	 * {@link TypeDeclaration} whose {@link TypeDeclaration#bodyDeclarations()
	 * bodyDeclarations} are the new trees. Other aspects of the type
	 * declaration are unspecified.</li>
	 * <li>{@link #K_STATEMENTS}: The result node is a {@link Block Block} whose
	 * {@link Block#statements() statements} are the new trees. Other aspects of
	 * the block are unspecified.</li>
	 * <li>{@link #K_EXPRESSION}: The result node is a subclass of
	 * {@link Expression Expression}. Other aspects of the expression are
	 * unspecified.</li>
	 * </ul>
	 * The resulting AST node is rooted under (possibly contrived)
	 * {@link CompilationUnit CompilationUnit} node, to allow the client to
	 * retrieve the following pieces of information available there:
	 * <ul>
	 * <li>{@linkplain CompilationUnit#lineNumber(int) Line number map}. Line
	 * numbers start at 1 and only cover the subrange scanned
	 * (<code>source[offset]</code> through
	 * <code>source[offset+length-1]</code>).</li>
	 * <li>{@linkplain CompilationUnit#getMessages() Compiler messages} and
	 * {@linkplain CompilationUnit#getProblems() detailed problem reports}.
	 * Character positions are relative to the start of <code>source</code>;
	 * line positions are for the subrange scanned.</li>
	 * <li>{@linkplain CompilationUnit#getCommentList() Comment list} for the
	 * subrange scanned.</li>
	 * </ul>
	 * The contrived nodes do not have source positions. Other aspects of the
	 * {@link CompilationUnit CompilationUnit} node are unspecified, including
	 * the exact arrangment of intervening nodes.
	 * </p>
	 * <p>
	 * Lexical or syntax errors detected while parsing can result in a result
	 * node being marked as {@link ASTNode#MALFORMED MALFORMED}. In more severe
	 * failure cases where the parser is unable to recognize the input, this
	 * method returns a {@link CompilationUnit CompilationUnit} node with at
	 * least the compiler messages.
	 * </p>
	 * <p>
	 * Each node in the subtree (other than the contrived nodes) carries source
	 * range(s) information relating back to positions in the given source (the
	 * given source itself is not remembered with the AST). The source range
	 * usually begins at the first character of the first token corresponding to
	 * the node; leading whitespace and comments are <b>not</b> included. The
	 * source range usually extends through the last character of the last token
	 * corresponding to the node; trailing whitespace and comments are
	 * <b>not</b> included. There are a handful of exceptions (including the
	 * various body declarations); the specification for these node type spells
	 * out the details. Source ranges nest properly: the source range for a
	 * child is always within the source range of its parent, and the source
	 * ranges of sibling nodes never overlap.
	 * </p>
	 * <p>
	 * Binding information is only computed when <code>kind</code> is
	 * <code>K_COMPILATION_UNIT</code>.
	 * </p>
	 * 
	 * @param kind
	 *        the kind of construct to parse: one of
	 *        {@link #K_COMPILATION_UNIT}, {@link #K_CLASS_BODY_DECLARATIONS},
	 *        {@link #K_EXPRESSION}, {@link #K_STATEMENTS}
	 */
	void setKind(int kind) {
	}
}
