package test0038;

public class X<T> {
	T x;
	T get(){
		X<T> s= this;
		return x;
	}
	void set(T o1){
		x = o1;
	}
}