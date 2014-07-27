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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.SignatureWrapper;

public class ExternalAnnotationProvider {

	public static final String ANNOTATION_FILE_SUFFIX = ".eea"; //$NON-NLS-1$ // FIXME(SH): define file extension

	private File annotationSource;
	private Map<String,String> methodAnnotationSources;
	
	public ExternalAnnotationProvider(String baseDir, String typeName) throws IOException {
		this.annotationSource = new File(baseDir+File.separatorChar+typeName+ANNOTATION_FILE_SUFFIX);
		if (!this.annotationSource.exists()) throw new FileNotFoundException(this.annotationSource.getAbsolutePath());
		this.methodAnnotationSources = new HashMap<String, String>();
		initialize(typeName);
	}
	
	private void initialize(String typeName) throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(this.annotationSource)));
		try {
			String line = reader.readLine();
			if (!line.startsWith("class ")) // TODO properly evaluate class header //$NON-NLS-1$
				throw new IOException("missing class header in annotation file"); //$NON-NLS-1$
			if (!line.endsWith(typeName))
				throw new IOException("mismatching class name in annotation file, expected "+typeName+", but header said "+line); //$NON-NLS-1$ //$NON-NLS-2$
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				String rawSig = null, annotSig = null;
				// selector:
				String selector = line;
				int errLine = -1;
				try {
					// raw signature:
					line = reader.readLine();
					if (line.charAt(0) == ' ')
						rawSig = line.substring(1);
					else
						errLine = reader.getLineNumber();
					// annotated signature:
					line = reader.readLine();
					if (line.charAt(0) == ' ')
						annotSig = line.substring(1);
				} catch (Exception ex) {
					// continue to escalate below
				}
				if (rawSig == null || annotSig == null) {
					if (errLine == -1) errLine = reader.getLineNumber();
					throw new IOException("Illegal format for annotation file at line "+errLine); //$NON-NLS-1$
				}
				this.methodAnnotationSources.put(selector+rawSig, annotSig);
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
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("External Annotations from "+this.annotationSource.getAbsolutePath()).append('\n'); //$NON-NLS-1$
		sb.append("Methods:\n"); //$NON-NLS-1$
		for (Entry<String,String> e : this.methodAnnotationSources.entrySet())
			sb.append('\t').append(e.getKey()).append('\n');
		return sb.toString();
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
		SignatureWrapper wrapper;
		int pos;
		int prevParamStart;
		int prevTypeArgStart;
		LookupEnvironment environment;

		MethodAnnotationWalker(char[] source, int pos, LookupEnvironment environment) {
			super();
			this.source = source;
			this.pos = pos;
			this.environment = environment;
		}
		
		SignatureWrapper wrapperWithStart(int start) {
			if (this.wrapper == null)
				this.wrapper = new SignatureWrapper(this.source);
			this.wrapper.start = start;
			return this.wrapper;
		}

		int typeEnd(int start) {
			while (this.source[start] == '[') {
				start++;
				char an = this.source[start];
				if (an == '0' || an == '1')
					start++;
			}
			int end = wrapperWithStart(start).computeEnd();
			return end;
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
			if (index == 0) {
				int start = CharOperation.indexOf('(', this.source) + 1;
				this.prevParamStart = start;
				return new MethodAnnotationWalker(this.source, start, this.environment);
			}
			int end = typeEnd(this.prevParamStart);
			end++;
		    this.prevParamStart = end;
		    return new MethodAnnotationWalker(this.source, end, this.environment);
		}

		@Override
		public ITypeAnnotationWalker toThrows(int index) {
			return this;
		}

		@Override
		public ITypeAnnotationWalker toTypeArgument(int rank) {
			if (rank == 0) {
				int start = CharOperation.indexOf('<', this.source, this.pos) + 1;
				this.prevTypeArgStart = start;
				return new MethodAnnotationWalker(this.source, start, this.environment);
			}
			int next = this.prevTypeArgStart;
			switch (this.source[next]) {
				case '*': 
					break;
				case '-': 
				case '+':
					next++;
					//$FALL-THROUGH$
				default:
					next = wrapperWithStart(next).computeEnd();
			}
			next++;
		    this.prevTypeArgStart = next;
		    return new MethodAnnotationWalker(this.source, next,	this.environment);
		}

		@Override
		public ITypeAnnotationWalker toWildcardBound() {
			switch (this.source[this.pos]) {
				case '-': 
				case '+':
					return new MethodAnnotationWalker(this.source, this.pos+1, this.environment);
				default: // includes unbounded '*'
					return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
			}			
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
