try
{
	do
	{
		i++;
	}
	while (i < 30);
	if (bool)
	{
		synchronized (this)
		{
			System.out.println();
		}
	}
}
catch (Exception e)
{
}
finally
{
	i++;
}