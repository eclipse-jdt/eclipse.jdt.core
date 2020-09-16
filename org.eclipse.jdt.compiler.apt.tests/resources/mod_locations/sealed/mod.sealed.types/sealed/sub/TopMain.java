package sealed.sub;

public class TopMain {
	public static void main(String[] args) {}
}
sealed class TopSecond permits TopThird, TopThird.NonSealedStaticNested {}
final class TopThird extends TopSecond {
	static non-sealed class NonSealedStaticNested extends TopSecond {}
}
