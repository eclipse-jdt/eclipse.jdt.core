package test0159;
import java.util.*;
public class Test {
	Test(int i){
	}
	void n(){
		final int y= 0;
		new Test(y){
			void f(){
				int y= 9;
			}
		};
	}
}