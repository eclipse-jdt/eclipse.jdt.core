package q9;
/* Test case for bug 5862 search : too many matches on search with OrPattern */
interface I{
void m();
}

class A1 implements I{
public void m(){}
}
interface I1{
void m();
}
