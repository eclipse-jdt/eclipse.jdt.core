public static String getLine()
{
	try
	{
		return textIn.readLine();
	}
	catch (IOException e)
	{
		//Should never happen, isReady() tests for errors
		doSomething();
	}
}