package test0548;

public class A {
    public void foo(int i) {
    	int j = 4;
        class Local {
        	int z = 3;
        	
        	public int bar(int k) {
        		return z + k;
        	}
        }
        int n = 5;
        System.out.println(j + n + i + new Local().bar(2));
    }	
}