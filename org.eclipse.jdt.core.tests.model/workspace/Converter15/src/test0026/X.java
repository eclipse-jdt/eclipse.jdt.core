package test0026;

public enum X {
    PLUS {
        @Override
        double eval(double x, double y) { return x + y; }
    } // comment
    ,

    MINUS {
        @Override
        double eval(double x, double y) { return x - y; }
    },

    TIMES {
        @Override
        double eval(double x, double y) { return x * y; }
    },

    DIVIDED_BY {
        @Override
        double eval(double x, double y) { return x / y; }
    };

    // Perform the arithmetic X represented by this constant
    abstract double eval(double x, double y);

    public static void main(String args[]) {
    	if (args.length < 2) {
    		System.out.println("Usage: X <double> <double>");
    		return;
    	}
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);

        for (X op : X.values())
            System.out.println(x + " " + op + " " + y + " = " + op.eval(x, y));
    }
}