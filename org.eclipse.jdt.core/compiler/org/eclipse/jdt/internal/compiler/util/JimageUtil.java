package org.eclipse.jdt.internal.compiler.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JimageUtil {

	public static final String JAVA_BASE = "java.base"; //$NON-NLS-1$
	static final String MODULES_SUBDIR = "/modules"; //$NON-NLS-1$
	static final String[] DEFAULT_MODULE = new String[]{JAVA_BASE};
	static final String[] NO_MODULE = new String[0];
	static final String MULTIPLE = "MU"; //$NON-NLS-1$
	static final String DEFAULT_PACKAGE = ""; //$NON-NLS-1$
	static final String MODULES_ON_DEMAND = System.getProperty("modules"); //$NON-NLS-1$
	static URI JRT_URI = URI.create("jrt:/"); //$NON-NLS-1$

	private static Map<File, JimageFileSystem> images = null;

	public interface JimageVisitor<T> {

		public FileVisitResult visitPackage(T dir, T mod, BasicFileAttributes attrs) throws IOException;

		public FileVisitResult visitFile(T file, T mod, BasicFileAttributes attrs) throws IOException;
		/**
		 * Invoked when a root directory of a module being visited. The element returned 
		 * contains only the module name segment - e.g. "java.base". Clients can use this to control
		 * how the Jimage needs to be processed, for e.g., clients can skip a particular module
		 * by returning FileVisitResult.SKIP_SUBTREE
		 */
		public FileVisitResult visitModule(T mod) throws IOException;
	}

	static abstract class AbstractFileVisitor<T> implements FileVisitor<T> {
		@Override
		public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}
	
		@Override
		public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}
	
		@Override
		public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}
	
		@Override
		public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}
	}

	public static JimageFileSystem getJimageSystem(File image) {
		JimageFileSystem system = null;
		if (images == null) {
			images = new HashMap<>();
			images.put(image, system = new JimageFileSystem(image));
			return system;
		}
		system = images.get(image);
		if (system == null) {
			images.put(image, system = new JimageFileSystem(image));
		}
		return system;
	}

	/**
	 * Given the path of a modular image file, this method walks the archive content and
	 * notifies the supplied visitor about packages and files visited.
	 * Note: At the moment, there's no way to open any arbitrary image. Currently,
	 * this method uses the JRT file system provider to look inside the JRE.
	 *
	 * The file system contains the following top level directories:
	 *  /modules/$MODULE/$PATH
	 *  /packages/$PACKAGE/$MODULE 
	 *  The latter provides quick look up of the module that contains a particular package. However,
	 *  this method only notifies its clients of the entries within the modules sub-directory.
	 *  
	 * @param image a java.io.File handle to the JRT image.
	 * @param visitor an instance of JimageVisitor to be notified of the entries in the JRT image.
	 * @throws IOException
	 */
	public static void walkModuleImage(File image, final JimageUtil.JimageVisitor<java.nio.file.Path> visitor) throws IOException {
		getJimageSystem(image).walkModuleImage(visitor, false);
	}

	public static InputStream getContentFromJimage(File jimage, String fileName, String module) throws IOException {
		return getJimageSystem(jimage).getContentFromJimage(fileName, module);
	}

	public static byte[] getClassfileContent(File jimage, String fileName, String module) throws IOException {
		return getJimageSystem(jimage).getClassfileContent(fileName, module);
	}
}
class JimageFileSystem {
	private final Map<String, String> packageToModule = new HashMap<String, String>();

	private final Map<String, List<String>> packageToModules = new HashMap<String, List<String>>();
	
	private final Set<String> notFound = new HashSet<>();

	/**
	 * As of now, the passed reference to the jimage file is not being used. Perhaps eventually
	 * when we know how to read a particular jimage, we will make use of this.
	 *
	 * @param image
	 */
	public JimageFileSystem(File image) {
		initialize(image);
	}
	void initialize(File image) {
		try {
			walkModuleImage(null, true);
		} catch (IOException e) {
			// Continue to exist as a dummy file system?
		}
	}

	public String[] getModules(String fileName) {
		int idx = fileName.lastIndexOf('/');
		String pack = null;
		if (idx != -1) {
			pack = fileName.substring(0, idx);
		} else {
			pack = JimageUtil.DEFAULT_PACKAGE;
		}
		String module = this.packageToModule.get(pack);
		if (module != null) {
			if (module == JimageUtil.MULTIPLE) {
				List<String> list = this.packageToModules.get(pack);
				return list.toArray(new String[list.size()]);
			} else {
				return new String[]{module};
			}
		}
		return JimageUtil.DEFAULT_MODULE;
	}

