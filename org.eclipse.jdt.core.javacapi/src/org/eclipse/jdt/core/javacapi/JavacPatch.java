/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.core.javacapi;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.URIUtil;

import nbjavac.VMWrapper;

public class JavacPatch {
	private static Reference<Path> cachedCtSym = new SoftReference<>(null);

	public static void loadVMWrapperPatch() {
		try {
			Field cachedCtSymField = VMWrapper.class.getDeclaredField("cachedCtSym");
			cachedCtSymField.setAccessible(true);
			if (cachedCtSym.get() == null) {
				cachedCtSym = new SoftReference<>(findCtSym());
			}
			cachedCtSymField.set(null, cachedCtSym);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

    private static Path findCtSym() {
        Path obj = cachedCtSym.get();
        if (obj instanceof Path) {
            return obj;
        }
        try {
            ClassLoader loader = VMWrapper.class.getClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
            Enumeration<URL> en = loader.getResources("META-INF/services/com.sun.tools.javac.platform.PlatformProvider");
            URL res = en.hasMoreElements() ? en.nextElement() : null;
            if (res == null) {
                throw new IllegalStateException("Cannot find ct.sym");
            }
            URL jar = FileLocator.resolve(res);
            String jarFile = jar.getFile();
            int idx = jarFile.indexOf('!');
            if (idx >= 0) {
            	jarFile = jarFile.substring(0, idx);
            }
            Path path = Paths.get(URIUtil.fromString(jarFile));
            FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null);
            Path ctSym = fs.getPath("META-INF", "ct.sym");
            cachedCtSym = new SoftReference<>(ctSym);
            return ctSym;
        } catch (IOException | URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
