package test0570;

import java.util.*;
import java.util.zip.ZipFile;

public class A {
	static ZipFile zipFile;
	
	static {
		zipFile = null;
	}

	HashMap hashMap = new HashMap(), hashMap2 = null;
	
	public static int[] foo(final String s, int i)[] {
		System.out.println(s + i);
	}
}