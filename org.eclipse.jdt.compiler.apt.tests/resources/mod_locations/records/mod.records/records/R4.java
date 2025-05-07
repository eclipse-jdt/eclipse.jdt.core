package records;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Repeatable;

record R4(@Marker4 int... i) {
	public R4 {}
}
// record R4(@Marker4  @Marker5() @Marker5() int... i) {} // This will cause failures in RecordElementProcessor#testRecords10

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD}) 
@interface Marker4 {}

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE_USE})@interface Marker5Container { Marker5[] value(); }
@Repeatable(Marker5Container.class)
@Target({ElementType.TYPE_USE})@interface Marker5 {}
