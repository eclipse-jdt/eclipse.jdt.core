package targets.model8.a;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Repeatable;


@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER }) @interface MarkerContainer {Marker[] value();}
@Repeatable(MarkerContainer .class)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER }) @interface Marker {}


class Test {
     <@Marker() @Marker() T> T foo()  { return null; }
}

public class TypeParameterTest  {
}
