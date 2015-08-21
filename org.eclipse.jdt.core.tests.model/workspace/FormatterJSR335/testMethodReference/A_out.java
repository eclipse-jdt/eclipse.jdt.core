interface I {
	String foo(String s);
}

interface J {
	int length();
}

interface K {
	int length(String s);
}

interface L {
	String foo();
}

interface M {
	int capacity(java.util.List<String> ls);
}

interface N {
	java.util.List<String> newList();
}

interface O {
	int[] vector(int x);
}

public class X {
	public void main(String[] args) {
		I i = System::getProperty;
		J j = "abc"::length;
		K k = String::<String, Integer>length;
		L l = super::toString;
		M m = java.util.List<String>::<X>size;
		N n = java.util.ArrayList<String>::new;
		O o = int[]::new;
	}
}