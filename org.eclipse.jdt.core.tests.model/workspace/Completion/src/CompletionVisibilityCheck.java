public class CompletionVisibilityCheck {
	public void foo(){
		CompletionVisibilityCheck1 x = new CompletionVisibilityCheck1();
		x.p
	}
}

class CompletionVisibilityCheck1{
	private void privateFoo(){}
	protected void protectedFoo(){}
	public void publicFoo(){}
}