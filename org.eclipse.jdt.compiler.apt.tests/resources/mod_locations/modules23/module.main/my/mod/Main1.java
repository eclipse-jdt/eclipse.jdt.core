package my.mod;

/**
 * A traditional Javadoc comment on a class
 */
public class Main1 {
	///
	////A markdown type comment on a method - line 1
	/////// A markdown type comment on a method - line 2
	///    A markdown type comment on a method - line 3
	///
    public static void myMethod(String argv[]) {
    }
    /// Doc comment with 3 lines
    ///
    /// with an empty line in the middle
    public void foo1() {}
    /// Dangling comment, not considered

    /// This is the actual doc commment.
    public void foo2() {}

    /// | Code  | Color |
    /// |-------|-------|
    /// | R     | Red   |
    /// | G     | Green |
    /// | B     | Blue  |
    public void foo3(){
    }

    /// {@inheritDoc}
    /// Get the inherited function.
    ///
    /// @param p parameter
    public void foo4(int p){
    }
}
