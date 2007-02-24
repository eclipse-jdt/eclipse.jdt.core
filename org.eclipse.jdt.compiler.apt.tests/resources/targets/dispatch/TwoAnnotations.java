package targets.dispatch;

import org.eclipse.jdt.compiler.apt.tests.annotations.CheckArgs;
import org.eclipse.jdt.compiler.apt.tests.annotations.GenClass;

/**
 * Target for annotation processor tests.
 * @since 3.3
 */
@CheckArgs
@GenClass(clazz="gen.TwoAnnotationsGen", method="foo")
public class TwoAnnotations {

}
