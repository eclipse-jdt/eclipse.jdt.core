class Foo
{
	class Bar
	{
	}

	void foo()
	{
		Bar obj = new Bar()
		{
			public void bar()
			{
				// Here is the problem.
				return;
			}
		};
	}
}