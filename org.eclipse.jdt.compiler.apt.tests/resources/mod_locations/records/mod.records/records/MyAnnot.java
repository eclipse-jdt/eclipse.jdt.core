package records;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.RECORD_COMPONENT, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnot {}

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnot2 {}

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnot3 {}

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnot4 {}


@Target({ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnot5 {}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE_USE})
@interface MyAnnot6 {}
