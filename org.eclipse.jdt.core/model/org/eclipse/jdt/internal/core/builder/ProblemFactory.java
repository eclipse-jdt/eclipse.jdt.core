package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.util.*;

public class ProblemFactory extends DefaultProblemFactory {

static SimpleLookupTable factories = new SimpleLookupTable(5);

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
