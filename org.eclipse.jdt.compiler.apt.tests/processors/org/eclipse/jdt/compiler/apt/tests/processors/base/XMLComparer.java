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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utility to compare two XML DOM trees that represent JSR269 (javax.lang.model) language models.
 * This could be done with existing third-party XMLDiff tools and a sufficiently articulate DTD, so
 * if it needs to be substantially enhanced at some point in the future, maintainers should consider
 * using that approach instead.
 * 
 * This is not a generic XML comparison tool; it has specific expectations about the structure of a
 * JSR269 model, for example, that declarations may contain annotations but not vice versa.
 * 
 * Note that in this body of code, we use the term "Decl" or "Declaration" to refer to the the
 * entities represented by javax.lang.model.element.Element, to avoid confusion with XML elements,
 * i.e. org.w3c.dom.Element.
 * 
 * @since 3.4
 */
public class XMLComparer {

	/**
	 * A structure to collect and organize all the contents of an &lt;elements&gt; node, that is,
	 * all the things that the {@link javax.lang.model.element.Element#getEnclosedElements()} method
	 * would return. The key is the simple name of the entity. The reason this is needed is because
	 * simple names can be repeated, e.g., a class may contain both a method and a nested class with
	 * the same name.
	 * 
	 * The structure also has a holder for an &lt;annotations&gt; node, as a convenience, because
	 * when searching the XML DOM we discover this node at the same time as the element
	 * declarations.
	 * 
	 * @since 3.4
	 */
	private class DeclarationContents {
		Element annotations = null;
		final TreeMap<String, Element> typeDecls = new TreeMap<String, Element>();
		final TreeMap<String, Element> executableDecls = new TreeMap<String, Element>();
		final TreeMap<String, Element> variableDecls = new TreeMap<String, Element>();
		// TODO: PACKAGE, TYPE_PARAMETER, OTHER
	}

	private static final String ANNOTATIONS = "annotations";
	
	private static final String ANNOTATION_VALUE = "annotation-value";
	
	private static final String ANNOTATION_VALUES = "annotation-values";

	private static final String VARIABLE_ELEMENT = "variable-element";

	private static final String TYPE_ELEMENT = "type-element";

	private static final String EXECUTABLE_ELEMENT = "executable-element";

	private static final String ANNOTATION = "annotation";

	private static final String SNAME = "sname";

	private static final String MODEL = "model";

	/**
	 * Compare two JSR269 language models, using the approximate criteria of the JSR269 spec. Ignore
	 * differences in order of sibling elements. If the two do not match, optionally send detailed
	 * information about the mismatch to an output stream.
	 * 
	 * @param actual
	 *            the observed language model
	 * @param expected
	 *            the reference language model
	 * @param out
	 *            a stream to which detailed information on mismatches will be output. Can be null
	 *            if no detailed information is desired.
	 * @return true if the models match sufficiently to satisfy the spec.
	 */
	public static boolean compare(Document actual, Document expected, OutputStream out) {
		XMLComparer comparer = new XMLComparer(actual, expected, out);
		return comparer._compare();
	}

	private final Document _actual;

	private final Document _expected;

	private final PrintStream _out;

	/**
	 * Clients should not construct instances of this object.
	 */
	private XMLComparer(Document actual, Document expected, OutputStream out) {
		_actual = actual;
		_expected = expected;
		OutputStream os;
		if (out != null) {
			os = out;
		} else {
			os = new OutputStream() {
				public void write(int b) throws IOException {
					// do nothing
				}
			};
		}
		_out = new PrintStream(os, true);
	}

	/**
	 * Non-static internal comparison routine called from
	 * {@link #compare(Document, Document, OutputStream)}
	 * 
	 * @return true if models are equivalent
	 */
	private boolean _compare() {
		// navigate to the outermost <model> nodes of each document
		Element actualModel = findRootNode(_actual);
		Element expectedModel = findRootNode(_expected);
		if (actualModel == null) {
			if (expectedModel == null) {
				return true;
			}
			printProblem("Actual model contained no <elements> node.");
			printDifferences();
			return false;
		}
		if (expectedModel == null) {
			printProblem("Actual model contained unexpected elements.");
			printDifferences();
			return false;
		}

		return compareDeclarations(actualModel, expectedModel);
	}

