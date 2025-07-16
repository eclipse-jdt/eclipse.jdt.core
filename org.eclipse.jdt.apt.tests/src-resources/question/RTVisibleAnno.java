package question;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface RTVisibleAnno
{
	String name() default "eclipse";
	boolean boolValue() default true;
	byte byteValue() default 0x0001;
	char charValue() default 'e';
	double doubleValue() default 0.0;
	float floatValue()  default 0;
	int intValue() default 17;
	long longValue() default 43;
	short shortValue() default 1;
	Color[] colors() default {Color.RED, Color.WHITE, Color.BLUE};
	SimpleAnnotation anno();
	SimpleAnnotation[] simpleAnnos() default { @SimpleAnnotation("org.eclipse.org") };
	Class<?>[] clazzes();
	Class<?> clazz() default java.lang.String.class;

 }