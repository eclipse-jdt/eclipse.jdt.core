package test0311;

public class Test {
	void m(){
		final int j= 0;
		Test a= new Test(){
			void m(int j){
				int u= j;
			}
		};
	}
}
