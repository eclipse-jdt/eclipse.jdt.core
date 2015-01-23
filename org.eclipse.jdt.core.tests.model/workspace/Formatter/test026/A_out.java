// test026
public class A {
	public void foo() {
		this.longReceiver.someQuiteLongMessageSend("aaaaaaaaaaa",
				"bbbbbbbbbbbbb", "cccccccc");
		this.extremlylongReceiverWillCauseTwoSplitActions
				.someQuiteLongMessageSend("aaaaaaaaaaa", "bbbbbbbbbbbbb",
						"cccccccc");
		Alignment expressionsAlignment = this.scribe.createAlignment(
				"expressions",
				Alignment.M_COMPACT_SPLIT + someMessageSend(
						Alignment.M_COMPACT_SPLIT, Alignment.M_COMPACT_SPLIT,
						Alignment.M_COMPACT_SPLIT, Alignment.M_COMPACT_SPLIT),
				expressionsLength - 1, this.scribe.scanner.currentPosition);
	}
}