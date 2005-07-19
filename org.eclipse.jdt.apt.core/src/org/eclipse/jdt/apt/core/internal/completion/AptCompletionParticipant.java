/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.apt.core.completion.AnnotationCompletionAssistant;
import org.eclipse.jdt.apt.core.completion.AptCompletionEvent;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorFactory;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.completion.AptCompletionEventImpl.AnnotationsImpl;
import org.eclipse.jdt.apt.core.internal.completion.AptCompletionEventImpl.CompletionNodeImpl;
import org.eclipse.jdt.apt.core.internal.completion.AptCompletionEventImpl.NameReferenceImpl;
import org.eclipse.jdt.apt.core.internal.declaration.DeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.Visitors.DeclarationFinder;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.completion.ICompletionEvent;
import org.eclipse.jdt.core.completion.ICompletionParticipant;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IResolvedAnnotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * Completion Participant that interest in completion of member values of 
 * annotations.
 *
 */
public class AptCompletionParticipant implements ICompletionParticipant
{
	private static final String VALUE = "value"; //$NON-NLS-1$
	
	private static final AnnotationCompletionAssistant NO_OP_ASSISTANT =
		new AnnotationCompletionAssistant(){
			public void completeOnMemberValue(AptCompletionEvent event) {}
			public ICompletionFilter getCompletionFilter(AptCompletionEvent event) {				
				return null;
			}
			public void endOfEvent(AptCompletionEvent event){}			
		};
		
	private static final EclipseAnnotationProcessorFactory[] NO_FACTORIES =
		new EclipseAnnotationProcessorFactory[0];
	
	private final AnnotationProcessorFactoryLoader _factoryLoader;	
	private final Map<ICompletionEvent, EventDispatchInfo> dispatchCache;
	private final EventDispatchInfo NoOpEvent; 
	/**
	 * This class is constructed indirectly, by registering an extension to the 
	 * org.eclipse.jdt.core.compilationParticipants extension point.
	 */
	public AptCompletionParticipant()
	{
		_factoryLoader = AnnotationProcessorFactoryLoader.getLoader();
		dispatchCache = 
			Collections.synchronizedMap( new HashMap<ICompletionEvent, EventDispatchInfo>(4) );
	
        NoOpEvent = new EventDispatchInfo(null);
       
	}
	
	private List<EclipseAnnotationProcessorFactory> getParticipatingFactories(final IJavaProject javaProj)
	{
		final List<AnnotationProcessorFactory> allFactories = 
			_factoryLoader.getFactoriesForProject(javaProj);
		
		final List<EclipseAnnotationProcessorFactory> eFactories = new ArrayList();
		for( int i=0, len=allFactories.size(); i<len; i++ ){
        	final AnnotationProcessorFactory f = allFactories.get(i);
        	if( f instanceof EclipseAnnotationProcessorFactory ){
        		if( ((EclipseAnnotationProcessorFactory)f).supportCodeAssistant() )
        			eFactories.add((EclipseAnnotationProcessorFactory) f); 
        	}	
        }
		if( eFactories.isEmpty()) return null;
		return eFactories;
	}
	
	private boolean hasParticipatingFactories(final IJavaProject javaProj)
	{
		final List<AnnotationProcessorFactory> allFactories = 
			_factoryLoader.getFactoriesForProject(javaProj);
		
		for( int i=0, len=allFactories.size(); i<len; i++ ){
        	final AnnotationProcessorFactory f = allFactories.get(i);
        	if( f instanceof EclipseAnnotationProcessorFactory ){
        		if( ((EclipseAnnotationProcessorFactory)f).supportCodeAssistant() )
        			return true;
        	}	
        }
		
		return false;
	}
	
	public int getSupportedEventMask()
	{
		return ICompletionParticipant.COMPLETION_INSIDE_ANNOTATION;
	}
	
	// (non-javadoc)
	// participation depends on the number of eclipse annotation factories
	// the has code assist turned on. see constructor of class
	public boolean doesParticipateInProject(IJavaProject project) {
		return hasParticipatingFactories(project);
	}
	
	private ASTNode findAnnotationOrMemberValuePair(ASTNode node)
	{
		LOOP:
		while(node != null){
			
			switch( node.getNodeType() )
			{
			
			case ASTNode.MEMBER_VALUE_PAIR:
			case ASTNode.MARKER_ANNOTATION:
			case ASTNode.SINGLE_MEMBER_ANNOTATION:
			case ASTNode.NORMAL_ANNOTATION:			
				return node;			
			}
			
			node = node.getParent();
		}	
		return null;
	}
	
	
	public void complete(final ICompletionEvent event) {
		if( event == null ) return;
		
		final EventDispatchInfo dispatchInfo = getEventDispatchInfo(event);
		if( dispatchInfo.isNoOpEvent() ) 
			return;
		else{
			dispatchInfo.computeDispatch();
			final AnnotationCompletionAssistant assistant = dispatchInfo.getDispatchedAssistant();
			assistant.completeOnMemberValue(dispatchInfo.aptEvent);
		}
	}
	
