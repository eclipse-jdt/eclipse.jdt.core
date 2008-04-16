package test.comments.line;

public interface X02b {

	int foo(); // This is a long comment that should be split in multiple line
				// comments in case the line comment formatting is enabled

	int bar();
}
