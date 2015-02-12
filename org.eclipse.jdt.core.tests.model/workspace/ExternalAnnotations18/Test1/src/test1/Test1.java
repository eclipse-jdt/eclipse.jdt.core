package test1;

import libs.MyMap;
import org.eclipse.jdt.annotation.*;

@NonNullByDefault
public class Test1 {
	void test(MyMap<String,Test1> map, String key) {
		Test1 v = map.get(key);
		if (v == null)
			throw new RuntimeException(); // should not be reported as dead code, although V is a '@NonNull Test1'
	}
}