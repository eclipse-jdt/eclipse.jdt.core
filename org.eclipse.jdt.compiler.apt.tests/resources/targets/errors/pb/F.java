package targets.errors.pb;

public class F {
	
	enum C {
		CONST1, CONST2
	}

	int field;

	static {
	}

	{
		field = 1;
	}

	F(int i) {
		this.field = i;
	}

	static class Member {
	}

	public void foo(int i) throws Exception {}
}
