// packages a? stand for search annotations tests
package a1;
import e1.Team;
import static e1.Team.FREDERIC;

public @interface Author {
	Team[] name() default FREDERIC;
}
