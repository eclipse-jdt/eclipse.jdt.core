package records;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigInteger;
public record R3(int x, BigInteger bigInt, R1 r1, float floatValue, @MyAnn(value=5) Character c, R1 recordInstance){}

record R1(){}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT,ElementType.METHOD})
@interface MyAnn {
     int value() default 1;
}