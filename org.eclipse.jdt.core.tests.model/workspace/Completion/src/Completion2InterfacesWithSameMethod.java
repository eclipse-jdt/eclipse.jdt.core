public class Completion2InterfacesWithSameMethod {
	void foo(Completion2InterfacesWithSameMethodI1 var){
		var.meth
	}
}
interface Completion2InterfacesWithSameMethodI1 extends Completion2InterfacesWithSameMethodI2, Completion2InterfacesWithSameMethodI3{
}
interface Completion2InterfacesWithSameMethodI2 {
	void method();
}
interface Completion2InterfacesWithSameMethodI3 {
	void method();
}
