package org.eclipse.jdt.internal.compiler.codegen;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.lookup.*;

  public class ExceptionLabel extends Label {
	public int start = POS_NOT_SET;
	public int end = POS_NOT_SET;
	public TypeBinding exceptionType;
public ExceptionLabel(CodeStream codeStream, TypeBinding exceptionType) {
	super(codeStream);
	this.exceptionType = exceptionType;
	this.start = codeStream.position;	
}
public boolean isStandardLabel(){
	return false;
}
public void place() {
	// register the handler inside the codeStream then normal place
	codeStream.registerExceptionHandler(this);
	super.place();

}
public void placeEnd() {
	end = codeStream.position;
}
}
