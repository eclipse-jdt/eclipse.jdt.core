/*******************************************************************************
 * Copyright (c) 2008 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import javax.lang.model.util.ElementScanner6;
import javax.xml.parsers.DocumentBuilderFactory;
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
 * 
 * @since 3.4
 */
public class XMLConverter extends ElementScanner6<Void, Node> {
	
	/**
	 * Convert an XML DOM document to a canonical string representation
	 */
	public static String xmlToString(Document model) {
		StringWriter s = new StringWriter();
		DOMSource domSource = new DOMSource(model);
		StreamResult streamResult = new StreamResult(s);
		TransformerFactory tf = TransformerFactory.newInstance();
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
	 * Recursively convert a collection of language elements (declarations) into an XML representation.
	 * @param declarations the collection of language elements to convert
	 * @return an XML document whose root node is named &lt;model&gt;.
	 * @throws ParserConfigurationException
	 */
	public static Document convertModel(Iterable<? extends javax.lang.model.element.Element> declarations) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document model = factory.newDocumentBuilder().newDocument();
		org.w3c.dom.Element modelNode = model.createElement("model");
		
		XMLConverter converter = new XMLConverter();
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
		Document doc = target.getOwnerDocument();

		Element executableNode = doc.createElement("executable-element");
		executableNode.setAttribute("kind", e.getKind().name());
		executableNode.setAttribute("sname", e.getSimpleName().toString());

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
		Document doc = target.getOwnerDocument();

		Element typeNode = doc.createElement("type-element");
		typeNode.setAttribute("kind", e.getKind().name());
		typeNode.setAttribute("sname", e.getSimpleName().toString());
		typeNode.setAttribute("qname", e.getQualifiedName().toString());

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
		Document doc = target.getOwnerDocument();

		Element variableNode = doc.createElement("variable-element");
		variableNode.setAttribute("kind", e.getKind().name());
		variableNode.setAttribute("sname", e.getSimpleName().toString());
		// TODO: the spec does not restrict the toString() implementation
		variableNode.setAttribute("type", e.asType().toString());

		convertAnnotationMirrors(e, variableNode);

		target.appendChild(variableNode);

		// Variables do not enclose any elements, so no need to call super.
		return null;
	}
	
	private void convertAnnotationMirrors(javax.lang.model.element.Element e, Node target) {
		Document doc = target.getOwnerDocument();
		List<? extends AnnotationMirror> mirrors = e.getAnnotationMirrors();
		if (mirrors != null && !mirrors.isEmpty()) {
			Element annotationsNode = doc.createElement("annotations");
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
		Document doc = target.getOwnerDocument();
		Element annoNode = doc.createElement("annotation");
		String sname = am.getAnnotationType().asElement().getSimpleName().toString();
		annoNode.setAttribute("sname", sname);
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = am.getElementValues();
		if (values.size() > 0) {
			Element valuesNode = doc.createElement("annotation-values");
			annoNode.appendChild(valuesNode);
			for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values
					.entrySet()) {
				AnnotationValue valueHolder = entry.getValue();
				if (valueHolder != null) {
					Object value = valueHolder.getValue();
					Element valueNode = doc.createElement("annotation-value");
					valueNode.setAttribute("member", entry.getKey().getSimpleName().toString());
					valueNode.setAttribute("type", value.getClass().getName());
					valueNode.setAttribute("value", value.toString());
					valuesNode.appendChild(valueNode);
				}
			}
		}
		target.appendChild(annoNode);
	}

}
