public class CompletionAmbiguousFieldName4 {
	public void foo(){
		new Object() {
			int xBar;
			class ClassFoo {
				public void foo(int xBar){
					xBa
				}
			}
		}
	}
}