	/**
	 * Collect the contents of an &lt;annotations&gt; node into a map. If there are declarations of
	 * the same name, report an error; if there are unexpected contents (e.g., declarations, which
	 * should not be contained within an annotations node), report an error.
	 * 
	 * @param annotsNode
	 *            must not be null
	 * @param map
	 *            a map from annotation type name to the XML node representing the annotation
	 *            instance
	 * @return true if no errors were reported
	 */
	private boolean collectAnnotations(Element annotsNode, Map<String, Element> map) {
		for (Node n = annotsNode.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element e = (Element)n;
			String nodeName = e.getNodeName();

			// get 'sname'
			String sname = e.getAttribute(SNAME);
			if (sname == null) {
				printProblem("A child of an <annotations> node was missing the \"sname\" attribute");
				printDifferences();
				return false;
			}

			// categorize
			Element old = null;
			if (ANNOTATION.equals(nodeName)) {
				old = map.put(sname, e);
			} else {
				printProblem("An <annotations> node unexpectedly contained something other than <annotation>: "
						+ nodeName);
				printDifferences();
				return false;
			}
			if (old != null) {
				printProblem("Two sibling annotation mirrors had the same sname: " + sname);
				printDifferences();
				return false;
			}
		}
		return true;
	}

	/**
	 * Collect the contents of a declaration, including child declarations and annotations, into a
	 * collection of maps. If there are declarations of the same type and simple name, report an
	 * error; if there are unexpected contents), report an error.
	 * 
	 * @param elementNode
	 *            must not be null
	 * @param contents
	 *            must not be null
	 * @return true if no errors were reported
	 */
	private boolean collectDeclarationContents(Element declarationNode, DeclarationContents contents) {
		for (Node n = declarationNode.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element e = (Element)n;
			String nodeName = e.getNodeName();

			if (ANNOTATIONS.equals(nodeName)) {
				if (contents.annotations != null) {
					printProblem("XML syntax error: a declaration contained more than one <annotations> node");
					printDifferences();
					return false;
				}
				contents.annotations = e;
			} else {
				// get 'sname'
				String sname = e.getAttribute(SNAME);
				if (sname == null) {
					printProblem("A child of an <elements> node was missing the \"sname\" attribute");
					printDifferences();
					return false;
				}

				// categorize
				Element old = null;
				if (EXECUTABLE_ELEMENT.equals(nodeName)) {
					old = contents.executableDecls.put(sname, e);
				} else if (TYPE_ELEMENT.equals(nodeName)) {
					old = contents.typeDecls.put(sname, e);
				} else if (VARIABLE_ELEMENT.equals(nodeName)) {
					old = contents.variableDecls.put(sname, e);
				} else {
					printProblem("A declaration contained an unexpected child node: " + nodeName);
					printDifferences();
					return false;
				}
				if (old != null) {
					printProblem("Two elements of the same kind had the same sname: " + sname);
					printDifferences();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Compare an actual annotation mirror to the expected reference. It is assumed that the
	 * annotation's sname has already been compared. Attributes that exist on the actual annotation
	 * but not on the expected annotation are not considered to be a mismatch. Note that the
	 * language model representation in XML does not include default values, since these are
	 * attributes of the annotation type rather than the annotation instance.
	 * 
	 * @param actualAnnot
	 *            must be non-null
	 * @param expectedAnnot
	 *            must be non-null
	 * @return true if the elements match
	 */
	private boolean compareAnnotationNodes(Element actualAnnot, Element expectedAnnot) {
		// Compare attributes of the annotation instances
		// Intentionally ignore the presence of additional actual attributes not present in the
		// expected model
		NamedNodeMap expectedAttrs = expectedAnnot.getAttributes();
		NamedNodeMap actualAttrs = actualAnnot.getAttributes();
		int numAttrs = expectedAttrs.getLength();
		String sname = null;
		for (int i = 0; i < numAttrs; ++i) {
			Node expectedAttr = expectedAttrs.item(i);
			String attrName = expectedAttr.getNodeName();
			Node actualAttr = actualAttrs.getNamedItem(attrName);
			if (actualAttr == null) {
				printProblem("Actual annotation mirror was missing expected attribute: " + attrName);
				printDifferences();
				return false;
			}
			String expectedValue = expectedAttr.getNodeValue();
			String actualValue = actualAttr.getNodeValue();
			if (!expectedValue.equals(actualValue)) {
				printProblem("Actual attribute value was different than expected: attribute "
						+ attrName + ", expected " + expectedValue + ", actual " + actualValue);
				printDifferences();
				return false;
			}
			if (SNAME.equals(attrName)) {
				sname = actualValue;
			}
		}

		// Examine member-value pairs
		Element actualValues = null;
		for (Node n = actualAnnot.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE && ANNOTATION_VALUES.equals(n.getNodeName())) {
				actualValues = (Element)n;
			}
		}
		Element expectedValues = null;
		for (Node n = expectedAnnot.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE && ANNOTATION_VALUES.equals(n.getNodeName())) {
				expectedValues = (Element)n;
			}
		}
		if (actualValues != null && expectedValues != null) {
			if (!compareAnnotationValuesNodes(actualValues, expectedValues)) {
				return false;
			}
		}
		else if (actualValues != null) {
			// expectedValues == null
			printProblem("Found unexpected <annotation-values> in annotation: " + sname);
			printDifferences();
			return false;
		} 
		else if (expectedValues != null) {
			// actualValues == null
			printProblem("Missing expected <annotation-values> in annotation: " + sname);
			printDifferences();
			return false;
		} 
		// both null is okay

		return true;
	}

	/**
	 * Compare the contents of two &lt;annotations&gt; nodes.
	 * 
	 * @param actualAnnots
	 *            may be empty, but must not be null
	 * @param expectedAnnots
	 *            may be empty, but must not be null
	 * @return true if the contents are equivalent.
	 */
	private boolean compareAnnotationsNodes(Element actualAnnots, Element expectedAnnots) {
		// Group declarations alphabetically so they can be compared
		Map<String, Element> actual = new TreeMap<String, Element>();
		Map<String, Element> expected = new TreeMap<String, Element>();
		if (!collectAnnotations(actualAnnots, actual))
			return false;
		if (!collectAnnotations(expectedAnnots, expected))
			return false;

		// Compare the collections at this level
		if (!actual.keySet().equals(expected.keySet())) {
			printProblem("Contents of <annotations> nodes did not match");
			printDifferences();
			return false;
		}

		// Compare individual annotations in more detail
		for (Map.Entry<String, Element> expectedEntry : expected.entrySet()) {
			String sname = expectedEntry.getKey();
			Element actualElement = actual.get(sname);
			if (!compareAnnotationNodes(actualElement, expectedEntry.getValue())) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Compare the contents of two &lt;annotation-values&gt; nodes; that is, compare
	 * actual and expected lists of annotation member/value pairs.  These lists do
	 * not typically include annotation value defaults, since those are an attribute
	 * of an annotation type rather than an annotation instance.  Ordering of the list
	 * is important: the same pairs, in a different order, is considered a mismatch.
	 * @param actual must be non-null
	 * @param expected must be non-null
	 * @return true if the sets are equivalent
	 */
	private boolean compareAnnotationValuesNodes(Element actual, Element expected) {
		Node nActual = actual.getFirstChild();
		for (Node nExpected = expected.getFirstChild(); nExpected != null; nExpected = nExpected.getNextSibling()) {
			if (nExpected.getNodeType() == Node.ELEMENT_NODE && ANNOTATION_VALUE.equals(nExpected.getNodeName())) {
				while (nActual != null && 
						(nActual.getNodeType() != Node.ELEMENT_NODE || 
						!ANNOTATION_VALUE.equals(nActual.getNodeName()))) {
					nActual = nActual.getNextSibling();
				}
				if (nActual == null) {
					printProblem("Annotation member-value pairs were different: expected more pairs than were found");
					printDifferences();
					return false;
				}
				// Now we've got two annotation-value elements; compare their attributes.
				// We will ignore "extra" (unexpected) attributes in the actual model.
				if (!compareAttributes(nActual, nExpected)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Compare the attributes of two nodes.  Ignore attributes that are found on
	 * the actual, but not expected, node.
	 * @param actual must not be null
	 * @param expected must not be null
	 * @return true if the attribute lists are equivalent
	 */
	private boolean compareAttributes(Node actual, Node expected) {
		NamedNodeMap expectedAttrs = expected.getAttributes();
		NamedNodeMap actualAttrs = actual.getAttributes();
		for (int i = 0; i < expectedAttrs.getLength(); ++i) {
			Node expectedAttr = expectedAttrs.item(i);
			String attrName = expectedAttr.getNodeName();
			Node actualAttr = actualAttrs.getNamedItem(attrName);
			if (actualAttr == null) {
				printProblem("Actual element was missing expected attribute: " + attrName);
				printDifferences();
				return false;
			}
			String expectedValue = expectedAttr.getNodeValue();
			String actualValue = actualAttr.getNodeValue();
			if (!expectedValue.equals(actualValue)) {
				printProblem("Actual attribute value was different than expected: attribute "
						+ attrName + ", expected " + expectedValue + ", actual " + actualValue);
				printDifferences();
				return false;
			}
		}
		return true;
	}

	/**
	 * Compare the sets of element declarations nested within an actual and an expected element.
	 * Note that the DeclarationContents object also may contain an &lt;annotations&gt; node,
	 * but that must be compared separately.
	 * 
	 * @param actual
	 *            must not be null
	 * @param expected
	 *            must not be null
	 * @return true if the contents are equivalent.
	 */
	private boolean compareDeclarationContents(DeclarationContents actual, DeclarationContents expected) {

		// Compare each collection at this level
		if (!actual.typeDecls.keySet().equals(expected.typeDecls.keySet())) {
			printProblem("Contents of <elements> nodes did not match: different sets of type-elements");
			printDifferences();
			return false;
		}
		if (!actual.executableDecls.keySet().equals(expected.executableDecls.keySet())) {
			printProblem("Contents of <elements> nodes did not match: different sets of executable-elements");
			printDifferences();
			return false;
		}
		if (!actual.variableDecls.keySet().equals(expected.variableDecls.keySet())) {
			printProblem("Contents of <elements> nodes did not match: different sets of variable-elements");
			printDifferences();
			return false;
		}

		// Recurse by comparing individual elements
		for (Map.Entry<String, Element> expectedEntry : expected.typeDecls.entrySet()) {
			String sname = expectedEntry.getKey();
			Element actualElement = actual.typeDecls.get(sname);
			if (!compareDeclarations(actualElement, expectedEntry.getValue())) {
				return false;
			}
		}
		for (Map.Entry<String, Element> expectedEntry : expected.executableDecls.entrySet()) {
			String sname = expectedEntry.getKey();
			Element actualElement = actual.executableDecls.get(sname);
			if (!compareDeclarations(actualElement, expectedEntry.getValue())) {
				return false;
			}
		}
		for (Map.Entry<String, Element> expectedEntry : expected.variableDecls.entrySet()) {
			String sname = expectedEntry.getKey();
			Element actualElement = actual.variableDecls.get(sname);
			if (!compareDeclarations(actualElement, expectedEntry.getValue())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Compare an actual declaration to the expected reference. It is assumed that the element name
	 * (e.g., type-element, variable-element) and sname (e.g., "Foo") have already been compared.
	 * Attributes that exist on the actual declaration element but not on the expected declaration
	 * element are not considered to be a mismatch.
	 * 
	 * @param actualDecl
	 *            must be non-null
	 * @param expectedDecl
	 *            must be non-null
	 * @return true if the declarations are equivalent
	 */
	private boolean compareDeclarations(Element actualDecl, Element expectedDecl) {
		// compare the element kinds and any other relevant attributes
		// Intentionally ignore the presence of additional actual attributes not present in the
		// expected model
		if (!compareAttributes(actualDecl, expectedDecl)) {
			return false;
		}
		

		// Find nested element and <annotations> nodes
		DeclarationContents actualContents = new DeclarationContents();
		if (!collectDeclarationContents(actualDecl, actualContents)) {
			return false;
		}
		DeclarationContents expectedContents = new DeclarationContents();
		if (!collectDeclarationContents(expectedDecl, expectedContents)) {
			return false;
		}

		// compare annotations on the element
		if (actualContents.annotations != null && expectedContents.annotations != null) {
			if (!compareAnnotationsNodes(actualContents.annotations, expectedContents.annotations)) {
				return false;
			}
		} else if (actualContents.annotations != null) {
			// expectedAnnots == null
			printProblem("Unexpected annotations within element: "
					+ expectedDecl.getAttribute(SNAME));
			printDifferences();
			return false;
		} else if (expectedContents.annotations != null) {
			// actualAnnots == null
			printProblem("Missing expected annotations within element: "
					+ actualDecl.getAttribute(SNAME));
			printDifferences();
			return false;
		}
		// both null at the same time is okay, not a mismatch

		// compare the child elements
		if (!compareDeclarationContents(actualContents, expectedContents)) {
			return false;
		}

		return true;
	}

	/**
	 * Locate the outer &lt;model&gt; node. This node should always exist unless the model is
	 * completely empty.
	 * 
	 * @return the root model node, or null if one could not be found.
	 */
	private Element findRootNode(Document doc) {
		Node model = null;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (Node.ELEMENT_NODE == n.getNodeType()) {
				if (MODEL.equals(n.getNodeName())) {
					model = n;
					break;
				}
			}
		}
		return (Element) model;
	}

	/**
	 * Print the actual and expected documents in string form
	 * 
	 * TODO: a cursor to show what was being compared when the difference was detected.
	 */
	private void printDifferences() {
		_out.println("Actual was:\n--------");
		_out.println(XMLConverter.xmlToString(_actual));
		_out.println("--------\nAnd expected was:");
		_out.println(XMLConverter.xmlToString(_expected));
	}

	/**
	 * Report a specific problem.
	 */
	private void printProblem(String msg) {
		_out.println(msg);
	}
}
