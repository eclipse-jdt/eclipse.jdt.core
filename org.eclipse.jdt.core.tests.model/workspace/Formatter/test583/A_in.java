package p;
public class X<T> {
	  public class B {}
	  public static void main (String[] args) {
	    p.X<?>.B[] b = new p.X<?>.B[1];
	  }
	}
