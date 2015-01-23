package test.comments.block;

public class X14 {
	String name;

	void foo(Z17 outerScope) {
		if (true) {
			if (true) {
				if (true) {
					Object existingVariable = outerScope.getBinding(this.name,
							Y17.VARIABLE_15CHAR, this, false /*
															 * do not resolve
															 * hidden field
															 */);
				}
			}
		}
	}
}

interface Y17 {

	String VARIABLE_15CHAR = null;
}

class Z17 {

	public Object getBinding(String name, String variable, X14 x17, boolean b) {
		return null;
	}
}
