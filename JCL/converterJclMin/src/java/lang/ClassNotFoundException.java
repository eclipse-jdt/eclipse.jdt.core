/*******************************************************************************
 * Copyright (c) 2020 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java.lang;
public class ClassNotFoundException extends ReflectiveOperationException {
  public ClassNotFoundException() { super(); }
  public ClassNotFoundException(String s) { super(s); }
  public ClassNotFoundException(String s, Throwable t) { super(s, t);}
  public Throwable getException() { return null; }
}
