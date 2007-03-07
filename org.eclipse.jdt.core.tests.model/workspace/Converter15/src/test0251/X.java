package test0251;

import java.util.Collections;
import java.util.Map;

public class X {
	void caller() {
		Map<String, String> explicitEmptyMap = Collections.<String, String> emptyMap();
		method(explicitEmptyMap);
		Map<String, String> emptyMap = Collections.emptyMap();
		method(emptyMap);
	}

	void method(Map<String, String> map) {
	}
}