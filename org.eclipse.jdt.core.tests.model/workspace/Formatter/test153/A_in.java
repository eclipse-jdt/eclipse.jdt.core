public class Parser {
	protected int				astLengthPtr;
	protected int[]				astLengthStack;
	public CompilationUnitDeclaration	compilationUnit; 	/*the result from parse()*/
	AstNode[]							noAstNodes	= new AstNode[AstStackIncrement];
}