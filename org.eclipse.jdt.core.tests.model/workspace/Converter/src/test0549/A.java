package test0549;

public class A {
    public void foo(int i) {
    	int j = 4;
        class Local {
        	int z = 3;
        	
        	public int bar(int k) {
        		int m = 3;
        		return z + k;
        	}
        }
        int n = 5;
        System.out.println(n + i + new Local().bar(2));
    }
    
    private static void bar(int j) {
    	int i;
    }
    
    static {
    	int i;
    }
    
    {
    	int i;
    }
}