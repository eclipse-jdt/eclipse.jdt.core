package q1;
/* Test case for PR 1GK90H4: ITPJCORE:WIN2000 - search: missing package reference */
public class B{
	void m(AA fred){
		q2.A.length(); //<<<
	}
}
class AA{
	static String A;
}