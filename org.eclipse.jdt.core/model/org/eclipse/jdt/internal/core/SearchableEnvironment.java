package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.codeassist.*;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;;

/**
 *	This class provides a <code>SearchableBuilderEnvironment</code> for code assist which
 *	uses the Java model as a search tool.  
 */
public class SearchableEnvironment
	implements ISearchableNameEnvironment, IJavaSearchConstants {
	protected NameLookup nameLookup;
	protected ICompilationUnit unitToSkip;
	public CompilationUnit unitToLookInside;

	protected IJavaProject project;

	/**
	 * Creates a SearchableEnvironment on the given project
	 */
	public SearchableEnvironment(IJavaProject project) throws JavaModelException {
		this.project = project;
		this.nameLookup = (NameLookup) ((JavaProject) project).getNameLookup();
	}

	/**
	 * Returns the given type in the the given package if it exists,
	 * otherwise <code>null</code>.
	 */
	protected NameEnvironmentAnswer find(String typeName, String packageName) {
		if (packageName == null)
			packageName = IPackageFragment.DEFAULT_PACKAGE_NAME;
		IType type =
			this.nameLookup.findType(
				typeName,
				packageName,
				false,
				INameLookup.ACCEPT_CLASSES | INameLookup.ACCEPT_INTERFACES);
		if (type == null) {
			// look inside the compilation unit that is being searched currently
			//for a non-public or inner type.
			if (this.unitToLookInside != null) {
				if (this.unitToLookInside.getParent().getElementName().equals(packageName)) {
					try {
						IType[] allTypes = this.unitToLookInside.getTypes();
						for (int i = 0; i < allTypes.length; i++) {
							if (allTypes[i].getElementName().equals(typeName)) {
								type = allTypes[i];
								break;
							}
						}
					} catch (JavaModelException e) {
					}
				}
			}
		}
		if (type != null) {
			if (type instanceof BinaryType) {
				try {
					return new NameEnvironmentAnswer(
						(IBinaryType) ((BinaryType) type).getRawInfo());
				} catch (JavaModelException npe) {
					return null;
				}
			} else { //SourceType
				try {
					// retrieve the requested type
					SourceTypeElementInfo sourceType = (SourceTypeElementInfo)((SourceType)type).getRawInfo();
					ISourceType topLevelType = sourceType;
					while (topLevelType.getEnclosingType() != null) {
						topLevelType = topLevelType.getEnclosingType();
					}
					// find all siblings (other types declared in same unit, since may be used for name resolution)
					IType[] types = sourceType.getHandle().getCompilationUnit().getTypes();
					ISourceType[] sourceTypes = new ISourceType[types.length];

					// in the resulting collection, ensure the requested type is the first one
					sourceTypes[0] = sourceType;
					for (int i = 0, index = 1; i < types.length; i++) {
						ISourceType otherType =
							(ISourceType) ((JavaElement) types[i]).getRawInfo();
						if (!otherType.equals(topLevelType))
							sourceTypes[index++] = otherType;
					}
					return new NameEnvironmentAnswer(sourceTypes);
				} catch (JavaModelException npe) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * @see SearchableBuilderEnvironment
	 */
	public void findPackages(char[] prefix, ISearchRequestor requestor) {
		this.nameLookup.seekPackageFragments(
			new String(prefix),
			true,
			new SearchableEnvironmentRequestor(requestor));
	}

	/**
	 * @see INameEnvironment
	 */
	public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		if (compoundTypeName == null)
			return null;
		int length = compoundTypeName.length;
		if (length == 1)
			return find(new String(compoundTypeName[0]), null);
		StringBuffer buffer = new StringBuffer(length * 6);
		int lengthM1 = length - 1;
		for (int i = 0; i < lengthM1; i++) {
			buffer.append(compoundTypeName[i]);
			if (i + 1 != lengthM1)
				buffer.append('.');
		}
		String className = new String(compoundTypeName[lengthM1]);
		return find(className, buffer.toString());
	}

	/**
	 * @see INameEnvironment
	 */
	public NameEnvironmentAnswer findType(char[] name, char[][] packages) {
		if (name == null)
			return null;

		if (packages == null || packages.length == 0)
			return find(new String(name), null);

		int length = packages.length;
		StringBuffer buffer = new StringBuffer(length * 6);
		for (int i = 0; i < length; i++) {
			buffer.append(packages[i]);
			if (i + 1 != length)
				buffer.append('.');
		}
		String className = new String(name);
		return find(className, buffer.toString());
	}

	/**
	 * @see ISearchableNameEnvironment
	 */
	public void findTypes(char[] prefix, final ISearchRequestor storage) {

		/*
			if (true){
				findTypes(new String(prefix), storage, INameLookup.ACCEPT_CLASSES | INameLookup.ACCEPT_INTERFACES);
				return;		
			}
		*/
		try {
			final String excludePath;
			if (this.unitToSkip != null) {
				if (!(this.unitToSkip instanceof IJavaElement)) {
					// revert to model investigation
					findTypes(
						new String(prefix),
						storage,
						INameLookup.ACCEPT_CLASSES | INameLookup.ACCEPT_INTERFACES);
					return;
				}
				excludePath =
					((IJavaElement) this.unitToSkip)
						.getUnderlyingResource()
						.getFullPath()
						.toString();
			} else {
				excludePath = null;
			}
			int lastDotIndex = CharOperation.lastIndexOf('.', prefix);
			char[] qualification, simpleName;
			if (lastDotIndex < 0) {
				qualification = null;
				simpleName = CharOperation.toLowerCase(prefix);
			} else {
				qualification = CharOperation.subarray(prefix, 0, lastDotIndex);
				simpleName =
					CharOperation.toLowerCase(
						CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length));
			}

			SearchEngine searchEngine = new SearchEngine();

			IProject projectRsc = (IProject) this.project.getUnderlyingResource();
			IJavaSearchScope scope =
				searchEngine.createJavaSearchScope(new IResource[] { projectRsc });

			IProgressMonitor progressMonitor = new IProgressMonitor() {
				boolean isCanceled = false;
				public void beginTask(String name, int totalWork) {
				}
				public void done() {
				}
				public void internalWorked(double work) {
				}
				public boolean isCanceled() {
					return isCanceled;
				}
				public void setCanceled(boolean value) {
					isCanceled = value;
				}
				public void setTaskName(String name) {
				}
				public void subTask(String name) {
				}
				public void worked(int work) {
				}
			};
			ITypeNameRequestor nameRequestor = new ITypeNameRequestor() {
				public void acceptClass(
					char[] packageName,
					char[] simpleTypeName,
					char[][] enclosingTypeNames,
					String path) {
					if (excludePath != null && excludePath.equals(path))
						return;
					if (enclosingTypeNames != null && enclosingTypeNames.length > 0)
						return; // accept only top level types
					storage.acceptClass(packageName, simpleTypeName, IConstants.AccPublic);
				}
				public void acceptInterface(
					char[] packageName,
					char[] simpleTypeName,
					char[][] enclosingTypeNames,
					String path) {
					if (excludePath != null && excludePath.equals(path))
						return;
					if (enclosingTypeNames != null && enclosingTypeNames.length > 0)
						return; // accept only top level types
					storage.acceptInterface(packageName, simpleTypeName, IConstants.AccPublic);
				}
			};
			try {
				searchEngine.searchAllTypeNames(
					projectRsc.getWorkspace(),
					qualification,
					simpleName,
					PREFIX_MATCH,
					CASE_INSENSITIVE,
					IJavaSearchConstants.TYPE,
					scope,
					nameRequestor,
					CANCEL_IF_NOT_READY_TO_SEARCH,
					progressMonitor);
			} catch (OperationCanceledException e) {
				findTypes(
					new String(prefix),
					storage,
					INameLookup.ACCEPT_CLASSES | INameLookup.ACCEPT_INTERFACES);
			}
		} catch (JavaModelException e) {
			findTypes(
				new String(prefix),
				storage,
				INameLookup.ACCEPT_CLASSES | INameLookup.ACCEPT_INTERFACES);
		}
	}

	/**
	 * Returns all types whose name starts with the given (qualified) <code>prefix</code>.
	 *
	 * If the <code>prefix</code> is unqualified, all types whose simple name matches
	 * the <code>prefix</code> are returned.
	 */
	private void findTypes(String prefix, ISearchRequestor storage, int type) {
		SearchableEnvironmentRequestor requestor =
			new SearchableEnvironmentRequestor(storage, this.unitToSkip);
		int index = prefix.lastIndexOf('.');
		if (index == -1) {
			this.nameLookup.seekTypes(prefix, null, true, type, requestor);
		} else {
			String packageName = prefix.substring(0, index);
			String className = prefix.substring(index + 1);
			JavaElementRequestor javaElementRequestor = new JavaElementRequestor();
			this.nameLookup.seekPackageFragments(packageName, false, javaElementRequestor);
			IPackageFragment[] packageFragments =
				javaElementRequestor.getPackageFragments();
			if (packageFragments == null)
				return;
			for (int i = 0, packagesLength = packageFragments.length;
				i < packagesLength;
				i++) {
				if (packageFragments[i] == null)
					continue;
				this.nameLookup.seekTypes(
					className,
					packageFragments[i],
					true,
					type,
					requestor);
			}
		}
	}

	/**
	 * @see SearchableBuilderEnvironment
	 */
	public boolean isPackage(char[][] parentPackageName, char[] subPackageName) {
		if (parentPackageName == null || parentPackageName.length == 0)
			return isTopLevelPackage(subPackageName);
		if (subPackageName == null)
			return false;
		int length = parentPackageName.length;
		StringBuffer buffer = new StringBuffer((length + 1) * 6);
		for (int i = 0; i < length; i++) {
			if (parentPackageName[i] == null || isQualified(parentPackageName[i]))
				return false;
			buffer.append(parentPackageName[i]);
			buffer.append('.');
		}
		if (isQualified(subPackageName)) {
			return false;
		}
		buffer.append(subPackageName);
		boolean result =
			this.nameLookup.findPackageFragments(buffer.toString(), false) != null;
		return result;

	}

	/**
	 * Returns true if there are no '.' characters in the given name.
	 */
	protected boolean isQualified(char[] name) {
		if (name != null) {
			return CharOperation.indexOf('.', name) > -1;
		}
		return false;
	}

	/**
	 * @see SearchableBuilderEnvironment
	 */
	public boolean isTopLevelPackage(char[] packageName) {
		if (packageName == null)
			return false;
		boolean result =
			!isQualified(packageName)
				&& this.nameLookup.findPackageFragments(new String(packageName), false) != null;
		return result;

	}

	/**
	 * Returns a printable string for the array.
	 */
	protected String toStringChar(char[] name) {
		return "[" /*nonNLS*/
		+ new String(name) + "]" /*nonNLS*/;
	}

	/**
	 * Returns a printable string for the array.
	 */
	protected String toStringCharChar(char[][] names) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			result.append(toStringChar(names[i]));
		}
		return result.toString();
	}
}