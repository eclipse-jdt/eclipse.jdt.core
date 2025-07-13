/*******************************************************************************
 * Copyright (c) 2024 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.search.matching.AndLocator;
import org.eclipse.jdt.internal.core.search.matching.AndPattern;
import org.eclipse.jdt.internal.core.search.matching.ConstructorLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMAndLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMConstructorLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMFieldLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMLocalVariableLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMMethodLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMModuleLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMOrLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMPackageReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMPatternLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMSuperTypeReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMTypeDeclarationLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMTypeParameterLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMTypeReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.FieldLocator;
import org.eclipse.jdt.internal.core.search.matching.LocalVariableLocator;
import org.eclipse.jdt.internal.core.search.matching.MethodLocator;
import org.eclipse.jdt.internal.core.search.matching.ModuleLocator;
import org.eclipse.jdt.internal.core.search.matching.ModulePattern;
import org.eclipse.jdt.internal.core.search.matching.OrLocator;
import org.eclipse.jdt.internal.core.search.matching.OrPattern;
import org.eclipse.jdt.internal.core.search.matching.PackageReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;
import org.eclipse.jdt.internal.core.search.matching.SuperTypeReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeParameterLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeReferenceLocator;

public class DOMPatternLocatorFactory {

	public static DOMPatternLocator createWrapper(PatternLocator locator, SearchPattern pattern) {
		// pattern is not always accessible from locator, so pass it explicitly
		// TODO implement all this.
		if( locator instanceof FieldLocator fl) {
			return new DOMFieldLocator(fl);
		}
		if( locator instanceof ConstructorLocator cl) {
			return new DOMConstructorLocator(cl);
		}
		if( locator instanceof LocalVariableLocator lcl) {
			return new DOMLocalVariableLocator(lcl);
		}
		if( locator instanceof MethodLocator ml) {
			return new DOMMethodLocator(ml);
		}
		if( locator instanceof PackageReferenceLocator prl) {
			return new DOMPackageReferenceLocator(prl);
		}
		if( locator instanceof SuperTypeReferenceLocator strl) {
			return new DOMSuperTypeReferenceLocator(strl);
		}
		if( locator instanceof TypeDeclarationLocator tdl) {
			return new DOMTypeDeclarationLocator(tdl);
		}
		if( locator instanceof TypeParameterLocator tpl) {
			return new DOMTypeParameterLocator(tpl);
		}
		if( locator instanceof TypeReferenceLocator trl) {
			return new DOMTypeReferenceLocator(trl);
		}
		if( locator instanceof ModuleLocator moduleLocator && pattern instanceof ModulePattern modulePattern) {
			return new DOMModuleLocator(moduleLocator, modulePattern);
		}
		if (locator instanceof OrLocator orLocator && pattern instanceof OrPattern orPattern) {
			return new DOMOrLocator(orLocator, orPattern);
		}
		if (locator instanceof AndLocator andLocator && pattern instanceof AndPattern andPattern) {
			return new DOMAndLocator(andLocator, andPattern);
		}
		ILog.get().warn("Cannot map pattern locator/pattern to DOMPatternLocator");
		return new DOMPatternLocator(null); // stub
	}
}
