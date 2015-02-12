package test1;

import libs.MyMap;

public class Test1 {
	String test1(MyMap<String,Test1> map) {
		map.put(null, null); // key is OK (@Nullable), val is err (@NonNull)
		Test1 v = map.get(null); // err: key is @NonNull via eea
		return v.toString(); // err: v is @Nullable via eea
	}
}