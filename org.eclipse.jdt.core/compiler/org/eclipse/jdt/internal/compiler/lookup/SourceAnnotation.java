package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;

/**
 * Annotation that came from source.
 * @author tyeung
 *
 */
public class SourceAnnotation implements IAnnotationInstance, TypeConstants
{
	private final Annotation astAnnotation;
	private final IElementValuePair[] pairs;
	
	public SourceAnnotation(Annotation astAnnotation)
	{
		this.astAnnotation = astAnnotation;
		final MemberValuePair[] astPairs = astAnnotation.memberValuePairs();
		int numberOfPairs = astPairs == null ? 0 : astPairs.length;
		if( numberOfPairs == 0 )
			this.pairs = NoElementValuePairs;
		else{
			this.pairs = new SourceElementValuePair[numberOfPairs];
			for( int i=0; i<numberOfPairs; i++ ){
				this.pairs[i] = new SourceElementValuePair(astPairs[i]);
			}
		}	
	}
	
	public ReferenceBinding getAnnotationType() {	
		return (ReferenceBinding)this.astAnnotation.resolvedType;
	}
	
	public IElementValuePair[] getElementValuePairs() { return this.pairs; }
}
