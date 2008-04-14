package test.prefs.example;

/**
 * A test case defines the fixture to run multiple tests. To define a test case<br>
 * 1) implement a subclass of TestCase<br>
 * 2) define instance variables that store the state of the fixture<br>
 * 3) initialize the fixture state by overriding <code>setUp</code><br>
 * 4) clean-up after a test by overriding <code>tearDown</code>.<br>
 * Each test runs in its own fixture so there can be no side effects among test
 * runs. Here is an example:
 * 
 * <pre>
 * public class MathTest extends TestCase {
 * 	protected double fValue1;
 * 	protected double fValue2;
 * 
 * 	protected void setUp() {
 * 		fValue1 = 2.0;
 * 		fValue2 = 3.0;
 * 	}
 * }
 * </pre>
 * 
 * For each test implement a method which interacts with the fixture. Verify the
 * expected results with assertions specified by calling <code>assertTrue</code>
 * with a boolean.
 * 
 * <pre>
 * public void testAdd() {
 * 	double result = fValue1 + fValue2;
 * 	assertTrue(result == 5.0);
 * }
 * </pre>
 * 
 * Once the methods are defined you can run them. The framework supports both a
 * static type safe and more dynamic way to run a test. In the static way you
 * override the runTest method and define the method to be invoked. A convenient
 * way to do so is with an anonymous inner class.
 * 
 * <pre>
 * TestCase test = new MathTest(&quot;add&quot;) {
 * 	public void runTest() {
 * 		testAdd();
 * 	}
 * };
 * test.run();
 * </pre>
 * 
 * The dynamic way uses reflection to implement <code>runTest</code>. It
 * dynamically finds and invokes a method. In this case the name of the test
 * case has to correspond to the test method to be run.
 * 
 * <pre>
 * TestCase = new MathTest(&quot;testAdd&quot;);
 * test.run();
 * </pre>
 * 
 * The tests to be run can be collected into a TestSuite. JUnit provides
 * different <i>test runners</i> which can run a test suite and collect the
 * results. A test runner either expects a static method <code>suite</code> as
 * the entry point to get a test to run or it will extract the suite
 * automatically.
 * 
 * <pre>
 * public static Test suite() {
 * 	suite.addTest(new MathTest(&quot;testAdd&quot;));
 * 	suite.addTest(new MathTest(&quot;testDivideByZero&quot;));
 * 	return suite;
 * }
 * </pre>
 * 
 * @see TestResult
 * @see TestSuite
 */
public class X03 {

}
