public class ResolveAbstractMethod {
	void test(AbstractClass a){
		a.foo();
	}
}
abstract class AbstractClass implements SuperInterface {
}
