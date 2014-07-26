package test1;

import java.util.Collection;
import org.eclipse.jdt.annotation.*;

@NonNullByDefault
public class Test1 {
	void test(Collection<String> map, int key) {
		String v = map.get(key);
		if (v == null)
			throw new RuntimeException(); // should not be reported as dead code, although map is a Collection<@NonNull String>
	}
}