package org.eclipse.jdt.internal.compiler.codegen;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.problem.*;

public class CaseLabel extends Label {
	public int instructionPosition = POS_NOT_SET;
	public int backwardsBranch = POS_NOT_SET;
/**
 * CaseLabel constructor comment.
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public CaseLabel(CodeStream codeStream) {
	super(codeStream);
}
/*
* Put down  a refernece to the array at the location in the codestream.
*/
void branch() {
	if (position == POS_NOT_SET) {
		addForwardReference(codeStream.position);
		// Leave 4 bytes free to generate the jump offset afterwards
		codeStream.position += 4;
		codeStream.classFileOffset += 4;
	} else { //Position is set. Write it!
		codeStream.writeSignedWord(position - codeStream.position + 1);
	}
}
/*
* Put down  a refernece to the array at the location in the codestream.
*/
void branchWide() {
	if (position == POS_NOT_SET) {
		addForwardReference(codeStream.position);
		// Leave 4 bytes free to generate the jump offset afterwards
		codeStream.position += 4;
	} else { //Position is set. Write it!
		codeStream.writeSignedWord(position - codeStream.position + 1);
	}
}
public boolean isStandardLabel(){
	return false;
}
/*
* Put down  a refernece to the array at the location in the codestream.
*/
public void place() {
	position = codeStream.position;
	if (instructionPosition == POS_NOT_SET)
		backwardsBranch = position;
	else {
		int offset = position - instructionPosition;
		for (int i = 0; i < forwardReferenceCount; i++) {
			codeStream.writeSignedWord(forwardReferences[i], offset);
		}
		// add the label int the codeStream labels collection
		codeStream.addLabel(this);
	}
}
/*
* Put down  a refernece to the array at the location in the codestream.
*/
void placeInstruction() {
	if (instructionPosition == POS_NOT_SET) {
		instructionPosition = codeStream.position;
		if (backwardsBranch != POS_NOT_SET) {
			int offset = backwardsBranch - instructionPosition;
			for (int i = 0; i < forwardReferenceCount; i++)
				codeStream.writeSignedWord(forwardReferences[i], offset);
			backwardsBranch = POS_NOT_SET;
		}
	}
}
}
