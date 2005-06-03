package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;

/**
 * Annotation that came from source.
 * @author tyeung
 *
 */
public class SourceAnnotation implements IAnnotationInstance, TypeConstants
{	
	private ReferenceBinding type;
	private final IElementValuePair[] pairs;
	
	public SourceAnnotation(Annotation astAnnotation)
	{	
		this.type = (ReferenceBinding)astAnnotation.resolvedType;
		if( astAnnotation instanceof NormalAnnotation ){
			final MemberValuePair[] astPairs = ((NormalAnnotation)astAnnotation).memberValuePairs;
			final int numberOfPairs = astPairs == null ? 0 : astPairs.length;			
			if( numberOfPairs > 0 ){
				this.pairs = new SourceElementValuePair[numberOfPairs];
				for( int i=0; i<numberOfPairs; i++ ){
					this.pairs[i] = new SourceElementValuePair(astPairs[i]);
				}				
			}
			else
				this.pairs = NoElementValuePairs;
		}
		else if( astAnnotation instanceof SingleMemberAnnotation ){
			final MemberValuePair astPair = ((SingleMemberAnnotation)astAnnotation).memberValuePairs()[0];
			if( astPair != null )
				this.pairs = new SourceElementValuePair[]{ new SourceElementValuePair(astPair) };
			else
				this.pairs = NoElementValuePairs;
		}
		else 
			this.pairs = NoElementValuePairs;
	}
	
	public ReferenceBinding getAnnotationType() {	
		return this.type;
	}
	
	public IElementValuePair[] getElementValuePairs() { return this.pairs; }
}
