package targets.AnnotationProcessorTests.bug317216;

import java.util.Map;

@Gen
public class Foo {
	 Map<String, String> getFoo() { return null; }
}

