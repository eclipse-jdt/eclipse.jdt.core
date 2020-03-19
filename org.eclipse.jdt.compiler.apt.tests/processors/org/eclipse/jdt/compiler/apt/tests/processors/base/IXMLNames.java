/*******************************************************************************
 * Copyright (c) 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.base;

/**
 * String constants used in representing the javax.lang.model typesystem as XML.
 *
 * @since 3.4
 */
public interface IXMLNames {
	static final String ANNOTATION_TAG = "annotation";
	static final String ANNOTATIONS_TAG = "annotations";
	static final String ANNOTATION_VALUE_TAG = "annotation-value";
	static final String ANNOTATION_VALUES_TAG = "annotation-values";
	static final String EXECUTABLE_ELEMENT_TAG = "executable-element";
	static final String INTERFACES_TAG = "interfaces";
	static final String KIND_TAG = "kind";
	static final String MEMBER_TAG = "member";
	static final String MODEL_TAG = "model";
	static final String OPTIONAL_TAG = "optional";
	static final String QNAME_TAG = "qname";
	static final String SNAME_TAG = "sname";
	static final String SUPERCLASS_TAG = "superclass";
	static final String TO_STRING_TAG = "to-string";
	static final String TYPE_ELEMENT_TAG = "type-element";
	static final String TYPE_MIRROR_TAG = "type-mirror";
	static final String TYPE_TAG = "type";
	static final String VALUE_TAG = "value";
	static final String VARIABLE_ELEMENT_TAG = "variable-element";

	static final String TYPEKIND_ERROR = "ERROR"; // see javax.lang.model.type.TypeKind
}
