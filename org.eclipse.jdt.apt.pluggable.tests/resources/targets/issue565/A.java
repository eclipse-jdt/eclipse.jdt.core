package targets.issue565;
import java.io.Serializable;

@Annotation565
public class A <I extends Serializable & Comparable<I> & Runnable>{}
