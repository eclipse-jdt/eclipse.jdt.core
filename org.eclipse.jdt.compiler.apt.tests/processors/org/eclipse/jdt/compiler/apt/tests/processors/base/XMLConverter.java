/*******************************************************************************
 * Copyright (c) 2008, 2023 BEA Systems, Inc.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Generate an XML representation of a language model.
 * Changes to this class will generally require changes to
 * the XMLComparer class (which compares documents generated
 * by this class) and possibly to the reference models of
 * various tests.
 *
 * @since 3.4
 */
public class XMLConverter extends ElementScanner6<Void, Node> implements IXMLNames {

	private final Document _doc;
	@Deprecated
	private XMLConverter(Document doc) {
		_doc = doc;
	}

	/**
	 * Convert an XML DOM document to a canonical string representation
	 */
	public static String xmlToString(Document model) {
		StringWriter s = new StringWriter();
		DOMSource domSource = new DOMSource(model);
		StreamResult streamResult = new StreamResult(s);
		TransformerFactory tf = org.eclipse.core.internal.runtime.XmlProcessorFactory.createTransformerFactoryWithErrorOnDOCTYPE();
		Transformer serializer;
		try {
			serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
			serializer.transform(domSource, streamResult);
		} catch (Exception e) {
			e.printStackTrace(new PrintWriter(s));
		}
		return s.toString();
	}

	/**
	 * Convert an XML DOM document to a string representation suitable for paste
	 * into a processor written in Java.
	 */
	// derived from org.eclipse.jdt.core.tests.util.Util#displayString
	public static String xmlToCutAndPasteString(Document model, int indent, boolean shift) {
		String modelAsString = xmlToString(model);
	    int length = modelAsString.length();
	    StringBuilder buffer = new StringBuilder(length);
	    java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(modelAsString, "\n\r", true);
	    for (int i = 0; i < indent; i++) buffer.append("\t");
	    if (shift) indent++;
	    buffer.append("\"");
	    while (tokenizer.hasMoreTokens()){
	        String token = tokenizer.nextToken();
	        if (token.equals("\n")) {
	            buffer.append("\\n");
	            if (tokenizer.hasMoreTokens()) {
	                buffer.append("\" + \n");
	                for (int i = 0; i < indent; i++) buffer.append("\t");
	                buffer.append("\"");
	            }
	            continue;
	        }
	        StringBuilder tokenBuffer = new StringBuilder();
	        for (int i = 0; i < token.length(); i++){
	            char c = token.charAt(i);
	            switch (c) {
	                case '\r' :
	                    break;
	                case '\n' :
	                    tokenBuffer.append("\\n");
	                    break;
	                case '\b' :
	                    tokenBuffer.append("\\b");
	                    break;
	                case '\t' :
	                    tokenBuffer.append("\t");
	                    break;
	                case '\f' :
	                    tokenBuffer.append("\\f");
	                    break;
	                case '\"' :
	                    tokenBuffer.append("\\\"");
	                    break;
	                case '\'' :
	                    tokenBuffer.append("\\'");
	                    break;
	                case '\\' :
	                    tokenBuffer.append("\\\\");
	                    break;
	                default :
	                    tokenBuffer.append(c);
	            }
	        }
	        buffer.append(tokenBuffer.toString());
	    }
	    buffer.append("\"");
	    return buffer.toString();
	}

	/**
	 * Recursively convert a collection of language elements (declarations) into an XML representation.
	 * @param declarations the collection of language elements to convert
	 * @return an XML document whose root node is named &lt;model&gt;.
	 */
	public static Document convertModel(Iterable<? extends javax.lang.model.element.Element> declarations) throws ParserConfigurationException {
		Document model = org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE().newDocument();
		org.w3c.dom.Element modelNode = model.createElement(MODEL_TAG);

		XMLConverter converter = new XMLConverter(model);
		converter.scan(declarations, modelNode);
		model.appendChild(modelNode);
		return model;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.lang.model.util.ElementScanner6#visitExecutable(javax.lang.model.element.ExecutableElement,
	 *      java.lang.Object)
	 */
	@Override
	public Void visitExecutable(ExecutableElement e, Node target) {
		Element executableNode = _doc.createElement(EXECUTABLE_ELEMENT_TAG);
		executableNode.setAttribute(KIND_TAG, e.getKind().name());
		executableNode.setAttribute(SNAME_TAG, e.getSimpleName().toString());

		convertAnnotationMirrors(e, executableNode);

		target.appendChild(executableNode);

		// scan the method's parameters
		return super.visitExecutable(e, executableNode);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.lang.model.util.ElementScanner6#visitPackage(javax.lang.model.element.PackageElement,
	 *      java.lang.Object)
	 */
	@Override
	public Void visitPackage(PackageElement e, Node target) {
		// TODO not yet implemented
		return super.visitPackage(e, target);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.lang.model.util.ElementScanner6#visitType(javax.lang.model.element.TypeElement,
	 *      java.lang.Object)
	 */
	@Override
	public Void visitType(TypeElement e, Node target) {
		Element typeNode = _doc.createElement(TYPE_ELEMENT_TAG);
		typeNode.setAttribute(KIND_TAG, e.getKind().name());
		typeNode.setAttribute(SNAME_TAG, e.getSimpleName().toString());
		typeNode.setAttribute(QNAME_TAG, e.getQualifiedName().toString());

		convertSuperclass(e, typeNode);
		convertInterfaces(e, typeNode);
		convertAnnotationMirrors(e, typeNode);

		target.appendChild(typeNode);

		// Scan the type's subtypes, fields, and methods
		return super.visitType(e, typeNode);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.lang.model.util.ElementScanner6#visitTypeParameter(javax.lang.model.element.TypeParameterElement,
	 *      java.lang.Object)
	 */
	@Override
	public Void visitTypeParameter(TypeParameterElement e, Node target) {
		// TODO not yet implemented
		return super.visitTypeParameter(e, target);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.lang.model.util.ElementScanner6#visitVariable(javax.lang.model.element.VariableElement,
	 *      java.lang.Object)
	 */
	@Override
	public Void visitVariable(VariableElement e, Node target) {
		Element variableNode = _doc.createElement(VARIABLE_ELEMENT_TAG);
		variableNode.setAttribute(KIND_TAG, e.getKind().name());
		variableNode.setAttribute(SNAME_TAG, e.getSimpleName().toString());
		// TODO: the spec does not restrict the toString() implementation
		variableNode.setAttribute(TYPE_TAG, e.asType().toString());

		convertAnnotationMirrors(e, variableNode);

		target.appendChild(variableNode);

		// Variables do not enclose any elements, so no need to call super.
		return null;
	}

