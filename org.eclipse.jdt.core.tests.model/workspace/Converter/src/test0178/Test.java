package test0178;
import java.util.*;

class Top {
	int j;
}

public class Test extends Top {
	int foo() {
		return super.j;
	}
}