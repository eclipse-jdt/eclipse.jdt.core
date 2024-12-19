/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.processing.Processor;
import javax.tools.FileObject;
import javax.tools.ForwardingFileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.core.runtime.ILog;

import com.sun.source.util.Plugin;
import com.sun.tools.javac.file.PathFileObject;
import com.sun.tools.javac.main.Arguments;
import com.sun.tools.javac.main.DelegatingJavaFileManager;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.platform.PlatformDescription;
import com.sun.tools.javac.platform.PlatformUtils;
import com.sun.tools.javac.resources.CompilerProperties.Errors;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Context.Factory;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;

public class CachingJDKPlatformArguments extends Arguments {

	private static Map<String, JavaFileManager> platformFMCache = new ConcurrentHashMap<>();

	private final Options options;
	private final Context context;

	public static void preRegister(Context context) {
		context.put(Arguments.argsKey, (Factory<Arguments>) c -> new CachingJDKPlatformArguments(c));
	}

	private CachingJDKPlatformArguments(Context context) {
		super(context);
		this.options = Options.instance(context);
		this.context = context;
	}

	@Override
	public boolean handleReleaseOptions(Predicate<Iterable<String>> additionalOptions) {
		// mostly copied from super, only wrapping the platformDescription so its
		// fileManager is reusable
		String platformString = options.get(Option.RELEASE);

		checkOptionAllowed(platformString == null,
				option -> Log.instance(this.context).error(Errors.ReleaseBootclasspathConflict(option)),
				Option.BOOT_CLASS_PATH, Option.XBOOTCLASSPATH, Option.XBOOTCLASSPATH_APPEND,
				Option.XBOOTCLASSPATH_PREPEND, Option.ENDORSEDDIRS, Option.DJAVA_ENDORSED_DIRS, Option.EXTDIRS,
				Option.DJAVA_EXT_DIRS, Option.SOURCE, Option.TARGET, Option.SYSTEM, Option.UPGRADE_MODULE_PATH);

		if (platformString != null) {
			PlatformDescription platformDescription = toReusable(
					PlatformUtils.lookupPlatformDescription(platformString));
			if (platformDescription == null) {
				Log.instance(this.context).error(Errors.UnsupportedReleaseVersion(platformString));
				return false;
			}

			options.put(Option.SOURCE, platformDescription.getSourceVersion());
			options.put(Option.TARGET, platformDescription.getTargetVersion());

			context.put(PlatformDescription.class, platformDescription);

			if (!additionalOptions.test(platformDescription.getAdditionalOptions()))
				return false;

			JavaFileManager platformFM = platformDescription.getFileManager();
			DelegatingJavaFileManager.installReleaseFileManager(context, platformFM,
					context.get(JavaFileManager.class));
		}
		return true;
	}

	private static PlatformDescription toReusable(PlatformDescription delegate) {
		if (delegate == null) {
			return null;
		}
		return new PlatformDescription() {
			@Override
			public JavaFileManager getFileManager() {
				return platformFMCache.computeIfAbsent(getSourceVersion(), _ -> new ForwardingJavaFileManager<JavaFileManager>(delegate.getFileManager()) {
					@Override
					public void close() {
						// do nothing, to keep instance usable in the future
					}
					@Override
					public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
						var res = super.getFileForInput(location, packageName, relativeName);
						makeUnderlyingFileObjectUninterruptible(res);
						return res;
					}
					@Override
					public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
						var res = super.getJavaFileForInput(location, className, kind);
						makeUnderlyingFileObjectUninterruptible(res);
						return res;
					}
					@Override
					public Iterable<JavaFileObject> list(Location location, String packageName, java.util.Set<Kind> kinds, boolean recurse) throws IOException {
						var res = super.list(location, packageName, kinds, recurse);
						res.forEach(this::makeUnderlyingFileObjectUninterruptible);
						return res;
					}
					private void makeUnderlyingFileObjectUninterruptible(FileObject fo) {
						PathFileObject toUninterrupted = null;
						if (fo instanceof PathFileObject o) {
							toUninterrupted = o;
						}
						if (fo instanceof ForwardingFileObject<?> forwarding) {
							try {
								Field fileObjectField = ForwardingFileObject.class.getDeclaredField("fileObject");
								Object o = fileObjectField.get(forwarding);
								if (o instanceof PathFileObject pathFileObject) {
									toUninterrupted = pathFileObject;
								}
							} catch (Exception e) {
								ILog.get().error(e.getMessage(), e);
							}
						}
						if (toUninterrupted != null) {
							ZipFileSystemProviderWithCache.makeFileSystemUninterruptible(toUninterrupted.getPath().getFileSystem());
						}
					}
				});
			}

			@Override
			public String getSourceVersion() {
				return delegate.getSourceVersion();
			}

			@Override
			public String getTargetVersion() {
				return delegate.getTargetVersion();
			}

			@Override
			public List<PluginInfo<Processor>> getAnnotationProcessors() {
				return delegate.getAnnotationProcessors();
			}

			@Override
			public List<PluginInfo<Plugin>> getPlugins() {
				return delegate.getPlugins();
			}

			@Override
			public List<String> getAdditionalOptions() {
				return delegate.getAdditionalOptions();
			}

			@Override
			public void close() throws IOException {
				// DO NOTHING!
			}

		};
	}

	void checkOptionAllowed(boolean allowed, Consumer<Option> r, Option... opts) {
		if (!allowed) {
			Stream.of(opts).filter(options::isSet).forEach(r::accept);
		}
	}

}
