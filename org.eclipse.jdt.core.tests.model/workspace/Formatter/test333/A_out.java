public class X {
	void bar() {
		for (final int i; 0 < (i = 1); i = i + 1) {
		}
	}

}

//public class X {
//	public static void main(String[] args) {
//		final int i;
//		for ( ; ; i = 1)
//		    break;
//		i = 1;
//	}
//}

//public class X {
//	final int blank;
//	{
//		while ((null == null || true)) {
//			blank = 1;
//			break;
//		}
//	}
//	X(){
//	}
//	public static void main(String[] argv){
//		System.out.println("SUCCESS");
//	}
//}

// should fail
//class X {
//
//	public static void main(String[] args) {
//		final boolean b;
//		do
//		    break;
//		while ((b = true) && false);
//		b = true;
//	}
//}

// ?
//class X {
//
//	public static void main(String[] args) {
//		final boolean b;
//		do
//		    break;
//		while ((b = true) && false);
//		b = true;
//	}
//}
