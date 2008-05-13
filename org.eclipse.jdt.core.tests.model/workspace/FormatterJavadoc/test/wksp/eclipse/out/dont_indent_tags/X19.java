package test.wksp.eclipse;

public class X19 {

	/**
	 * Returns a list of the comments encountered while parsing this compilation
	 * unit.
	 * <p>
	 * Since the Java language allows comments to appear most anywhere in the
	 * source text, it is problematic to locate comments in relation to the
	 * structure of an AST. The one exception is doc comments which, by
	 * convention, immediately precede type, field, and method declarations;
	 * these comments are located in the AST by
	 * {@link BodyDeclaration#getJavadoc BodyDeclaration.getJavadoc}. Other
	 * comments do not show up in the AST. The table of comments is provided for
	 * clients that need to find the source ranges of all comments in the
	 * original source string. It includes entries for comments of all kinds
	 * (line, block, and doc), arranged in order of increasing source position.
	 * </p>
	 * Note on comment parenting: The {@link ASTNode#getParent() getParent()} of
	 * a doc comment associated with a body declaration is the body declaration
	 * node; for these comment nodes {@link ASTNode#getRoot() getRoot()} will
	 * return the compilation unit (assuming an unmodified AST) reflecting the
	 * fact that these nodes are property located in the AST for the compilation
	 * unit. However, for other comment nodes, {@link ASTNode#getParent()
	 * getParent()} will return <code>null</code>, and {@link ASTNode#getRoot()
	 * getRoot()} will return the comment node itself, indicating that these
	 * comment nodes are not directly connected to the AST for the compilation
	 * unit. The {@link Comment#getAlternateRoot Comment.getAlternateRoot}
	 * method provides a way to navigate from a comment to its compilation unit.
	 * </p>
	 * <p>
	 * A note on visitors: The only comment nodes that will be visited when
	 * visiting a compilation unit are the doc comments parented by body
	 * declarations. To visit all comments in normal reading order, iterate over
	 * the comment table and call {@link ASTNode#accept(ASTVisitor) accept} on
	 * each element.
	 * </p>
	 * <p>
	 * Clients cannot modify the resulting list.
	 * </p>
	 * 
	 * @return an unmodifiable list of comments in increasing order of source
	 * start position, or <code>null</code> if comment information for this
	 * compilation unit is not available
	 * @see ASTParser
	 * @since 3.0
	 */
	void foo() {
	}
}
