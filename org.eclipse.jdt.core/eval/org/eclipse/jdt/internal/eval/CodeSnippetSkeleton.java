package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * The skeleton of the class 'org.eclipse.jdt.internal.eval.target.CodeSnippet'
 * used at compile time. Note that the method run() is declared to
 * throw Throwable so that the user can write a code snipet that
 * throws checked exceptio without having to catch those.
 */
public class CodeSnippetSkeleton implements IBinaryType, EvaluationConstants {
	IBinaryMethod[] methods = new IBinaryMethod[] {
		new BinaryMethodSkeleton(
			"<init>"/*nonNLS*/.toCharArray(),
			"()V"/*nonNLS*/.toCharArray(),
			new char[][] {},
			true
		),
		new BinaryMethodSkeleton(
			"run"/*nonNLS*/.toCharArray(),
			"()V"/*nonNLS*/.toCharArray(),
			new char[][] {"java/lang/Throwable"/*nonNLS*/.toCharArray()},
			false
		),
		new BinaryMethodSkeleton(
			"setResult"/*nonNLS*/.toCharArray(),
			"(Ljava/lang/Object;Ljava/lang/Class;)V"/*nonNLS*/.toCharArray(),
			new char[][] {},
			false
		)
	};

	public class BinaryMethodSkeleton implements IBinaryMethod {
		char[][] exceptionTypeNames;
		char[] methodDescriptor;
		char[] selector;
		boolean isConstructor;
		
		public BinaryMethodSkeleton(char[] selector, char[] methodDescriptor, char[][] exceptionTypeNames, boolean isConstructor) {
			this.selector = selector;
			this.methodDescriptor = methodDescriptor;
			this.exceptionTypeNames = exceptionTypeNames;
			this.isConstructor = this.isConstructor;
		}
		
		public char[][] getExceptionTypeNames() {
			return this.exceptionTypeNames;
		}
		
		public char[] getMethodDescriptor() {
			return this.methodDescriptor;
		}
		
		public int getModifiers() {
			return IConstants.AccPublic;
		}
		
		public char[] getSelector() {
			return this.selector;
		}
		
		public boolean isClinit() {
			return false;
		}
		
		public boolean isConstructor() {
			return this.isConstructor;
		}
	}
	
/**
 * CodeSnippetSkeleton constructor comment.
 */
public CodeSnippetSkeleton() {
	super();
}
public char[] getEnclosingTypeName() {
	return null;
}
public IBinaryField[] getFields() {
	return null;
}
public char[] getFileName() {
	return CharOperation.concat(CODE_SNIPPET_NAME, ".java"/*nonNLS*/.toCharArray());
}
public char[][] getInterfaceNames() {
	return null;
}
public IBinaryNestedType[] getMemberTypes() {
	return null;
}
public IBinaryMethod[] getMethods() {
	return this.methods;
}
public int getModifiers() {
	return IConstants.AccPublic;
}
public char[] getName() {
	return CODE_SNIPPET_NAME;
}
public char[] getSuperclassName() {
	return null;
}
public boolean isBinaryType() {
	return true;
}
public boolean isClass() {
	return true;
}
public boolean isInterface() {
	return false;
}
}
