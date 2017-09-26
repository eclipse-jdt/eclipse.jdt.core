package testBug497719_001;
public class X {

    public void foo() throws Exception {
         final Y y1 = new Y();
         try (final Y y = new Y(); y1; final Y y2 = new Y()) { 
        	 //
         }
    } 
    public static void main(String[] args) {
		System.out.println("Done");
	}
}
class Y implements AutoCloseable {
    @Override
    public void close() throws Exception {
            // nothing
    }
}
