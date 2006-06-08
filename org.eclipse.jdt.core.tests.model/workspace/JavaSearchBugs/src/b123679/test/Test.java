package b123679.test;
import b123679.pack.I123679;
public class Test {
	static class StaticClass {
		class Member {
			private I123679 parent;
			Member(Object obj) {
				if (obj instanceof I123679) {
					parent = (I123679) obj;
				} else {
					parent = new I123679() {};
				}
			}
			I123679 getParent() {
				return parent;
			}
		}
	}
}
