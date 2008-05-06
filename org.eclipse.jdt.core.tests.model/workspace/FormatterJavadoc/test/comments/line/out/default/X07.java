package test.comments.line;

public class X07 {

	boolean inTitle;
	boolean inMetaTag;
	boolean inStyle;
	boolean inImg;

	void foo(String tagName) {
		inTitle = tagName.equalsIgnoreCase("<title"); // keep track if in
													// <TITLE>
		inMetaTag = tagName.equalsIgnoreCase("<META"); // keep track if in
														// <META>
		inStyle = tagName.equalsIgnoreCase("<STYLE"); // keep track if in
													// <STYLE>
		inImg = tagName.equalsIgnoreCase("<img"); // keep track if in <IMG>
	}
}