	public void endOfEvent(ICompletionEvent event) {
		final EventDispatchInfo dispatch = dispatchCache.remove(event);
		if( dispatch != null ){
			final AnnotationCompletionAssistant assistant = dispatch.getDispatchedAssistant();
			if( assistant != null )
				assistant.endOfEvent(dispatch.aptEvent);
		}
	}
	
	/**
	 * @param node 
	 * @return the list of annotation nodes that are parents of the parameter.
	 *         The first in the returned list is annotation 'closest' to
	 *         the parameter
	 */
	private AnnotationsImpl computeAnnotationStack(final ASTNode node)
	{
		int size = 0;
		ASTNode temp = node;
		while( temp != null ){
			if( temp instanceof Annotation ){
				size ++;
			}
			temp = temp.getParent();
		}
		if(size == 0) return null;
		temp = node;
		if(size == 1){			
			Annotation anno = null;
			while( temp != null ){
				if( temp instanceof Annotation ){
					anno = (Annotation)temp;
					final IResolvedAnnotation resolved = anno.resolveAnnotation();
					final ITypeBinding type = resolved.getAnnotationType();
					// we won't know where to dispatch, so abort.
					if( type == null || !type.isAnnotation() )
						return null;
					break;
				}
				temp = temp.getParent();
			}
			if( temp == null )
				return null;
			return new AnnotationsImpl(anno);
		}
		else{
			Annotation[] annos = new Annotation[size];
			int index = 0;
			boolean haveSomeType = false;
			while( temp != null ){
				if( temp instanceof Annotation ){
					final Annotation anno = (Annotation)temp;
					annos[index++] = anno;
					final IResolvedAnnotation resolved = anno.resolveAnnotation();
					final ITypeBinding type = resolved.getAnnotationType();
					if( type != null && type.isAnnotation() )
						haveSomeType = true;
					break;
				}
				temp = temp.getParent();
			}
			if( haveSomeType)
				return new AnnotationsImpl(annos);
			else
				return null;
		}
	}
	
	private static AptCompletionEvent.CompletionNode convertExpression(final Expression expr,
																	   final DeclarationImpl annotated,
																	   final BaseProcessorEnv env)
	{
		if(expr == null) return null;
		Object value = null;
		final Object constantValue = expr.resolveConstantExpressionValue();		
		if( constantValue != null)
			value = constantValue;
		else if( expr instanceof Name ){
			value = new NameReferenceImpl((Name)expr, env);
		}
		else if( expr instanceof Annotation ){
			final IResolvedAnnotation resolvedAnno = ((Annotation)expr).resolveAnnotation();
			value = Factory.createAnnotationMirror(resolvedAnno, annotated, env);
		}
		if( value == null )
			return null;
		
		return new CompletionNodeImpl(value, expr.getStartPosition(), expr.getLength());
		
	}
	
	private EventDispatchInfo getEventDispatchInfo(ICompletionEvent event )
	{
		EventDispatchInfo dispatchEvent = dispatchCache.get(event);
		if( dispatchEvent == null ){
			final AptCompletionEventImpl aptEvent = buildCompletionEvent(event);
			if( aptEvent == null )
				dispatchEvent = NoOpEvent;
			else
				dispatchEvent = new EventDispatchInfo(aptEvent);
			dispatchCache.put(event, dispatchEvent);
		}
		
		return dispatchEvent;
	}
	
