package test0019;

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
		
		class C {
			Float z;
			C(Float z) {
				this.z = z;
			}
			@Override
			public String toString() {
				return String.valueOf(this.z);
			}
		}
	}
}
public class X {
	public static void main(String[] args) {
		test0019.A<String>.B<Integer>.C o = new test0019.A<String>("Hello").new B<Integer>(new Integer(1)).new C(new Float(1.2f));
		System.out.println(o);
	}
}