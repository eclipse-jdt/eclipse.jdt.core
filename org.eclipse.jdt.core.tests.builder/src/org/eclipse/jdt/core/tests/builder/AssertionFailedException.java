package org.eclipse.jdt.core.tests.builder;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * <code>AssertionFailedException</code> is a runtime exception thrown
 * by some of the methods in <code>Assert</code>.
 * <p>
 * This class is not declared public to prevent some misuses; programs that catch 
 * or otherwise depend on assertion failures are susceptible to unexpected
 * breakage when assertions in the code are added or removed.
 * </p>
 */
/* package */
class AssertionFailedException extends RuntimeException {
/** Constructs a new exception.
 */
public AssertionFailedException() {
}
/** Constructs a new exception with the given message.
 */
public AssertionFailedException(String detail) {
	super(detail);
}
}
