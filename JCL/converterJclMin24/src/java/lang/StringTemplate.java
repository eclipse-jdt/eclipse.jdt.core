package java.lang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

public interface StringTemplate {
    List<String> fragments();

    List<Object> values();

    default String interpolate() {
        return null;
    }
    static String interpolate(List<String> fragments, List<?> values) {
    	return null;
    }
    default <R, E extends Throwable> R
    process(Processor<? extends R, ? extends E> processor) throws E {
        return processor.process(this);
    }

    Processor<String, RuntimeException> STR = null;

    Processor<StringTemplate, RuntimeException> RAW = null;

    public interface Processor<R, E extends Throwable> {

        R process(StringTemplate stringTemplate) throws E;
    }

}
