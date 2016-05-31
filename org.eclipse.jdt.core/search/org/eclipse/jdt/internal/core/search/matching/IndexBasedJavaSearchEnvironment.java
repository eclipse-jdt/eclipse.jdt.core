package org.eclipse.jdt.internal.core.search.matching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeId;
import org.eclipse.jdt.internal.core.nd.java.TypeRef;
import org.eclipse.jdt.internal.core.nd.java.model.IndexBinaryType;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.nd.util.PathMap;

public class IndexBasedJavaSearchEnvironment implements INameEnvironment, SuffixConstants {

	private Map<String, ICompilationUnit> workingCopies;
	private PathMap<Integer> mapPathsToRoots = new PathMap<>();
	private IPackageFragmentRoot[] roots;
	private int sourceEntryPosition;

	public IndexBasedJavaSearchEnvironment(List<IJavaProject> javaProject, org.eclipse.jdt.core.ICompilationUnit[] copies) {
		this.workingCopies = JavaSearchNameEnvironment.getWorkingCopyMap(copies);

		try {
			List<IPackageFragmentRoot> localRoots = new ArrayList<>();
			
			for (IJavaProject next : javaProject) {
				for (IPackageFragmentRoot nextRoot : next.getAllPackageFragmentRoots()) {
					localRoots.add(nextRoot);
				}
			}

			this.roots = localRoots.toArray(new IPackageFragmentRoot[0]);
		} catch (JavaModelException e) {
			this.roots = new IPackageFragmentRoot[0];
			// project doesn't exist
		}

		// Build the map of paths onto root indices
		int length = this.roots.length;
		for (int i = 0; i < length; i++) {
			IPath nextPath = JavaIndex.getLocationForElement(this.roots[i]);
			this.mapPathsToRoots.put(nextPath, i);
		}

		// Locate the position of the first source entry
		this.sourceEntryPosition = Integer.MAX_VALUE;
		for (int i = 0; i < length; i++) {
			IPackageFragmentRoot nextRoot = this.roots[i];
			try {
				if (nextRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
					this.sourceEntryPosition = i;
				}
			} catch (JavaModelException e) {
				// project doesn't exist
			}
		}
	}

	@Override
	public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		char[] binaryName = CharOperation.concatWith(compoundTypeName, '/');

		int bestEntryPosition = Integer.MAX_VALUE;
		NameEnvironmentAnswer result = null;
		ICompilationUnit cu = this.workingCopies.get(new String(binaryName));
		if (cu != null) {
			result = new NameEnvironmentAnswer((org.eclipse.jdt.internal.compiler.env.ICompilationUnit)cu, null);
			bestEntryPosition = this.sourceEntryPosition;
		}

		char[] fieldDescriptor = JavaNames.binaryNameToFieldDescriptor(binaryName);
		JavaIndex index = JavaIndex.getIndex();
		Nd nd = index.getNd();
		try (IReader lock = nd.acquireReadLock()) {
			NdTypeId typeId = index.findType(fieldDescriptor);

			if (typeId != null) {
				List<NdType> types = typeId.getTypes();
				for (NdType next : types) {
					NdResourceFile resource = next.getFile();

					IPath path = resource.getPath();
					Integer nextRoot = this.mapPathsToRoots.getMostSpecific(path);
					if (nextRoot != null) {
						IPackageFragmentRoot root = this.roots[nextRoot];

						ClasspathEntry classpathEntry = (ClasspathEntry)root.getRawClasspathEntry();
						AccessRestriction accessRestriction = classpathEntry.getAccessRuleSet().getViolatedRestriction(binaryName);
						TypeRef typeRef = TypeRef.create(next);
						IBinaryType binaryType = new IndexBinaryType(typeRef, resource.getLocation().getChars()); 
						NameEnvironmentAnswer nextAnswer = new NameEnvironmentAnswer(binaryType, accessRestriction);

						boolean useNewAnswer = isBetter(result, bestEntryPosition, nextAnswer, nextRoot);

						if (useNewAnswer) {
							bestEntryPosition = nextRoot;
							result = nextAnswer;
						}
					}
				}
			}
		} catch (JavaModelException e) {
			// project doesn't exist
		}

		return result;
	}

	public boolean isBetter(NameEnvironmentAnswer currentBest, int currentBestClasspathPosition,
			NameEnvironmentAnswer toTest, int toTestClasspathPosition) {
		boolean useNewAnswer = false;

		if (currentBest == null) {
			useNewAnswer = true;
		} else {
			if (toTest.isBetter(currentBest)) {
				useNewAnswer = true;
			} else {
				// If neither one is better, use the one with the earlier classpath position
				if (!currentBest.isBetter(toTest)) {
					useNewAnswer = (toTestClasspathPosition < currentBestClasspathPosition);
				}
			}
		}
		return useNewAnswer;
	}

	@Override
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		char[][] newArray = new char[packageName.length + 1][];
		for (int idx = 0; idx < packageName.length; idx++) {
			newArray[idx] = packageName[idx];
		}
		newArray[packageName.length] = typeName;
		return findType(newArray);
	}

	@Override
	public boolean isPackage(char[][] parentPackageName, char[] packageName) {
		char[] binaryPackageName = CharOperation.concatWith(parentPackageName, '/');
		char[] fieldDescriptorPrefix = CharArrayUtils.concat(JavaNames.FIELD_DESCRIPTOR_PREFIX, binaryPackageName,
				packageName, new char[] { '/' });

		// Search all the types that are a subpackage of the given package name. Return if we find any one of them on
		// the classpath of this project.
		JavaIndex index = JavaIndex.getIndex();
		Nd nd = index.getNd();
		try (IReader lock = nd.acquireReadLock()) {
			return !index.visitFieldDescriptorsStartingWith(fieldDescriptorPrefix,
					new FieldSearchIndex.Visitor<NdTypeId>() {
						@Override
						public boolean visit(NdTypeId typeId) {
							List<NdType> types = typeId.getTypes();
							for (NdType next : types) {
								NdResourceFile resource = next.getFile();

								IPath path = resource.getPath();

								if (IndexBasedJavaSearchEnvironment.this.mapPathsToRoots.containsPrefixOf(path)) {
									return false;
								}
							}
							return true;
						}
					});
		}
	}

	@Override
	public void cleanup() {
	}

	public static INameEnvironment create(List<IJavaProject> javaProjects, org.eclipse.jdt.core.ICompilationUnit[] copies) {
		if (JavaIndex.isEnabled()) {
			return new IndexBasedJavaSearchEnvironment(javaProjects, copies);
		} else {
			Iterator<IJavaProject> next = javaProjects.iterator();
			JavaSearchNameEnvironment result = new JavaSearchNameEnvironment(next.next(), copies);

			while (next.hasNext()) {
				result.addProjectClassPath((JavaProject)next.next());
			}
			return result;
		}
	}
}
