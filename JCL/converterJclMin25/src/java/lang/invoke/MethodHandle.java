package java.lang.invoke;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public abstract class MethodHandle {
	@Target(METHOD)
	@Retention(RUNTIME)
	@interface PolymorphicSignature {
	}

	@PolymorphicSignature
	public final native Object invoke(Object... args) throws Throwable;

	@PolymorphicSignature
	public final native Object invokeExact(Object... args) throws Throwable;

	public native Object invokeWithArguments(Object... arguments)
			throws Throwable;

	public native boolean isVarargsCollector();

	public native MethodHandle asType(MethodType newType);
}
