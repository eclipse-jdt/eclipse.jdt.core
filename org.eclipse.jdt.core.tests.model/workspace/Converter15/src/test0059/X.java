package test0059;

public class X {   
    public static final String val = "foo";
    
    public static void main(String[] args) {
    	for (Test t : Test.values()) {
    		System.out.println(t.name());
    	}
    }
}
enum Test {
    VAL_A;

    private static final String VAL_A_LABEL;

    static {
        VAL_A_LABEL = X.val;
    }
}



