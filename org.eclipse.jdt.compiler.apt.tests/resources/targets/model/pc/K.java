/*******************************************************************************
 * Copyright (c) 2014 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package targets.model.pc;

import org.eclipse.jdt.compiler.apt.tests.annotations.*;

@TypedAnnos.AnnoString("I'm \"special\": \t\\\n")
@TypedAnnos.AnnoChar('\'')
public class K {}