package p1.p2.test;

import java.io.Serializable;
interface I {
	void doit();
}
class Bug402819 {
	Number n = (Serializable &  Number ) Long.parseInt("0");
	
	void foo(   int x   ,     int   y ) {}
	I i = (I       &       Serializable   ) () -> {};
}
