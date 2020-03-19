/*
 * @(#)RoundCompleteEvent.java	1.2 04/07/19
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

/**
 * Event for the completion of a round of annotation processing.
 *
 * <p>While this class extends the serializable <code>EventObject</code>, it
 * cannot meaningfully be serialized because all of the annotation
 * processing tool's internal state would potentially be needed.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.2 04/07/19
 * @since 1.5
 */
@SuppressWarnings("serial")
public abstract class RoundCompleteEvent extends java.util.EventObject {
    private RoundState rs;

    /**
     * The current <code>AnnotationProcessorEnvironment</code> is regarded
     * as the source of events.
     *
     * @param source The source of events
     * @param rs     The state of the round
     */
    protected RoundCompleteEvent(AnnotationProcessorEnvironment source,
				 RoundState rs) {
	super(source);
	this.rs = rs;
    }

    /**
     * Return round state.
     */
    public RoundState getRoundState() {
	return rs;
    }

    /**
     * Return source.
     */
    @Override
	public AnnotationProcessorEnvironment getSource() {
	return (AnnotationProcessorEnvironment)super.getSource();
    }
}
