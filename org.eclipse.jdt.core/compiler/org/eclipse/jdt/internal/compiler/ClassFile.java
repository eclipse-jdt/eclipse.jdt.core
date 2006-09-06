/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
import org.eclipse.jdt.internal.compiler.codegen.VerificationTypeInfo;
import org.eclipse.jdt.internal.compiler.codegen.StackMapFrame;
import org.eclipse.jdt.internal.compiler.codegen.StackMapFrameCodeStream;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Messages;

/**
 * Represents a class file wrapper on bytes, it is aware of its actual
 * type name.
 *
 * Public APIs are listed below:
 *
 * byte[] getBytes();
 *		Answer the actual bytes of the class file
 *
 * char[][] getCompoundName();
 * 		Answer the compound name of the class file.
 * 		For example, {{java}, {util}, {Hashtable}}.
 *
 * byte[] getReducedBytes();
 * 		Answer a smaller byte format, which is only contains some structural 
 *      information. Those bytes are decodable with a regular class file reader, 
 *      such as DietClassFileReader
 */
public class ClassFile
	implements TypeConstants, TypeIds {

	private byte[] bytes;
	public CodeStream codeStream;
	public ConstantPool constantPool;

	public int constantPoolOffset;

	// the header contains all the bytes till the end of the constant pool
	public byte[] contents;

	public int contentsOffset;

	protected boolean creatingProblemType;
	
	public ClassFile enclosingClassFile;
	public byte[] header;
	// that collection contains all the remaining bytes of the .class file
	public int headerOffset;
	public ReferenceBinding[] innerClassesBindings;
	public int methodCount;
	public int methodCountOffset;
	public int numberOfInnerClasses;
	// pool managment
	public boolean isShared = false;
	// used to generate private access methods
	// debug and stack map attributes
	public int produceAttributes;
	public SourceTypeBinding referenceBinding;
	public long targetJDK;
	public static final int INITIAL_CONTENTS_SIZE = 400;
	public static final int INITIAL_HEADER_SIZE = 1500;
	public static final int INNER_CLASSES_SIZE = 5;
	
	/**
	 * INTERNAL USE-ONLY
	 * Build all the directories and subdirectories corresponding to the packages names
	 * into the directory specified in parameters.
	 *
	 * outputPath is formed like:
	 *	   c:\temp\ the last character is a file separator
	 * relativeFileName is formed like:
	 *     java\lang\String.class *
	 * 
	 * @param outputPath java.lang.String
	 * @param relativeFileName java.lang.String
	 * @return java.lang.String
	 */
	public static String buildAllDirectoriesInto(
		String outputPath,
		String relativeFileName)
		throws IOException {
		char fileSeparatorChar = File.separatorChar;
		String fileSeparator = File.separator;
		File f;
		outputPath = outputPath.replace('/', fileSeparatorChar);
			// these could be optimized out if we normalized paths once and for
			// all
		relativeFileName = relativeFileName.replace('/', fileSeparatorChar);
		String outputDirPath, fileName;
		int separatorIndex = relativeFileName.lastIndexOf(fileSeparatorChar);
		if (separatorIndex == -1) {
			if (outputPath.endsWith(fileSeparator)) {
				outputDirPath = outputPath.substring(0, outputPath.length() - 1);
				fileName = outputPath + relativeFileName;
			} else {
				outputDirPath = outputPath;
				fileName = outputPath + fileSeparator + relativeFileName;
			}
		} else {
			if (outputPath.endsWith(fileSeparator)) {
				outputDirPath = outputPath + 
					relativeFileName.substring(0, separatorIndex);
				fileName = outputPath + relativeFileName;
			} else {
				outputDirPath = outputPath + fileSeparator +
					relativeFileName.substring(0, separatorIndex);
				fileName = outputPath + fileSeparator + relativeFileName;
			}
		}
		f = new File(outputDirPath);
		f.mkdirs();
		if (f.isDirectory()) {
			return fileName;
		} else {
			// the directory creation failed for some reason - retry using
			// a slower algorithm so as to refine the diagnostic
			if (outputPath.endsWith(fileSeparator)) {
				outputPath = outputPath.substring(0, outputPath.length() - 1);
			}
			f = new File(outputPath);
			boolean checkFileType = false;
			if (f.exists()) {
			  	checkFileType = true; // pre-existed
			} else {
				// we have to create that directory
				if (!f.mkdirs()) {
				  	if (f.exists()) {
				  	  	// someone else created f -- need to check its type
				  	  	checkFileType = true;
				  	} else {
				  	  	// no one could create f -- complain
	    				throw new IOException(Messages.bind(
	    					Messages.output_notValidAll, f.getAbsolutePath()));
				  	}
				}
			}
			if (checkFileType) {
			  	if (!f.isDirectory()) {
	    			throw new IOException(Messages.bind(
	    				Messages.output_isFile, f.getAbsolutePath()));
			  	}
			}
			StringBuffer outDir = new StringBuffer(outputPath);
			outDir.append(fileSeparator);
			StringTokenizer tokenizer =
				new StringTokenizer(relativeFileName, fileSeparator);
			String token = tokenizer.nextToken();
			while (tokenizer.hasMoreTokens()) {
				f = new File(outDir.append(token).append(fileSeparator).toString());
			  	checkFileType = false; // reset
				if (f.exists()) {
				  	checkFileType = true; // this is suboptimal, but it catches corner cases
				  						  // in which a regular file pre-exists
				} else {
				// we have to create that directory
	    			if (!f.mkdir()) {
	    			  	if (f.exists()) {
	    			  	  	// someone else created f -- need to check its type
	    			  	  	checkFileType = true;
	    			  	} else {
	    			  	  	// no one could create f -- complain
	        				throw new IOException(Messages.bind(
	        					Messages.output_notValid, 
	        						outDir.substring(outputPath.length() + 1, 
	        							outDir.length() - 1),
	        						outputPath));
	    			  	}
	    			}
				}
	    		if (checkFileType) {
	    		  	if (!f.isDirectory()) {
	        			throw new IOException(Messages.bind(
	        				Messages.output_isFile, f.getAbsolutePath()));
	    		  	}
	    		}
				token = tokenizer.nextToken();
			}
			// token contains the last one
			return outDir.append(token).toString();
		}
	}
	/**
	 * INTERNAL USE-ONLY
	 * Request the creation of a ClassFile compatible representation of a problematic type
	 *
	 * @param typeDeclaration org.eclipse.jdt.internal.compiler.ast.TypeDeclaration
	 * @param unitResult org.eclipse.jdt.internal.compiler.CompilationUnitResult
	 */
	public static void createProblemType(
		TypeDeclaration typeDeclaration,
		CompilationResult unitResult) {
		SourceTypeBinding typeBinding = typeDeclaration.binding;
		ClassFile classFile = ClassFile.getNewInstance(typeBinding);
		classFile.initialize(typeBinding, null, true);
	
		// TODO (olivier) handle cases where a field cannot be generated (name too long)
		// TODO (olivier) handle too many methods
		// inner attributes
		if (typeBinding.isMemberType())
			classFile.recordEnclosingTypeAttributes(typeBinding);
	
		// add its fields
		FieldBinding[] fields = typeBinding.fields();
		if ((fields != null) && (fields != Binding.NO_FIELDS)) {
			classFile.addFieldInfos();
		} else {
			// we have to set the number of fields to be equals to 0
			classFile.contents[classFile.contentsOffset++] = 0;
			classFile.contents[classFile.contentsOffset++] = 0;
		}
		// leave some space for the methodCount
		classFile.setForMethodInfos();
		// add its user defined methods
		int problemsLength;
		CategorizedProblem[] problems = unitResult.getErrors();
		if (problems == null) {
			problems = new CategorizedProblem[0];
		}
		CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
		System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
		
		AbstractMethodDeclaration[] methodDecls = typeDeclaration.methods;
		if (methodDecls != null) {
			if (typeBinding.isInterface()) {
				// we cannot create problem methods for an interface. So we have to generate a clinit
				// which should contain all the problem
				classFile.addProblemClinit(problemsCopy);
				for (int i = 0, length = methodDecls.length; i < length; i++) {
					AbstractMethodDeclaration methodDecl = methodDecls[i];
					MethodBinding method = methodDecl.binding;
					if (method == null || method.isConstructor()) continue;
					classFile.addAbstractMethod(methodDecl, method);
				}		
			} else {
				for (int i = 0, length = methodDecls.length; i < length; i++) {
					AbstractMethodDeclaration methodDecl = methodDecls[i];
					MethodBinding method = methodDecl.binding;
					if (method == null) continue;
					if (method.isConstructor()) {
						classFile.addProblemConstructor(methodDecl, method, problemsCopy);
					} else {
						classFile.addProblemMethod(methodDecl, method, problemsCopy);
					}
				}
			}
			// add abstract methods
			classFile.addDefaultAbstractMethods();
		}		
		
		// propagate generation of (problem) member types
		if (typeDeclaration.memberTypes != null) {
			for (int i = 0, max = typeDeclaration.memberTypes.length; i < max; i++) {
				TypeDeclaration memberType = typeDeclaration.memberTypes[i];
				if (memberType.binding != null) {
					classFile.recordNestedMemberAttribute(memberType.binding);
					ClassFile.createProblemType(memberType, unitResult);
				}
			}
		}
		classFile.addAttributes();
		unitResult.record(typeBinding.constantPoolName(), classFile);
	}
	public static ClassFile getNewInstance(SourceTypeBinding typeBinding) {
		LookupEnvironment env = typeBinding.scope.environment();
		return env.classFilePool.acquire(typeBinding);
	}
	/**
	 * INTERNAL USE-ONLY
	 * Search the line number corresponding to a specific position
	 */
	public static final int searchLineNumber(int[] startLineIndexes, int position) {
		// this code is completely useless, but it is the same implementation than
		// org.eclipse.jdt.internal.compiler.problem.ProblemHandler.searchLineNumber(int[], int)
		// if (startLineIndexes == null)
		//	return 1;
		int length = startLineIndexes.length;
		if (length == 0)
			return 1;
		int g = 0, d = length - 1;
		int m = 0, start;
		while (g <= d) {
			m = (g + d) / 2;
			if (position < (start = startLineIndexes[m])) {
				d = m - 1;
			} else if (position > start) {
				g = m + 1;
			} else {
				return m + 1;
			}
		}
		if (position < startLineIndexes[m]) {
			return m + 1;
		}
		return m + 2;
	}
	
	/**
	 * INTERNAL USE-ONLY
	 * outputPath is formed like:
	 *	   c:\temp\ the last character is a file separator
	 * relativeFileName is formed like:
	 *     java\lang\String.class
	 * @param generatePackagesStructure a flag to know if the packages structure has to be generated.
	 * @param outputPath the given output directory
	 * @param relativeFileName the given relative file name
	 * @param classFile the given classFile to write
	 * 
	 */
	public static void writeToDisk(
		boolean generatePackagesStructure,
		String outputPath,
		String relativeFileName,
		ClassFile classFile)
		throws IOException {
			
		BufferedOutputStream output = null;
		if (generatePackagesStructure) {
			output = new BufferedOutputStream(
				new FileOutputStream(
						new File(buildAllDirectoriesInto(outputPath, relativeFileName))));
		} else {
			String fileName = null;
			char fileSeparatorChar = File.separatorChar;
			String fileSeparator = File.separator;
			// First we ensure that the outputPath exists
			outputPath = outputPath.replace('/', fileSeparatorChar);
			// To be able to pass the mkdirs() method we need to remove the extra file separator at the end of the outDir name
			int indexOfPackageSeparator = relativeFileName.lastIndexOf(fileSeparatorChar);
			if (indexOfPackageSeparator == -1) {
				if (outputPath.endsWith(fileSeparator)) {
					fileName = outputPath + relativeFileName;
				} else {
					fileName = outputPath + fileSeparator + relativeFileName;
				}
			} else {
				int length = relativeFileName.length();
				if (outputPath.endsWith(fileSeparator)) {
					fileName = outputPath + relativeFileName.substring(indexOfPackageSeparator + 1, length);
				} else {
					fileName = outputPath + fileSeparator + relativeFileName.substring(indexOfPackageSeparator + 1, length);
				}
			}
			output = new BufferedOutputStream(
				new FileOutputStream(
						new File(fileName)));
		}
		try {
			output.write(classFile.header, 0, classFile.headerOffset);
			output.write(classFile.contents, 0, classFile.contentsOffset);
		} finally {
			output.flush();
			output.close();
		}
	}
	
	/**
	 * INTERNAL USE-ONLY
	 * This methods creates a new instance of the receiver.
	 */
	protected ClassFile() {
		// default constructor for subclasses
	}
	
	public ClassFile(SourceTypeBinding typeBinding) {
		// default constructor for subclasses
		this.constantPool = new ConstantPool(this);
		final CompilerOptions options = typeBinding.scope.compilerOptions();
		this.targetJDK = options.targetJDK;
		this.produceAttributes = options.produceDebugAttributes;
		this.referenceBinding = typeBinding;
		if (this.targetJDK >= ClassFileConstants.JDK1_6) {
			this.produceAttributes |= ClassFileConstants.ATTR_STACK_MAP;
			this.codeStream = new StackMapFrameCodeStream(this);
		} else {
			this.codeStream = new CodeStream(this);
		}
		this.initByteArrays();
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus method.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 */
	public void addAbstractMethod(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding) {

		// force the modifiers to be public and abstract
		methodBinding.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract;

		this.generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		int attributeNumber = this.generateMethodInfoAttribute(methodBinding);
		this.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods generate all the attributes for the receiver.
	 * For a class they could be:
	 * - source file attribute
	 * - inner classes attribute
	 * - deprecated attribute
	 */
	public void addAttributes() {
		// update the method count
		contents[methodCountOffset++] = (byte) (methodCount >> 8);
		contents[methodCountOffset] = (byte) methodCount;

		int attributeNumber = 0;
		// leave two bytes for the number of attributes and store the current offset
		int attributeOffset = contentsOffset;
		contentsOffset += 2;

		// source attribute
		if ((produceAttributes & ClassFileConstants.ATTR_SOURCE) != 0) {
			String fullFileName =
				new String(referenceBinding.scope.referenceCompilationUnit().getFileName());
			fullFileName = fullFileName.replace('\\', '/');
			int lastIndex = fullFileName.lastIndexOf('/');
			if (lastIndex != -1) {
				fullFileName = fullFileName.substring(lastIndex + 1, fullFileName.length());
			}
			// check that there is enough space to write all the bytes for the field info corresponding
			// to the @fieldBinding
			if (contentsOffset + 8 >= contents.length) {
				resizeContents(8);
			}
			int sourceAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SourceName);
			contents[contentsOffset++] = (byte) (sourceAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) sourceAttributeNameIndex;
			// The length of a source file attribute is 2. This is a fixed-length
			// attribute
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 2;
			// write the source file name
			int fileNameIndex = constantPool.literalIndex(fullFileName.toCharArray());
			contents[contentsOffset++] = (byte) (fileNameIndex >> 8);
			contents[contentsOffset++] = (byte) fileNameIndex;
			attributeNumber++;
		}
		// Deprecated attribute
		if (referenceBinding.isDeprecated()) {
			// check that there is enough space to write all the bytes for the field info corresponding
			// to the @fieldBinding
			if (contentsOffset + 6 >= contents.length) {
				resizeContents(6);
			}
			int deprecatedAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.DeprecatedName);
			contents[contentsOffset++] = (byte) (deprecatedAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) deprecatedAttributeNameIndex;
			// the length of a deprecated attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			attributeNumber++;
		}
		// Inner class attribute
		if (numberOfInnerClasses != 0) {
			// Generate the inner class attribute
			int exSize = 8 * numberOfInnerClasses + 8;
			if (exSize + contentsOffset >= this.contents.length) {
				resizeContents(exSize);
			}
			// Now we now the size of the attribute and the number of entries
			// attribute name
			int attributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.InnerClassName);
			contents[contentsOffset++] = (byte) (attributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) attributeNameIndex;
			int value = (numberOfInnerClasses << 3) + 2;
			contents[contentsOffset++] = (byte) (value >> 24);
			contents[contentsOffset++] = (byte) (value >> 16);
			contents[contentsOffset++] = (byte) (value >> 8);
			contents[contentsOffset++] = (byte) value;
			contents[contentsOffset++] = (byte) (numberOfInnerClasses >> 8);
			contents[contentsOffset++] = (byte) numberOfInnerClasses;
			for (int i = 0; i < numberOfInnerClasses; i++) {
				ReferenceBinding innerClass = innerClassesBindings[i];
				int accessFlags = innerClass.getAccessFlags();
				int innerClassIndex = constantPool.literalIndexForType(innerClass.constantPoolName());
				// inner class index
				contents[contentsOffset++] = (byte) (innerClassIndex >> 8);
				contents[contentsOffset++] = (byte) innerClassIndex;
				// outer class index: anonymous and local have no outer class index
				if (innerClass.isMemberType()) {
					// member or member of local
					int outerClassIndex = constantPool.literalIndexForType(innerClass.enclosingType().constantPoolName());
					contents[contentsOffset++] = (byte) (outerClassIndex >> 8);
					contents[contentsOffset++] = (byte) outerClassIndex;
				} else {
					// equals to 0 if the innerClass is not a member type
					contents[contentsOffset++] = 0;
					contents[contentsOffset++] = 0;
				}
				// name index
				if (!innerClass.isAnonymousType()) {
					int nameIndex = constantPool.literalIndex(innerClass.sourceName());
					contents[contentsOffset++] = (byte) (nameIndex >> 8);
					contents[contentsOffset++] = (byte) nameIndex;
				} else {
					// equals to 0 if the innerClass is an anonymous type
					contents[contentsOffset++] = 0;
					contents[contentsOffset++] = 0;
				}
				// access flag
				if (innerClass.isAnonymousType()) {
					accessFlags |= ClassFileConstants.AccPrivate;
				} else if (innerClass.isLocalType() && !innerClass.isMemberType()) {
					accessFlags |= ClassFileConstants.AccPrivate;
				} else if (innerClass.isMemberType() && innerClass.isInterface()) {
					accessFlags |= ClassFileConstants.AccStatic; // implicitely static
				}
				contents[contentsOffset++] = (byte) (accessFlags >> 8);
				contents[contentsOffset++] = (byte) accessFlags;
			}
			attributeNumber++;
		}
		// add signature attribute
		char[] genericSignature = referenceBinding.genericSignature();
		if (genericSignature != null) {
			// check that there is enough space to write all the bytes for the field info corresponding
			// to the @fieldBinding
			if (contentsOffset + 8 >= contents.length) {
				resizeContents(8);
			}
			int signatureAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SignatureName);
			contents[contentsOffset++] = (byte) (signatureAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) signatureAttributeNameIndex;
			// the length of a signature attribute is equals to 2
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 2;
			int signatureIndex =
				constantPool.literalIndex(genericSignature);
			contents[contentsOffset++] = (byte) (signatureIndex >> 8);
			contents[contentsOffset++] = (byte) signatureIndex;
			attributeNumber++;
		}
		if (targetJDK >= ClassFileConstants.JDK1_5
				&& this.referenceBinding.isNestedType()
				&& !this.referenceBinding.isMemberType()) {
			// add enclosing method attribute (1.5 mode only)
			if (contentsOffset + 10 >= contents.length) {
				resizeContents(10);
			}
			int enclosingMethodAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.EnclosingMethodName);
			contents[contentsOffset++] = (byte) (enclosingMethodAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) enclosingMethodAttributeNameIndex;
			// the length of a signature attribute is equals to 2
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 4;
			
			int enclosingTypeIndex = constantPool.literalIndexForType(this.referenceBinding.enclosingType().constantPoolName());
			contents[contentsOffset++] = (byte) (enclosingTypeIndex >> 8);
			contents[contentsOffset++] = (byte) enclosingTypeIndex;
			byte methodIndexByte1 = 0;
			byte methodIndexByte2 = 0;
			if (this.referenceBinding instanceof LocalTypeBinding) {
				MethodBinding methodBinding = ((LocalTypeBinding) this.referenceBinding).enclosingMethod;
				if (methodBinding != null) {
					int enclosingMethodIndex = constantPool.literalIndexForNameAndType(methodBinding.selector, methodBinding.signature());
					methodIndexByte1 = (byte) (enclosingMethodIndex >> 8);
					methodIndexByte2 = (byte) enclosingMethodIndex;
				}
			}
			contents[contentsOffset++] = methodIndexByte1;
			contents[contentsOffset++] = methodIndexByte2;
			attributeNumber++;			
		}
		if (this.targetJDK >= ClassFileConstants.JDK1_5 && !this.creatingProblemType) {
			TypeDeclaration typeDeclaration = referenceBinding.scope.referenceContext;
			if (typeDeclaration != null) {
				final Annotation[] annotations = typeDeclaration.annotations;
				if (annotations != null) {
					attributeNumber += generateRuntimeAnnotations(annotations);
				}
			}
		}
		
		if (this.referenceBinding.isHierarchyInconsistent()) {
			// add an attribute for inconsistent hierarchy
			if (contentsOffset + 6 >= contents.length) {
				resizeContents(6);
			}
			int inconsistentHierarchyNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.InconsistentHierarchy);
			contents[contentsOffset++] = (byte) (inconsistentHierarchyNameIndex >> 8);
			contents[contentsOffset++] = (byte) inconsistentHierarchyNameIndex;
			// the length of an inconsistent hierarchy attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			attributeNumber++;
		}
		// update the number of attributes
		if (attributeOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}
		contents[attributeOffset++] = (byte) (attributeNumber >> 8);
		contents[attributeOffset] = (byte) attributeNumber;

		// resynchronize all offsets of the classfile
		header = constantPool.poolContent;
		headerOffset = constantPool.currentOffset;
		int constantPoolCount = constantPool.currentIndex;
		header[constantPoolOffset++] = (byte) (constantPoolCount >> 8);
		header[constantPoolOffset] = (byte) constantPoolCount;
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods generate all the default abstract method infos that correpond to
	 * the abstract methods inherited from superinterfaces.
	 */
	public void addDefaultAbstractMethods() { // default abstract methods
		MethodBinding[] defaultAbstractMethods =
			referenceBinding.getDefaultAbstractMethods();
		for (int i = 0, max = defaultAbstractMethods.length; i < max; i++) {
			generateMethodInfoHeader(defaultAbstractMethods[i]);
			int methodAttributeOffset = contentsOffset;
			int attributeNumber = generateMethodInfoAttribute(defaultAbstractMethods[i]);
			completeMethodInfo(methodAttributeOffset, attributeNumber);
		}
	}
	
	private int addFieldAttributes(FieldBinding fieldBinding, int fieldAttributeOffset) {
		int attributesNumber = 0;
		// 4.7.2 only static constant fields get a ConstantAttribute
		// Generate the constantValueAttribute
		Constant fieldConstant = fieldBinding.constant();
		if (fieldConstant != Constant.NotAConstant){
			if (contentsOffset + 8 >= contents.length) {
				resizeContents(8);
			}
			// Now we generate the constant attribute corresponding to the fieldBinding
			int constantValueNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.ConstantValueName);
			contents[contentsOffset++] = (byte) (constantValueNameIndex >> 8);
			contents[contentsOffset++] = (byte) constantValueNameIndex;
			// The attribute length = 2 in case of a constantValue attribute
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 2;
			attributesNumber++;
			// Need to add the constant_value_index
			switch (fieldConstant.typeID()) {
				case T_boolean :
					int booleanValueIndex =
						constantPool.literalIndex(fieldConstant.booleanValue() ? 1 : 0);
					contents[contentsOffset++] = (byte) (booleanValueIndex >> 8);
					contents[contentsOffset++] = (byte) booleanValueIndex;
					break;
				case T_byte :
				case T_char :
				case T_int :
				case T_short :
					int integerValueIndex =
						constantPool.literalIndex(fieldConstant.intValue());
					contents[contentsOffset++] = (byte) (integerValueIndex >> 8);
					contents[contentsOffset++] = (byte) integerValueIndex;
					break;
				case T_float :
					int floatValueIndex =
						constantPool.literalIndex(fieldConstant.floatValue());
					contents[contentsOffset++] = (byte) (floatValueIndex >> 8);
					contents[contentsOffset++] = (byte) floatValueIndex;
					break;
				case T_double :
					int doubleValueIndex =
						constantPool.literalIndex(fieldConstant.doubleValue());
					contents[contentsOffset++] = (byte) (doubleValueIndex >> 8);
					contents[contentsOffset++] = (byte) doubleValueIndex;
					break;
				case T_long :
					int longValueIndex =
						constantPool.literalIndex(fieldConstant.longValue());
					contents[contentsOffset++] = (byte) (longValueIndex >> 8);
					contents[contentsOffset++] = (byte) longValueIndex;
					break;
				case T_JavaLangString :
					int stringValueIndex =
						constantPool.literalIndex(
							((StringConstant) fieldConstant).stringValue());
					if (stringValueIndex == -1) {
						if (!creatingProblemType) {
							// report an error and abort: will lead to a problem type classfile creation
							TypeDeclaration typeDeclaration = referenceBinding.scope.referenceContext;
							FieldDeclaration[] fieldDecls = typeDeclaration.fields;
							for (int i = 0, max = fieldDecls.length; i < max; i++) {
								if (fieldDecls[i].binding == fieldBinding) {
									// problem should abort
									typeDeclaration.scope.problemReporter().stringConstantIsExceedingUtf8Limit(
										fieldDecls[i]);
								}
							}
						} else {
							// already inside a problem type creation : no constant for this field
							contentsOffset = fieldAttributeOffset;
						}
					} else {
						contents[contentsOffset++] = (byte) (stringValueIndex >> 8);
						contents[contentsOffset++] = (byte) stringValueIndex;
					}
			}
		}
		if (this.targetJDK < ClassFileConstants.JDK1_5 && fieldBinding.isSynthetic()) {
			if (contentsOffset + 6 >= contents.length) {
				resizeContents(6);
			}
			int syntheticAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
			contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
			// the length of a synthetic attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			attributesNumber++;
		}
		if (fieldBinding.isDeprecated()) {
			if (contentsOffset + 6 >= contents.length) {
				resizeContents(6);
			}
			int deprecatedAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.DeprecatedName);
			contents[contentsOffset++] = (byte) (deprecatedAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) deprecatedAttributeNameIndex;
			// the length of a deprecated attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			attributesNumber++;
		}
		// add signature attribute
		char[] genericSignature = fieldBinding.genericSignature();
		if (genericSignature != null) {
			// check that there is enough space to write all the bytes for the field info corresponding
			// to the @fieldBinding
			if (contentsOffset + 8 >= contents.length) {
				resizeContents(8);
			}
			int signatureAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SignatureName);
			contents[contentsOffset++] = (byte) (signatureAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) signatureAttributeNameIndex;
			// the length of a signature attribute is equals to 2
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 2;
			int signatureIndex =
				constantPool.literalIndex(genericSignature);
			contents[contentsOffset++] = (byte) (signatureIndex >> 8);
			contents[contentsOffset++] = (byte) signatureIndex;
			attributesNumber++;
		}
		if (this.targetJDK >= ClassFileConstants.JDK1_5 && !this.creatingProblemType) {
			FieldDeclaration fieldDeclaration = fieldBinding.sourceField();
			if (fieldDeclaration != null) {
				Annotation[] annotations = fieldDeclaration.annotations;
				if (annotations != null) {
					attributesNumber += generateRuntimeAnnotations(annotations);
				}
			}
		}
		return attributesNumber;
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods generates the bytes for the given field binding
	 * @param fieldBinding the given field binding
	 */
	private void addFieldInfo(FieldBinding fieldBinding) {
		// check that there is enough space to write all the bytes for the field info corresponding
		// to the @fieldBinding
		if (contentsOffset + 8 >= contents.length) {
			resizeContents(8);
		}
		// Now we can generate all entries into the byte array
		// First the accessFlags
		int accessFlags = fieldBinding.getAccessFlags();
		if (targetJDK < ClassFileConstants.JDK1_5) {
		    // pre 1.5, synthetic was an attribute, not a modifier
		    accessFlags &= ~ClassFileConstants.AccSynthetic;
		}		
		contents[contentsOffset++] = (byte) (accessFlags >> 8);
		contents[contentsOffset++] = (byte) accessFlags;
		// Then the nameIndex
		int nameIndex = constantPool.literalIndex(fieldBinding.name);
		contents[contentsOffset++] = (byte) (nameIndex >> 8);
		contents[contentsOffset++] = (byte) nameIndex;
		// Then the descriptorIndex
		int descriptorIndex = constantPool.literalIndex(fieldBinding.type.signature());
		contents[contentsOffset++] = (byte) (descriptorIndex >> 8);
		contents[contentsOffset++] = (byte) descriptorIndex;
		int fieldAttributeOffset = contentsOffset;
		int attributeNumber = 0;
		// leave some space for the number of attributes
		contentsOffset += 2;
		attributeNumber += addFieldAttributes(fieldBinding, fieldAttributeOffset);
		if (contentsOffset + 2 >= contents.length) {
			resizeContents(2);
		}
		contents[fieldAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[fieldAttributeOffset] = (byte) attributeNumber;
	}
	/**
	 * INTERNAL USE-ONLY
	 * This methods generate all the fields infos for the receiver.
	 * This includes:
	 * - a field info for each defined field of that class
	 * - a field info for each synthetic field (e.g. this$0)
	 */
	/**
	 * INTERNAL USE-ONLY
	 * This methods generate all the fields infos for the receiver.
	 * This includes:
	 * - a field info for each defined field of that class
	 * - a field info for each synthetic field (e.g. this$0)
	 */
	public void addFieldInfos() {
		SourceTypeBinding currentBinding = referenceBinding;
		FieldBinding[] syntheticFields = currentBinding.syntheticFields();
		int fieldCount =
			currentBinding.fieldCount()
				+ (syntheticFields == null ? 0 : syntheticFields.length);

		// write the number of fields
		if (fieldCount > 0xFFFF) {
			referenceBinding.scope.problemReporter().tooManyFields(referenceBinding.scope.referenceType());
		}
		contents[contentsOffset++] = (byte) (fieldCount >> 8);
		contents[contentsOffset++] = (byte) fieldCount;

		FieldDeclaration[] fieldDecls = currentBinding.scope.referenceContext.fields;
		for (int i = 0, max = fieldDecls == null ? 0 : fieldDecls.length; i < max; i++) {
			FieldDeclaration fieldDecl = fieldDecls[i];
			if (fieldDecl.binding != null) {
				addFieldInfo(fieldDecl.binding);
			}
		}

		if (syntheticFields != null) {
			for (int i = 0, max = syntheticFields.length; i < max; i++) {
				addFieldInfo(syntheticFields[i]);
			}
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods stores the bindings for each inner class. They will be used to know which entries
	 * have to be generated for the inner classes attributes.
	 * @param refBinding org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding 
	 */
	private void addInnerClasses(ReferenceBinding refBinding) {
		// check first if that reference binding is there
		for (int i = 0; i < numberOfInnerClasses; i++) {
			if (innerClassesBindings[i] == refBinding)
				return;
		}
		int length = innerClassesBindings.length;
		if (numberOfInnerClasses == length) {
			System.arraycopy(
				innerClassesBindings,
				0,
				innerClassesBindings = new ReferenceBinding[length * 2],
				0,
				length);
		}
		innerClassesBindings[numberOfInnerClasses++] = refBinding;
	}

	private void addMissingAbstractProblemMethod(MethodDeclaration methodDeclaration, MethodBinding methodBinding, CategorizedProblem problem, CompilationResult compilationResult) {
		// always clear the strictfp/native/abstract bit for a problem method
		generateMethodInfoHeader(methodBinding, methodBinding.modifiers & ~(ClassFileConstants.AccStrictfp | ClassFileConstants.AccNative | ClassFileConstants.AccAbstract));
		int methodAttributeOffset = contentsOffset;
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		
		// Code attribute
		attributeNumber++;
		
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		StringBuffer buffer = new StringBuffer(25);
		buffer.append("\t"  + problem.getMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.insert(0, Messages.compilation_unresolvedProblem);
		String problemString = buffer.toString();
		
		codeStream.init(this);
		codeStream.preserveUnusedLocals = true;
		codeStream.initializeMaxLocals(methodBinding);

		// return codeStream.generateCodeAttributeForProblemMethod(comp.options.runtimeExceptionNameForCompileError, "")
		codeStream.generateCodeAttributeForProblemMethod(problemString);
				
		completeCodeAttributeForMissingAbstractProblemMethod(
			methodBinding,
			codeAttributeOffset,
			compilationResult.getLineSeparatorPositions(),
			problem.getSourceLineNumber());
			
		completeMethodInfo(methodAttributeOffset, attributeNumber);
	}
	
	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem clinit method info that correspond to a boggus method.
	 *
	 * @param problems org.eclipse.jdt.internal.compiler.problem.Problem[]
	 */
	public void addProblemClinit(CategorizedProblem[] problems) {
		generateMethodInfoHeaderForClinit();
		// leave two spaces for the number of attributes
		contentsOffset -= 2;
		int attributeOffset = contentsOffset;
		contentsOffset += 2;
		int attributeNumber = 0;

		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		codeStream.resetForProblemClinit(this);
		String problemString = "" ; //$NON-NLS-1$
		int problemLine = 0;
		if (problems != null) {
			int max = problems.length;
			StringBuffer buffer = new StringBuffer(25);
			int count = 0;
			for (int i = 0; i < max; i++) {
				CategorizedProblem problem = problems[i];
				if ((problem != null) && (problem.isError())) {
					buffer.append("\t"  +problem.getMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
					count++;
					if (problemLine == 0) {
						problemLine = problem.getSourceLineNumber();
					}
					problems[i] = null;
				}
			} // insert the top line afterwards, once knowing how many problems we have to consider
			if (count > 1) {
				buffer.insert(0, Messages.compilation_unresolvedProblems);
			} else {
				buffer.insert(0, Messages.compilation_unresolvedProblem);
			}
			problemString = buffer.toString();
		}

		// return codeStream.generateCodeAttributeForProblemMethod(comp.options.runtimeExceptionNameForCompileError, "")
		codeStream.generateCodeAttributeForProblemMethod(problemString);
		attributeNumber++; // code attribute
		completeCodeAttributeForClinit(
			codeAttributeOffset,
			problemLine);
		if (contentsOffset + 2 >= contents.length) {
			resizeContents(2);
		}
		contents[attributeOffset++] = (byte) (attributeNumber >> 8);
		contents[attributeOffset] = (byte) attributeNumber;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus constructor.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 * @param problems org.eclipse.jdt.internal.compiler.problem.Problem[]
	 */
	public void addProblemConstructor(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding,
		CategorizedProblem[] problems) {

		// always clear the strictfp/native/abstract bit for a problem method
		generateMethodInfoHeader(methodBinding, methodBinding.modifiers & ~(ClassFileConstants.AccStrictfp | ClassFileConstants.AccNative | ClassFileConstants.AccAbstract));
		int methodAttributeOffset = contentsOffset;
		int attributeNumber = generateMethodInfoAttribute(methodBinding, true);
		
		// Code attribute
		attributeNumber++;
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		codeStream.reset(method, this);
		String problemString = "" ; //$NON-NLS-1$
		int problemLine = 0;
		if (problems != null) {
			int max = problems.length;
			StringBuffer buffer = new StringBuffer(25);
			int count = 0;
			for (int i = 0; i < max; i++) {
				CategorizedProblem problem = problems[i];
				if ((problem != null) && (problem.isError())) {
					buffer.append("\t"  +problem.getMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
					count++;
					if (problemLine == 0) {
						problemLine = problem.getSourceLineNumber();
					}
				}
			} // insert the top line afterwards, once knowing how many problems we have to consider
			if (count > 1) {
				buffer.insert(0, Messages.compilation_unresolvedProblems);
			} else {
				buffer.insert(0, Messages.compilation_unresolvedProblem);
			}
			problemString = buffer.toString();
		}

		// return codeStream.generateCodeAttributeForProblemMethod(comp.options.runtimeExceptionNameForCompileError, "")
		codeStream.generateCodeAttributeForProblemMethod(problemString);
		completeCodeAttributeForProblemMethod(
			method,
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions(),
			problemLine);
		completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus constructor.
	 * Reset the position inside the contents byte array to the savedOffset.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 * @param problems org.eclipse.jdt.internal.compiler.problem.Problem[]
	 * @param savedOffset <CODE>int</CODE>
	 */
	public void addProblemConstructor(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding,
		CategorizedProblem[] problems,
		int savedOffset) {
		// we need to move back the contentsOffset to the value at the beginning of the method
		contentsOffset = savedOffset;
		methodCount--; // we need to remove the method that causes the problem
		addProblemConstructor(method, methodBinding, problems);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus method.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 * @param problems org.eclipse.jdt.internal.compiler.problem.Problem[]
	 */
	public void addProblemMethod(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding,
		CategorizedProblem[] problems) {
		if (methodBinding.isAbstract() && methodBinding.declaringClass.isInterface()) {
			method.abort(ProblemSeverities.AbortType, null);
		}
		// always clear the strictfp/native/abstract bit for a problem method
		generateMethodInfoHeader(methodBinding, methodBinding.modifiers & ~(ClassFileConstants.AccStrictfp | ClassFileConstants.AccNative | ClassFileConstants.AccAbstract));
		int methodAttributeOffset = contentsOffset;
		int attributeNumber = generateMethodInfoAttribute(methodBinding, true);
		
		// Code attribute
		attributeNumber++;
		
		int codeAttributeOffset = contentsOffset;
		generateCodeAttributeHeader();
		codeStream.reset(method, this);
		String problemString = "" ; //$NON-NLS-1$
		int problemLine = 0;
		if (problems != null) {
			int max = problems.length;
			StringBuffer buffer = new StringBuffer(25);
			int count = 0;
			for (int i = 0; i < max; i++) {
				CategorizedProblem problem = problems[i];
				if ((problem != null)
					&& (problem.isError())
					&& (problem.getSourceStart() >= method.declarationSourceStart)
					&& (problem.getSourceEnd() <= method.declarationSourceEnd)) {
					buffer.append("\t"  +problem.getMessage() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
					count++;
					if (problemLine == 0) {
						problemLine = problem.getSourceLineNumber();
					}
					problems[i] = null;
				}
			} // insert the top line afterwards, once knowing how many problems we have to consider
			if (count > 1) {
				buffer.insert(0, Messages.compilation_unresolvedProblems);
			} else {
				buffer.insert(0, Messages.compilation_unresolvedProblem);
			}
			problemString = buffer.toString();
		}

		// return codeStream.generateCodeAttributeForProblemMethod(comp.options.runtimeExceptionNameForCompileError, "")
		codeStream.generateCodeAttributeForProblemMethod(problemString);
		completeCodeAttributeForProblemMethod(
			method,
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions(),
			problemLine);
		completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a boggus method.
	 * Reset the position inside the contents byte array to the savedOffset.
	 *
	 * @param method org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.MethodBinding
	 * @param problems org.eclipse.jdt.internal.compiler.problem.Problem[]
	 * @param savedOffset <CODE>int</CODE>
	 */
	public void addProblemMethod(
		AbstractMethodDeclaration method,
		MethodBinding methodBinding,
		CategorizedProblem[] problems,
		int savedOffset) {
		// we need to move back the contentsOffset to the value at the beginning of the method
		contentsOffset = savedOffset;
		methodCount--; // we need to remove the method that causes the problem
		addProblemMethod(method, methodBinding, problems);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for all the special method infos.
	 * They are:
	 * - synthetic access methods
	 * - default abstract methods
	 */
	public void addSpecialMethods() {
	    
		// add all methods (default abstract methods and synthetic)

		// default abstract methods
		generateMissingAbstractMethods(referenceBinding.scope.referenceType().missingAbstractMethods, referenceBinding.scope.referenceCompilationUnit().compilationResult);

		MethodBinding[] defaultAbstractMethods = this.referenceBinding.getDefaultAbstractMethods();
		for (int i = 0, max = defaultAbstractMethods.length; i < max; i++) {
			generateMethodInfoHeader(defaultAbstractMethods[i]);
			int methodAttributeOffset = contentsOffset;
			int attributeNumber = generateMethodInfoAttribute(defaultAbstractMethods[i]);
			completeMethodInfo(methodAttributeOffset, attributeNumber);
		}
		// add synthetic methods infos
		SyntheticMethodBinding[] syntheticMethods = this.referenceBinding.syntheticMethods();
		if (syntheticMethods != null) {
			for (int i = 0, max = syntheticMethods.length; i < max; i++) {
				SyntheticMethodBinding syntheticMethod = syntheticMethods[i];
				switch (syntheticMethod.kind) {
					case SyntheticMethodBinding.FieldReadAccess :
						// generate a method info to emulate an reading access to
						// a non-accessible field
						addSyntheticFieldReadAccessMethod(syntheticMethod);
						break;
					case SyntheticMethodBinding.FieldWriteAccess :
						// generate a method info to emulate an writing access to
						// a non-accessible field
						addSyntheticFieldWriteAccessMethod(syntheticMethod);
						break;
					case SyntheticMethodBinding.MethodAccess :
					case SyntheticMethodBinding.SuperMethodAccess :
					case SyntheticMethodBinding.BridgeMethod :
						// generate a method info to emulate an access to a non-accessible method / super-method or bridge method
						addSyntheticMethodAccessMethod(syntheticMethod);
						break;
					case SyntheticMethodBinding.ConstructorAccess :
						// generate a method info to emulate an access to a non-accessible constructor
						addSyntheticConstructorAccessMethod(syntheticMethod);
						break;
					case SyntheticMethodBinding.EnumValues :
						// generate a method info to define <enum>#values()
						addSyntheticEnumValuesMethod(syntheticMethod);
						break;
					case SyntheticMethodBinding.EnumValueOf :
						// generate a method info to define <enum>#valueOf(String)
						addSyntheticEnumValueOfMethod(syntheticMethod);
						break;
					case SyntheticMethodBinding.SwitchTable :
						// generate a method info to define the switch table synthetic method
						addSyntheticSwitchTable(syntheticMethod);
				}
			}
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the bytes for a synthetic method that provides an access to a private constructor.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public void addSyntheticConstructorAccessMethod(SyntheticMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		// this will add exception attribute, synthetic attribute, deprecated attribute,...
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		attributeNumber++; // add code attribute
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForConstructorAccess(methodBinding);
		completeCodeAttributeForSyntheticMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions());
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;
	}
		
	/**
	 * INTERNAL USE-ONLY
	 *  Generate the bytes for a synthetic method that implements Enum#valueOf(String) for a given enum type
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */	
	public void addSyntheticEnumValueOfMethod(SyntheticMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		// this will add exception attribute, synthetic attribute, deprecated attribute,...
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		attributeNumber++; // add code attribute
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForEnumValueOf(methodBinding);
		completeCodeAttributeForSyntheticMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions());
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;			
	}

	/**
	 * INTERNAL USE-ONLY
	 *  Generate the bytes for a synthetic method that implements Enum#values() for a given enum type
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */	
	public void addSyntheticEnumValuesMethod(SyntheticMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		// this will add exception attribute, synthetic attribute, deprecated attribute,...
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		attributeNumber++; // add code attribute
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForEnumValues(methodBinding);
		completeCodeAttributeForSyntheticMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions());
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;		
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a synthetic method that
	 * generate an read access to a private field.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public void addSyntheticFieldReadAccessMethod(SyntheticMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		// this will add exception attribute, synthetic attribute, deprecated attribute,...
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		attributeNumber++; // add code attribute
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForFieldReadAccess(methodBinding);
		completeCodeAttributeForSyntheticMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions());
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;
	}
	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for a problem method info that correspond to a synthetic method that
	 * generate an write access to a private field.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public void addSyntheticFieldWriteAccessMethod(SyntheticMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		// this will add exception attribute, synthetic attribute, deprecated attribute,...
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		attributeNumber++; // add code attribute
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForFieldWriteAccess(methodBinding);
		completeCodeAttributeForSyntheticMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions());
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the bytes for a synthetic method that provides access to a private method.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.nameloopkup.SyntheticAccessMethodBinding
	 */
	public void addSyntheticMethodAccessMethod(SyntheticMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		// this will add exception attribute, synthetic attribute, deprecated attribute,...
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		attributeNumber++; // add code attribute
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForMethodAccess(methodBinding);
		completeCodeAttributeForSyntheticMethod(
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions());
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;
	}

	public void addSyntheticSwitchTable(SyntheticMethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding);
		int methodAttributeOffset = this.contentsOffset;
		// this will add exception attribute, synthetic attribute, deprecated attribute,...
		int attributeNumber = generateMethodInfoAttribute(methodBinding);
		// Code attribute
		int codeAttributeOffset = contentsOffset;
		attributeNumber++; // add code attribute
		generateCodeAttributeHeader();
		codeStream.init(this);
		codeStream.generateSyntheticBodyForSwitchTable(methodBinding);
		completeCodeAttributeForSyntheticMethod(
			true,
			methodBinding,
			codeAttributeOffset,
			((SourceTypeBinding) methodBinding.declaringClass)
				.scope
				.referenceCompilationUnit()
				.compilationResult
				.getLineSeparatorPositions());
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param codeAttributeOffset <CODE>int</CODE>
	 */
	public void completeCodeAttribute(int codeAttributeOffset) {
		// reinitialize the localContents with the byte modified by the code stream
		this.contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside localContents byte array before we started to write
		// any information about the codeAttribute
		// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset
		// to get the right position, 6 for the max_stack etc...
		int code_length = codeStream.position;
		if (code_length > 65535) {
			codeStream.methodDeclaration.scope.problemReporter().bytecodeExceeds64KLimit(
				codeStream.methodDeclaration);
		}
		if (localContentsOffset + 20 >= this.contents.length) {
			resizeContents(20);
		}
		int max_stack = codeStream.stackMax;
		this.contents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		this.contents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		this.contents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		this.contents[codeAttributeOffset + 9] = (byte) max_locals;
		this.contents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		this.contents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		this.contents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		this.contents[codeAttributeOffset + 13] = (byte) code_length;

		// write the exception table
		ExceptionLabel[] exceptionLabels = codeStream.exceptionLabels;
		int exceptionHandlersCount = 0; // each label holds one handler per range (start/end contiguous)
		for (int i = 0, length = codeStream.exceptionLabelsCounter; i < length; i++) {
			exceptionHandlersCount += codeStream.exceptionLabels[i].count / 2; 
		}
		int exSize = exceptionHandlersCount * 8 + 2;
		if (exSize + localContentsOffset >= this.contents.length) {
			resizeContents(exSize);
		}
		// there is no exception table, so we need to offset by 2 the current offset and move 
		// on the attribute generation
		this.contents[localContentsOffset++] = (byte) (exceptionHandlersCount >> 8);
		this.contents[localContentsOffset++] = (byte) exceptionHandlersCount;
		for (int i = 0, max = codeStream.exceptionLabelsCounter; i < max; i++) {
			ExceptionLabel exceptionLabel = exceptionLabels[i];
			if (exceptionLabel != null) {
				int iRange = 0, maxRange = exceptionLabel.count;
				if ((maxRange & 1) != 0) {
					codeStream.methodDeclaration.scope.problemReporter().abortDueToInternalError(
							Messages.bind(Messages.abort_invalidExceptionAttribute, new String(codeStream.methodDeclaration.selector)), 
							codeStream.methodDeclaration);
				}
				while  (iRange < maxRange) {
					int start = exceptionLabel.ranges[iRange++]; // even ranges are start positions
					this.contents[localContentsOffset++] = (byte) (start >> 8);
					this.contents[localContentsOffset++] = (byte) start;
					int end = exceptionLabel.ranges[iRange++]; // odd ranges are end positions
					this.contents[localContentsOffset++] = (byte) (end >> 8);
					this.contents[localContentsOffset++] = (byte) end;
					int handlerPC = exceptionLabel.position;
					this.contents[localContentsOffset++] = (byte) (handlerPC >> 8);
					this.contents[localContentsOffset++] = (byte) handlerPC;
					if (exceptionLabel.exceptionType == null) {
						// any exception handler
						this.contents[localContentsOffset++] = 0;
						this.contents[localContentsOffset++] = 0;
					} else {
						int nameIndex;
						if (exceptionLabel.exceptionType == TypeBinding.NULL) {
							/* represents ClassNotFoundException, see class literal access*/
							nameIndex = constantPool.literalIndexForType(ConstantPool.JavaLangClassNotFoundExceptionConstantPoolName);
						} else {
							nameIndex = constantPool.literalIndexForType(exceptionLabel.exceptionType.constantPoolName());
						}
						this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
						this.contents[localContentsOffset++] = (byte) nameIndex;
					}
				}
			}
		}
		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0;
		// leave two bytes for the attribute_length
		localContentsOffset += 2;
		if (localContentsOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}

		// first we handle the linenumber attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_LINES) != 0) {
			/* Create and add the line number attribute (used for debugging) 
			 * Build the pairs of:
			 * 	(bytecodePC lineNumber)
			 * according to the table of start line indexes and the pcToSourceMap table
			 * contained into the codestream
			 */
			int[] pcToSourceMapTable;
			if (((pcToSourceMapTable = codeStream.pcToSourceMap) != null)
				&& (codeStream.pcToSourceMapSize != 0)) {
				int lineNumberNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				this.contents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) lineNumberNameIndex;
				int lineNumberTableOffset = localContentsOffset;
				localContentsOffset += 6;
				// leave space for attribute_length and line_number_table_length
				int numberOfEntries = 0;
				int length = codeStream.pcToSourceMapSize;
				for (int i = 0; i < length;) {
					// write the entry
					if (localContentsOffset + 4 >= this.contents.length) {
						resizeContents(4);
					}
					int pc = pcToSourceMapTable[i++];
					this.contents[localContentsOffset++] = (byte) (pc >> 8);
					this.contents[localContentsOffset++] = (byte) pc;
					int lineNumber = pcToSourceMapTable[i++];
					this.contents[localContentsOffset++] = (byte) (lineNumber >> 8);
					this.contents[localContentsOffset++] = (byte) lineNumber;
					numberOfEntries++;
				}
				// now we change the size of the line number attribute
				int lineNumberAttr_length = numberOfEntries * 4 + 2;
				this.contents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 24);
				this.contents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 16);
				this.contents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 8);
				this.contents[lineNumberTableOffset++] = (byte) lineNumberAttr_length;
				this.contents[lineNumberTableOffset++] = (byte) (numberOfEntries >> 8);
				this.contents[lineNumberTableOffset++] = (byte) numberOfEntries;
				attributeNumber++;
			}
		}
		// then we do the local variable attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_VARS) != 0) {
			int numberOfEntries = 0;
			int localVariableNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
			final boolean methodDeclarationIsStatic = codeStream.methodDeclaration.isStatic();
			int maxOfEntries = 8 + 10 * (methodDeclarationIsStatic ? 0 : 1);
			for (int i = 0; i < codeStream.allLocalsCounter; i++) {
				maxOfEntries += 10 * codeStream.locals[i].initializationCount;
			}
			// reserve enough space
			if (localContentsOffset + maxOfEntries >= this.contents.length) {
				resizeContents(maxOfEntries);
			}
			this.contents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
			this.contents[localContentsOffset++] = (byte) localVariableNameIndex;
			int localVariableTableOffset = localContentsOffset;
			// leave space for attribute_length and local_variable_table_length
			localContentsOffset += 6;
			int nameIndex;
			int descriptorIndex;
			SourceTypeBinding declaringClassBinding = null;
			if (!methodDeclarationIsStatic) {
				numberOfEntries++;
				this.contents[localContentsOffset++] = 0; // the startPC for this is always 0
				this.contents[localContentsOffset++] = 0;
				this.contents[localContentsOffset++] = (byte) (code_length >> 8);
				this.contents[localContentsOffset++] = (byte) code_length;
				nameIndex = constantPool.literalIndex(ConstantPool.This);
				this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) nameIndex;
				declaringClassBinding = (SourceTypeBinding) codeStream.methodDeclaration.binding.declaringClass;
				descriptorIndex =
					constantPool.literalIndex(
						declaringClassBinding.signature());
				this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
				this.contents[localContentsOffset++] = (byte) descriptorIndex;
				this.contents[localContentsOffset++] = 0;// the resolved position for this is always 0
				this.contents[localContentsOffset++] = 0;
			}
			// used to remember the local variable with a generic type
			int genericLocalVariablesCounter = 0;
			LocalVariableBinding[] genericLocalVariables = null;
			int numberOfGenericEntries = 0;
			
			for (int i = 0, max = codeStream.allLocalsCounter; i < max; i++) {
				LocalVariableBinding localVariable = codeStream.locals[i];
				final TypeBinding localVariableTypeBinding = localVariable.type;
				boolean isParameterizedType = localVariableTypeBinding.isParameterizedType() || localVariableTypeBinding.isTypeVariable();
				if (localVariable.initializationCount != 0 && isParameterizedType) {
					if (genericLocalVariables == null) {
						// we cannot have more than max locals
						genericLocalVariables = new LocalVariableBinding[max];
					}
					genericLocalVariables[genericLocalVariablesCounter++] = localVariable;
				}
				for (int j = 0; j < localVariable.initializationCount; j++) {
					int startPC = localVariable.initializationPCs[j << 1];
					int endPC = localVariable.initializationPCs[(j << 1) + 1];
					if (startPC != endPC) { // only entries for non zero length
						if (endPC == -1) {
							localVariable.declaringScope.problemReporter().abortDueToInternalError(
									Messages.bind(Messages.abort_invalidAttribute, new String(localVariable.name)), 
									(ASTNode) localVariable.declaringScope.methodScope().referenceContext);
						}
						if (isParameterizedType) {
							numberOfGenericEntries++;
						}
						// now we can safely add the local entry
						numberOfEntries++;
						this.contents[localContentsOffset++] = (byte) (startPC >> 8);
						this.contents[localContentsOffset++] = (byte) startPC;
						int length = endPC - startPC;
						this.contents[localContentsOffset++] = (byte) (length >> 8);
						this.contents[localContentsOffset++] = (byte) length;
						nameIndex = constantPool.literalIndex(localVariable.name);
						this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
						this.contents[localContentsOffset++] = (byte) nameIndex;
						descriptorIndex = constantPool.literalIndex(localVariableTypeBinding.signature());
						this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
						this.contents[localContentsOffset++] = (byte) descriptorIndex;
						int resolvedPosition = localVariable.resolvedPosition;
						this.contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
						this.contents[localContentsOffset++] = (byte) resolvedPosition;
					}
				}
			}
			int value = numberOfEntries * 10 + 2;
			this.contents[localVariableTableOffset++] = (byte) (value >> 24);
			this.contents[localVariableTableOffset++] = (byte) (value >> 16);
			this.contents[localVariableTableOffset++] = (byte) (value >> 8);
			this.contents[localVariableTableOffset++] = (byte) value;
			this.contents[localVariableTableOffset++] = (byte) (numberOfEntries >> 8);
			this.contents[localVariableTableOffset] = (byte) numberOfEntries;
			attributeNumber++;
			
			final boolean currentInstanceIsGeneric = 
				!methodDeclarationIsStatic
				&& declaringClassBinding != null 
				&& declaringClassBinding.typeVariables != Binding.NO_TYPE_VARIABLES;
			if (genericLocalVariablesCounter != 0 || currentInstanceIsGeneric) {
				// add the local variable type table attribute
				numberOfGenericEntries += (currentInstanceIsGeneric ? 1 : 0);
				maxOfEntries = 8 + numberOfGenericEntries * 10;
				// reserve enough space
				if (localContentsOffset + maxOfEntries >= this.contents.length) {
					resizeContents(maxOfEntries);
				}
				int localVariableTypeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LocalVariableTypeTableName);
				this.contents[localContentsOffset++] = (byte) (localVariableTypeNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) localVariableTypeNameIndex;
				value = numberOfGenericEntries * 10 + 2;
				this.contents[localContentsOffset++] = (byte) (value >> 24);
				this.contents[localContentsOffset++] = (byte) (value >> 16);
				this.contents[localContentsOffset++] = (byte) (value >> 8);
				this.contents[localContentsOffset++] = (byte) value;
				this.contents[localContentsOffset++] = (byte) (numberOfGenericEntries >> 8);
				this.contents[localContentsOffset++] = (byte) numberOfGenericEntries;
				if (currentInstanceIsGeneric) {
					this.contents[localContentsOffset++] = 0; // the startPC for this is always 0
					this.contents[localContentsOffset++] = 0;
					this.contents[localContentsOffset++] = (byte) (code_length >> 8);
					this.contents[localContentsOffset++] = (byte) code_length;
					nameIndex = constantPool.literalIndex(ConstantPool.This);
					this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
					this.contents[localContentsOffset++] = (byte) nameIndex;
					descriptorIndex = constantPool.literalIndex(declaringClassBinding.genericTypeSignature());
					this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
					this.contents[localContentsOffset++] = (byte) descriptorIndex;
					this.contents[localContentsOffset++] = 0;// the resolved position for this is always 0
					this.contents[localContentsOffset++] = 0;
				}
				
				for (int i = 0; i < genericLocalVariablesCounter; i++) {
					LocalVariableBinding localVariable = genericLocalVariables[i];
					for (int j = 0; j < localVariable.initializationCount; j++) {
						int startPC = localVariable.initializationPCs[j << 1];
						int endPC = localVariable.initializationPCs[(j << 1) + 1];
						if (startPC != endPC) {
							// only entries for non zero length
							// now we can safely add the local entry
							this.contents[localContentsOffset++] = (byte) (startPC >> 8);
							this.contents[localContentsOffset++] = (byte) startPC;
							int length = endPC - startPC;
							this.contents[localContentsOffset++] = (byte) (length >> 8);
							this.contents[localContentsOffset++] = (byte) length;
							nameIndex = constantPool.literalIndex(localVariable.name);
							this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
							this.contents[localContentsOffset++] = (byte) nameIndex;
							descriptorIndex = constantPool.literalIndex(localVariable.type.genericTypeSignature());
							this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
							this.contents[localContentsOffset++] = (byte) descriptorIndex;
							int resolvedPosition = localVariable.resolvedPosition;
							this.contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
							this.contents[localContentsOffset++] = (byte) resolvedPosition;
						}
					}
				}
				attributeNumber++;
			}
		}

		if ((this.produceAttributes & ClassFileConstants.ATTR_STACK_MAP) != 0) {
			final Set framesPositions = ((StackMapFrameCodeStream) codeStream).framePositions;
			final int framesPositionsSize = framesPositions.size();
			int numberOfFrames = framesPositionsSize - 1; // -1 because last return doesn't count
			if (numberOfFrames > 0) {
				ArrayList framePositions = new ArrayList(framesPositionsSize);
				framePositions.addAll(framesPositions);
				Collections.sort(framePositions);
				int stackMapTableAttributeOffset = localContentsOffset;
				// add the stack map table attribute
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				int stackMapTableAttributeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.StackMapTableName);
				this.contents[localContentsOffset++] = (byte) (stackMapTableAttributeNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) stackMapTableAttributeNameIndex;
				
				int stackMapTableAttributeLengthOffset = localContentsOffset;
				// generate the attribute
				localContentsOffset += 4;
				if (localContentsOffset + 4 >= this.contents.length) {
					resizeContents(4);
				}
				numberOfFrames = 0;
				int numberOfFramesOffset = localContentsOffset;
				localContentsOffset += 2;
				if (localContentsOffset + 2 >= this.contents.length) {
					resizeContents(2);
				}
				ArrayList frames = ((StackMapFrameCodeStream) codeStream).frames;
				StackMapFrame currentFrame = (StackMapFrame) frames.get(0);
				StackMapFrame prevFrame = null;
				int framesSize = frames.size();
				int frameIndex = 0;
				for (int j = 0; j < framesPositionsSize && ((Integer) framePositions.get(j)).intValue() < code_length; j++) {
					// select next frame
					prevFrame = currentFrame;
					currentFrame = null;
					for (; frameIndex < framesSize; frameIndex++) {
						currentFrame = (StackMapFrame) frames.get(frameIndex);
						if (currentFrame.pc == ((Integer) framePositions.get(j)).intValue()) {
							break;
						}
					}
					if (currentFrame == null) break;
					// generate current frame
					// need to find differences between the current frame and the previous frame
					numberOfFrames++;
					int offsetDelta = currentFrame.getOffsetDelta(prevFrame);
					switch (currentFrame.getFrameType(prevFrame)) {
						case StackMapFrame.APPEND_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							int numberOfDifferentLocals = currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 + numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int index = currentFrame.getIndexOfDifferentLocals(numberOfDifferentLocals);
							int numberOfLocals = currentFrame.getNumberOfLocals();
							for (int i = index; i < currentFrame.locals.length && numberOfDifferentLocals > 0; i++) {
								if (localContentsOffset + 6 >= this.contents.length) {
									resizeContents(6);
								}
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfDifferentLocals--;
								}
							}
							break;
						case StackMapFrame.SAME_FRAME :
							if (localContentsOffset + 1 >= this.contents.length) {
								resizeContents(1);
							}							
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.SAME_FRAME_EXTENDED :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							this.contents[localContentsOffset++] = (byte) 251;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.CHOP_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							numberOfDifferentLocals = -currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 - numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;							
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS :
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[localContentsOffset++] = (byte) (offsetDelta + 64);
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										byte tag = (byte) info.tag;
										this.contents[localContentsOffset++] = tag;
										switch (tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS_EXTENDED :
							if (localContentsOffset + 6 >= this.contents.length) {
								resizeContents(6);
							}							
							this.contents[localContentsOffset++] = (byte) 247;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										byte tag = (byte) info.tag;
										this.contents[localContentsOffset++] = tag;
										switch (tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						default :
							// FULL_FRAME
							if (localContentsOffset + 5 >= this.contents.length) {
								resizeContents(5);
							}							
							this.contents[localContentsOffset++] = (byte) 255;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int numberOfLocalOffset = localContentsOffset;
							localContentsOffset += 2; // leave two spots for number of locals
							int numberOfLocalEntries = 0;
							numberOfLocals = currentFrame.getNumberOfLocals();
							int numberOfEntries = 0;
							int localsLength = currentFrame.locals == null ? 0 : currentFrame.locals.length;
							for (int i = 0; i < localsLength && numberOfLocalEntries < numberOfLocals; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}							
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfLocalEntries++;
								}
								numberOfEntries++;
							}
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[numberOfLocalOffset++] = (byte) (numberOfEntries >> 8);
							this.contents[numberOfLocalOffset] = (byte) numberOfEntries;
							int numberOfStackItems = currentFrame.numberOfStackItems;
							this.contents[localContentsOffset++] = (byte) (numberOfStackItems >> 8);
							this.contents[localContentsOffset++] = (byte) numberOfStackItems;
							for (int i = 0; i < numberOfStackItems; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}
								VerificationTypeInfo info = currentFrame.stackItems[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
								}
							}
					}
				}
				
				if (numberOfFrames != 0) {
					this.contents[numberOfFramesOffset++] = (byte) (numberOfFrames >> 8);
					this.contents[numberOfFramesOffset] = (byte) numberOfFrames;
	
					int attributeLength = localContentsOffset - stackMapTableAttributeLengthOffset - 4;
					this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 24);
					this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 16);
					this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 8);
					this.contents[stackMapTableAttributeLengthOffset] = (byte) attributeLength;
					attributeNumber++;
				} else {
					localContentsOffset = stackMapTableAttributeOffset;
				}
			}
		}

		this.contents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		this.contents[codeAttributeAttributeOffset] = (byte) attributeNumber;

		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		this.contents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		this.contents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		this.contents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		this.contents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param codeAttributeOffset <CODE>int</CODE>
	 */
	public void completeCodeAttributeForClinit(int codeAttributeOffset) {
		// reinitialize the contents with the byte modified by the code stream
		this.contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside contents byte array before we started to write
		// any information about the codeAttribute
		// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset
		// to get the right position, 6 for the max_stack etc...
		int code_length = codeStream.position;
		if (code_length > 65535) {
			codeStream.methodDeclaration.scope.problemReporter().bytecodeExceeds64KLimit(
				codeStream.methodDeclaration.scope.referenceType());
		}
		if (localContentsOffset + 20 >= this.contents.length) {
			resizeContents(20);
		}
		int max_stack = codeStream.stackMax;
		this.contents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		this.contents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		this.contents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		this.contents[codeAttributeOffset + 9] = (byte) max_locals;
		this.contents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		this.contents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		this.contents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		this.contents[codeAttributeOffset + 13] = (byte) code_length;

		// write the exception table
		ExceptionLabel[] exceptionLabels = codeStream.exceptionLabels;
		int exceptionHandlersCount = 0; // each label holds one handler per range (start/end contiguous)
		for (int i = 0, length = codeStream.exceptionLabelsCounter; i < length; i++) {
			exceptionHandlersCount += codeStream.exceptionLabels[i].count / 2; 
		}
		int exSize = exceptionHandlersCount * 8 + 2;
		if (exSize + localContentsOffset >= this.contents.length) {
			resizeContents(exSize);
		}
		// there is no exception table, so we need to offset by 2 the current offset and move 
		// on the attribute generation
		this.contents[localContentsOffset++] = (byte) (exceptionHandlersCount >> 8);
		this.contents[localContentsOffset++] = (byte) exceptionHandlersCount;
		for (int i = 0, max = codeStream.exceptionLabelsCounter; i < max; i++) {
			ExceptionLabel exceptionLabel = exceptionLabels[i];
			if (exceptionLabel != null) {
				int iRange = 0, maxRange = exceptionLabel.count;
				if ((maxRange & 1) != 0) {
					codeStream.methodDeclaration.scope.problemReporter().abortDueToInternalError(
							Messages.bind(Messages.abort_invalidExceptionAttribute, new String(codeStream.methodDeclaration.selector)), 
							codeStream.methodDeclaration);
				}				
				while  (iRange < maxRange) {
					int start = exceptionLabel.ranges[iRange++]; // even ranges are start positions
					this.contents[localContentsOffset++] = (byte) (start >> 8);
					this.contents[localContentsOffset++] = (byte) start;
					int end = exceptionLabel.ranges[iRange++]; // odd ranges are end positions
					this.contents[localContentsOffset++] = (byte) (end >> 8);
					this.contents[localContentsOffset++] = (byte) end;
					int handlerPC = exceptionLabel.position;
					this.contents[localContentsOffset++] = (byte) (handlerPC >> 8);
					this.contents[localContentsOffset++] = (byte) handlerPC;
					if (exceptionLabel.exceptionType == null) {
						// any exception handler
						this.contents[localContentsOffset++] = 0;
						this.contents[localContentsOffset++] = 0;
					} else {
						int nameIndex;
						if (exceptionLabel.exceptionType == TypeBinding.NULL) {
							/* represents denote ClassNotFoundException, see class literal access*/
							nameIndex = constantPool.literalIndexForType(ConstantPool.JavaLangClassNotFoundExceptionConstantPoolName);
						} else {
							nameIndex = constantPool.literalIndexForType(exceptionLabel.exceptionType.constantPoolName());
						}
						this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
						this.contents[localContentsOffset++] = (byte) nameIndex;
					}
				}
			}
		}
		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0;
		// leave two bytes for the attribute_length
		localContentsOffset += 2;
		if (localContentsOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}

		// first we handle the linenumber attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_LINES) != 0) {
			/* Create and add the line number attribute (used for debugging) 
			 * Build the pairs of:
			 * 	(bytecodePC lineNumber)
			 * according to the table of start line indexes and the pcToSourceMap table
			 * contained into the codestream
			 */
			int[] pcToSourceMapTable;
			if (((pcToSourceMapTable = codeStream.pcToSourceMap) != null)
				&& (codeStream.pcToSourceMapSize != 0)) {
				int lineNumberNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				this.contents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) lineNumberNameIndex;
				int lineNumberTableOffset = localContentsOffset;
				localContentsOffset += 6;
				// leave space for attribute_length and line_number_table_length
				int numberOfEntries = 0;
				int length = codeStream.pcToSourceMapSize;
				for (int i = 0; i < length;) {
					// write the entry
					if (localContentsOffset + 4 >= this.contents.length) {
						resizeContents(4);
					}
					int pc = pcToSourceMapTable[i++];
					this.contents[localContentsOffset++] = (byte) (pc >> 8);
					this.contents[localContentsOffset++] = (byte) pc;
					int lineNumber = pcToSourceMapTable[i++];
					this.contents[localContentsOffset++] = (byte) (lineNumber >> 8);
					this.contents[localContentsOffset++] = (byte) lineNumber;
					numberOfEntries++;
				}
				// now we change the size of the line number attribute
				int lineNumberAttr_length = numberOfEntries * 4 + 2;
				this.contents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 24);
				this.contents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 16);
				this.contents[lineNumberTableOffset++] = (byte) (lineNumberAttr_length >> 8);
				this.contents[lineNumberTableOffset++] = (byte) lineNumberAttr_length;
				this.contents[lineNumberTableOffset++] = (byte) (numberOfEntries >> 8);
				this.contents[lineNumberTableOffset++] = (byte) numberOfEntries;
				attributeNumber++;
			}
		}
		// then we do the local variable attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_VARS) != 0) {
			int numberOfEntries = 0;
			//		codeAttribute.addLocalVariableTableAttribute(this);
			if ((codeStream.pcToSourceMap != null)
				&& (codeStream.pcToSourceMapSize != 0)) {
				int localVariableNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				this.contents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) localVariableNameIndex;
				int localVariableTableOffset = localContentsOffset;
				localContentsOffset += 6;

				// leave space for attribute_length and local_variable_table_length
				int nameIndex;
				int descriptorIndex;

				// used to remember the local variable with a generic type
				int genericLocalVariablesCounter = 0;
				LocalVariableBinding[] genericLocalVariables = null;
				int numberOfGenericEntries = 0;

				for (int i = 0, max = codeStream.allLocalsCounter; i < max; i++) {
					LocalVariableBinding localVariable = codeStream.locals[i];
					final TypeBinding localVariableTypeBinding = localVariable.type;
					boolean isParameterizedType = localVariableTypeBinding.isParameterizedType() || localVariableTypeBinding.isTypeVariable();
					if (localVariable.initializationCount != 0 && isParameterizedType) {
						if (genericLocalVariables == null) {
							// we cannot have more than max locals
							genericLocalVariables = new LocalVariableBinding[max];
						}
						genericLocalVariables[genericLocalVariablesCounter++] = localVariable;
					}
					for (int j = 0; j < localVariable.initializationCount; j++) {
						int startPC = localVariable.initializationPCs[j << 1];
						int endPC = localVariable.initializationPCs[(j << 1) + 1];
						if (startPC != endPC) { // only entries for non zero length
							if (endPC == -1) {
								localVariable.declaringScope.problemReporter().abortDueToInternalError(
									Messages.bind(Messages.abort_invalidAttribute, new String(localVariable.name)), 
									(ASTNode) localVariable.declaringScope.methodScope().referenceContext);
							}
							if (localContentsOffset + 10 >= this.contents.length) {
								resizeContents(10);
							}
							// now we can safely add the local entry
							numberOfEntries++;
							if (isParameterizedType) {
								numberOfGenericEntries++;
							}
							this.contents[localContentsOffset++] = (byte) (startPC >> 8);
							this.contents[localContentsOffset++] = (byte) startPC;
							int length = endPC - startPC;
							this.contents[localContentsOffset++] = (byte) (length >> 8);
							this.contents[localContentsOffset++] = (byte) length;
							nameIndex = constantPool.literalIndex(localVariable.name);
							this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
							this.contents[localContentsOffset++] = (byte) nameIndex;
							descriptorIndex = constantPool.literalIndex(localVariableTypeBinding.signature());
							this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
							this.contents[localContentsOffset++] = (byte) descriptorIndex;
							int resolvedPosition = localVariable.resolvedPosition;
							this.contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
							this.contents[localContentsOffset++] = (byte) resolvedPosition;
						}
					}
				}
				int value = numberOfEntries * 10 + 2;
				this.contents[localVariableTableOffset++] = (byte) (value >> 24);
				this.contents[localVariableTableOffset++] = (byte) (value >> 16);
				this.contents[localVariableTableOffset++] = (byte) (value >> 8);
				this.contents[localVariableTableOffset++] = (byte) value;
				this.contents[localVariableTableOffset++] = (byte) (numberOfEntries >> 8);
				this.contents[localVariableTableOffset] = (byte) numberOfEntries;
				attributeNumber++;

				if (genericLocalVariablesCounter != 0) {
					// add the local variable type table attribute
					// reserve enough space
					int maxOfEntries = 8 + numberOfGenericEntries * 10;

					if (localContentsOffset + maxOfEntries >= this.contents.length) {
						resizeContents(maxOfEntries);
					}
					int localVariableTypeNameIndex =
						constantPool.literalIndex(AttributeNamesConstants.LocalVariableTypeTableName);
					this.contents[localContentsOffset++] = (byte) (localVariableTypeNameIndex >> 8);
					this.contents[localContentsOffset++] = (byte) localVariableTypeNameIndex;
					value = numberOfGenericEntries * 10 + 2;
					this.contents[localContentsOffset++] = (byte) (value >> 24);
					this.contents[localContentsOffset++] = (byte) (value >> 16);
					this.contents[localContentsOffset++] = (byte) (value >> 8);
					this.contents[localContentsOffset++] = (byte) value;
					this.contents[localContentsOffset++] = (byte) (numberOfGenericEntries >> 8);
					this.contents[localContentsOffset++] = (byte) numberOfGenericEntries;
					for (int i = 0; i < genericLocalVariablesCounter; i++) {
						LocalVariableBinding localVariable = genericLocalVariables[i];
						for (int j = 0; j < localVariable.initializationCount; j++) {
							int startPC = localVariable.initializationPCs[j << 1];
							int endPC = localVariable.initializationPCs[(j << 1) + 1];
							if (startPC != endPC) { // only entries for non zero length
								// now we can safely add the local entry
								this.contents[localContentsOffset++] = (byte) (startPC >> 8);
								this.contents[localContentsOffset++] = (byte) startPC;
								int length = endPC - startPC;
								this.contents[localContentsOffset++] = (byte) (length >> 8);
								this.contents[localContentsOffset++] = (byte) length;
								nameIndex = constantPool.literalIndex(localVariable.name);
								this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
								this.contents[localContentsOffset++] = (byte) nameIndex;
								descriptorIndex = constantPool.literalIndex(localVariable.type.genericTypeSignature());
								this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
								this.contents[localContentsOffset++] = (byte) descriptorIndex;
								int resolvedPosition = localVariable.resolvedPosition;
								this.contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
								this.contents[localContentsOffset++] = (byte) resolvedPosition;
							}
						}
					}
					attributeNumber++;
				}
			}
		}
		
		if ((this.produceAttributes & ClassFileConstants.ATTR_STACK_MAP) != 0) {
			final Set framesPositions = ((StackMapFrameCodeStream) codeStream).framePositions;
			final int framesPositionsSize = framesPositions.size();
			int numberOfFrames = framesPositionsSize - 1; // -1 because last return doesn't count
			if (numberOfFrames > 0) {
				ArrayList framePositions = new ArrayList(framesPositionsSize);
				framePositions.addAll(framesPositions);
				Collections.sort(framePositions);
				// add the stack map table attribute
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				int stackMapTableAttributeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.StackMapTableName);
				this.contents[localContentsOffset++] = (byte) (stackMapTableAttributeNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) stackMapTableAttributeNameIndex;
				
				int stackMapTableAttributeLengthOffset = localContentsOffset;
				// generate the attribute
				localContentsOffset += 4;
				numberOfFrames = 0;
				int numberOfFramesOffset = localContentsOffset;
				localContentsOffset += 2;
				// generate all frames
				ArrayList frames = ((StackMapFrameCodeStream) codeStream).frames;
				StackMapFrame currentFrame = (StackMapFrame) frames.get(0);
				StackMapFrame prevFrame = null;
				int framesSize = frames.size();
				int frameIndex = 0;
				for (int j = 0; j < framesPositionsSize && ((Integer) framePositions.get(j)).intValue() < code_length; j++) {
					// select next frame
					prevFrame = currentFrame;
					currentFrame = null;
					for (; frameIndex < framesSize; frameIndex++) {
						currentFrame = (StackMapFrame) frames.get(frameIndex);
						if (currentFrame.pc == ((Integer) framePositions.get(j)).intValue()) {
							break;
						}
					}
					if (currentFrame == null) break;
					numberOfFrames++;
					int offsetDelta = currentFrame.getOffsetDelta(prevFrame);
					switch (currentFrame.getFrameType(prevFrame)) {
						case StackMapFrame.APPEND_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							int numberOfDifferentLocals = currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 + numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int index = currentFrame.getIndexOfDifferentLocals(numberOfDifferentLocals);
							int numberOfLocals = currentFrame.getNumberOfLocals();
							for (int i = index; i < currentFrame.locals.length && numberOfDifferentLocals > 0; i++) {
								if (localContentsOffset + 6 >= this.contents.length) {
									resizeContents(6);
								}
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfDifferentLocals--;
								}
							}
							break;
						case StackMapFrame.SAME_FRAME :
							if (localContentsOffset + 1 >= this.contents.length) {
								resizeContents(1);
							}							
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.SAME_FRAME_EXTENDED :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							this.contents[localContentsOffset++] = (byte) 251;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.CHOP_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							numberOfDifferentLocals = -currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 - numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;							
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS :
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[localContentsOffset++] = (byte) (offsetDelta + 64);
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS_EXTENDED :
							if (localContentsOffset + 6 >= this.contents.length) {
								resizeContents(6);
							}							
							this.contents[localContentsOffset++] = (byte) 247;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						default :
							// FULL_FRAME
							if (localContentsOffset + 5 >= this.contents.length) {
								resizeContents(5);
							}							
							this.contents[localContentsOffset++] = (byte) 255;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int numberOfLocalOffset = localContentsOffset;
							localContentsOffset += 2; // leave two spots for number of locals
							int numberOfLocalEntries = 0;
							numberOfLocals = currentFrame.getNumberOfLocals();
							int numberOfEntries = 0;
							int localsLength = currentFrame.locals == null ? 0 : currentFrame.locals.length;
							for (int i = 0; i < localsLength && numberOfLocalEntries < numberOfLocals; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}							
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfLocalEntries++;
								}
								numberOfEntries++;
							}
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[numberOfLocalOffset++] = (byte) (numberOfEntries >> 8);
							this.contents[numberOfLocalOffset] = (byte) numberOfEntries;
							int numberOfStackItems = currentFrame.numberOfStackItems;
							this.contents[localContentsOffset++] = (byte) (numberOfStackItems >> 8);
							this.contents[localContentsOffset++] = (byte) numberOfStackItems;
							for (int i = 0; i < numberOfStackItems; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}
								VerificationTypeInfo info = currentFrame.stackItems[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
								}
							}
					}
				}
				
				this.contents[numberOfFramesOffset++] = (byte) (numberOfFrames >> 8);
				this.contents[numberOfFramesOffset] = (byte) numberOfFrames;

				int attributeLength = localContentsOffset - stackMapTableAttributeLengthOffset - 4;
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 24);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 16);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 8);
				this.contents[stackMapTableAttributeLengthOffset] = (byte) attributeLength;
				attributeNumber++;
			}
		}
		
		// update the number of attributes
		// ensure first that there is enough space available inside the contents array
		if (codeAttributeAttributeOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}
		this.contents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		this.contents[codeAttributeAttributeOffset] = (byte) attributeNumber;
		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		this.contents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		this.contents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		this.contents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		this.contents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 */
	public void completeCodeAttributeForClinit(
		int codeAttributeOffset,
		int problemLine) {
		// reinitialize the contents with the byte modified by the code stream
		this.contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside contents byte array before we started to write
		// any information about the codeAttribute
		// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset
		// to get the right position, 6 for the max_stack etc...
		int code_length = codeStream.position;
		if (code_length > 65535) {
			codeStream.methodDeclaration.scope.problemReporter().bytecodeExceeds64KLimit(
				codeStream.methodDeclaration.scope.referenceType());
		}
		if (localContentsOffset + 20 >= this.contents.length) {
			resizeContents(20);
		}
		int max_stack = codeStream.stackMax;
		this.contents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		this.contents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		this.contents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		this.contents[codeAttributeOffset + 9] = (byte) max_locals;
		this.contents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		this.contents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		this.contents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		this.contents[codeAttributeOffset + 13] = (byte) code_length;

		// write the exception table
		this.contents[localContentsOffset++] = 0;
		this.contents[localContentsOffset++] = 0;

		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0; // leave two bytes for the attribute_length
		localContentsOffset += 2; // first we handle the linenumber attribute
		if (localContentsOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}

		// first we handle the linenumber attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_LINES) != 0) {
			if (localContentsOffset + 20 >= this.contents.length) {
				resizeContents(20);
			}			
			/* Create and add the line number attribute (used for debugging) 
			    * Build the pairs of:
			    * (bytecodePC lineNumber)
			    * according to the table of start line indexes and the pcToSourceMap table
			    * contained into the codestream
			    */
			int lineNumberNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
			this.contents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
			this.contents[localContentsOffset++] = (byte) lineNumberNameIndex;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 6;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 1;
			// first entry at pc = 0
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = (byte) (problemLine >> 8);
			this.contents[localContentsOffset++] = (byte) problemLine;
			// now we change the size of the line number attribute
			attributeNumber++;
		}
		// then we do the local variable attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_VARS) != 0) {
			int localVariableNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
			if (localContentsOffset + 8 >= this.contents.length) {
				resizeContents(8);
			}
			this.contents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
			this.contents[localContentsOffset++] = (byte) localVariableNameIndex;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 2;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			attributeNumber++;
		}
		
		if ((this.produceAttributes & ClassFileConstants.ATTR_STACK_MAP) != 0) {
			final Set framesPositions = ((StackMapFrameCodeStream) codeStream).framePositions;
			final int framesPositionsSize = framesPositions.size();
			int numberOfFrames = framesPositionsSize - 1; // -1 because last return doesn't count
			if (numberOfFrames > 0) {
				ArrayList framePositions = new ArrayList(framesPositionsSize);
				framePositions.addAll(framesPositions);
				Collections.sort(framePositions);
				// add the stack map table attribute
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				int stackMapTableAttributeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.StackMapTableName);
				this.contents[localContentsOffset++] = (byte) (stackMapTableAttributeNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) stackMapTableAttributeNameIndex;
				
				int stackMapTableAttributeLengthOffset = localContentsOffset;
				// generate the attribute
				localContentsOffset += 4;
				numberOfFrames = 0;
				int numberOfFramesOffset = localContentsOffset;
				localContentsOffset += 2;
				// generate all frames
				ArrayList frames = ((StackMapFrameCodeStream) codeStream).frames;
				StackMapFrame currentFrame = (StackMapFrame) frames.get(0);
				StackMapFrame prevFrame = null;
				int framesSize = frames.size();
				int frameIndex = 0;
				for (int j = 0; j < framesPositionsSize && ((Integer) framePositions.get(j)).intValue() < code_length; j++) {
					// select next frame
					prevFrame = currentFrame;
					currentFrame = null;
					for (; frameIndex < framesSize; frameIndex++) {
						currentFrame = (StackMapFrame) frames.get(frameIndex);
						if (currentFrame.pc == ((Integer) framePositions.get(j)).intValue()) {
							break;
						}
					}
					if (currentFrame == null) break;
					// generate current frame
					// need to find differences between the current frame and the previous frame
					numberOfFrames++;
					int offsetDelta = currentFrame.getOffsetDelta(prevFrame);
					switch (currentFrame.getFrameType(prevFrame)) {
						case StackMapFrame.APPEND_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							int numberOfDifferentLocals = currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 + numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int index = currentFrame.getIndexOfDifferentLocals(numberOfDifferentLocals);
							int numberOfLocals = currentFrame.getNumberOfLocals();
							for (int i = index; i < currentFrame.locals.length && numberOfDifferentLocals > 0; i++) {
								if (localContentsOffset + 6 >= this.contents.length) {
									resizeContents(6);
								}
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfDifferentLocals--;
								}
							}
							break;
						case StackMapFrame.SAME_FRAME :
							if (localContentsOffset + 1 >= this.contents.length) {
								resizeContents(1);
							}							
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.SAME_FRAME_EXTENDED :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							this.contents[localContentsOffset++] = (byte) 251;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.CHOP_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							numberOfDifferentLocals = -currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 - numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;							
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS :
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[localContentsOffset++] = (byte) (offsetDelta + 64);
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS_EXTENDED :
							if (localContentsOffset + 6 >= this.contents.length) {
								resizeContents(6);
							}							
							this.contents[localContentsOffset++] = (byte) 247;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						default :
							// FULL_FRAME
							if (localContentsOffset + 5 >= this.contents.length) {
								resizeContents(5);
							}							
							this.contents[localContentsOffset++] = (byte) 255;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int numberOfLocalOffset = localContentsOffset;
							localContentsOffset += 2; // leave two spots for number of locals
							int numberOfLocalEntries = 0;
							numberOfLocals = currentFrame.getNumberOfLocals();
							int numberOfEntries = 0;
							int localsLength = currentFrame.locals == null ? 0 : currentFrame.locals.length;
							for (int i = 0; i < localsLength && numberOfLocalEntries < numberOfLocals; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}							
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfLocalEntries++;
								}
								numberOfEntries++;
							}
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[numberOfLocalOffset++] = (byte) (numberOfEntries >> 8);
							this.contents[numberOfLocalOffset] = (byte) numberOfEntries;
							int numberOfStackItems = currentFrame.numberOfStackItems;
							this.contents[localContentsOffset++] = (byte) (numberOfStackItems >> 8);
							this.contents[localContentsOffset++] = (byte) numberOfStackItems;
							for (int i = 0; i < numberOfStackItems; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}
								VerificationTypeInfo info = currentFrame.stackItems[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
								}
							}
					}
				}
				
				this.contents[numberOfFramesOffset++] = (byte) (numberOfFrames >> 8);
				this.contents[numberOfFramesOffset] = (byte) numberOfFrames;

				int attributeLength = localContentsOffset - stackMapTableAttributeLengthOffset - 4;
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 24);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 16);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 8);
				this.contents[stackMapTableAttributeLengthOffset] = (byte) attributeLength;
				attributeNumber++;
			}
		}
		
		// update the number of attributes
		// ensure first that there is enough space available inside the contents array
		if (codeAttributeAttributeOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}
		this.contents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		this.contents[codeAttributeAttributeOffset] = (byte) attributeNumber;
		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		this.contents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		this.contents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		this.contents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		this.contents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * 
	 */
	public void completeCodeAttributeForMissingAbstractProblemMethod(
		MethodBinding binding,
		int codeAttributeOffset,
		int[] startLineIndexes,
		int problemLine) {
		// reinitialize the localContents with the byte modified by the code stream
		this.contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside localContents byte array before we started to write// any information about the codeAttribute// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset// to get the right position, 6 for the max_stack etc...
		int max_stack = codeStream.stackMax;
		this.contents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		this.contents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		this.contents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		this.contents[codeAttributeOffset + 9] = (byte) max_locals;
		int code_length = codeStream.position;
		this.contents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		this.contents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		this.contents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		this.contents[codeAttributeOffset + 13] = (byte) code_length;
		// write the exception table
		if (localContentsOffset + 50 >= this.contents.length) {
			resizeContents(50);
		}
		this.contents[localContentsOffset++] = 0;
		this.contents[localContentsOffset++] = 0;
		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0; // leave two bytes for the attribute_length
		localContentsOffset += 2; // first we handle the linenumber attribute
		if (localContentsOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}

		if ((this.produceAttributes & ClassFileConstants.ATTR_LINES) != 0) {
			if (localContentsOffset + 12 >= this.contents.length) {
				resizeContents(12);
			}
			/* Create and add the line number attribute (used for debugging) 
			    * Build the pairs of:
			    * (bytecodePC lineNumber)
			    * according to the table of start line indexes and the pcToSourceMap table
			    * contained into the codestream
			    */
			int lineNumberNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
			this.contents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
			this.contents[localContentsOffset++] = (byte) lineNumberNameIndex;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 6;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 1;
			if (problemLine == 0) {
				problemLine = searchLineNumber(startLineIndexes, binding.sourceStart());
			}
			// first entry at pc = 0
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = (byte) (problemLine >> 8);
			this.contents[localContentsOffset++] = (byte) problemLine;
			// now we change the size of the line number attribute
			attributeNumber++;
		}

		if ((this.produceAttributes & ClassFileConstants.ATTR_STACK_MAP) != 0) {
			final Set framesPositions = ((StackMapFrameCodeStream) codeStream).framePositions;
			final int framesPositionsSize = framesPositions.size();
			int numberOfFrames = framesPositionsSize - 1; // -1 because last return doesn't count
			if (numberOfFrames > 0) {
				ArrayList framePositions = new ArrayList(framesPositionsSize);
				framePositions.addAll(framesPositions);
				Collections.sort(framePositions);
				// add the stack map table attribute
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				int stackMapTableAttributeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.StackMapTableName);
				this.contents[localContentsOffset++] = (byte) (stackMapTableAttributeNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) stackMapTableAttributeNameIndex;
				
				int stackMapTableAttributeLengthOffset = localContentsOffset;
				// generate the attribute
				localContentsOffset += 4;
				numberOfFrames = 0;
				int numberOfFramesOffset = localContentsOffset;
				localContentsOffset += 2;
				// generate all frames
				ArrayList frames = ((StackMapFrameCodeStream) codeStream).frames;
				StackMapFrame currentFrame = (StackMapFrame) frames.get(0);
				StackMapFrame prevFrame = null;
				int framesSize = frames.size();
				int frameIndex = 0;
				for (int j = 0; j < framesPositionsSize && ((Integer) framePositions.get(j)).intValue() < code_length; j++) {
					// select next frame
					prevFrame = currentFrame;
					currentFrame = null;
					for (; frameIndex < framesSize; frameIndex++) {
						currentFrame = (StackMapFrame) frames.get(frameIndex);
						if (currentFrame.pc == ((Integer) framePositions.get(j)).intValue()) {
							break;
						}
					}
					if (currentFrame == null) break;
					numberOfFrames++;
					int offsetDelta = currentFrame.getOffsetDelta(prevFrame);
					switch (currentFrame.getFrameType(prevFrame)) {
						case StackMapFrame.APPEND_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							int numberOfDifferentLocals = currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 + numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int index = currentFrame.getIndexOfDifferentLocals(numberOfDifferentLocals);
							int numberOfLocals = currentFrame.getNumberOfLocals();
							for (int i = index; i < currentFrame.locals.length && numberOfDifferentLocals > 0; i++) {
								if (localContentsOffset + 6 >= this.contents.length) {
									resizeContents(6);
								}
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfDifferentLocals--;
								}
							}
							break;
						case StackMapFrame.SAME_FRAME :
							if (localContentsOffset + 1 >= this.contents.length) {
								resizeContents(1);
							}							
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.SAME_FRAME_EXTENDED :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							this.contents[localContentsOffset++] = (byte) 251;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.CHOP_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							numberOfDifferentLocals = -currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 - numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;							
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS :
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[localContentsOffset++] = (byte) (offsetDelta + 64);
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS_EXTENDED :
							if (localContentsOffset + 6 >= this.contents.length) {
								resizeContents(6);
							}							
							this.contents[localContentsOffset++] = (byte) 247;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						default :
							// FULL_FRAME
							if (localContentsOffset + 5 >= this.contents.length) {
								resizeContents(5);
							}							
							this.contents[localContentsOffset++] = (byte) 255;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int numberOfLocalOffset = localContentsOffset;
							localContentsOffset += 2; // leave two spots for number of locals
							int numberOfLocalEntries = 0;
							numberOfLocals = currentFrame.getNumberOfLocals();
							int numberOfEntries = 0;
							int localsLength = currentFrame.locals == null ? 0 : currentFrame.locals.length;
							for (int i = 0; i < localsLength && numberOfLocalEntries < numberOfLocals; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}							
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfLocalEntries++;
								}
								numberOfEntries++;
							}
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[numberOfLocalOffset++] = (byte) (numberOfEntries >> 8);
							this.contents[numberOfLocalOffset] = (byte) numberOfEntries;
							int numberOfStackItems = currentFrame.numberOfStackItems;
							this.contents[localContentsOffset++] = (byte) (numberOfStackItems >> 8);
							this.contents[localContentsOffset++] = (byte) numberOfStackItems;
							for (int i = 0; i < numberOfStackItems; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}
								VerificationTypeInfo info = currentFrame.stackItems[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
								}
							}
					}
				}
				
				this.contents[numberOfFramesOffset++] = (byte) (numberOfFrames >> 8);
				this.contents[numberOfFramesOffset] = (byte) numberOfFrames;

				int attributeLength = localContentsOffset - stackMapTableAttributeLengthOffset - 4;
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 24);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 16);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 8);
				this.contents[stackMapTableAttributeLengthOffset] = (byte) attributeLength;
				attributeNumber++;
			}
		}
		
		// then we do the local variable attribute
		// update the number of attributes// ensure first that there is enough space available inside the localContents array
		if (codeAttributeAttributeOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}
		this.contents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		this.contents[codeAttributeAttributeOffset] = (byte) attributeNumber;
		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		this.contents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		this.contents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		this.contents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		this.contents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param codeAttributeOffset <CODE>int</CODE>
	 */
	public void completeCodeAttributeForProblemMethod(
		AbstractMethodDeclaration method,
		MethodBinding binding,
		int codeAttributeOffset,
		int[] startLineIndexes,
		int problemLine) {
		// reinitialize the localContents with the byte modified by the code stream
		this.contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside localContents byte array before we started to write// any information about the codeAttribute// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset// to get the right position, 6 for the max_stack etc...
		int max_stack = codeStream.stackMax;
		this.contents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		this.contents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		this.contents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		this.contents[codeAttributeOffset + 9] = (byte) max_locals;
		int code_length = codeStream.position;
		this.contents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		this.contents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		this.contents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		this.contents[codeAttributeOffset + 13] = (byte) code_length;
		// write the exception table
		if (localContentsOffset + 50 >= this.contents.length) {
			resizeContents(50);
		}

		// write the exception table
		this.contents[localContentsOffset++] = 0;
		this.contents[localContentsOffset++] = 0;
		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0; // leave two bytes for the attribute_length
		localContentsOffset += 2; // first we handle the linenumber attribute
		if (localContentsOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}

		if ((this.produceAttributes & ClassFileConstants.ATTR_LINES) != 0) {
			if (localContentsOffset + 20 >= this.contents.length) {
				resizeContents(20);
			}
			/* Create and add the line number attribute (used for debugging) 
			    * Build the pairs of:
			    * (bytecodePC lineNumber)
			    * according to the table of start line indexes and the pcToSourceMap table
			    * contained into the codestream
			    */
			int lineNumberNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
			this.contents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
			this.contents[localContentsOffset++] = (byte) lineNumberNameIndex;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 6;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 1;
			if (problemLine == 0) {
				problemLine = searchLineNumber(startLineIndexes, binding.sourceStart());
			}
			// first entry at pc = 0
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = 0;
			this.contents[localContentsOffset++] = (byte) (problemLine >> 8);
			this.contents[localContentsOffset++] = (byte) problemLine;
			// now we change the size of the line number attribute
			attributeNumber++;
		}
		// then we do the local variable attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_VARS) != 0) {
			// compute the resolved position for the arguments of the method
			int argSize;
			int numberOfEntries = 0;
			//		codeAttribute.addLocalVariableTableAttribute(this);
			int localVariableNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
			if (localContentsOffset + 8 >= this.contents.length) {
				resizeContents(8);
			}
			this.contents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
			this.contents[localContentsOffset++] = (byte) localVariableNameIndex;
			int localVariableTableOffset = localContentsOffset;
			localContentsOffset += 6;
			// leave space for attribute_length and local_variable_table_length
			int descriptorIndex;
			int nameIndex;
			SourceTypeBinding declaringClassBinding = null;
			final boolean methodDeclarationIsStatic = codeStream.methodDeclaration.isStatic();
			if (!methodDeclarationIsStatic) {
				numberOfEntries++;
				if (localContentsOffset + 10 >= this.contents.length) {
					resizeContents(10);
				}
				this.contents[localContentsOffset++] = 0;
				this.contents[localContentsOffset++] = 0;
				this.contents[localContentsOffset++] = (byte) (code_length >> 8);
				this.contents[localContentsOffset++] = (byte) code_length;
				nameIndex = constantPool.literalIndex(ConstantPool.This);
				this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) nameIndex;
				declaringClassBinding = (SourceTypeBinding) codeStream.methodDeclaration.binding.declaringClass;
				descriptorIndex =
					constantPool.literalIndex(declaringClassBinding.signature());
				this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
				this.contents[localContentsOffset++] = (byte) descriptorIndex;
				// the resolved position for this is always 0
				this.contents[localContentsOffset++] = 0;
				this.contents[localContentsOffset++] = 0;
			}
			// used to remember the local variable with a generic type
			int genericLocalVariablesCounter = 0;
			LocalVariableBinding[] genericLocalVariables = null;
			int numberOfGenericEntries = 0;
			
			if (binding.isConstructor()) {
				ReferenceBinding declaringClass = binding.declaringClass;
				if (declaringClass.isNestedType()) {
					NestedTypeBinding methodDeclaringClass = (NestedTypeBinding) declaringClass;
					argSize = methodDeclaringClass.enclosingInstancesSlotSize;
					SyntheticArgumentBinding[] syntheticArguments;
					if ((syntheticArguments = methodDeclaringClass.syntheticEnclosingInstances()) != null) {
						for (int i = 0, max = syntheticArguments.length; i < max; i++) {
							LocalVariableBinding localVariable = syntheticArguments[i];
							final TypeBinding localVariableTypeBinding = localVariable.type;
							if (localVariableTypeBinding.isParameterizedType() || localVariableTypeBinding.isTypeVariable()) {
								if (genericLocalVariables == null) {
									// we cannot have more than max locals
									genericLocalVariables = new LocalVariableBinding[max];
								}
								genericLocalVariables[genericLocalVariablesCounter++] = localVariable;
								numberOfGenericEntries++;								
							}
							if (localContentsOffset + 10 >= this.contents.length) {
								resizeContents(10);
							}
							// now we can safely add the local entry
							numberOfEntries++;
							this.contents[localContentsOffset++] = 0;
							this.contents[localContentsOffset++] = 0;
							this.contents[localContentsOffset++] = (byte) (code_length >> 8);
							this.contents[localContentsOffset++] = (byte) code_length;
							nameIndex = constantPool.literalIndex(localVariable.name);
							this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
							this.contents[localContentsOffset++] = (byte) nameIndex;
							descriptorIndex = constantPool.literalIndex(localVariableTypeBinding.signature());
							this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
							this.contents[localContentsOffset++] = (byte) descriptorIndex;
							int resolvedPosition = localVariable.resolvedPosition;
							this.contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
							this.contents[localContentsOffset++] = (byte) resolvedPosition;
						}
					}
				} else {
					argSize = 1;
				}
			} else {
				argSize = binding.isStatic() ? 0 : 1;
			}
			
			int genericArgumentsCounter = 0;
			int[] genericArgumentsNameIndexes = null;
			int[] genericArgumentsResolvedPositions = null;
			TypeBinding[] genericArgumentsTypeBindings = null;

			if (method.binding != null) {
				TypeBinding[] parameters = method.binding.parameters;
				Argument[] arguments = method.arguments;
				if ((parameters != null) && (arguments != null)) {
					for (int i = 0, max = parameters.length; i < max; i++) {
						TypeBinding argumentBinding = parameters[i];
						if (localContentsOffset + 10 >= this.contents.length) {
							resizeContents(10);
						}
						// now we can safely add the local entry
						numberOfEntries++;
						this.contents[localContentsOffset++] = 0;
						this.contents[localContentsOffset++] = 0;
						this.contents[localContentsOffset++] = (byte) (code_length >> 8);
						this.contents[localContentsOffset++] = (byte) code_length;
						nameIndex = constantPool.literalIndex(arguments[i].name);
						this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
						this.contents[localContentsOffset++] = (byte) nameIndex;
						int resolvedPosition = argSize;
						if (argumentBinding.isParameterizedType() || argumentBinding.isTypeVariable()) {
							if (genericArgumentsCounter == 0) {
								// we cannot have more than max locals
								genericArgumentsNameIndexes = new int[max];
								genericArgumentsResolvedPositions = new int[max];
								genericArgumentsTypeBindings = new TypeBinding[max];
							}
							genericArgumentsNameIndexes[genericArgumentsCounter] = nameIndex;
							genericArgumentsResolvedPositions[genericArgumentsCounter] = resolvedPosition;
							genericArgumentsTypeBindings[genericArgumentsCounter++] = argumentBinding;
						}
						descriptorIndex = constantPool.literalIndex(argumentBinding.signature());
						this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
						this.contents[localContentsOffset++] = (byte) descriptorIndex;
						if ((argumentBinding == TypeBinding.LONG)
							|| (argumentBinding == TypeBinding.DOUBLE))
							argSize += 2;
						else
							argSize++;
						this.contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
						this.contents[localContentsOffset++] = (byte) resolvedPosition;
					}
				}
			}
			int value = numberOfEntries * 10 + 2;
			this.contents[localVariableTableOffset++] = (byte) (value >> 24);
			this.contents[localVariableTableOffset++] = (byte) (value >> 16);
			this.contents[localVariableTableOffset++] = (byte) (value >> 8);
			this.contents[localVariableTableOffset++] = (byte) value;
			this.contents[localVariableTableOffset++] = (byte) (numberOfEntries >> 8);
			this.contents[localVariableTableOffset] = (byte) numberOfEntries;
			attributeNumber++;
			
			final boolean currentInstanceIsGeneric = 
				!methodDeclarationIsStatic
				&& declaringClassBinding != null
				&& declaringClassBinding.typeVariables != Binding.NO_TYPE_VARIABLES;
			if (genericLocalVariablesCounter != 0 || genericArgumentsCounter != 0 || currentInstanceIsGeneric) {
				// add the local variable type table attribute
				numberOfEntries = numberOfGenericEntries + genericArgumentsCounter + (currentInstanceIsGeneric ? 1 : 0);
				// reserve enough space
				int maxOfEntries = 8 + numberOfEntries * 10;
				if (localContentsOffset + maxOfEntries >= this.contents.length) {
					resizeContents(maxOfEntries);
				}
				int localVariableTypeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LocalVariableTypeTableName);
				this.contents[localContentsOffset++] = (byte) (localVariableTypeNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) localVariableTypeNameIndex;
				value = numberOfEntries * 10 + 2;
				this.contents[localContentsOffset++] = (byte) (value >> 24);
				this.contents[localContentsOffset++] = (byte) (value >> 16);
				this.contents[localContentsOffset++] = (byte) (value >> 8);
				this.contents[localContentsOffset++] = (byte) value;
				this.contents[localContentsOffset++] = (byte) (numberOfEntries >> 8);
				this.contents[localContentsOffset++] = (byte) numberOfEntries;
				if (currentInstanceIsGeneric) {
					numberOfEntries++;
					this.contents[localContentsOffset++] = 0; // the startPC for this is always 0
					this.contents[localContentsOffset++] = 0;
					this.contents[localContentsOffset++] = (byte) (code_length >> 8);
					this.contents[localContentsOffset++] = (byte) code_length;
					nameIndex = constantPool.literalIndex(ConstantPool.This);
					this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
					this.contents[localContentsOffset++] = (byte) nameIndex;
					descriptorIndex = constantPool.literalIndex(declaringClassBinding.genericTypeSignature());
					this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
					this.contents[localContentsOffset++] = (byte) descriptorIndex;
					this.contents[localContentsOffset++] = 0;// the resolved position for this is always 0
					this.contents[localContentsOffset++] = 0;
				}
				
				for (int i = 0; i < genericLocalVariablesCounter; i++) {
					LocalVariableBinding localVariable = genericLocalVariables[i];
					this.contents[localContentsOffset++] = 0;
					this.contents[localContentsOffset++] = 0;
					this.contents[localContentsOffset++] = (byte) (code_length >> 8);
					this.contents[localContentsOffset++] = (byte) code_length;
					nameIndex = constantPool.literalIndex(localVariable.name);
					this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
					this.contents[localContentsOffset++] = (byte) nameIndex;
					descriptorIndex = constantPool.literalIndex(localVariable.type.genericTypeSignature());
					this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
					this.contents[localContentsOffset++] = (byte) descriptorIndex;
					int resolvedPosition = localVariable.resolvedPosition;
					this.contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
					this.contents[localContentsOffset++] = (byte) resolvedPosition;
				}
				for (int i = 0; i < genericArgumentsCounter; i++) {
					this.contents[localContentsOffset++] = 0;
					this.contents[localContentsOffset++] = 0;
					this.contents[localContentsOffset++] = (byte) (code_length >> 8);
					this.contents[localContentsOffset++] = (byte) code_length;
					nameIndex = genericArgumentsNameIndexes[i];
					this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
					this.contents[localContentsOffset++] = (byte) nameIndex;
					descriptorIndex = constantPool.literalIndex(genericArgumentsTypeBindings[i].genericTypeSignature());
					this.contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
					this.contents[localContentsOffset++] = (byte) descriptorIndex;
					int resolvedPosition = genericArgumentsResolvedPositions[i];
					this.contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
					this.contents[localContentsOffset++] = (byte) resolvedPosition;
				}				
				attributeNumber++;
			}			
		}
		
		if ((this.produceAttributes & ClassFileConstants.ATTR_STACK_MAP) != 0) {
			final Set framesPositions = ((StackMapFrameCodeStream) codeStream).framePositions;
			final int framesPositionsSize = framesPositions.size();
			int numberOfFrames = framesPositionsSize - 1; // -1 because last return doesn't count
			if (numberOfFrames > 0) {
				ArrayList framePositions = new ArrayList(framesPositionsSize);
				framePositions.addAll(framesPositions);
				Collections.sort(framePositions);
				// add the stack map table attribute
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				int stackMapTableAttributeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.StackMapTableName);
				this.contents[localContentsOffset++] = (byte) (stackMapTableAttributeNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) stackMapTableAttributeNameIndex;
				
				int stackMapTableAttributeLengthOffset = localContentsOffset;
				// generate the attribute
				localContentsOffset += 4;
				numberOfFrames = 0;
				int numberOfFramesOffset = localContentsOffset;
				localContentsOffset += 2;
				// generate all frames
				ArrayList frames = ((StackMapFrameCodeStream) codeStream).frames;
				StackMapFrame currentFrame = (StackMapFrame) frames.get(0);
				StackMapFrame prevFrame = null;
				int framesSize = frames.size();
				int frameIndex = 0;
				for (int j = 0; j < framesPositionsSize && ((Integer) framePositions.get(j)).intValue() < code_length; j++) {
					// select next frame
					prevFrame = currentFrame;
					currentFrame = null;
					for (; frameIndex < framesSize; frameIndex++) {
						currentFrame = (StackMapFrame) frames.get(frameIndex);
						if (currentFrame.pc == ((Integer) framePositions.get(j)).intValue()) {
							break;
						}
					}
					if (currentFrame == null) break;
					numberOfFrames++;
					int offsetDelta = currentFrame.getOffsetDelta(prevFrame);
					switch (currentFrame.getFrameType(prevFrame)) {
						case StackMapFrame.APPEND_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							int numberOfDifferentLocals = currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 + numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int index = currentFrame.getIndexOfDifferentLocals(numberOfDifferentLocals);
							int numberOfLocals = currentFrame.getNumberOfLocals();
							for (int i = index; i < currentFrame.locals.length && numberOfDifferentLocals > 0; i++) {
								if (localContentsOffset + 6 >= this.contents.length) {
									resizeContents(6);
								}
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfDifferentLocals--;
								}
							}
							break;
						case StackMapFrame.SAME_FRAME :
							if (localContentsOffset + 1 >= this.contents.length) {
								resizeContents(1);
							}							
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.SAME_FRAME_EXTENDED :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							this.contents[localContentsOffset++] = (byte) 251;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.CHOP_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							numberOfDifferentLocals = -currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 - numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;							
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS :
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[localContentsOffset++] = (byte) (offsetDelta + 64);
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS_EXTENDED :
							if (localContentsOffset + 6 >= this.contents.length) {
								resizeContents(6);
							}							
							this.contents[localContentsOffset++] = (byte) 247;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						default :
							// FULL_FRAME
							if (localContentsOffset + 5 >= this.contents.length) {
								resizeContents(5);
							}							
							this.contents[localContentsOffset++] = (byte) 255;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int numberOfLocalOffset = localContentsOffset;
							localContentsOffset += 2; // leave two spots for number of locals
							int numberOfLocalEntries = 0;
							numberOfLocals = currentFrame.getNumberOfLocals();
							int numberOfEntries = 0;
							int localsLength = currentFrame.locals == null ? 0 : currentFrame.locals.length;
							for (int i = 0; i < localsLength && numberOfLocalEntries < numberOfLocals; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}							
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfLocalEntries++;
								}
								numberOfEntries++;
							}
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[numberOfLocalOffset++] = (byte) (numberOfEntries >> 8);
							this.contents[numberOfLocalOffset] = (byte) numberOfEntries;
							int numberOfStackItems = currentFrame.numberOfStackItems;
							this.contents[localContentsOffset++] = (byte) (numberOfStackItems >> 8);
							this.contents[localContentsOffset++] = (byte) numberOfStackItems;
							for (int i = 0; i < numberOfStackItems; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}
								VerificationTypeInfo info = currentFrame.stackItems[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
								}
							}
					}
				}
				
				this.contents[numberOfFramesOffset++] = (byte) (numberOfFrames >> 8);
				this.contents[numberOfFramesOffset] = (byte) numberOfFrames;

				int attributeLength = localContentsOffset - stackMapTableAttributeLengthOffset - 4;
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 24);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 16);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 8);
				this.contents[stackMapTableAttributeLengthOffset] = (byte) attributeLength;
				attributeNumber++;
			}
		}
		
		// update the number of attributes// ensure first that there is enough space available inside the localContents array
		if (codeAttributeAttributeOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}
		this.contents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		this.contents[codeAttributeAttributeOffset] = (byte) attributeNumber;
		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		this.contents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		this.contents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		this.contents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		this.contents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.SyntheticAccessMethodBinding
	 * @param codeAttributeOffset <CODE>int</CODE>
	 */
	public void completeCodeAttributeForSyntheticMethod(
		boolean hasExceptionHandlers,
		SyntheticMethodBinding binding,
		int codeAttributeOffset,
		int[] startLineIndexes) {
		// reinitialize the contents with the byte modified by the code stream
		this.contents = codeStream.bCodeStream;
		int localContentsOffset = codeStream.classFileOffset;
		// codeAttributeOffset is the position inside contents byte array before we started to write
		// any information about the codeAttribute
		// That means that to write the attribute_length you need to offset by 2 the value of codeAttributeOffset
		// to get the right position, 6 for the max_stack etc...
		int max_stack = codeStream.stackMax;
		contents[codeAttributeOffset + 6] = (byte) (max_stack >> 8);
		contents[codeAttributeOffset + 7] = (byte) max_stack;
		int max_locals = codeStream.maxLocals;
		contents[codeAttributeOffset + 8] = (byte) (max_locals >> 8);
		contents[codeAttributeOffset + 9] = (byte) max_locals;
		int code_length = codeStream.position;
		contents[codeAttributeOffset + 10] = (byte) (code_length >> 24);
		contents[codeAttributeOffset + 11] = (byte) (code_length >> 16);
		contents[codeAttributeOffset + 12] = (byte) (code_length >> 8);
		contents[codeAttributeOffset + 13] = (byte) code_length;
		if ((localContentsOffset + 40) >= this.contents.length) {
			resizeContents(40);
		}
		
		if (hasExceptionHandlers) {
			// write the exception table
			ExceptionLabel[] exceptionLabels = codeStream.exceptionLabels;
			int exceptionHandlersCount = 0; // each label holds one handler per range (start/end contiguous)
			for (int i = 0, length = codeStream.exceptionLabelsCounter; i < length; i++) {
				exceptionHandlersCount += codeStream.exceptionLabels[i].count / 2; 
			}
			int exSize = exceptionHandlersCount * 8 + 2;
			if (exSize + localContentsOffset >= this.contents.length) {
				resizeContents(exSize);
			}
			// there is no exception table, so we need to offset by 2 the current offset and move 
			// on the attribute generation
			this.contents[localContentsOffset++] = (byte) (exceptionHandlersCount >> 8);
			this.contents[localContentsOffset++] = (byte) exceptionHandlersCount;
			for (int i = 0, max = codeStream.exceptionLabelsCounter; i < max; i++) {
				ExceptionLabel exceptionLabel = exceptionLabels[i];
				if (exceptionLabel != null) {
					int iRange = 0, maxRange = exceptionLabel.count;
					if ((maxRange & 1) != 0) {
						referenceBinding.scope.problemReporter().abortDueToInternalError(
								Messages.bind(Messages.abort_invalidExceptionAttribute, new String(binding.selector), 
										referenceBinding.scope.problemReporter().referenceContext));
					}
					while  (iRange < maxRange) {
						int start = exceptionLabel.ranges[iRange++]; // even ranges are start positions
						this.contents[localContentsOffset++] = (byte) (start >> 8);
						this.contents[localContentsOffset++] = (byte) start;
						int end = exceptionLabel.ranges[iRange++]; // odd ranges are end positions
						this.contents[localContentsOffset++] = (byte) (end >> 8);
						this.contents[localContentsOffset++] = (byte) end;
						int handlerPC = exceptionLabel.position;
						this.contents[localContentsOffset++] = (byte) (handlerPC >> 8);
						this.contents[localContentsOffset++] = (byte) handlerPC;
						if (exceptionLabel.exceptionType == null) {
							// any exception handler
							this.contents[localContentsOffset++] = 0;
							this.contents[localContentsOffset++] = 0;
						} else {
							int nameIndex;
							switch(exceptionLabel.exceptionType.id) {
								case T_null :
									/* represents ClassNotFoundException, see class literal access*/
									nameIndex = constantPool.literalIndexForType(ConstantPool.JavaLangClassNotFoundExceptionConstantPoolName);
									break;
								case T_long :
									/* represents NoSuchFieldError, see switch table generation*/
									nameIndex = constantPool.literalIndexForType(ConstantPool.JavaLangNoSuchFieldErrorConstantPoolName);
									break;
								default:
									nameIndex = constantPool.literalIndexForType(exceptionLabel.exceptionType.constantPoolName());
							}
							this.contents[localContentsOffset++] = (byte) (nameIndex >> 8);
							this.contents[localContentsOffset++] = (byte) nameIndex;
						}
					}
				}
			}
		} else {
			// there is no exception table, so we need to offset by 2 the current offset and move 
			// on the attribute generation
			contents[localContentsOffset++] = 0;
			contents[localContentsOffset++] = 0;
		}
		// debug attributes
		int codeAttributeAttributeOffset = localContentsOffset;
		int attributeNumber = 0;
		// leave two bytes for the attribute_length
		localContentsOffset += 2;
		if (localContentsOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}

		// first we handle the linenumber attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_LINES) != 0) {
			if (localContentsOffset + 12 >= this.contents.length) {
				resizeContents(12);
			}		
			int index = 0;
			int lineNumberNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LineNumberTableName);
			contents[localContentsOffset++] = (byte) (lineNumberNameIndex >> 8);
			contents[localContentsOffset++] = (byte) lineNumberNameIndex;
			int lineNumberTableOffset = localContentsOffset;
			localContentsOffset += 6;
			// leave space for attribute_length and line_number_table_length
			// Seems like do would be better, but this preserves the existing behavior.
			index = searchLineNumber(startLineIndexes, binding.sourceStart);
			contents[localContentsOffset++] = 0;
			contents[localContentsOffset++] = 0;
			contents[localContentsOffset++] = (byte) (index >> 8);
			contents[localContentsOffset++] = (byte) index;
			// now we change the size of the line number attribute
			contents[lineNumberTableOffset++] = 0;
			contents[lineNumberTableOffset++] = 0;
			contents[lineNumberTableOffset++] = 0;
			contents[lineNumberTableOffset++] = 6;
			contents[lineNumberTableOffset++] = 0;
			contents[lineNumberTableOffset++] = 1;
			attributeNumber++;
		}
		// then we do the local variable attribute
		if ((this.produceAttributes & ClassFileConstants.ATTR_VARS) != 0) {
			int numberOfEntries = 0;
			int localVariableNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.LocalVariableTableName);
			if (localContentsOffset + 8 > this.contents.length) {
				resizeContents(8);
			}
			contents[localContentsOffset++] = (byte) (localVariableNameIndex >> 8);
			contents[localContentsOffset++] = (byte) localVariableNameIndex;
			int localVariableTableOffset = localContentsOffset;
			localContentsOffset += 6;
			// leave space for attribute_length and local_variable_table_length
			int nameIndex;
			int descriptorIndex;

			// used to remember the local variable with a generic type
			int genericLocalVariablesCounter = 0;
			LocalVariableBinding[] genericLocalVariables = null;
			int numberOfGenericEntries = 0;
			
			for (int i = 0, max = codeStream.allLocalsCounter; i < max; i++) {
				LocalVariableBinding localVariable = codeStream.locals[i];
				final TypeBinding localVariableTypeBinding = localVariable.type;
				boolean isParameterizedType = localVariableTypeBinding.isParameterizedType() || localVariableTypeBinding.isTypeVariable();
				if (localVariable.initializationCount != 0 && isParameterizedType) {
					if (genericLocalVariables == null) {
						// we cannot have more than max locals
						genericLocalVariables = new LocalVariableBinding[max];
					}
					genericLocalVariables[genericLocalVariablesCounter++] = localVariable;
				}
				for (int j = 0; j < localVariable.initializationCount; j++) {
					int startPC = localVariable.initializationPCs[j << 1];
					int endPC = localVariable.initializationPCs[(j << 1) + 1];
					if (startPC != endPC) { // only entries for non zero length
						if (endPC == -1) {
							localVariable.declaringScope.problemReporter().abortDueToInternalError(
								Messages.bind(Messages.abort_invalidAttribute, new String(localVariable.name)), 
								(ASTNode) localVariable.declaringScope.methodScope().referenceContext);
						}
						if (localContentsOffset + 10 > this.contents.length) {
							resizeContents(10);
						}
						// now we can safely add the local entry
						numberOfEntries++;
						if (isParameterizedType) {
							numberOfGenericEntries++;
						}
						contents[localContentsOffset++] = (byte) (startPC >> 8);
						contents[localContentsOffset++] = (byte) startPC;
						int length = endPC - startPC;
						contents[localContentsOffset++] = (byte) (length >> 8);
						contents[localContentsOffset++] = (byte) length;
						nameIndex = constantPool.literalIndex(localVariable.name);
						contents[localContentsOffset++] = (byte) (nameIndex >> 8);
						contents[localContentsOffset++] = (byte) nameIndex;
						descriptorIndex = constantPool.literalIndex(localVariableTypeBinding.signature());
						contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
						contents[localContentsOffset++] = (byte) descriptorIndex;
						int resolvedPosition = localVariable.resolvedPosition;
						contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
						contents[localContentsOffset++] = (byte) resolvedPosition;
					}
				}
			}
			int value = numberOfEntries * 10 + 2;
			contents[localVariableTableOffset++] = (byte) (value >> 24);
			contents[localVariableTableOffset++] = (byte) (value >> 16);
			contents[localVariableTableOffset++] = (byte) (value >> 8);
			contents[localVariableTableOffset++] = (byte) value;
			contents[localVariableTableOffset++] = (byte) (numberOfEntries >> 8);
			contents[localVariableTableOffset] = (byte) numberOfEntries;
			attributeNumber++;

			if (genericLocalVariablesCounter != 0) {
				// add the local variable type table attribute
				int maxOfEntries = 8 + numberOfGenericEntries * 10;
				// reserve enough space
				if (localContentsOffset + maxOfEntries >= this.contents.length) {
					resizeContents(maxOfEntries);
				}
				int localVariableTypeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.LocalVariableTypeTableName);
				contents[localContentsOffset++] = (byte) (localVariableTypeNameIndex >> 8);
				contents[localContentsOffset++] = (byte) localVariableTypeNameIndex;
				value = numberOfGenericEntries * 10 + 2;
				contents[localContentsOffset++] = (byte) (value >> 24);
				contents[localContentsOffset++] = (byte) (value >> 16);
				contents[localContentsOffset++] = (byte) (value >> 8);
				contents[localContentsOffset++] = (byte) value;
				contents[localContentsOffset++] = (byte) (numberOfGenericEntries >> 8);
				contents[localContentsOffset++] = (byte) numberOfGenericEntries;

				for (int i = 0; i < genericLocalVariablesCounter; i++) {
					LocalVariableBinding localVariable = genericLocalVariables[i];
					for (int j = 0; j < localVariable.initializationCount; j++) {
						int startPC = localVariable.initializationPCs[j << 1];
						int endPC = localVariable.initializationPCs[(j << 1) + 1];
						if (startPC != endPC) { // only entries for non zero length
							// now we can safely add the local entry
							contents[localContentsOffset++] = (byte) (startPC >> 8);
							contents[localContentsOffset++] = (byte) startPC;
							int length = endPC - startPC;
							contents[localContentsOffset++] = (byte) (length >> 8);
							contents[localContentsOffset++] = (byte) length;
							nameIndex = constantPool.literalIndex(localVariable.name);
							contents[localContentsOffset++] = (byte) (nameIndex >> 8);
							contents[localContentsOffset++] = (byte) nameIndex;
							descriptorIndex = constantPool.literalIndex(localVariable.type.genericTypeSignature());
							contents[localContentsOffset++] = (byte) (descriptorIndex >> 8);
							contents[localContentsOffset++] = (byte) descriptorIndex;
							int resolvedPosition = localVariable.resolvedPosition;
							contents[localContentsOffset++] = (byte) (resolvedPosition >> 8);
							contents[localContentsOffset++] = (byte) resolvedPosition;
						}
					}
				}
				attributeNumber++;
			}
		}
		
		if ((this.produceAttributes & ClassFileConstants.ATTR_STACK_MAP) != 0) {
			final Set framesPositions = ((StackMapFrameCodeStream) codeStream).framePositions;
			final int framesPositionsSize = framesPositions.size();
			int numberOfFrames = framesPositionsSize - 1; // -1 because last return doesn't count
			if (numberOfFrames > 0) {
				ArrayList framePositions = new ArrayList(framesPositionsSize);
				framePositions.addAll(framesPositions);
				Collections.sort(framePositions);
				// add the stack map table attribute
				if (localContentsOffset + 8 >= this.contents.length) {
					resizeContents(8);
				}
				int stackMapTableAttributeNameIndex =
					constantPool.literalIndex(AttributeNamesConstants.StackMapTableName);
				this.contents[localContentsOffset++] = (byte) (stackMapTableAttributeNameIndex >> 8);
				this.contents[localContentsOffset++] = (byte) stackMapTableAttributeNameIndex;
				
				int stackMapTableAttributeLengthOffset = localContentsOffset;
				// generate the attribute
				localContentsOffset += 4;
				numberOfFrames = 0;
				int numberOfFramesOffset = localContentsOffset;
				localContentsOffset += 2;
				// generate all frames
				ArrayList frames = ((StackMapFrameCodeStream) codeStream).frames;
				StackMapFrame currentFrame = (StackMapFrame) frames.get(0);
				StackMapFrame prevFrame = null;
				int framesSize = frames.size();
				int frameIndex = 0;
				for (int j = 0; j < framesPositionsSize && ((Integer) framePositions.get(j)).intValue() < code_length; j++) {
					// select next frame
					prevFrame = currentFrame;
					currentFrame = null;
					for (; frameIndex < framesSize; frameIndex++) {
						currentFrame = (StackMapFrame) frames.get(frameIndex);
						if (currentFrame.pc == ((Integer) framePositions.get(j)).intValue()) {
							break;
						}
					}
					if (currentFrame == null) break;
					numberOfFrames++;
					int offsetDelta = currentFrame.getOffsetDelta(prevFrame);
					switch (currentFrame.getFrameType(prevFrame)) {
						case StackMapFrame.APPEND_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							int numberOfDifferentLocals = currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 + numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int index = currentFrame.getIndexOfDifferentLocals(numberOfDifferentLocals);
							int numberOfLocals = currentFrame.getNumberOfLocals();
							for (int i = index; i < currentFrame.locals.length && numberOfDifferentLocals > 0; i++) {
								if (localContentsOffset + 6 >= this.contents.length) {
									resizeContents(6);
								}
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfDifferentLocals--;
								}
							}
							break;
						case StackMapFrame.SAME_FRAME :
							if (localContentsOffset + 1 >= this.contents.length) {
								resizeContents(1);
							}							
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.SAME_FRAME_EXTENDED :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							this.contents[localContentsOffset++] = (byte) 251;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							break;
						case StackMapFrame.CHOP_FRAME :
							if (localContentsOffset + 3 >= this.contents.length) {
								resizeContents(3);
							}							
							numberOfDifferentLocals = -currentFrame.numberOfDifferentLocals(prevFrame);
							this.contents[localContentsOffset++] = (byte) (251 - numberOfDifferentLocals);
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;							
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS :
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[localContentsOffset++] = (byte) (offsetDelta + 64);
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						case StackMapFrame.SAME_LOCALS_1_STACK_ITEMS_EXTENDED :
							if (localContentsOffset + 6 >= this.contents.length) {
								resizeContents(6);
							}							
							this.contents[localContentsOffset++] = (byte) 247;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							if (currentFrame.stackItems[0] == null) {
								this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
							} else {
								switch(currentFrame.stackItems[0].id()) {
									case T_boolean :
									case T_byte :
									case T_char :
									case T_int :
									case T_short :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
										break;
									case T_float :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
										break;
									case T_long :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
										break;
									case T_double :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
										break;
									case T_null :
										this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
										break;
									default:
										VerificationTypeInfo info = currentFrame.stackItems[0];
										this.contents[localContentsOffset++] = (byte) info.tag;
										switch (info.tag) {
											case VerificationTypeInfo.ITEM_UNINITIALIZED :
												int offset = info.offset;
												this.contents[localContentsOffset++] = (byte) (offset >> 8);
												this.contents[localContentsOffset++] = (byte) offset;
												break;
											case VerificationTypeInfo.ITEM_OBJECT :
												int indexForType = constantPool.literalIndexForType(info.constantPoolName());
												this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
												this.contents[localContentsOffset++] = (byte) indexForType;
										}
								}
							}
							break;
						default :
							// FULL_FRAME
							if (localContentsOffset + 5 >= this.contents.length) {
								resizeContents(5);
							}							
							this.contents[localContentsOffset++] = (byte) 255;
							this.contents[localContentsOffset++] = (byte) (offsetDelta >> 8);
							this.contents[localContentsOffset++] = (byte) offsetDelta;
							int numberOfLocalOffset = localContentsOffset;
							localContentsOffset += 2; // leave two spots for number of locals
							int numberOfLocalEntries = 0;
							numberOfLocals = currentFrame.getNumberOfLocals();
							int numberOfEntries = 0;
							int localsLength = currentFrame.locals == null ? 0 : currentFrame.locals.length;
							for (int i = 0; i < localsLength && numberOfLocalEntries < numberOfLocals; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}							
								VerificationTypeInfo info = currentFrame.locals[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											i++;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											i++;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
									numberOfLocalEntries++;
								}
								numberOfEntries++;
							}
							if (localContentsOffset + 4 >= this.contents.length) {
								resizeContents(4);
							}							
							this.contents[numberOfLocalOffset++] = (byte) (numberOfEntries >> 8);
							this.contents[numberOfLocalOffset] = (byte) numberOfEntries;
							int numberOfStackItems = currentFrame.numberOfStackItems;
							this.contents[localContentsOffset++] = (byte) (numberOfStackItems >> 8);
							this.contents[localContentsOffset++] = (byte) numberOfStackItems;
							for (int i = 0; i < numberOfStackItems; i++) {
								if (localContentsOffset + 3 >= this.contents.length) {
									resizeContents(3);
								}
								VerificationTypeInfo info = currentFrame.stackItems[i];
								if (info == null) {
									this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_TOP;
								} else {
									switch(info.id()) {
										case T_boolean :
										case T_byte :
										case T_char :
										case T_int :
										case T_short :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_INTEGER;
											break;
										case T_float :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_FLOAT;
											break;
										case T_long :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_LONG;
											break;
										case T_double :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_DOUBLE;
											break;
										case T_null :
											this.contents[localContentsOffset++] = (byte) VerificationTypeInfo.ITEM_NULL;
											break;
										default:
											this.contents[localContentsOffset++] = (byte) info.tag;
											switch (info.tag) {
												case VerificationTypeInfo.ITEM_UNINITIALIZED :
													int offset = info.offset;
													this.contents[localContentsOffset++] = (byte) (offset >> 8);
													this.contents[localContentsOffset++] = (byte) offset;
													break;
												case VerificationTypeInfo.ITEM_OBJECT :
													int indexForType = constantPool.literalIndexForType(info.constantPoolName());
													this.contents[localContentsOffset++] = (byte) (indexForType >> 8);
													this.contents[localContentsOffset++] = (byte) indexForType;
											}
									}
								}
							}
					}
				}
				
				this.contents[numberOfFramesOffset++] = (byte) (numberOfFrames >> 8);
				this.contents[numberOfFramesOffset] = (byte) numberOfFrames;

				int attributeLength = localContentsOffset - stackMapTableAttributeLengthOffset - 4;
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 24);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 16);
				this.contents[stackMapTableAttributeLengthOffset++] = (byte) (attributeLength >> 8);
				this.contents[stackMapTableAttributeLengthOffset] = (byte) attributeLength;
				attributeNumber++;
			}
		}
		
		// update the number of attributes
		// ensure first that there is enough space available inside the contents array
		if (codeAttributeAttributeOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}
		contents[codeAttributeAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[codeAttributeAttributeOffset] = (byte) attributeNumber;

		// update the attribute length
		int codeAttributeLength = localContentsOffset - (codeAttributeOffset + 6);
		contents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
		contents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
		contents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
		contents[codeAttributeOffset + 5] = (byte) codeAttributeLength;
		contentsOffset = localContentsOffset;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method completes the creation of the code attribute by setting
	 * - the attribute_length
	 * - max_stack
	 * - max_locals
	 * - code_length
	 * - exception table
	 * - and debug attributes if necessary.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.SyntheticAccessMethodBinding
	 * @param codeAttributeOffset <CODE>int</CODE>
	 */
	public void completeCodeAttributeForSyntheticMethod(
		SyntheticMethodBinding binding,
		int codeAttributeOffset,
		int[] startLineIndexes) {
		
		this.completeCodeAttributeForSyntheticMethod(
				false,
				binding,
				codeAttributeOffset,
				startLineIndexes);
	}

	/**
	 * INTERNAL USE-ONLY
	 * Complete the creation of a method info by setting up the number of attributes at the right offset.
	 *
	 * @param methodAttributeOffset <CODE>int</CODE>
	 * @param attributeNumber <CODE>int</CODE> 
	 */
	public void completeMethodInfo(
		int methodAttributeOffset,
		int attributeNumber) {
		// update the number of attributes
		contents[methodAttributeOffset++] = (byte) (attributeNumber >> 8);
		contents[methodAttributeOffset] = (byte) attributeNumber;
	}
	
	/**
	 * INTERNAL USE-ONLY
	 * This methods returns a char[] representing the file name of the receiver
	 *
	 * @return char[]
	 */
	public char[] fileName() {
		return constantPool.UTF8Cache.returnKeyFor(2);
	}

	private void generateAnnotation(Annotation annotation, int attributeOffset) {
		if (contentsOffset + 4 >= this.contents.length) {
			resizeContents(4);
		}
		TypeBinding annotationTypeBinding = annotation.resolvedType;
		if (annotationTypeBinding == null) {
			this.contentsOffset = attributeOffset;
			return;
		}
		final int typeIndex = constantPool.literalIndex(annotationTypeBinding.signature());
		contents[contentsOffset++] = (byte) (typeIndex >> 8);
		contents[contentsOffset++] = (byte) typeIndex;
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
			MemberValuePair[] memberValuePairs = normalAnnotation.memberValuePairs;
			if (memberValuePairs != null) {
				final int memberValuePairsLength = memberValuePairs.length;
				contents[contentsOffset++] = (byte) (memberValuePairsLength >> 8);
				contents[contentsOffset++] = (byte) memberValuePairsLength;
				for (int i = 0; i < memberValuePairsLength; i++) {
					MemberValuePair memberValuePair = memberValuePairs[i];
					if (contentsOffset + 2 >= this.contents.length) {
						resizeContents(2);
					}
					final int elementNameIndex = constantPool.literalIndex(memberValuePair.name);
					contents[contentsOffset++] = (byte) (elementNameIndex >> 8);
					contents[contentsOffset++] = (byte) elementNameIndex;
					MethodBinding methodBinding = memberValuePair.binding;
					if (methodBinding == null) {
						contentsOffset = attributeOffset;
					} else {
						generateElementValue(memberValuePair.value, methodBinding.returnType, attributeOffset);
					}
				}
			} else {
				contents[contentsOffset++] = 0;
				contents[contentsOffset++] = 0;
			}
		} else if (annotation instanceof SingleMemberAnnotation) {
			SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) annotation;
			// this is a single member annotation (one member value)
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 1;
			if (contentsOffset + 2 >= this.contents.length) {
				resizeContents(2);
			}
			final int elementNameIndex = constantPool.literalIndex(VALUE);
			contents[contentsOffset++] = (byte) (elementNameIndex >> 8);
			contents[contentsOffset++] = (byte) elementNameIndex;
			MethodBinding methodBinding = singleMemberAnnotation.memberValuePairs()[0].binding;
			if (methodBinding == null) {
				contentsOffset = attributeOffset;
			} else {
				generateElementValue(singleMemberAnnotation.memberValue, methodBinding.returnType, attributeOffset);
			}
		} else {
			// this is a marker annotation (no member value pairs)
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method generates the header of a code attribute.
	 * - the index inside the constant pool for the attribute name ("Code")
	 * - leave some space for attribute_length(4), max_stack(2), max_locals(2), code_length(4).
	 */
	public void generateCodeAttributeHeader() {
		if (contentsOffset + 20 >= this.contents.length) {
			resizeContents(20);
		}
		int constantValueNameIndex =
			constantPool.literalIndex(AttributeNamesConstants.CodeName);
		contents[contentsOffset++] = (byte) (constantValueNameIndex >> 8);
		contents[contentsOffset++] = (byte) constantValueNameIndex;
		// leave space for attribute_length(4), max_stack(2), max_locals(2), code_length(4)
		contentsOffset += 12;
	}

	private void generateElementValue(
			Expression defaultValue,
			TypeBinding memberValuePairReturnType,
			int attributeOffset) {
		Constant constant = defaultValue.constant;
		TypeBinding defaultValueBinding = defaultValue.resolvedType;
		if (defaultValueBinding == null) {
			contentsOffset = attributeOffset;
		} else {
			if (memberValuePairReturnType.isArrayType() && !defaultValueBinding.isArrayType()) {
				// automatic wrapping
				if (contentsOffset + 3 >= this.contents.length) {
					resizeContents(3);
				}
				contents[contentsOffset++] = (byte) '[';
				contents[contentsOffset++] = (byte) 0;
				contents[contentsOffset++] = (byte) 1;
			}
			if (constant != null && constant != Constant.NotAConstant) {
				generateElementValue(attributeOffset, defaultValue, constant, memberValuePairReturnType.leafComponentType());
			} else {
				generateElementValueForNonConstantExpression(defaultValue, attributeOffset, defaultValueBinding);
			}
		}
	}

	/**
	 * @param attributeOffset
	 */
	private void generateElementValue(int attributeOffset, Expression defaultValue, Constant constant, TypeBinding binding) {
		if (contentsOffset + 3 >= this.contents.length) {
			resizeContents(3);
		}
		switch (binding.id) {
			case T_boolean :
				contents[contentsOffset++] = (byte) 'Z';
				int booleanValueIndex =
					constantPool.literalIndex(constant.booleanValue() ? 1 : 0);
				contents[contentsOffset++] = (byte) (booleanValueIndex >> 8);
				contents[contentsOffset++] = (byte) booleanValueIndex;
				break;
			case T_byte :
				contents[contentsOffset++] = (byte) 'B';
				int integerValueIndex =
					constantPool.literalIndex(constant.intValue());
				contents[contentsOffset++] = (byte) (integerValueIndex >> 8);
				contents[contentsOffset++] = (byte) integerValueIndex;
				break;
			case T_char :
				contents[contentsOffset++] = (byte) 'C';
				integerValueIndex =
					constantPool.literalIndex(constant.intValue());
				contents[contentsOffset++] = (byte) (integerValueIndex >> 8);
				contents[contentsOffset++] = (byte) integerValueIndex;
				break;
			case T_int :
				contents[contentsOffset++] = (byte) 'I';
				integerValueIndex =
					constantPool.literalIndex(constant.intValue());
				contents[contentsOffset++] = (byte) (integerValueIndex >> 8);
				contents[contentsOffset++] = (byte) integerValueIndex;
				break;
			case T_short :
				contents[contentsOffset++] = (byte) 'S';
				integerValueIndex =
					constantPool.literalIndex(constant.intValue());
				contents[contentsOffset++] = (byte) (integerValueIndex >> 8);
				contents[contentsOffset++] = (byte) integerValueIndex;
				break;
			case T_float :
				contents[contentsOffset++] = (byte) 'F';
				int floatValueIndex =
					constantPool.literalIndex(constant.floatValue());
				contents[contentsOffset++] = (byte) (floatValueIndex >> 8);
				contents[contentsOffset++] = (byte) floatValueIndex;
				break;
			case T_double :
				contents[contentsOffset++] = (byte) 'D';
				int doubleValueIndex =
					constantPool.literalIndex(constant.doubleValue());
				contents[contentsOffset++] = (byte) (doubleValueIndex >> 8);
				contents[contentsOffset++] = (byte) doubleValueIndex;
				break;
			case T_long :
				contents[contentsOffset++] = (byte) 'J';
				int longValueIndex =
					constantPool.literalIndex(constant.longValue());
				contents[contentsOffset++] = (byte) (longValueIndex >> 8);
				contents[contentsOffset++] = (byte) longValueIndex;
				break;
			case T_JavaLangString :
				contents[contentsOffset++] = (byte) 's';
				int stringValueIndex =
					constantPool.literalIndex(((StringConstant) constant).stringValue().toCharArray());
				if (stringValueIndex == -1) {
					if (!creatingProblemType) {
						// report an error and abort: will lead to a problem type classfile creation
						TypeDeclaration typeDeclaration = referenceBinding.scope.referenceContext;
						typeDeclaration.scope.problemReporter().stringConstantIsExceedingUtf8Limit(defaultValue);
					} else {
						// already inside a problem type creation : no attribute
						contentsOffset = attributeOffset;
					}
				} else {
					contents[contentsOffset++] = (byte) (stringValueIndex >> 8);
					contents[contentsOffset++] = (byte) stringValueIndex;
				}
		}
	}

	private void generateElementValueForNonConstantExpression(Expression defaultValue, int attributeOffset, TypeBinding defaultValueBinding) {
		if (defaultValueBinding != null) {
			if (defaultValueBinding.isEnum()) {
				if (contentsOffset + 5 >= this.contents.length) {
					resizeContents(5);
				}
				contents[contentsOffset++] = (byte) 'e';
				FieldBinding fieldBinding = null;
				if (defaultValue instanceof QualifiedNameReference) {
					QualifiedNameReference nameReference = (QualifiedNameReference) defaultValue;
					fieldBinding = (FieldBinding) nameReference.binding;
				} else if (defaultValue instanceof SingleNameReference) {
					SingleNameReference nameReference = (SingleNameReference) defaultValue;
					fieldBinding = (FieldBinding) nameReference.binding;
				} else {
					contentsOffset = attributeOffset;
				}
				if (fieldBinding != null) {
					final int enumConstantTypeNameIndex = constantPool.literalIndex(fieldBinding.type.signature());
					final int enumConstantNameIndex = constantPool.literalIndex(fieldBinding.name);
					contents[contentsOffset++] = (byte) (enumConstantTypeNameIndex >> 8);
					contents[contentsOffset++] = (byte) enumConstantTypeNameIndex;
					contents[contentsOffset++] = (byte) (enumConstantNameIndex >> 8);
					contents[contentsOffset++] = (byte) enumConstantNameIndex;
				}
			} else if (defaultValueBinding.isAnnotationType()) {
				if (contentsOffset + 1 >= this.contents.length) {
					resizeContents(1);
				}
				contents[contentsOffset++] = (byte) '@';
				generateAnnotation((Annotation) defaultValue, attributeOffset);
			} else if (defaultValueBinding.isArrayType()) {
				// array type
				if (contentsOffset + 3 >= this.contents.length) {
					resizeContents(3);
				}
				contents[contentsOffset++] = (byte) '[';
				if (defaultValue instanceof ArrayInitializer) {
					ArrayInitializer arrayInitializer = (ArrayInitializer) defaultValue;
					int arrayLength = arrayInitializer.expressions != null ? arrayInitializer.expressions.length : 0;
					contents[contentsOffset++] = (byte) (arrayLength >> 8);
					contents[contentsOffset++] = (byte) arrayLength;
					for (int i = 0; i < arrayLength; i++) {
						generateElementValue(arrayInitializer.expressions[i], defaultValueBinding.leafComponentType(), attributeOffset);
					}
				} else {
					contentsOffset = attributeOffset;
				}
			} else {
				// class type
				if (contentsOffset + 3 >= this.contents.length) {
					resizeContents(3);
				}
				contents[contentsOffset++] = (byte) 'c';
				if (defaultValue instanceof ClassLiteralAccess) {
					ClassLiteralAccess classLiteralAccess = (ClassLiteralAccess) defaultValue;
					final int classInfoIndex = constantPool.literalIndex(classLiteralAccess.targetType.signature());
					contents[contentsOffset++] = (byte) (classInfoIndex >> 8);
					contents[contentsOffset++] = (byte) classInfoIndex;
				} else {
					contentsOffset = attributeOffset;
				}
			}
		} else {
			contentsOffset = attributeOffset;
		}
	}
	
	public int generateMethodInfoAttribute(MethodBinding methodBinding) {
		return generateMethodInfoAttribute(methodBinding, false);
	}

	public int generateMethodInfoAttribute(MethodBinding methodBinding, AnnotationMethodDeclaration declaration) {
		int attributesNumber = generateMethodInfoAttribute(methodBinding);
		int attributeOffset = contentsOffset;
		if ((declaration.modifiers & ClassFileConstants.AccAnnotationDefault) != 0) {
			// add an annotation default attribute
			int annotationDefaultNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.AnnotationDefaultName);
			contents[contentsOffset++] = (byte) (annotationDefaultNameIndex >> 8);
			contents[contentsOffset++] = (byte) annotationDefaultNameIndex;
			int attributeLengthOffset = contentsOffset;
			contentsOffset += 4;
			if (contentsOffset + 4 >= this.contents.length) {
				resizeContents(4);
			}
			generateElementValue(declaration.defaultValue, declaration.binding.returnType, attributeOffset);
			if (contentsOffset != attributeOffset) {
				int attributeLength = contentsOffset - attributeLengthOffset - 4;
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 24);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 16);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 8);
				contents[attributeLengthOffset++] = (byte) attributeLength;			
				attributesNumber++;
			}
		}
		return attributesNumber;
	}
	/**
	 * INTERNAL USE-ONLY
	 * That method generates the attributes of a code attribute.
	 * They could be:
	 * - an exception attribute for each try/catch found inside the method
	 * - a deprecated attribute
	 * - a synthetic attribute for synthetic access methods
	 *
	 * It returns the number of attributes created for the code attribute.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.lookup.MethodBinding
	 * @return <CODE>int</CODE>
	 */
	public int generateMethodInfoAttribute(MethodBinding methodBinding, boolean createProblemMethod) {
		// leave two bytes for the attribute_number
		contentsOffset += 2;
		if (contentsOffset + 2 >= this.contents.length) {
			resizeContents(2);
		}
		// now we can handle all the attribute for that method info:
		// it could be:
		// - a CodeAttribute
		// - a ExceptionAttribute
		// - a DeprecatedAttribute
		// - a SyntheticAttribute

		// Exception attribute
		ReferenceBinding[] thrownsExceptions;
		int attributeNumber = 0;
		if ((thrownsExceptions = methodBinding.thrownExceptions) != Binding.NO_EXCEPTIONS) {
			// The method has a throw clause. So we need to add an exception attribute
			// check that there is enough space to write all the bytes for the exception attribute
			int length = thrownsExceptions.length;
			int exSize = 8 + length * 2;
			if (exSize + contentsOffset >= this.contents.length) {
				resizeContents(exSize);
			}
			int exceptionNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.ExceptionsName);
			contents[contentsOffset++] = (byte) (exceptionNameIndex >> 8);
			contents[contentsOffset++] = (byte) exceptionNameIndex;
			// The attribute length = length * 2 + 2 in case of a exception attribute
			int attributeLength = length * 2 + 2;
			contents[contentsOffset++] = (byte) (attributeLength >> 24);
			contents[contentsOffset++] = (byte) (attributeLength >> 16);
			contents[contentsOffset++] = (byte) (attributeLength >> 8);
			contents[contentsOffset++] = (byte) attributeLength;
			contents[contentsOffset++] = (byte) (length >> 8);
			contents[contentsOffset++] = (byte) length;
			for (int i = 0; i < length; i++) {
				int exceptionIndex = constantPool.literalIndexForType(thrownsExceptions[i].constantPoolName());
				contents[contentsOffset++] = (byte) (exceptionIndex >> 8);
				contents[contentsOffset++] = (byte) exceptionIndex;
			}
			attributeNumber++;
		}
		if (methodBinding.isDeprecated()) {
			// Deprecated attribute
			// Check that there is enough space to write the deprecated attribute
			if (contentsOffset + 6 >= this.contents.length) {
				resizeContents(6);
			}
			int deprecatedAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.DeprecatedName);
			contents[contentsOffset++] = (byte) (deprecatedAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) deprecatedAttributeNameIndex;
			// the length of a deprecated attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;

			attributeNumber++;
		}
		if (this.targetJDK < ClassFileConstants.JDK1_5 && methodBinding.isSynthetic()) {
			// Synthetic attribute
			// Check that there is enough space to write the deprecated attribute
			if (contentsOffset + 6 >= this.contents.length) {
				resizeContents(6);
			}
			int syntheticAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
			contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
			// the length of a synthetic attribute is equals to 0
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;

			attributeNumber++;
		}
		// add signature attribute
		char[] genericSignature = methodBinding.genericSignature();
		if (genericSignature != null) {
			// check that there is enough space to write all the bytes for the field info corresponding
			// to the @fieldBinding
			if (contentsOffset + 8 >= this.contents.length) {
				resizeContents(8);
			}
			int signatureAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.SignatureName);
			contents[contentsOffset++] = (byte) (signatureAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) signatureAttributeNameIndex;
			// the length of a signature attribute is equals to 2
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 0;
			contents[contentsOffset++] = 2;
			int signatureIndex =
				constantPool.literalIndex(genericSignature);
			contents[contentsOffset++] = (byte) (signatureIndex >> 8);
			contents[contentsOffset++] = (byte) signatureIndex;
			attributeNumber++;
		}
		if (this.targetJDK >= ClassFileConstants.JDK1_5 && !this.creatingProblemType && !createProblemMethod) {
			AbstractMethodDeclaration methodDeclaration = methodBinding.sourceMethod();
			if (methodDeclaration != null) {
				Annotation[] annotations = methodDeclaration.annotations;
				if (annotations != null) {
					attributeNumber += generateRuntimeAnnotations(annotations);
				}
				if ((methodBinding.tagBits & TagBits.HasParameterAnnotations) != 0) {
					Argument[] arguments = methodDeclaration.arguments;
					if (arguments != null) {
						attributeNumber += generateRuntimeAnnotationsForParameters(arguments);
					}
				}
			}
		}
		return attributeNumber;
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method generates the header of a method info:
	 * The header consists in:
	 * - the access flags
	 * - the name index of the method name inside the constant pool
	 * - the descriptor index of the signature of the method inside the constant pool.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.lookup.MethodBinding
	 */
	public void generateMethodInfoHeader(MethodBinding methodBinding) {
		generateMethodInfoHeader(methodBinding, methodBinding.modifiers);
	}

	/**
	 * INTERNAL USE-ONLY
	 * That method generates the header of a method info:
	 * The header consists in:
	 * - the access flags
	 * - the name index of the method name inside the constant pool
	 * - the descriptor index of the signature of the method inside the constant pool.
	 *
	 * @param methodBinding org.eclipse.jdt.internal.compiler.lookup.MethodBinding
	 * @param accessFlags the access flags
	 */
	public void generateMethodInfoHeader(MethodBinding methodBinding, int accessFlags) {
		// check that there is enough space to write all the bytes for the method info corresponding
		// to the @methodBinding
		methodCount++; // add one more method
		if (contentsOffset + 10 >= this.contents.length) {
			resizeContents(10);
		}
		if (targetJDK < ClassFileConstants.JDK1_5) {
		    // pre 1.5, synthetic was an attribute, not a modifier
		    accessFlags &= ~ClassFileConstants.AccSynthetic;
		}
		if ((methodBinding.tagBits & TagBits.ClearPrivateModifier) != 0) {
			accessFlags &= ~ClassFileConstants.AccPrivate;
		}
		contents[contentsOffset++] = (byte) (accessFlags >> 8);
		contents[contentsOffset++] = (byte) accessFlags;
		int nameIndex = constantPool.literalIndex(methodBinding.selector);
		contents[contentsOffset++] = (byte) (nameIndex >> 8);
		contents[contentsOffset++] = (byte) nameIndex;
		int descriptorIndex = constantPool.literalIndex(methodBinding.signature());
		contents[contentsOffset++] = (byte) (descriptorIndex >> 8);
		contents[contentsOffset++] = (byte) descriptorIndex;
	}
	/**
	 * INTERNAL USE-ONLY
	 * That method generates the method info header of a clinit:
	 * The header consists in:
	 * - the access flags (always default access + static)
	 * - the name index of the method name (always <clinit>) inside the constant pool 
	 * - the descriptor index of the signature (always ()V) of the method inside the constant pool.
	 */
	public void generateMethodInfoHeaderForClinit() {
		// check that there is enough space to write all the bytes for the method info corresponding
		// to the @methodBinding
		methodCount++; // add one more method
		if (contentsOffset + 10 >= this.contents.length) {
			resizeContents(10);
		}
		contents[contentsOffset++] = (byte) ((ClassFileConstants.AccDefault | ClassFileConstants.AccStatic) >> 8);
		contents[contentsOffset++] = (byte) (ClassFileConstants.AccDefault | ClassFileConstants.AccStatic);
		int nameIndex = constantPool.literalIndex(ConstantPool.Clinit);
		contents[contentsOffset++] = (byte) (nameIndex >> 8);
		contents[contentsOffset++] = (byte) nameIndex;
		int descriptorIndex =
			constantPool.literalIndex(ConstantPool.ClinitSignature);
		contents[contentsOffset++] = (byte) (descriptorIndex >> 8);
		contents[contentsOffset++] = (byte) descriptorIndex;
		// We know that we won't get more than 1 attribute: the code attribute
		contents[contentsOffset++] = 0;
		contents[contentsOffset++] = 1;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Generate the byte for problem method infos that correspond to missing abstract methods.
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=3179
	 *
	 * @param methodDeclarations Array of all missing abstract methods
	 */
	public void generateMissingAbstractMethods(MethodDeclaration[] methodDeclarations, CompilationResult compilationResult) {
		if (methodDeclarations != null) {
			TypeDeclaration currentDeclaration = this.referenceBinding.scope.referenceContext;
			int typeDeclarationSourceStart = currentDeclaration.sourceStart();
			int typeDeclarationSourceEnd = currentDeclaration.sourceEnd();
			for (int i = 0, max = methodDeclarations.length; i < max; i++) {
				MethodDeclaration methodDeclaration = methodDeclarations[i];
				MethodBinding methodBinding = methodDeclaration.binding;
		 		String readableName = new String(methodBinding.readableName());
		 		CategorizedProblem[] problems = compilationResult.problems;
		 		int problemsCount = compilationResult.problemCount;
				for (int j = 0; j < problemsCount; j++) {
					CategorizedProblem problem = problems[j];
					if (problem != null
    						&& problem.getID() == IProblem.AbstractMethodMustBeImplemented
    						&& problem.getMessage().indexOf(readableName) != -1
    						&& problem.getSourceStart() >= typeDeclarationSourceStart
    						&& problem.getSourceEnd() <= typeDeclarationSourceEnd) {
						// we found a match
						addMissingAbstractProblemMethod(methodDeclaration, methodBinding, problem, compilationResult);
					}
				}
			}
		}
	}

	/**
	 * @param annotations
	 * @return the number of attributes created while dumping the annotations in the .class file
	 */
	private int generateRuntimeAnnotations(final Annotation[] annotations) {
		int attributesNumber = 0;
		final int length = annotations.length;
		int visibleAnnotationsCounter = 0;
		int invisibleAnnotationsCounter = 0;
		
		for (int i = 0; i < length; i++) {
			Annotation annotation = annotations[i];
			if (isRuntimeInvisible(annotation)) {
				invisibleAnnotationsCounter++;
			} else if (isRuntimeVisible(annotation)) {
				visibleAnnotationsCounter++;
			}
		}

		if (invisibleAnnotationsCounter != 0) {
			int annotationAttributeOffset = contentsOffset;
			if (contentsOffset + 10 >= contents.length) {
				resizeContents(10);
			}
			int runtimeInvisibleAnnotationsAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.RuntimeInvisibleAnnotationsName);
			contents[contentsOffset++] = (byte) (runtimeInvisibleAnnotationsAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) runtimeInvisibleAnnotationsAttributeNameIndex;
			int attributeLengthOffset = contentsOffset;
			contentsOffset += 4; // leave space for the attribute length
	
			int annotationsLengthOffset = contentsOffset;
			contentsOffset += 2; // leave space for the annotations length
		
			contents[annotationsLengthOffset++] = (byte) (invisibleAnnotationsCounter >> 8);
			contents[annotationsLengthOffset++] = (byte) invisibleAnnotationsCounter;

			loop: for (int i = 0; i < length; i++) {
				if (invisibleAnnotationsCounter == 0) break loop;
				Annotation annotation = annotations[i];
				if (isRuntimeInvisible(annotation)) {
					generateAnnotation(annotation, annotationAttributeOffset);
					invisibleAnnotationsCounter--;
					if (this.contentsOffset == annotationAttributeOffset) {
						break loop;
					}
				}
			}
			if (contentsOffset != annotationAttributeOffset) {
				int attributeLength = contentsOffset - attributeLengthOffset - 4;
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 24);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 16);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 8);
				contents[attributeLengthOffset++] = (byte) attributeLength;			
				attributesNumber++;
			} else {		
				contentsOffset = annotationAttributeOffset;
			}
		}
	
		if (visibleAnnotationsCounter != 0) {
			int annotationAttributeOffset = contentsOffset;
			if (contentsOffset + 10 >= contents.length) {
				resizeContents(10);
			}
			int runtimeVisibleAnnotationsAttributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.RuntimeVisibleAnnotationsName);
			contents[contentsOffset++] = (byte) (runtimeVisibleAnnotationsAttributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) runtimeVisibleAnnotationsAttributeNameIndex;
			int attributeLengthOffset = contentsOffset;
			contentsOffset += 4; // leave space for the attribute length
	
			int annotationsLengthOffset = contentsOffset;
			contentsOffset += 2; // leave space for the annotations length
		
			contents[annotationsLengthOffset++] = (byte) (visibleAnnotationsCounter >> 8);
			contents[annotationsLengthOffset++] = (byte) visibleAnnotationsCounter;

			loop: for (int i = 0; i < length; i++) {
				if (visibleAnnotationsCounter == 0) break loop;
				Annotation annotation = annotations[i];
				if (isRuntimeVisible(annotation)) {
					visibleAnnotationsCounter--;
					generateAnnotation(annotation, annotationAttributeOffset);
					if (this.contentsOffset == annotationAttributeOffset) {
						break loop;
					}
				}
			}
			if (contentsOffset != annotationAttributeOffset) {
				int attributeLength = contentsOffset - attributeLengthOffset - 4;
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 24);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 16);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 8);
				contents[attributeLengthOffset++] = (byte) attributeLength;			
				attributesNumber++;
			} else {
				contentsOffset = annotationAttributeOffset;
			}
		}
		return attributesNumber;
	}

	private int generateRuntimeAnnotationsForParameters(Argument[] arguments) {
		final int argumentsLength = arguments.length;
		final int VISIBLE_INDEX = 0;
		final int INVISIBLE_INDEX = 1;
		int invisibleParametersAnnotationsCounter = 0;
		int visibleParametersAnnotationsCounter = 0;
		int[][] annotationsCounters = new int[argumentsLength][2];
		for (int i = 0; i < argumentsLength; i++) {
			Argument argument = arguments[i];
			Annotation[] annotations = argument.annotations;
			if (annotations != null) {
				for (int j = 0, max2 = annotations.length; j < max2; j++) {
					Annotation annotation = annotations[j];
					if (isRuntimeInvisible(annotation)) {
						annotationsCounters[i][INVISIBLE_INDEX]++;
						invisibleParametersAnnotationsCounter++;
					} else if (isRuntimeVisible(annotation)) {
						annotationsCounters[i][VISIBLE_INDEX]++;
						visibleParametersAnnotationsCounter++;
					}
				}
			}
		}
		int attributesNumber = 0;
		int annotationAttributeOffset = contentsOffset;
		if (invisibleParametersAnnotationsCounter != 0) {
			if (contentsOffset + 7 >= contents.length) {
				resizeContents(7);
			}
			int attributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.RuntimeInvisibleParameterAnnotationsName);
			contents[contentsOffset++] = (byte) (attributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) attributeNameIndex;
			int attributeLengthOffset = contentsOffset;
			contentsOffset += 4; // leave space for the attribute length

			contents[contentsOffset++] = (byte) argumentsLength;
			invisibleLoop: for (int i = 0; i < argumentsLength; i++) {
				if (contentsOffset + 2 >= contents.length) {
					resizeContents(2);
				}
				if (invisibleParametersAnnotationsCounter == 0) {
					contents[contentsOffset++] = (byte) 0;
					contents[contentsOffset++] = (byte) 0;					
				} else {
					final int numberOfInvisibleAnnotations = annotationsCounters[i][INVISIBLE_INDEX];
					contents[contentsOffset++] = (byte) (numberOfInvisibleAnnotations >> 8);
					contents[contentsOffset++] = (byte) numberOfInvisibleAnnotations;
					if (numberOfInvisibleAnnotations != 0) {
						Argument argument = arguments[i];
						Annotation[] annotations = argument.annotations;
						for (int j = 0, max = annotations.length; j < max; j++) {
							Annotation annotation = annotations[j];
							if (isRuntimeInvisible(annotation)) {
								generateAnnotation(annotation, annotationAttributeOffset);
								if (contentsOffset == annotationAttributeOffset) {
									break invisibleLoop;
								}
								invisibleParametersAnnotationsCounter--;
							}
						}
					}
				}
			}
			if (contentsOffset != annotationAttributeOffset) {
				int attributeLength = contentsOffset - attributeLengthOffset - 4;
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 24);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 16);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 8);
				contents[attributeLengthOffset++] = (byte) attributeLength;			
				attributesNumber++;
			} else {
				contentsOffset = annotationAttributeOffset;
			}
		}
		if (visibleParametersAnnotationsCounter != 0) {
			if (contentsOffset + 7 >= contents.length) {
				resizeContents(7);
			}
			int attributeNameIndex =
				constantPool.literalIndex(AttributeNamesConstants.RuntimeVisibleParameterAnnotationsName);
			contents[contentsOffset++] = (byte) (attributeNameIndex >> 8);
			contents[contentsOffset++] = (byte) attributeNameIndex;
			int attributeLengthOffset = contentsOffset;
			contentsOffset += 4; // leave space for the attribute length

			contents[contentsOffset++] = (byte) argumentsLength;
			visibleLoop: for (int i = 0; i < argumentsLength; i++) {
				if (contentsOffset + 2 >= contents.length) {
					resizeContents(2);
				}
				if (visibleParametersAnnotationsCounter == 0) {
					contents[contentsOffset++] = (byte) 0;
					contents[contentsOffset++] = (byte) 0;					
				} else {
					final int numberOfVisibleAnnotations = annotationsCounters[i][VISIBLE_INDEX];
					contents[contentsOffset++] = (byte) (numberOfVisibleAnnotations >> 8);
					contents[contentsOffset++] = (byte) numberOfVisibleAnnotations;
					if (numberOfVisibleAnnotations != 0) {
						Argument argument = arguments[i];
						Annotation[] annotations = argument.annotations;
						for (int j = 0, max = annotations.length; j < max; j++) {
							Annotation annotation = annotations[j];
							if (isRuntimeVisible(annotation)) {
								generateAnnotation(annotation, annotationAttributeOffset);
								if (contentsOffset == annotationAttributeOffset) {
									break visibleLoop;
								}
								visibleParametersAnnotationsCounter--;
							}
						}
					}
				}
			}
			if (contentsOffset != annotationAttributeOffset) {
				int attributeLength = contentsOffset - attributeLengthOffset - 4;
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 24);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 16);
				contents[attributeLengthOffset++] = (byte) (attributeLength >> 8);
				contents[attributeLengthOffset++] = (byte) attributeLength;			
				attributesNumber++;
			} else {
				contentsOffset = annotationAttributeOffset;
			}
		}
		return attributesNumber;
	}
	
	/**
	 * EXTERNAL API
	 * Answer the actual bytes of the class file
	 *
	 * This method encodes the receiver structure into a byte array which is the content of the classfile.
	 * Returns the byte array that represents the encoded structure of the receiver.
	 *
	 * @return byte[]
	 */
	public byte[] getBytes() {
		if (this.bytes == null) {
			this.bytes = new byte[this.headerOffset + this.contentsOffset];
			System.arraycopy(this.header, 0, this.bytes, 0, this.headerOffset);
			System.arraycopy(this.contents, 0, this.bytes, this.headerOffset, this.contentsOffset);
		}
		return this.bytes;
	}
	/**
	 * EXTERNAL API
	 * Answer the compound name of the class file.
	 * @return char[][]
	 * e.g. {{java}, {util}, {Hashtable}}.
	 */
	public char[][] getCompoundName() {
		return CharOperation.splitOn('/', fileName());
	}

	protected void initByteArrays() {
		int members = referenceBinding.methods().length + referenceBinding.fields().length;
		this.header = new byte[INITIAL_HEADER_SIZE];
		this.contents = new byte[members < 15 ? INITIAL_CONTENTS_SIZE : INITIAL_HEADER_SIZE];
	}

	public void initialize(SourceTypeBinding aType, ClassFile parentClassFile, boolean createProblemType) {
		// generate the magic numbers inside the header
		header[headerOffset++] = (byte) (0xCAFEBABEL >> 24);
		header[headerOffset++] = (byte) (0xCAFEBABEL >> 16);
		header[headerOffset++] = (byte) (0xCAFEBABEL >> 8);
		header[headerOffset++] = (byte) (0xCAFEBABEL >> 0);
		
		header[headerOffset++] = (byte) (this.targetJDK >> 8); // minor high
		header[headerOffset++] = (byte) (this.targetJDK >> 0); // minor low
		header[headerOffset++] = (byte) (this.targetJDK >> 24); // major high
		header[headerOffset++] = (byte) (this.targetJDK >> 16); // major low

		constantPoolOffset = headerOffset;
		headerOffset += 2;
		this.constantPool.initialize(this);
		
		// Modifier manipulations for classfile
		int accessFlags = aType.getAccessFlags();
		if (aType.isPrivate()) { // rewrite private to non-public
			accessFlags &= ~ClassFileConstants.AccPublic;
		}
		if (aType.isProtected()) { // rewrite protected into public
			accessFlags |= ClassFileConstants.AccPublic;
		}
		// clear all bits that are illegal for a class or an interface
		accessFlags
			&= ~(
				ClassFileConstants.AccStrictfp
					| ClassFileConstants.AccProtected
					| ClassFileConstants.AccPrivate
					| ClassFileConstants.AccStatic
					| ClassFileConstants.AccSynchronized
					| ClassFileConstants.AccNative);
					
		// set the AccSuper flag (has to be done after clearing AccSynchronized - since same value)
		if (!aType.isInterface()) { // class or enum
			accessFlags |= ClassFileConstants.AccSuper;
		}
		
		this.enclosingClassFile = parentClassFile;
		// innerclasses get their names computed at code gen time

		// now we continue to generate the bytes inside the contents array
		contents[contentsOffset++] = (byte) (accessFlags >> 8);
		contents[contentsOffset++] = (byte) accessFlags;
		int classNameIndex = constantPool.literalIndexForType(aType.constantPoolName());
		contents[contentsOffset++] = (byte) (classNameIndex >> 8);
		contents[contentsOffset++] = (byte) classNameIndex;
		int superclassNameIndex;
		if (aType.isInterface()) {
			superclassNameIndex = constantPool.literalIndexForType(ConstantPool.JavaLangObjectConstantPoolName);
		} else {
			superclassNameIndex =
				(aType.superclass == null ? 0 : constantPool.literalIndexForType(aType.superclass.constantPoolName()));
		}
		contents[contentsOffset++] = (byte) (superclassNameIndex >> 8);
		contents[contentsOffset++] = (byte) superclassNameIndex;
		ReferenceBinding[] superInterfacesBinding = aType.superInterfaces();
		int interfacesCount = superInterfacesBinding.length;
		contents[contentsOffset++] = (byte) (interfacesCount >> 8);
		contents[contentsOffset++] = (byte) interfacesCount;
		for (int i = 0; i < interfacesCount; i++) {
			int interfaceIndex = constantPool.literalIndexForType(superInterfacesBinding[i].constantPoolName());
			contents[contentsOffset++] = (byte) (interfaceIndex >> 8);
			contents[contentsOffset++] = (byte) interfaceIndex;
		}
		innerClassesBindings = new ReferenceBinding[INNER_CLASSES_SIZE];
		this.creatingProblemType = createProblemType;

		// retrieve the enclosing one guaranteed to be the one matching the propagated flow info
		// 1FF9ZBU: LFCOM:ALL - Local variable attributes busted (Sanity check)
		if (this.enclosingClassFile == null) {
			this.codeStream.maxFieldCount = aType.scope.referenceType().maxFieldCount;
		} else {
			ClassFile outermostClassFile = this.outerMostEnclosingClassFile();
			this.codeStream.maxFieldCount = outermostClassFile.codeStream.maxFieldCount;
		}
	}

	
	private boolean isRuntimeInvisible(Annotation annotation) {
		final TypeBinding annotationBinding = annotation.resolvedType;
		if (annotationBinding == null) {
			return false;
		}
		long metaTagBits = annotationBinding.getAnnotationTagBits(); // could be forward reference
		if ((metaTagBits & TagBits.AnnotationRetentionMASK) == 0)
			return true; // by default the retention is CLASS
			
		return (metaTagBits & TagBits.AnnotationRetentionMASK) == TagBits.AnnotationClassRetention;
	}

	private boolean isRuntimeVisible(Annotation annotation) {
		final TypeBinding annotationBinding = annotation.resolvedType;
		if (annotationBinding == null) {
			return false;
		}
		long metaTagBits = annotationBinding.getAnnotationTagBits();
		if ((metaTagBits & TagBits.AnnotationRetentionMASK) == 0)
			return false; // by default the retention is CLASS
			
		return (metaTagBits & TagBits.AnnotationRetentionMASK) == TagBits.AnnotationRuntimeRetention;
	}

	/**
	 * INTERNAL USE-ONLY
	 * Returns the most enclosing classfile of the receiver. This is used know to store the constant pool name
	 * for all inner types of the receiver.
	 * @return org.eclipse.jdt.internal.compiler.codegen.ClassFile
	 */
	public ClassFile outerMostEnclosingClassFile() {
		ClassFile current = this;
		while (current.enclosingClassFile != null)
			current = current.enclosingClassFile;
		return current;
	}

	/**
	 * INTERNAL USE-ONLY
	 * This is used to store a new inner class. It checks that the binding @binding doesn't already exist inside the
	 * collection of inner classes. Add all the necessary classes in the right order to fit to the specifications.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
	 */
	public void recordEnclosingTypeAttributes(ReferenceBinding binding) {
		// add all the enclosing types
		ReferenceBinding enclosingType = referenceBinding.enclosingType();
		int depth = 0;
		while (enclosingType != null) {
			depth++;
			enclosingType = enclosingType.enclosingType();
		}
		enclosingType = referenceBinding;
		ReferenceBinding enclosingTypes[];
		if (depth >= 2) {
			enclosingTypes = new ReferenceBinding[depth];
			for (int i = depth - 1; i >= 0; i--) {
				enclosingTypes[i] = enclosingType;
				enclosingType = enclosingType.enclosingType();
			}
			for (int i = 0; i < depth; i++) {
				addInnerClasses(enclosingTypes[i]);
			}
		} else {
			addInnerClasses(referenceBinding);
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * This is used to store a new inner class. It checks that the binding @binding doesn't already exist inside the
	 * collection of inner classes. Add all the necessary classes in the right order to fit to the specifications.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
	 */
	public void recordNestedLocalAttribute(ReferenceBinding binding) {
		// add all the enclosing types
		ReferenceBinding enclosingType = referenceBinding.enclosingType();
		int depth = 0;
		while (enclosingType != null) {
			depth++;
			enclosingType = enclosingType.enclosingType();
		}
		enclosingType = referenceBinding;
		ReferenceBinding enclosingTypes[];
		if (depth >= 2) {
			enclosingTypes = new ReferenceBinding[depth];
			for (int i = depth - 1; i >= 0; i--) {
				enclosingTypes[i] = enclosingType;
				enclosingType = enclosingType.enclosingType();
			}
			for (int i = 0; i < depth; i++)
				addInnerClasses(enclosingTypes[i]);
		} else {
			addInnerClasses(binding);
		}
	}

	/**
	 * INTERNAL USE-ONLY
	 * This is used to store a new inner class. It checks that the binding @binding doesn't already exist inside the
	 * collection of inner classes. Add all the necessary classes in the right order to fit to the specifications.
	 *
	 * @param binding org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
	 */
	public void recordNestedMemberAttribute(ReferenceBinding binding) {
		addInnerClasses(binding);
	}
	
	public void reset(SourceTypeBinding typeBinding) {
		// the code stream is reinitialized for each method
		final CompilerOptions options = typeBinding.scope.compilerOptions();
		this.referenceBinding = typeBinding;
		this.targetJDK = options.targetJDK;
		this.produceAttributes = options.produceDebugAttributes;
		if (this.targetJDK >= ClassFileConstants.JDK1_6) {
			this.produceAttributes |= ClassFileConstants.ATTR_STACK_MAP;
		}
		this.bytes = null;
		this.constantPool.reset();
		this.codeStream.reset(this);
		this.constantPoolOffset = 0;
		this.contentsOffset = 0;
		this.creatingProblemType = false;
		this.enclosingClassFile = null;
		this.headerOffset = 0;
		this.methodCount = 0;
		this.methodCountOffset = 0;
		this.numberOfInnerClasses = 0;
	}

	/**
	 * Resize the pool contents
	 */
	private final void resizeContents(int minimalSize) {
		int length = this.contents.length;
		int toAdd = length;
		if (toAdd < minimalSize)
			toAdd = minimalSize;
		System.arraycopy(this.contents, 0, this.contents = new byte[length + toAdd], 0, length);
	}

	/**
	 * INTERNAL USE-ONLY
	 * This methods leaves the space for method counts recording.
	 */
	public void setForMethodInfos() {
		// leave some space for the methodCount
		methodCountOffset = contentsOffset;
		contentsOffset += 2;
	}
}