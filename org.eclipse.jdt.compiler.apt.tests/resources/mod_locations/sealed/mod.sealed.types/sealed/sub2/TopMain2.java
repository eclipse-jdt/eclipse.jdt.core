package sealed.sub2;

public class TopMain2 {
	sealed interface SealedIntf permits MyRecord {}
	record MyRecord() implements SealedIntf {}
}

