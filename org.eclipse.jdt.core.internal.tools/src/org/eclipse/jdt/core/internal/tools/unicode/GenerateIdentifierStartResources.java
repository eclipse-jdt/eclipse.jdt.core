package org.eclipse.jdt.core.internal.tools.unicode;

import java.io.IOException;

public class GenerateIdentifierStartResources {

	public static void main(String[] args) throws IOException {
		UnicodeResourceGenerator generator = new UnicodeResourceGenerator(args, false);
		generator.generate();
	}
}
