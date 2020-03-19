/*
 * @(#)Messager.java	1.2 04/07/27
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


import com.sun.mirror.util.SourcePosition;


/**
 * A <code>Messager</code> provides the way for
 * an annotation processor to report error messages, warnings, and
 * other notices.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.2 04/07/27
 * @since 1.5
 */

public interface Messager {

    /**
     * Prints an error message.
     * Equivalent to <code>printError(null, msg)</code>.
     * @param msg  the message, or an empty string if none
     */
    void printError(String msg);

    /**
     * Prints an error message.
     * @param pos  the position where the error occured, or null if it is
     *			unknown or not applicable
     * @param msg  the message, or an empty string if none
     */
    void printError(SourcePosition pos, String msg);

    /**
     * Prints a warning message.
     * Equivalent to <code>printWarning(null, msg)</code>.
     * @param msg  the message, or an empty string if none
     */
    void printWarning(String msg);

    /**
     * Prints a warning message.
     * @param pos  the position where the warning occured, or null if it is
     *			unknown or not applicable
     * @param msg  the message, or an empty string if none
     */
    void printWarning(SourcePosition pos, String msg);

    /**
     * Prints a notice.
     * Equivalent to <code>printNotice(null, msg)</code>.
     * @param msg  the message, or an empty string if none
     */
    void printNotice(String msg);

    /**
     * Prints a notice.
     * @param pos  the position where the noticed occured, or null if it is
     *			unknown or not applicable
     * @param msg  the message, or an empty string if none
     */
    void printNotice(SourcePosition pos, String msg);
}
