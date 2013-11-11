package targets.model8;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.TYPE_USE}) @interface TypeAnnot {} 

interface Iface {
	static void foo(@TypeAnnot int i) {}
}

public class InterfaceTest implements Iface {

    public static void main(String[] argv) {
    	Iface.foo(10);
    }
}

