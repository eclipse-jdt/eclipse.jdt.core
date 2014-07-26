/*******************************************************************************
 * Copyright (c) 2014 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

public class ExternalAnnotationProvider {

	public static final String ANNOTATION_FILE_SUFFIX = ".eea"; //$NON-NLS-1$ // FIXME(SH): define file extension

	private File annotationSource;
	private Map<String,String> methodAnnotationSources;
	
	public ExternalAnnotationProvider(String baseDir, String typeName) throws IOException {
		this.annotationSource = new File(baseDir+File.separatorChar+typeName+ANNOTATION_FILE_SUFFIX);
		if (!this.annotationSource.exists()) throw new FileNotFoundException(this.annotationSource.getAbsolutePath());
		this.methodAnnotationSources = new HashMap<String, String>();
		initialize();
	}
	
	private void initialize() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.annotationSource)));
		try {
			String line = reader.readLine();
			if (!line.startsWith("class ")) // TODO properly evaluate class header //$NON-NLS-1$
				throw new IOException("missing class header in annotation file"); //$NON-NLS-1$
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				int pos=line.indexOf('=');
				if (pos == -1) throw new IOException("Illegal format for annotation file, missing '='"); //$NON-NLS-1$
				this.methodAnnotationSources.put(line.substring(0, pos), line.substring(pos+1));
			}
		} finally {
			reader.close();
		}
	}

	public ITypeAnnotationWalker forMethod(char[] selector, char[] signature, LookupEnvironment environment) {
		String source = this.methodAnnotationSources.get(String.valueOf(CharOperation.concat(selector, signature)));
		if (source != null)
			return new MethodAnnotationWalker(source.toCharArray(), 0, environment);
		return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
	}
	
	abstract class SingleMarkerAnnotation implements IBinaryAnnotation {
		@Override
		public IBinaryElementValuePair[] getElementValuePairs() {
			return ElementValuePairInfo.NoMembers;
		}
		protected char[] getBinaryTypeName(char[][] name) {
			return CharOperation.concat('L', CharOperation.concatWith(name, '/'), ';');
		}
	}

	class MethodAnnotationWalker implements ITypeAnnotationWalker {

		private SingleMarkerAnnotation NULLABLE = new SingleMarkerAnnotation() {
			@Override public char[] getTypeName() { return getBinaryTypeName(MethodAnnotationWalker.this.environment.getNullableAnnotationName()); }
		};
		private SingleMarkerAnnotation NONNULL = new SingleMarkerAnnotation() {
			@Override public char[] getTypeName() { return getBinaryTypeName(MethodAnnotationWalker.this.environment.getNonNullAnnotationName()); }
		};

		char[] source;
		int pos;
		LookupEnvironment environment;

		MethodAnnotationWalker(char[] source, int pos, LookupEnvironment environment) {
			super();
			this.source = source;
			this.pos = pos;
			this.environment = environment;
		}

		@Override
		public ITypeAnnotationWalker toMethodReturn() {
			int close = CharOperation.indexOf(')', this.source);
			if (close != -1)
				return new MethodAnnotationWalker(this.source, close+1, this.environment);
			return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
		}

		@Override
		public ITypeAnnotationWalker toReceiver() {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toTypeBound(short boundIndex) {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toSupertype(short index) {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toMethodParameter(short index) {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toThrows(int index) {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toTypeArgument(int rank) {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toWildcardBound() {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toNextArrayDimension() {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toNextNestedType() {
			return this;
		}

		@Override
		public IBinaryAnnotation[] getAnnotationsAtCursor(int currentTypeId) {
			if (this.pos != -1 && this.pos < this.source.length-2) {
				switch (this.source[this.pos]) {
					case 'T':
					case 'L':
						switch (this.source[this.pos+1]) {
							case '0':
								return new IBinaryAnnotation[]{ this.NULLABLE };
							case '1':
								return new IBinaryAnnotation[]{ this.NONNULL };
						}
				}				
			}
			return null;
		}

		@Override
		public ITypeAnnotationWalker toField() {
			throw new UnsupportedOperationException("Methods have no fields"); //$NON-NLS-1$
		}		
	}
}
