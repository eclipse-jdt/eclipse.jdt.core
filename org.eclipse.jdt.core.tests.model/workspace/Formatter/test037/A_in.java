class F{
	public void foo() {
		F foo= new F() {
			public void bar() {
				// comment
				return;
			}
		}; 
	}
}