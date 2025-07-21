package markdown.test000a;
import java.io.IOException;
import java.util.*;
public class Test {
int field;
class X {
	int x;
}
interface CharOperation{}
/// Method outside javaDoc Comment
/// 1) [String] tag description not empty
/// 2) [Unknown class][CharOperation] tag description not empty
/// @param str
/// @param VAR Unknown parameter
/// @param list third param with embedded tag: [List]
/// @param array fourth param with several embedded tags on several lines:
/// 1) [String] tag description not empty
/// 2) [Unknown class][CharOperation] tag description not empty
/// @throws ArithmeticException Unknown class
/// @throws NoSuchMethodException
/// @return an integer
/// @see String
/// @see List tag description not empty
/// @see Object tag description includes embedded tags and several lines:
/// 1) [Unknown class][CharOperation] tag description not empty
/// 2) [Unknown class][CharOperation] tag description not empty
/// @see Object#equals(Object)
/// @see Object#equals() Not applicable method
/// @see #foo(String,int,List,char[])
/// @see #foo(String str,int var,List list,char[] array) valid method
/// @see #field
/// @see X#x valid field
/// @see Object#unknown Unknown field
int foo(String str, int var, List list, char[] array) throws NoSuchMethodException { return 0; }
}
