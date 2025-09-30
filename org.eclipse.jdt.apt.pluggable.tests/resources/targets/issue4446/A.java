package targets.issue4446;
import java.io.Serializable;

@Annotation4446
public class A<T extends Number & Runnable & Serializable> {
    public <U extends Number & Runnable> void doSomething(U input) { }
}
