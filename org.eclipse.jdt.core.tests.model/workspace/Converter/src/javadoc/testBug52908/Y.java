package javadoc.testBug52908;
public class Y {
	/**
	 *   while (true)
	 *  {
	 *     int token = scanner.getNextToken();
	 *     if (token == ITerminalSymbols.TokenNameEOF) break;
	 *     System.out.println(token + " : " + new String(scanner.getCurrentTokenSource()));
	 *   }
	 * <pre>
	 * </pre>
	 */
	void foo() {}
}
