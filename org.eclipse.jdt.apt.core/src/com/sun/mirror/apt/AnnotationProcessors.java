/*
 * @(#)AnnotationProcessors.java	1.2 04/06/21
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of
 *       its contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.mirror.apt;

import java.util.*;

/**
 * Utilities to create specialized annotation processors.
 *
 * @since 1.5
 * @author Joseph D. Darcy
 * @author Scott Seligman
 */
public class AnnotationProcessors {
    static class NoOpAP implements AnnotationProcessor {
	NoOpAP() {}
	@Override
	public void process(){}
    }

    /**
     * Combines multiple annotation processors into a simple composite
     * processor.
     * The composite processor functions by invoking each of its component
     * processors in sequence.
     */
    static class CompositeAnnotationProcessor implements AnnotationProcessor {

	private List<AnnotationProcessor> aps =
	    new LinkedList<AnnotationProcessor>();

	/**
	 * Constructs a new composite annotation processor.
	 * @param aps  the component annotation processors
	 */
	public CompositeAnnotationProcessor(Collection<AnnotationProcessor> aps) {
	    this.aps.addAll(aps);
	}

	/**
	 * Constructs a new composite annotation processor.
	 * @param aps  the component annotation processors
	 */
	public CompositeAnnotationProcessor(AnnotationProcessor... aps) {
		this.aps.addAll(Arrays.asList(aps));
	}

	/**
	 * Invokes the <tt>process</tt> method of each component processor,
	 * in the order in which the processors were passed to the constructor.
	 */
	@Override
	public void process() {
	    for(AnnotationProcessor ap: aps)
		ap.process();
	}
    }


    /**
     *  An annotation processor that does nothing and has no state.
     *  May be used multiple times.
     *
     * @since 1.5
     */
    public final static AnnotationProcessor NO_OP = new NoOpAP();

    /**
     * Constructs a new composite annotation processor.  A composite
     * annotation processor combines multiple annotation processors
     * into one and functions by invoking each of its component
     * processors' process methods in sequence.
     *
     * @param aps The processors to create a composite of
     * @since 1.5
     */
    public static AnnotationProcessor getCompositeAnnotationProcessor(AnnotationProcessor... aps) {
	return new CompositeAnnotationProcessor(aps);
    }

    /**
     * Constructs a new composite annotation processor.  A composite
     * annotation processor combines multiple annotation processors
     * into one and functions by invoking each of its component
     * processors' process methods in the sequence the processors are
     * returned by the collection's iterator.
     *
     * @param aps A collection of processors to create a composite of
     * @since 1.5
     */
    public static AnnotationProcessor getCompositeAnnotationProcessor(Collection<AnnotationProcessor> aps) {
	return new CompositeAnnotationProcessor(aps);
    }
}



