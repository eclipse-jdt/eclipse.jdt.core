package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ConfigurableOption;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.builder.IBinaryBroker;
import org.eclipse.jdt.internal.core.builder.IBuildListener;
import org.eclipse.jdt.internal.core.builder.IBuildMonitor;
import org.eclipse.jdt.internal.core.builder.IDevelopmentContext;
import org.eclipse.jdt.internal.core.builder.IImage;
import org.eclipse.jdt.internal.core.builder.IImageBuilder;
import org.eclipse.jdt.internal.core.builder.IImageContext;
import org.eclipse.jdt.internal.core.builder.IPackage;
import org.eclipse.jdt.internal.core.builder.IProblemReporter;
import org.eclipse.jdt.internal.core.builder.IState;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.core.builder.NotPresentException;
import org.eclipse.jdt.internal.core.util.IProgressListener;

public class JavaDevelopmentContextImpl implements IDevelopmentContext {

	/**
	 * The default current state
	 */
	protected StateImpl fCurrentState;

	/**
	 * A handle to the image for this develoment context
	 */
	private IImage fImage = new ImageImpl(this);

	/**
	 * The binary broker output for storing compiled binaries
	 */
	private BinaryBrokerOutput fBinaryBrokerOutput;

	/**
	 * The build monitor for tracking what has been compiled -- only used for testing
	 */
	private IBuildMonitor fBuildMonitor;

	/**
	 * The progress monitor.  It is the client's responsibility to set
	 * the monitor.
	 */
	private IProgressMonitor fProgressMonitor = null;

	/**
	 * List of build listeners which get notified when things are (re)compiled or removed.
	 */
	private Vector fBuildListeners = new Vector(1);

	/**
	 * The default package.
	 */
	private final IPackage fDefaultPackage = fImage.getPackageHandle("java.lang"/*nonNLS*/, false);

	/**
	 * The root class handle
	 */
	private final IType fRootClass = fDefaultPackage.getClassHandle("Object"/*nonNLS*/);

	/**
	 * Primitive types
	 */
	final IType fVoidType = new PrimitiveTypeHandleImpl(this, 'V');
	final IType fIntType = new PrimitiveTypeHandleImpl(this, 'I');
	final IType fByteType = new PrimitiveTypeHandleImpl(this, 'B');
	final IType fCharType = new PrimitiveTypeHandleImpl(this, 'C');
	final IType fDoubleType = new PrimitiveTypeHandleImpl(this, 'D');
	final IType fFloatType = new PrimitiveTypeHandleImpl(this, 'F');
	final IType fLongType = new PrimitiveTypeHandleImpl(this, 'J');
	final IType fShortType = new PrimitiveTypeHandleImpl(this, 'S');
	final IType fBooleanType = new PrimitiveTypeHandleImpl(this, 'Z');

