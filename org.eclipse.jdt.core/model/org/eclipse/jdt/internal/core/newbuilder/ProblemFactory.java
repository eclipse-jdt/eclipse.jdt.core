package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.util.LookupTable;
import java.util.Locale;

public class ProblemFactory extends DefaultProblemFactory {

static LookupTable factories = new LookupTable(5);

private ProblemFactory(Locale locale) {
	super(locale);
}

public static ProblemFactory getProblemFactory(Locale locale) {
	ProblemFactory factory = (ProblemFactory) factories.get(locale);
	if (factory == null)
		factories.put(locale, factory = new ProblemFactory(locale));
	return factory;
}
}
