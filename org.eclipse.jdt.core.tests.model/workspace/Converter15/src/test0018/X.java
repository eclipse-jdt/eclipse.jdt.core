package test0018;

class A<E> {
	E e;
	
	A(E e) {
		this.e = e;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.e);
	}
	class B {
		Integer t;
		B(Integer t) {
			this.t = t;
		}
		@Override
		public String toString() {
			return String.valueOf(this.t);
		}
		
		class C<Z> {
			Z z;
			C(Z z) {
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
		test0018.A<String>.B.C<Float> o = new test0018.A<String>("Hello").new B(new Integer(1)).new C<Float>(new Float(1.2f));
		System.out.println(o);
	}
}