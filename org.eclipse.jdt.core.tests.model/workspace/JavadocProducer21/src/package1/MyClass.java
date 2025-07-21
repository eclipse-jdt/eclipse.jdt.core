/**
 *
 */
package package1;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

/**
 * Javadoc for MyClass
 */
public abstract class MyClass {

	/**
	 * Javadoc for protectedField
	 */
	protected String protectedField;

	/**
	 * Javadoc for publicField
	 */
	public String publicField;

	/**
	 * Javadoc for publicStaticField
	 */
	public static String publicStaticField;

	/**
	 * Javadoc for init
	 */
	public MyClass() {
	}

	/**
	 * Javadoc for init
	 * @param flag some argument
	 */
	public MyClass(boolean flag) {
	}

	/**
	 * Javadoc for main
	 * @param args hello
	 */
	public static void main(String[] args) {
	}

	/**
	 * Javadoc for abstractProtectedMethod
	 * @return something
	 */
	abstract protected String abstractProtectedMethod();

	/**
	 * Javadoc for abstractPublicMethod
	 * @return something
	 */
	abstract public String abstractPublicMethod();

	/**
	 * Javadoc for NestedInterface
	 * @param <E> type javadoc
	 */
	public interface NestedInterface<E> {

		/**
		 * Javadoc for INTERFACE_CONSTANT
		 */
		public static final String INTERFACE_CONSTANT = "OMG";

		/**
		 * Javadoc for abstractPublicMethod
		 *
		 * @return something
		 */
		abstract String abstractPublicMethod();

	    /**
	     * Javadoc for d()
	     * @return javadoc for return
	     */
		List<E> d();

	    /**
	     * Javadoc for bar()
	     * @return javadoc for return
	     */
	    Object[] bar();

	    /**
	     * Javadoc for foo(T[])
	     * @param a javadoc for a
	     * @param <T> javadoc for T
	     * @return javadoc for return
	     */
	    <T> T[] toArray(T[] a);

	    /**
	     * Javadoc for m(E)
	     * @param o javadoc for o
	     * @return javadoc for return
	     */
	    boolean m(E o);

	    /**
	     * Javadoc for bar4
	     * @param i javadoc for i
	     * @param c javadoc for c
	     * @return javadoc for return
	     */
	    boolean bar4(int i, List<? extends E> c);

	    /**
	     * Javadoc for bar3
	     * @param c javadoc for c
	     * @return javadoc on return
	     */
	    boolean bar3(List<?> c);

	    /**
	     * Javadoc for bar5
	     * @param m javadoc for m
	     * @param j javadoc for j
	     * @param m2 javadoc for m2
	     * @param <K> javadoc for K
	     * @param <V> javadoc for V
	     * @return javadoc for return
	     */
	    public <K,V> Map<K,V> bar5(Map<K,V> m, int j, Map<K,V> m2);

	    /**
	     * Javadoc of equals
	     */
	    @Override
	    boolean equals(Object o);

	    /**
	     * Javadoc for bar1
	     * @param i javadoc for i
	     * @param e javadoc for e
	     * @return javadoc for return
	     */
	    E bar1(int i, E e);

	    /**
	     * Javadoc for m1
	     * @param i javadoc for i
	     * @param e javadoc for e
	     */
	    void m1(int i, E e);

	    /**
	     * Javadoc for m2
	     * @param index javadoc for index
	     * @return javadoc for return
	     */
	    E m2(int index);
	}

	/**
	 * Javadoc for NestedEnum
	 */
	public enum NestedEnum {
		/**
		 * Javadoc for ONE
		 */
		ONE("one"),
		/**
		 * Javadoc for TWO
		 */
		TWO("two");

		/**
		 * Javadoc for publicField
		 */
		private String publicField;

		NestedEnum(String arg) {
			publicField = arg;
		}

		/**
		 * Javadoc for getPublicField
		 * @return anything
		 */
		public String getPublicField() {
			return publicField;
		}
	}

	/**
	 * Javadoc for NestedAnnotation
	 */
	@Documented
	@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
	@Retention(RetentionPolicy.CLASS)
	public @interface NestedAnnotation {

	    /**
	     * Javadoc for explanation
	     * @return some value
	     */
	    String explanation() default "";
	}

	/**
	 * Javadoc for NestedClass
	 * @param <T> type javadoc
	 */
	public static class NestedClass<T> {

		/**
		 * Javadoc for field
		 */
		protected String field;

		/**
		 * Javadoc for fieldT
		 */
		protected List<T> fieldT;

		/**
		 * Javadoc for ID
		 */
		public static String ID = "42";

		/**
		 * Javadoc for NestedClass
		 */
		public NestedClass() {
		}

		/**
		 * Javadoc for NestedClass
		 * @param t javadoc for t
		 */
		public NestedClass(T t) {
		}

		/**
		 * Javadoc for NestedClass
		 * @param t javadoc for t
		 * @param ints javadoc for ints
		 */
		public NestedClass(T t, int... ints) {
		}

		/**
		 * Javadoc for getField
		 * @return something
		 */
		public String getField() {
			return field;
		}

		/**
		 * Javadoc for getSomething
		 * @return something
		 */
		public List<T> getSomething() {
			return null;
		}

		/**
		 * Javadoc for getSomething
		 * @param t javadoc for t
		 * @return something
		 */
		public List<T> getSomething(int t) {
			return null;
		}

		/**
		 * Javadoc for getSomething
		 * @param t javadoc for t
		 * @return something
		 */
		public List<T> getSomething(int... t) {
			return null;
		}

		/**
		 * Javadoc for getSomething
		 * @param t javadoc for t
		 * @return something
		 */
		public List<T> getSomething(Integer... t) {
			return null;
		}

		/**
		 * Javadoc for getSomething
		 * @param t javadoc for t
		 * @return something
		 */
		public List<T> getSomething(Integer t) {
			return null;
		}

		/**
		 * Javadoc for getSomething
		 * @param t javadoc for t
		 * @return something
		 */
		public List<T> getSomething(T t) {
			return null;
		}

		/**
		 * Javadoc for getSomething
		 * @param t javadoc for t
		 * @param list javadoc for list
		 * @return something
		 */
		public T getSomething(T t, List<T> list) {
			return null;
		}

		/**
		 * Javadoc for getSomething
		 * @param i javadoc for i
		 * @param t javadoc for t
		 * @param list javadoc for list
		 * @return something
		 */
		public String getSomething(int i, T t, List<T> list) {
			return field;
		}

		/**
		 * Javadoc for setField
		 *
		 * @param field something
		 */
		protected void setField(String field) {
			this.field = field;
		}
	}

	/**
	 * Javadoc for NestedRecord
	 *
	 * @param one param1
	 * @param two param2
	 */
	public record NestedRecord(String one, String two) {

		/**
		 * Javadoc for one
		 * @return something
		 */
		public String one() {
			return one;
		}
	}
}
