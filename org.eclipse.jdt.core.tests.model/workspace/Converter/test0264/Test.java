package test0264;
import java.util.*;
public class Test {
	void m(final int i){
		Test a= new Test(){
			void m(int k){
				k= i;
			}
		};
	}
}
