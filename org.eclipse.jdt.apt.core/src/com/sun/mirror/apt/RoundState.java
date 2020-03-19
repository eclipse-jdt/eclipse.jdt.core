/*
 * @(#)RoundState.java	1.1 04/06/25
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
 * Represents the status of a completed round of annotation processing.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.1 04/06/25
 * @since 1.5
 */
public interface RoundState {
    /**
     * Returns <code>true</code> if this was the last round of annotation
     * processing; returns <code>false</code> if there will be a subsequent round.
     */
    boolean finalRound();

    /**
     * Returns <code>true</code> if an error was raised in this round of processing;
     * returns <code>false</code> otherwise.
     */
    boolean errorRaised();

    /**
     * Returns <code>true</code> if new source files were created in this round of
     * processing; returns <code>false</code> otherwise.
     */
    boolean sourceFilesCreated();

    /**
     * Returns <code>true</code> if new class files were created in this round of
     * processing; returns <code>false</code> otherwise.
     */
    boolean classFilesCreated();
}
