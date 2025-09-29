package targets.issue4446;

import targets.issue4446.Annotation4446;
import java.io.Serializable;

@Annotation4446
public class Example<T extends Number & Runnable & Serializable> {
    public <U extends Number & Runnable> void doSomething(U input) { }
}
