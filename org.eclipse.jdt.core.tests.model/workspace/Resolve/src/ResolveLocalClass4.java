import p1.*;

public class ResolveLocalClass4{
	void foo() {
		class Y {
		}
		class Z {
			Y bar() {
				return null;
			}
		}
	}
}