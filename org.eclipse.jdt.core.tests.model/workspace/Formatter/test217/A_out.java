public void foo()
{
	B b = new B();
	b.bar();
	for (int i = 0, max = b.length(); i < max; i++)
	{
		System.out.println(i);
	}
}