	private void convertAnnotationMirrors(javax.lang.model.element.Element e, Node target) {
		List<? extends AnnotationMirror> mirrors = e.getAnnotationMirrors();
		if (mirrors != null && !mirrors.isEmpty()) {
			Element annotationsNode = _doc.createElement(ANNOTATIONS_TAG);
			for (AnnotationMirror am : mirrors) {
				convertAnnotationMirror(am, annotationsNode);
			}
			target.appendChild(annotationsNode);
		}
	}

	/**
	 * Scan an annotation instance in the model and represent it in XML, including all its explicit
	 * values (but not any default values).
	 *
	 * @param am
	 *            the annotation mirror to be converted
	 * @param target
	 *            the &lt;annotations&gt; XML node to which a new &lt;annotation&gt; node will be
	 *            added
	 */
	private void convertAnnotationMirror(AnnotationMirror am, Node target) {
		javax.lang.model.element.Element annoElement = am.getAnnotationType().asElement();
		if (annoElement == null) {
			return;
		}
		Element annoNode = _doc.createElement(ANNOTATION_TAG);
		String sname = am.getAnnotationType().asElement().getSimpleName().toString();
		annoNode.setAttribute(SNAME_TAG, sname);
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = am.getElementValues();
		if (values.size() > 0) {
			Element valuesNode = _doc.createElement(ANNOTATION_VALUES_TAG);
			annoNode.appendChild(valuesNode);
			for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values
					.entrySet()) {
				AnnotationValue valueHolder = entry.getValue();
				if (valueHolder != null) {
					Object value = valueHolder.getValue();
					Element valueNode = _doc.createElement(ANNOTATION_VALUE_TAG);
					valueNode.setAttribute(MEMBER_TAG, entry.getKey().getSimpleName().toString());
					valueNode.setAttribute(TYPE_TAG, value.getClass().getName());
					valueNode.setAttribute(VALUE_TAG, value.toString());
					valuesNode.appendChild(valueNode);
				}
			}
		}
		target.appendChild(annoNode);
	}

	/**
	 * Scan a type for its extended and implemented interfaces and represent them
	 * in XML.
	 * @param target the node representing the type; an &lt;interfaces&gt; node
	 * will be added as a child of this node, if any interfaces are found.
	 */
	private void convertInterfaces(TypeElement e, Node target) {
		List<? extends TypeMirror> interfaces = e.getInterfaces();
		if (interfaces != null && !interfaces.isEmpty()) {
			Element interfacesNode = _doc.createElement(INTERFACES_TAG);
			for (TypeMirror intfc : interfaces) {
				convertTypeMirror(intfc, interfacesNode);
			}
			target.appendChild(interfacesNode);
		}
	}

	/**
	 * Create a node representing a class declaration's superclass
	 */
	private void convertSuperclass(TypeElement e, Node target) {
		TypeMirror tmSuper = e.getSuperclass();
		if (null != tmSuper) {
			Element node = _doc.createElement(SUPERCLASS_TAG);
			convertTypeMirror(tmSuper, node);
			target.appendChild(node);
		}
	}

	/**
	 * Represent an arbitrary TypeMirror in XML, and append it as a child to
	 * the specified parent node.
	 *
	 * Note this is problematic, because TypeMirror has no well-specified ways
	 * to canonicalize an arbitrary (and possibly erroneous) type.
	 *
	 * @param tm must be non-null
	 * @param target the parent XML node, to which this new node will be appended
	 */
	private void convertTypeMirror(TypeMirror tm, Node target) {
		Element n = _doc.createElement(TYPE_MIRROR_TAG);
		n.setAttribute(KIND_TAG, tm.getKind().name());
		n.setAttribute(TO_STRING_TAG, tm.toString());
		// TODO: potentially walk type-variables here
		target.appendChild(n);
	}
}
