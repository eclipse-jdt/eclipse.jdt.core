package b95152;
public class T4 {
	T42 c2;
	T42.T43 c3;
	T4(T4 c, String str) {
		c2 = new T42(c, str);
		c3 = c2.new T43(c2, str);
	}
	class T42 {
		T42(T4 c, String str) {}
		class T43 {
			T43(T42 c, String str) {}
		}
	}
}
