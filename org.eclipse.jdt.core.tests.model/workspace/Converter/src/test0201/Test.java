package test0201;
import java.util.*;
public class Test {
        public void foo() {
                /*]*/foo();/*[*/
                
                for (int i= 0; i < 10; i++)
                        foo();  
        }
}