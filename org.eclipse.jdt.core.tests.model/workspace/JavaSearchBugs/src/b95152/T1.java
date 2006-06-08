package b95152;
public class T1 {
	T12 c2;
	T12.T13 c3;
	T1() {
		c2 = new T12();
		c3 = c2.new T13();
	}
	class T12 {
		T12() {}
		class T13 {
			T13() {}
		}
	}
}
