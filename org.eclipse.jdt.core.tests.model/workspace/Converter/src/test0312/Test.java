package test0312;

public class Test {
	void m(){
		final int j= 0;
		A a= new A(){
			void m(int j){
				int u= j;
			}
		};
	}
}
