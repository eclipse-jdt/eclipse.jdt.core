package de.neze.bugreport.eclipse;
//Code generator imported: import de.neze.bugreport.eclipse.GeneralConcept.Item;
//Code generator imported: import de.neze.bugreport.eclipse.GeneralConcept.Problem;
/**
 * Feature Missing in Eclispe M5:
 * 
 * I like to write code like as noted below, and it seams that eclispe 
 * handling of ambigouse inner class names is suboptimal during codeassistance.
 * And the code formater has problems with disambigouated names.
 * Note: The exampe below is optimzed for shortness and that make it look a 
 * little bit stupid, but "real world" exmples are much longer .. .
 */
public abstract class AssistMyStyle {
	public static final class Problem extends Exception {
	}
	public static final class Item {
	}
	public static interface Factory {
		AssistMyStyle newAssistMyStyle() throws Problem;
	}
	abstract Item someMethod() throws Problem;
}
/**
 * and
 */
interface GeneralConcept {
	public static final class Problem extends Exception {
	}
	public static final class Item {
	}
	abstract Item anotherMethodWithLongNameToForceCodeformaterToBreakTheLine()
			throws Problem;
}
/**
 * ... and as a result I have many inner classes with the names
 * "Problem", "Factory" or "Item".
 * Now I Use code completation to procduce method stubs and got errors
 * as note in the method comments below.
 * I use "Automatically add import instead of qualified name" in 
 * 		Window > Preferences > Java > Editor > Code Assist:
 */
class AssistMyStyleImpl extends AssistMyStyle implements GeneralConcept {
	public static final class Factory implements AssistMyStyle.Factory {
		public AssistMyStyle newAssistMyStyle() {
			return new AssistMyStyleImpl();
		}
	}
	/**
	 * <pre>
	 * 	Item someMethod() throws Problem {
	 *		return null;
	 *	}
	 * </pre>
	 * Implies errors:
	 *  [1] The exception type Problem is ambiguous for the method someMethod
	 *  [2] The return type Item is ambiguous for the method someMethod
	 * I fix this:
	 */
	AssistMyStyle.Item someMethod() throws AssistMyStyle.Problem {
		return null;
	}
	/**
	 * anotherMethodWithLongNameToForceCodeformaterToBreakTheLine()
	 * <pre>
	 * 	public Item anotherMethodWithLongNameToForceCodeformaterToBreakTheLine()
	 *	throws Problem {
	 *	// XXX Auto-generated method stub
	 *	return null;
	 *	}
	 * </pre>
	 * Implies error:
	 * [1] The exception type Problem is ambiguous for the method 
	 *      anotherMethodWithLongNameToForceCodeformaterToBreakTheLine()
	 * [2] The return type Item is ambiguous for the method 
	 *      anotherMethodWithLongNameToForceCodeformaterToBreakTheLine
	 * I fix this and used code format (looks ugly!!!!):
	 */
	public GeneralConcept.Item anotherMethodWithLongNameToForceCodeformaterToBreakTheLine()
			throws GeneralConcept.Problem {
		return null;
	}
}