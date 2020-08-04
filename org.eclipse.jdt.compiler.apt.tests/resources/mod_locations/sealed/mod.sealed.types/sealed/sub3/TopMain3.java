package sealed.sub3;

public class TopMain3 {
	TopMain3 top = new TopMain3() {};
	sealed interface SealedIntf permits MyEnum {}
	enum MyEnum implements SealedIntf{
		 A {
	            int val() { return 1; }
	        }, B, C;
	        int val() {
	            return 0;
	        }
	}
}