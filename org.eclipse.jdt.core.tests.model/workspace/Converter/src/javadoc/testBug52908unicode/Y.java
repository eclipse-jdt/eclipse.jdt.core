package javadoc.testBug52908unicode;
public class Y {
	/**
	 *   while (true)
	 *   \u007b
	 *     int token = scanner.getNextToken();
	 *     if (token == ITerminalSymbols.TokenNameEOF) break;
	 *     System.out.println(token + " : " + new String(scanner.getCurrentTokenSource()));
	 *   \u007d
	 * <pre>
	 * </pre>
	 */
	void foo() {}
}
