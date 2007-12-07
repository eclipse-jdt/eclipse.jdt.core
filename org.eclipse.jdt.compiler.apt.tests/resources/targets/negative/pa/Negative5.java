/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.negative.pa;

/**
 * This code contains missing types.  The missing types all start with 'M'.
 * The desired behavior is specified in the javadoc for package 
 * javax.lang.model.element: in general, missing types should be replaced
 * by empty types with the same name.
 */
class Negative5 {
	class C1 extends M1 implements MI2 {}
	class C2 extends M3<M4<M5>> implements M6<M7>, M8<M9> {}
	interface I1 extends M10 {}
	interface I2 extends M11<M12> {}
}

interface INegative5 {
	class C101 extends M101 implements M103, M104 {}
	class C102 extends M105<M106> implements M107<M108, MP109> {}
	interface I101 extends M110, M111 {}
	interface I102 extends M112, M113<M114> {}
}
