package test0026;

public abstract class X {
	
	void foo() {
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
}