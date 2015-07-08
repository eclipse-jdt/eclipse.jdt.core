package test.wksp.eclipse;

public class X24 {

	/**
	 * Creates a new unparented expression statement node owned by this AST, for
	 * the given expression.
	 * <p>
	 * This method can be used to convert an expression
	 * (<code>Expression</code>) into a statement (<code>Type</code>) by
	 * wrapping it. Note, however, that the result is only legal for limited
	 * expression types, including method invocations, assignments, and
	 * increment/decrement operations.
	 * </p>
	 * 
	 * @param expression
	 *            the expression
	 * @return a new unparented statement node
	 * @exception IllegalArgumentException
	 *                if:
	 *                <ul>
	 *                <li>the node belongs to a different AST</li>
	 *                <li>the node already has a parent</li>
	 *                <li>a cycle in would be created</li>
	 *                </ul>
	 */
	void foo() {
	}
}
