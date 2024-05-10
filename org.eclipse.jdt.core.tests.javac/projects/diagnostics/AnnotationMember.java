
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// compiler.err.annotation.value.must.be.name.value -> UndefinedAnnotationMember(67109475)
@Retention(RetentionPolicy.RUNTIME, "error")
public @interface AnnotationMember {
}
