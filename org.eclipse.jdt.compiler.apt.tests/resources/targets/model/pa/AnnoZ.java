package targets.model.pa;

import java.lang.annotation.Inherited;

@Inherited
public @interface AnnoZ {
	String annoZString();
	int annoZint() default 17;
}