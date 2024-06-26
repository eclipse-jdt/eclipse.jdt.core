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

package org.eclipse.jdt.internal.javac;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.code.Symbol.ClassSymbol;

public class JavacTaskListener implements TaskListener {
	private Map<ICompilationUnit, IContainer> sourceOutputMapping = new HashMap<>();

	public JavacTaskListener(JavacConfig config, Map<File, List<ICompilationUnit>> outputSourceMapping) {
		Map<File, IContainer> outputs = config.originalConfig().sourceOutputMapping().values().stream()
				.distinct().filter(container -> container.getRawLocation() != null)
				.collect(Collectors.toMap(container -> container.getRawLocation().toFile(), container -> container));
		for (Entry<File, List<ICompilationUnit>> entry : outputSourceMapping.entrySet()) {
			if (outputs.containsKey(entry.getKey())) {
				IContainer currentOutput = outputs.get(entry.getKey());
				entry.getValue().forEach(cu -> sourceOutputMapping.put(cu, currentOutput));
			}
		}
	}

	@Override
	public void finished(TaskEvent e) {
		if (e.getKind() == TaskEvent.Kind.GENERATE) {
			if (e.getSourceFile() instanceof JavacFileObject sourceFile) {
				ICompilationUnit originalUnit = sourceFile.getOriginalUnit();
				IContainer outputDir = this.sourceOutputMapping.get(originalUnit);
				if (outputDir != null) {
					TypeElement element = e.getTypeElement();
					if (element instanceof ClassSymbol clazz) {
						String fileName = clazz.flatName().toString().replace('.', File.separatorChar);
						IPath filePath = new Path(fileName);
						IFile classFile = outputDir.getFile(filePath.addFileExtension(SuffixConstants.EXTENSION_class));
						try {
							// refresh the class file to make sure it is visible in the Eclipse workspace
							classFile.refreshLocal(IResource.DEPTH_ZERO, null);
						} catch (CoreException e1) {
							// TODO error handling
						}
					}
				}
			}
		}
	}
}
