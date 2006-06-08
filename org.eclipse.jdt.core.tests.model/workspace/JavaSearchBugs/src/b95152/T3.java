package b95152;
public class T3 {
	T32 c2;
	T32.T33 c3;
	T3(T3 c) {
		c2 = new T32(c);
		c3 = c2.new T33(c2);
	}
	class T32 {
		T32(T3 c) {}
		class T33 {
			T33(T32 c) {}
		}
	}
}
