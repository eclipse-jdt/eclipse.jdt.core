
package javadoc.testBug68025;
public class TestA {

	/**
	 * @see IJavaElement#getElementName#bug
	 * or closer to the reality (COPY is a constant):
	 * @see org.eclipse.ui.actions.ActionFactory#COPY#getId
	 */
    void foo() {
    }
}
