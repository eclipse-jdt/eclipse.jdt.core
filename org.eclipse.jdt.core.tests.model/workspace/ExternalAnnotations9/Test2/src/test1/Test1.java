package test1;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault; // note: if org.eclipse.jdt.annotation.* was used, no problem appeared

@NonNullByDefault
public class Test1 {
	void test(Map<String,Test1> map, String key) {
		Test1 v = map.get(key);
		if (v == null)
			throw new RuntimeException(); // should not be reported as dead code, although V is a '@NonNull Test1'
	}
}