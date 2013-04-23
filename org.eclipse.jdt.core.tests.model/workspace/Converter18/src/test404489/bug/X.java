package test404489.bug;
import java.lang.annotation.*;
public class X {
	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	static @interface NonNull { }
	class Inner {}
	
	/**
 	* @param arg  
 	*/
	test404489.bug.@NonNull IOException foo(
			test404489.bug.@NonNull FileNotFoundException arg)
		throws test404489.bug.@NonNull EOFException {
		try {
			test404489.bug.@NonNull IOError e = new test404489.bug.IOError();
			throw e;
		} catch (test404489.bug.@NonNull IOError e) {
		}
		return null;
	} 
	test404489.bug.@NonNull X.@NonNull Inner fInner;
} 
@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {} 
@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface A {} 
@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface B {} 

class Outer {
	public class Inner {
		public class Deeper {}
	}
}
class IOException extends Exception {private static final long serialVersionUID=10001L;}
class FileNotFoundException extends Exception{private static final long serialVersionUID=10002L;}
class EOFException extends Exception{private static final long serialVersionUID=10003L;}
class IOError extends Exception{private static final long serialVersionUID=10004L;}