package test0028;

public abstract class X {
	
	void foo(String[] args) {
    	if (args.length < 2) {
    		System.out.println("Usage: X <double> <double>");
    		return;
    	}
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);

        for (X op : X.values())
            System.out.println(x + " " + op + " " + y + " = " + op.eval(x, y));
	}

	// Perform the arithmetic X represented by this constant
    abstract double bar(double x, double y);

	static X[] values() {
		return null;
	}
	
    abstract double eval(double x, double y);
}