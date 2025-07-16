package package1;

import java.util.List;
import java.util.Map;


/**
 * Javadoc for MyInterface
 * @param <E> some type javadoc
 */
public interface MyInterface<E> {

	/**
	 * javadoc for INTERFACE_CONSTANT
	 */
	public static final String INTERFACE_CONSTANT = "OMG";

	/**
	 * javadoc for abstractPublicMethod
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
