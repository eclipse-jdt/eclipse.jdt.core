package 5;
/* Test case for bug 48350 IType#resolveType(String) fails on local types */
public class A {
	void foo() {
		class Local {
			void bar() {
			}
		}
	}
}
