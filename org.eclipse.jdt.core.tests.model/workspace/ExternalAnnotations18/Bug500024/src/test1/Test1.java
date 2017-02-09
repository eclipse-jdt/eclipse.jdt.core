package test1;

import java.util.Map;
import org.eclipse.jdt.annotation.*;

@NonNullByDefault
public class Test1 {
	void test(Map<String,Test1> map, String key) {
		Test1 v = map.get(key);
		if (v == null)
			throw new RuntimeException();
	}
	public boolean equals(@NonNull Object other) { return false; }
}