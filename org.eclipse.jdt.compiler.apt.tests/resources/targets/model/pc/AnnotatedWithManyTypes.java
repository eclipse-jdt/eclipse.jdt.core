/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
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

/**
 * Annotated with variously typed values.
 */
public class AnnotatedWithManyTypes {
	// Do not change this code without also changing VisitorProc and ElementProc
	@TypedAnnos.AnnoByte(3)
	@TypedAnnos.AnnoBoolean(true)
	@TypedAnnos.AnnoChar('c')
	@TypedAnnos.AnnoDouble(6.3)
	@TypedAnnos.AnnoFloat(26.7F)
	@TypedAnnos.AnnoInt(19)
	@TypedAnnos.AnnoLong(300L)
	@TypedAnnos.AnnoShort(289)
	@TypedAnnos.AnnoString("foo")
	@TypedAnnos.AnnoEnumConst(TypedAnnos.Enum.A)
	@TypedAnnos.AnnoType(Exception.class)
	@TypedAnnos.AnnoAnnoChar(@TypedAnnos.AnnoChar('x'))
	@TypedAnnos.AnnoArrayInt({5, 8, 11})
	@TypedAnnos.AnnoArrayString({"bar", "quux"})
	@TypedAnnos.AnnoArrayEnumConst({TypedAnnos.Enum.B, TypedAnnos.Enum.C})
	@TypedAnnos.AnnoArrayType({ String.class, Annotated.class })
	@TypedAnnos.AnnoArrayAnnoChar({@TypedAnnos.AnnoChar('y'), @TypedAnnos.AnnoChar('z')})
	public class Annotated {}
}