	private AptCompletionEventImpl buildCompletionEvent(ICompletionEvent event)
	{
		final ASTNode recoveredNode = event.getRecoveredASTNode();		
		final ASTNode parent = findAnnotationOrMemberValuePair(recoveredNode);
		if(parent == null) return null;
		final AnnotationsImpl enclosings;
		final String membername;
		// the recovered node must then be the value of the value pair. 
		if(parent instanceof MemberValuePair ){
			final MemberValuePair pair = (MemberValuePair)parent;
			enclosings = computeAnnotationStack(parent);			
			// something is not right, either we didn't find an Annotaiton or
			// the annotation is not sufficient for completion dispatch
			if( enclosings == null ) return null;
			if(pair.getName() == null )
				membername = ""; //$NON-NLS-1$
			else{
				final String simpleName = pair.getName().getIdentifier();
				membername = simpleName.length() == 0 ? VALUE : simpleName;	
			}
		}
		else if( parent instanceof Annotation ){
			// only complete it's a single member annotation
			final Annotation annoNode = (Annotation)parent;
			final IResolvedAnnotation resolved = annoNode.resolveAnnotation();
			final ITypeBinding annoType = resolved.getAnnotationType();
			if( annoType == null ) return null;
			final IMethodBinding[] methods = annoType.getDeclaredMethods();
			if( methods == null && methods.length != 1 )
				return null;
				
			enclosings = computeAnnotationStack(parent);
			// the information is not sufficient for dispatch
			if( enclosings == null ) return null;
			membername = VALUE;
		}
		else
			return null;

		final CompilationUnit unit = (CompilationUnit)recoveredNode.getRoot();
		final CompletionEnvImpl env = new CompletionEnvImpl(unit, 
														    event.getIFile(),
														    event.getJavaProject());	
		
		final DeclarationFinder finder = new DeclarationFinder(enclosings.getOuterMost());
		unit.accept(finder);
		final DeclarationImpl annotated;
		final ASTNode annotatedNode = finder.getAnnotatedNode();
		if( annotatedNode == null )
			annotated = null;
		else{
			// if it is a parameter
			if( annotatedNode.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION ){
				annotated = Factory.createParameterDeclaration((SingleVariableDeclaration)annotatedNode, env);
			}
			else{
				final List<IBinding> bindings = new ArrayList<IBinding>(1);
				BaseProcessorEnv.getBinding(annotatedNode, bindings);
				if( bindings.isEmpty() )
					annotated = null;
				else
					annotated = Factory.createDeclaration(bindings.get(0), env);
			}
		}	
		enclosings.setAnnotated(annotated);
		enclosings.setEnvironment(env);
		
		final AptCompletionEvent.CompletionNode srcValue = recoveredNode instanceof Expression ?
															convertExpression((Expression)recoveredNode, annotated, env) :
															null;
		final AptCompletionEventImpl aptEvent = 
			new AptCompletionEventImpl(event, membername, srcValue, enclosings);
		
		return aptEvent;
	}
	
	public ICompletionFilter getCompletionFilter(ICompletionEvent event) {
		if( event == null ) return null; 
		
		final EventDispatchInfo dispatchInfo = getEventDispatchInfo(event);
		if( dispatchInfo.isNoOpEvent() ) return null;
		else{
			dispatchInfo.computeDispatch();
			return dispatchInfo.getDispatchedAssistant().getCompletionFilter(dispatchInfo.aptEvent);			
		}
	}
	
	private class EventDispatchInfo {
		// null to indicate this is an no-op;
		protected final AptCompletionEventImpl aptEvent;
		// should never access these fields directly.
		private AnnotationCompletionAssistant assistant = null;
		
		EventDispatchInfo(AptCompletionEventImpl aptEvent)
		{
			this.aptEvent = aptEvent;
		}
		
		boolean isNoOpEvent(){
			return aptEvent == null;
		}
	
		synchronized AnnotationCompletionAssistant getDispatchedAssistant(){
			return assistant; 
		}
		
		private CompletionEnvImpl getCompletionEnv()
		{
			if(aptEvent == null)
				throw new IllegalStateException();
			return ((AnnotationsImpl)aptEvent.getEnclosingAnnotations())._env;
		}
		
		/** 
		 * Compute the factory that we should dispatch to. 
		 * The factory that supports the type of the outermost annotation is the winner. 
		 * (Not necessarily the factory that supports the anntotation that completion is actually
		 * happenining. This is to match the semantics of APT compilation dispatch.)
		 */ 
		synchronized void computeDispatch(){
			if( assistant != null ) return;
			
			final AnnotationsImpl enclosings = (AnnotationsImpl)aptEvent.getEnclosingAnnotations();
			final String annoTypename = enclosings.getOuterMostTypeName();		
			final List<EclipseAnnotationProcessorFactory> factories = 
				getParticipatingFactories(aptEvent.getJavaProject());
			
			if( factories != null )
			{
				for(int i=0, len=factories.size(); i<len; i++ ){
					final EclipseAnnotationProcessorFactory f = factories.get(i);
					final Collection<String> supported = f.supportedAnnotationTypes();
					if( supported.contains(annoTypename) ){
						final CompletionEnvImpl env = getCompletionEnv();
						final Set<AnnotationTypeDeclaration> annoTypes = enclosings.computeAnnotationTypes();
						final AnnotationCompletionAssistant aptAssistant = f.getCodeAssistantFor(annoTypes, env );
						if( aptAssistant != null ){
							assistant = aptAssistant;						
							return;
						}
					}
				}
			}
			assistant = NO_OP_ASSISTANT;
		}
	}
}
