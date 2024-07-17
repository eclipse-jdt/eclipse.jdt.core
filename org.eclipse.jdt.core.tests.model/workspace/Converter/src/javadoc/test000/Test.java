package javadoc.test000;
import java.io.IOException;
import java.util.*;
public class Test {
int field;
class X {
	int x;
}
/** 
 * Method outside javaDoc Comment
 * 1) {@link String} tag description not empty
 * 2) {@link CharOperation Unknown class} tag description not empty
 * @param str
 * @param VAR Unknown parameter
 * @param list third param with embedded tag: {@link Vector}
 * @param array fourth param with several embedded tags on several lines:
 * 1) {@link String} tag description not empty
 * 2) {@link CharOperation Unknown class} tag description not empty
 * @throws NullPointerException
 * @return an integer
 * @see String
 * @see Vector tag description not empty
 * @see Object tag description includes embedded tags and several lines:
 * 1) {@link String} tag description not empty
 * 2) {@link CharOperation Unknown class} tag description not empty
 * @see Object#equals(Object)
 * @see Object#equals() Not applicable method
 * @see #foo(String,int,Vector,char[])
 * @see #foo(String str,int var,Vector list,char[] array) valid method
 * @see #field
 * @see X#x valid field
 * @see Object#unknown Unknown field
 */
int foo(String str, int var, Vector list, char[] array) throws IOException { return 0; }
}