	public InputStream getContentFromJimage(String fileName, String module) throws IOException {
		java.nio.file.FileSystem fs = FileSystems.getFileSystem(JimageUtil.JRT_URI);
		if (module != null) {
			return Files.newInputStream(fs.getPath(JimageUtil.MODULES_SUBDIR, module, fileName));
		}
		String[] modules = getModules(fileName);
		for (String mod : modules) {
			return Files.newInputStream(fs.getPath(JimageUtil.MODULES_SUBDIR, mod, fileName));
		}
		return null;
	}

	public byte[] getClassfileContent(String fileName, String module) throws IOException {
		if (this.notFound.contains(fileName)) 
			return null;
		java.nio.file.FileSystem fs = FileSystems.getFileSystem(JimageUtil.JRT_URI);
		if (module != null) {
			return Files.readAllBytes(fs.getPath(JimageUtil.MODULES_SUBDIR, module, fileName));
		}
		String[] modules = getModules(fileName);
		for (String string : modules) {
			try {
				byte[] bytes = Files.readAllBytes(fs.getPath(JimageUtil.MODULES_SUBDIR, string, fileName));
				if (bytes != null) return bytes;
			} catch(NoSuchFileException e) {
				continue;
			}
		}
		this.notFound.add(fileName);
		return null;
	}

	void walkModuleImage(final JimageUtil.JimageVisitor<java.nio.file.Path> visitor, boolean visitPackageMapping) throws IOException {
		java.nio.file.FileSystem fs = FileSystems.getFileSystem(JimageUtil.JRT_URI);
		Iterable<java.nio.file.Path> roots = fs.getRootDirectories();
		for (java.nio.file.Path path : roots) {
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(path)) {
				for (final java.nio.file.Path subdir: stream) {
					if (subdir.toString().equals(JimageUtil.MODULES_SUBDIR)) {
						if (visitPackageMapping) continue;
						Files.walkFileTree(subdir, new JimageUtil.AbstractFileVisitor<java.nio.file.Path>() {
							@Override
							public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs) throws IOException {
								int count = dir.getNameCount();
								if (count == 2) {
									java.nio.file.Path mod = dir.getName(1);
									if (JimageUtil.MODULES_ON_DEMAND != null && JimageUtil.MODULES_ON_DEMAND.indexOf(mod.toString()) == -1) {
										return FileVisitResult.SKIP_SUBTREE;
									}
									return visitor.visitModule(mod);
								}
								if (dir == subdir || count < 3) return FileVisitResult.CONTINUE;
								return visitor.visitPackage(dir.subpath(2, count), dir.getName(1), attrs);
							}

							@Override
							public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
								int count = file.getNameCount();
								// This happens when a file in a default package is present. E.g. /modules/some.module/file.name
								if (count == 3) {
									cachePackage(JimageUtil.DEFAULT_PACKAGE, file.getName(1).toString());
								}
								return visitor.visitFile(file.subpath(2, file.getNameCount()), file.getName(1), attrs);
							}
						});
					} else if (visitPackageMapping) {
						Files.walkFileTree(subdir, new JimageUtil.AbstractFileVisitor<java.nio.file.Path>() {
							@Override
							public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
								java.nio.file.Path relative = subdir.relativize(file);
								cachePackage(relative.getParent().toString(), relative.getFileName().toString());
								return FileVisitResult.CONTINUE;
							}
						});
					}
			    }
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}
	}

	void cachePackage(String packageName, String module) {
		packageName = packageName.intern();
		module = module.intern();
		packageName = packageName.replace('.', '/');
		Object current = this.packageToModule.get(packageName);
		if (current == null) {
			this.packageToModule.put(packageName, module);
		} else if(current == module || current.equals(module)) {
			return;
		} else if (current == JimageUtil.MULTIPLE) {
			List<String> list = this.packageToModules.get(packageName);
			if (!list.contains(module)) {
				if (JimageUtil.JAVA_BASE == module || JimageUtil.JAVA_BASE.equals(module)) {
					list.add(0, JimageUtil.JAVA_BASE);
				} else {
					list.add(module);
				}
			}
		} else {
			String first = (String) current;
			this.packageToModule.put(packageName, JimageUtil.MULTIPLE);
			List<String> list = new ArrayList<String>();
			// Just do this as comparator might be overkill
			if (JimageUtil.JAVA_BASE == current || JimageUtil.JAVA_BASE.equals(current)) {
				list.add(first);
				list.add(module);
			} else {
				list.add(module);
				list.add(first);
			}
			this.packageToModules.put(packageName, list);
		}
	}
}