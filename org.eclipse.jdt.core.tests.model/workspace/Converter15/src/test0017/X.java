package test0017;

class A<E> {
	E e;
	
	A(E e) {
		this.e = e;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.e);
	}
	class B<T> {
		T t;
		B(T t) {
			this.t = t;
		}
		@Override
		public String toString() {
			return String.valueOf(this.t);
		}
	}
}
public class X {
	public static void main(String[] args) {
		test0017.A<String>.B<Integer> o = new test0017.A<String>("Hello").new B<Integer>(new Integer(1));
		System.out.println(o);
	}
}