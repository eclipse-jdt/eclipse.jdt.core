package test0038;

public class Data22<T> {
	T x;
	T get(){
		Data22<T> s= this;
		return x;
	}
	void set(T o1){
		x = o1;
	}
}