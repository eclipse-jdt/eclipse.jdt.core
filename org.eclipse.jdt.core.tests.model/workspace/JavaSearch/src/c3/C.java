package c3;
/* Test case for bug 18418  search: searchDeclarationsOfReferencedTypes reports import declarations  */
import c2.A;
import c2.*;
import c2.B;
public class C{
	A a;
	c2.A c2a;
	B b;
	c2.B c2B;
}
