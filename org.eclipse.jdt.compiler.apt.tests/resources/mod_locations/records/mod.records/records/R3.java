package records;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigInteger;
public record R3(	@MyAnn4(value=5) int x, 
					@MyAnn2(value=5) BigInteger bigInt, 
					@MyAnn2(value=5) @MyAnn3(value=5) R1 r1, 
					@MyAnn3(value=5) float floatValue, 
					@MyAnn(value=5) Character c, 
					@MyAnn(value=5) @MyAnn2(value=5) @MyAnn3(value=5) R1 recordInstance){}

record R1(){}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT, ElementType.METHOD})
@interface MyAnn {
     int value() default 1;
}
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnn2 {
    int value() default 1;
}
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT})
@interface MyAnn3 {
    int value() default 1;
}
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@interface MyAnn4 {
    int value() default 1;
}