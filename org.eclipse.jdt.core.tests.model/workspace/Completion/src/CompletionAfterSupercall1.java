public class CompletionAfterSupercall1 extends CompletionAfterSupercall1_1 {
	public void foo(){
		super.foo
	}
}
abstract class CompletionAfterSupercall1_1 extends CompletionAfterSupercall1_2 implements CompletionAfterSupercall1_3 {
	
}
class CompletionAfterSupercall1_2 implements CompletionAfterSupercall1_3 {
	public void foo(){}
}
interface CompletionAfterSupercall1_3 {
	public void foo();
}