	/**
	 * Whether the compiler is in the call stack.
	 */
	boolean inCompiler; 
/**
 * Create a new Java DC.
 */
public JavaDevelopmentContextImpl() {
}
	/**
	 * @see IDevelopmentContext
	 */
	public void addBuildListener(IBuildListener buildListener) {
		fBuildListeners.addElement(buildListener);
	}
/**
  * Returns  a class type handle corresponding to the given 
  * (fully qualified) class type name.
  */
protected IType classTypeFromName(final String name) {

	/* strip off the leading "L" and trailing ";" */
	String localName = name.substring(1, name.length() - 1);
	int lastDot = localName.lastIndexOf('.');
	IPackage pkg;
	if (lastDot == -1) {
		pkg = getImage().getPackageHandle(IPackageFragment.DEFAULT_PACKAGE_NAME, true);
	} else {
		pkg = this.packageHandleFromSignature(localName.substring(0, lastDot));
	}
	return pkg.getClassHandle(localName.substring(lastDot + 1, localName.length()));
}
/**
 * @see IDevelopmentContext
 */
public IImageBuilder createState(IProject project, IImageContext buildContext) {
	return createState(project, buildContext, (IProblemReporter)null);
}
/**
 * @see IDevelopmentContext
 */
public IImageBuilder createState(IProject project, IImageContext buildContext, IProblemReporter problemReporter) {
	return createState(project, buildContext, problemReporter, getDefaultCompilerOptions());
}
/**
 * @see IDevelopmentContext
 */
public IImageBuilder createState(IProject project, IImageContext buildContext, IProblemReporter problemReporter, ConfigurableOption[] compilerOptions) {
	StateImpl state = new StateImpl(this, project, buildContext);
	BatchImageBuilder builder = new BatchImageBuilder(state, compilerOptions);
	if (problemReporter != null) {
		state.setProblemReporter(problemReporter);
	}
	builder.build();
	return builder;
}
/**
 * @see IDevelopmentContext
 */
public void garbageCollect(IState[] statesInUse) {
	getBinaryOutput().garbageCollect(statesInUse);
}
/**
 * Returns the binary broker for this development context.
 * Returns null if none has been assigned to the DC.
 */
public IBinaryBroker getBinaryBroker() {
	if (fBinaryBrokerOutput == null)
		return null;
	else
		return fBinaryBrokerOutput.getBinaryBroker();
}
/**
 * Workaround for 1GAMR1K: ITPCORE:Platform should be more fault tolerant
 */
byte[] getBinaryFromFileSystem(org.eclipse.core.resources.IFile file) {
	try {
		IPath location = file.getLocation();
		if (location == null) return new byte[0];
		InputStream input = new java.io.FileInputStream(location.toOSString());
		return org.eclipse.jdt.internal.core.Util.readContentsAsBytes(input);
	} catch (IOException e) {
		return new byte[0];
	}
}
/**
 * Returns the binary output for this development context.
 */
public BinaryOutput getBinaryOutput() {
	return fBinaryBrokerOutput;
}
/**
 * Returns the vector of build listeners.
 */
protected Vector getBuildListeners() {
	return fBuildListeners;
}
	/**
	 * Returns the build monitor.
	 */
	public IBuildMonitor getBuildMonitor() {
		return fBuildMonitor;
	}
public IState getCurrentState() throws NotPresentException {
	if (fCurrentState == null) {
		throw new NotPresentException(Util.bind("build.noState"/*nonNLS*/));
	}
	return fCurrentState;
}
/**
 * Reads the default compiler options.
 */
protected static ConfigurableOption[] getDefaultCompilerOptions() {
	ConfigurableOption[] options = Compiler.getDefaultOptions(Locale.getDefault());

	/**
	 * Ugly because this requires knowledge of the compiler's
	 * internal problem representation.
	 */
	setCompilerOption(options, 11, 1);
	setCompilerOption(options, 12, 1);
	return options;
}
/**
 * Returns the default package handle (java.lang).
 */
protected IPackage getDefaultPackage() {
	return fDefaultPackage;
}
/**
 * Returns the image handle
 */
public IImage getImage() {
	return fImage;
}
	/**
	 * Returns the progress monitor.  Returns null if one hasn't been set.
	 */
	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
	}
protected IType getRootClassHandle() {
	return fRootClass;
}
/**
 * Process an internal exception: if we're being called by the compiler, throw an AbortCompilation
 * otherwise throw an internal image builder exception.
 */
protected RuntimeException internalException(String message) {
	ImageBuilderInternalException imageBuilderException =
		new ImageBuilderInternalException(message);
	if (this.inCompiler) {
		return new AbortCompilation(true, imageBuilderException);
	} else {
		return imageBuilderException;
	}
}
/**
 * Process an internal exception: if we're being called by the compiler, throw an AbortCompilation
 * otherwise throw an internal image builder exception.
 */
protected RuntimeException internalException(Throwable t) {
	ImageBuilderInternalException imageBuilderException =
		new ImageBuilderInternalException(t);
	if (this.inCompiler) {
		return new AbortCompilation(true, imageBuilderException);
	} else {
		return imageBuilderException;
	}
}
	/**
	 * Returns a new package handle for the given signature.
	 */
	public IPackage packageHandleFromSignature (String signature) {
		return new PackageImpl(this, signature, false);
	}
/**
 * Returns the parameter type handles, extracted from the given method 
 * or constructor signature.  Parameter names can either be fully
 * qualified VM type names, or DC API source signature names.  The
 * class java.lang.String would be represented as either: 
 * 	VM name: Ljava.lang.String;
 *		DC name: QString;
 */
