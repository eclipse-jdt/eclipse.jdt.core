package b95152;
public class T2 {
	T22 c2;
	T22.T23 c3;
	T2(int c) {
		c2 = new T22(c);
		c3 = c2.new T23(c);
	}
	class T22 {
		T22(int x) {}
		class T23 {
			T23(int x) {}
		}
	}
}
