package d7;
/* Test case for bug 29516  SearchEngine regressions in 20030114 */
class A{
   A A;
   A A(A A){
	 A:
		for (;;){
		  if (A.A(A)==A)
			 break A;
		}
	  return A;
   };
}