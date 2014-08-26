/*******************************************************************************
 * Copyright (c) 2014 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.model.pc;

import org.eclipse.jdt.compiler.apt.tests.annotations.*;

@TypedAnnos.AnnoString("I'm \"special\": \t\\\n")
@TypedAnnos.AnnoChar('\'')
public class K {}