package test0466;

public class Assert {
	public static final void notNull(Object ref, String message)
	{
		assert ref != null : message;
	}
	
	void method(String param1, String param2, String param3)
	{
		Assert.notNull(param1, "param1 != null");
		Assert.notNull(param2, "param2 != null");
		Assert.notNull(param3, "param3 != null");
	}
}