protected IType[] parameterTypesFromSignature(final String signature) {
	Vector typeVector = new Vector();

	/* The signature looks like this:
	 * 	name(<parm1><parm2><parm3>...)<return type>
	 */

	/* extract parameters from signature */
	String localSig = signature.substring(signature.indexOf('(') + 1, signature.lastIndexOf(')'));

	/* parse each parameter */
	while (localSig.length() > 0) {

		/* 
		 * Each parameter can be defined by the following productions:
		 * 	parameter: arrayType
		 * 	arrayType: [arrayType OR type
		 *		type: L<classname>; OR <single character for base type>
		 */

		/* skip array characters */
		int position = 0;
		while (localSig.charAt(position) == '[') {
			position++;
		}
		IType parmType;
		char c = localSig.charAt(position);
		if (c == 'L' || c == 'Q') {
			/* its a class type */
			int endIndex = localSig.indexOf(";"/*nonNLS*/) + 1;
			parmType = classTypeFromName(localSig.substring(position, endIndex));
			localSig = localSig.substring(endIndex);
		} else {
			/* its a base type */
			parmType = primitiveTypeFromTypeCode(localSig.charAt(position));
			localSig = localSig.substring(position + 1);
		}

		/* if its an array type */
		if (position != 0) {
			parmType = new ArrayTypeHandleImpl((TypeImpl) parmType, position);
		}
		typeVector.addElement(parmType);
	}

	/* convert results vector to an array */
	IType[] results = new IType[typeVector.size()];
	typeVector.copyInto(results);
	return results;
}
/**
 * Returns a primitive type handle corresponding to the given type code char.
 * Returns nulll if type code is not a valid primitive type code.
 */
protected IType primitiveTypeFromTypeCode(char typeCode) {
	switch (typeCode) {
		case 'V': return fVoidType;
		case 'I': return fIntType;
		case 'B': return fByteType;
		case 'C': return fCharType;
		case 'D': return fDoubleType;
		case 'F': return fFloatType;
		case 'J': return fLongType;
		case 'S': return fShortType;
		case 'Z': return fBooleanType;
		default: return null;
	}
}
	/**
	 * @see IDevelopmentContext
	 */
	public void removeBuildListener(IBuildListener buildListener) {
		fBuildListeners.removeElement(buildListener);
	}
/**
 * @see IDevelopmentContext.
 */
public IState restoreState(IProject project, DataInputStream in) throws IOException {
	try {
		return new StateSnap().read(this, project, in);
	}
	catch (RuntimeException e) {
		e.printStackTrace();
		throw e;
	}
	catch (Error e) {
		e.printStackTrace();
		throw e;
	}
}
/**
 * @see IDevelopmentContext.
 */
public void saveState(IState state, DataOutputStream out) throws IOException {
	try {
		new StateSnap().save((StateImpl) state, out);
	}
	catch (IOException e) {
		e.printStackTrace();
		throw e;
	}
	catch (RuntimeException e) {
		e.printStackTrace();
		throw e;
	}
	catch (Error e) {
		e.printStackTrace();
		throw e;
	}
}
/**
 * Sets the binary broker for this developent context
 */
public void setBinaryBroker(IBinaryBroker broker) {
	if (broker == null)
		fBinaryBrokerOutput = null;
	else
		fBinaryBrokerOutput = new BinaryBrokerOutput(broker);
}
	/**
	 * Sets the build monitor.  The build monitor is a hook used
	 * by the test suites to test the image builder's efficiency.
	 */
	public void setBuildMonitor(IBuildMonitor monitor) {
		fBuildMonitor = monitor;
	}
/**
 * Sets the build progress listener for this development context
 */
public void setBuildProgressListener(IProgressListener listener) {
}
/**
 * Sets a compiler option.  This seems awkward.
 */
protected static void setCompilerOption(ConfigurableOption[] options, int optionID, int valueIndex) {
	for (int i = 0; i < options.length; i++) {
		if (options[i].getID() == optionID) {
			options[i].setValueIndex(valueIndex);
			return;
		}
	}
}
/**
 * setCurrentState method comment.
 */
public void setCurrentState(IState state) {
	fCurrentState = (StateImpl)state;
}
	/**
	 * Sets the progress monitor for all build activities.
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}
	public String toString() {
		return "a JavaDevelopmentContextImpl("/*nonNLS*/ + fCurrentState + ")"/*nonNLS*/;
	}
}
