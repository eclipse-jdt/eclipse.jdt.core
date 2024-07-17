package javadoc.testBug54424;
import java.io.IOException;
import java.util.ArrayList;

public class Test {
	/**
	 * @param
	 * @param tho {@link getList(int, long)}
	 * @version throwaway
	 * @param from 1st param of {@link A#getList(int, long, boolean) me}
	 * @see #getList(Object, java.util.SequencedCollection)
	 * @param from 2nd
	 * @see #getList(int from, tho long)
	 * @see #getList(int from, long tho)
	 * @param 
	 * @return the list
	 * @see #getList(..)
	 * @param to
	 * @throws .IOException
	 * @deprecated
	 * @throws IOException.
	 * @todo it
	 */
	public ArrayList getList(int from, long to) throws IOException {return null;}
}
