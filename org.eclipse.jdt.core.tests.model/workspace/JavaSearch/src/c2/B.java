package c2;
/* Test case for bug 18418 search: searchDeclarationsOfReferencedTypes reports import declarations  */
import c3.C;
public class B{
	C c;
	c3.C m(C C, A A){
		return C;
	}	
}
