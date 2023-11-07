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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

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
public class XMLComparer implements IXMLNames {

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
		Element superclass = null;
		Element interfaces = null;
		final TreeMap<String, Element> typeDecls = new TreeMap<String, Element>();
		final TreeMap<String, Element> executableDecls = new TreeMap<String, Element>();
		final TreeMap<String, Element> variableDecls = new TreeMap<String, Element>();
		// TODO: PACKAGE, TYPE_PARAMETER, OTHER
	}

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
	 * @param summary
	 *            a StringBuilder to which will be appended a brief summary of the problem if a
	 *            problem was encountered. Can be null if no summary is desired.
	 * @param ignoreJavacBugs
	 *            true if mismatches corresponding to known javac bugs should be ignored.
	 * @return true if the models match sufficiently to satisfy the spec.
	 */
	public static boolean compare(Document actual, Document expected,
			OutputStream out, StringBuilder summary, boolean ignoreJavacBugs) {
		XMLComparer comparer = new XMLComparer(actual, expected, out, summary, ignoreJavacBugs);
		return comparer._compare();
	}

	private final Document _actual;

	private final Document _expected;

	/**
	 * If true, don't complain about mismatches corresponding to known javac bugs,
	 * even if they represent a violation of the spec.  This is useful when running
	 * tests against the reference implementation.
	 */
	private final boolean _ignoreJavacBugs;

	private final PrintStream _out;

	private final StringBuilder _summary;

	/**
	 * Clients should not construct instances of this object.
	 */
	private XMLComparer(Document actual, Document expected, OutputStream out, StringBuilder summary, boolean ignoreJavacBugs) {
		_actual = actual;
		_expected = expected;
		_ignoreJavacBugs = ignoreJavacBugs;
		OutputStream os;
		if (out != null) {
			os = out;
		} else {
			os = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					// do nothing
				}
			};
		}
		_out = new PrintStream(os, true);
		_summary = summary;
	}

	/**
	 * Test this class by creating a known XML language model and using
	 * this class to compare it to a known reference model.  The models
	 * should match.
	 * @return true if the models matched, i.e., if the test passed
	 */
	public static boolean test() throws Exception {
		final String XML_FRAMEWORK_TEST_MODEL =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			"<model>\n" +
			" <type-element kind=\"CLASS\" qname=\"pa.A\" sname=\"A\">\n" +
			"  <superclass>\n" +
			"   <type-mirror kind=\"DECLARED\" to-string=\"java.lang.Object\"/>\n" +
			"  </superclass>\n" +
			"  <variable-element kind=\"FIELD\" sname=\"f\" type=\"java.lang.String\">\n" +
			"   <annotations>\n" +
			"    <annotation sname=\"Anno1\">\n" +
			"     <annotation-values>\n" +
			"      <annotation-value member=\"value\" type=\"java.lang.String\" value=\"spud\"/>\n" +
			"     </annotation-values>\n" +
			"    </annotation>\n" +
			"   </annotations>\n" +
			"  </variable-element>\n" +
			" </type-element>\n" +
			"</model>\n";

		// create "actual" model
		Document actualModel = org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE().newDocument();
		Element modelNode = actualModel.createElement(MODEL_TAG);
		// primary type
		Element typeNode = actualModel.createElement(TYPE_ELEMENT_TAG);
		typeNode.setAttribute(KIND_TAG, "CLASS");
		typeNode.setAttribute(SNAME_TAG, "A");
		typeNode.setAttribute(QNAME_TAG, "pa.A");
		// superclass
		Element scNode = actualModel.createElement(SUPERCLASS_TAG);
		Element tmNode = actualModel.createElement(TYPE_MIRROR_TAG);
		tmNode.setAttribute(KIND_TAG, "DECLARED");
		tmNode.setAttribute(TO_STRING_TAG, "java.lang.Object");
		scNode.appendChild(tmNode);
		typeNode.appendChild(scNode);
		// field
		Element variableNode = actualModel.createElement(VARIABLE_ELEMENT_TAG);
		variableNode.setAttribute(KIND_TAG, "FIELD");
		variableNode.setAttribute(SNAME_TAG, "f");
		variableNode.setAttribute(TYPE_TAG, "java.lang.String");
		// annotation on field
		Element annotationsNode = actualModel.createElement(ANNOTATIONS_TAG);
		Element annoNode = actualModel.createElement(ANNOTATION_TAG);
		annoNode.setAttribute(SNAME_TAG, "Anno1");
		Element valuesNode = actualModel.createElement(ANNOTATION_VALUES_TAG);
		Element valueNode = actualModel.createElement(ANNOTATION_VALUE_TAG);
		valueNode.setAttribute(MEMBER_TAG, "value");
		valueNode.setAttribute(TYPE_TAG, "java.lang.String");
		valueNode.setAttribute(VALUE_TAG, "spud");
		valuesNode.appendChild(valueNode);
		annoNode.appendChild(valuesNode);
		annotationsNode.appendChild(annoNode);
		variableNode.appendChild(annotationsNode);
		typeNode.appendChild(variableNode);
		modelNode.appendChild(typeNode);
		actualModel.appendChild(modelNode);

		// load reference model
    	InputSource source = new InputSource(new StringReader(XML_FRAMEWORK_TEST_MODEL));
        Document expectedModel = org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE().parse(source);

        // compare actual and reference
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder summary = new StringBuilder();
        summary.append("testXMLFramework failed; see console for details.  ");
        boolean success = compare(actualModel, expectedModel, out, summary, false /* ignoreJavacBugs */);
        if (!success) {
        	System.out.println("testXMLFramework failed.  Detailed output follows:");
        	System.out.print(out.toString());
        	System.out.println("=============== end output ===============");
        }
        return success;
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
	 * TODO: revisit this - we need to model duplications, in order to handle incorrect code.
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
			String sname = e.getAttribute(SNAME_TAG);
			if (sname == null) {
				printProblem("A child of an <annotations> node was missing the \"sname\" attribute");
				printDifferences();
				return false;
			}

			// categorize
			Element old = null;
			if (ANNOTATION_TAG.equals(nodeName)) {
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
	 * TODO: revisit this - we need to model duplications, in order to handle incorrect code.
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

			if (ANNOTATIONS_TAG.equals(nodeName)) {
				if (contents.annotations != null) {
					printProblem("XML syntax error: a declaration contained more than one <annotations> node");
					printDifferences();
					return false;
				}
				contents.annotations = e;
			} else if (SUPERCLASS_TAG.equals(nodeName)) {
				if (contents.superclass != null) {
					printProblem("XML syntax error: a declaration contained more than one <superclass> node");
					printDifferences();
					return false;
				}
				contents.superclass = e;
			} else if (INTERFACES_TAG.equals(nodeName)) {
				if (contents.interfaces != null) {
					printProblem("XML syntax error: a declaration contained more than one <interfaces> node");
					printDifferences();
					return false;
				}
				contents.interfaces = e;
			} else {
				// get 'sname'
				String sname = e.getAttribute(SNAME_TAG);
				if (sname == null) {
					printProblem("A child of an <elements> node was missing the \"sname\" attribute");
					printDifferences();
					return false;
				}

				// categorize
				Element old = null;
				if (EXECUTABLE_ELEMENT_TAG.equals(nodeName)) {
					old = contents.executableDecls.put(sname, e);
				} else if (TYPE_ELEMENT_TAG.equals(nodeName)) {
					old = contents.typeDecls.put(sname, e);
				} else if (VARIABLE_ELEMENT_TAG.equals(nodeName)) {
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
	 * Collect the &lt;type-mirror&gt; children of a parent node into a map,
	 * keyed and sorted by the canonicalized type name.
	 * For now, we use the toString() output as the canonical name, even though
	 * that is unspecified and implementation-dependent.
	 * Reject duplicated types.
	 * TODO: revisit this - we need to model duplications, in order to handle incorrect code.
	 * @param parent the parent node
	 * @param typesMap the map, presumed to be empty on entry
	 * @return true if no errors were reported
	 */
	private boolean collectTypes(Node parent, Map<String, Element> typesMap) {
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE & TYPE_MIRROR_TAG.equals(n.getNodeName())) {
				Element typeMirror = (Element)n;
				String toStringAttr = typeMirror.getAttribute(TO_STRING_TAG);
				if (null == toStringAttr || toStringAttr.length() < 1) {
					printProblem("<type-mirror> node was missing its \"to-string\" attribute");
					printDifferences();
					return false;
				}
				Element old = typesMap.put(toStringAttr, typeMirror);
				if (null != old) {
					printProblem("Two <type-mirror> nodes had the same \"to-string\" attribute: " + toStringAttr);
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
			if (SNAME_TAG.equals(attrName)) {
				sname = actualValue;
			}
		}

		// Examine member-value pairs
		Element actualValues = findNamedChildElement(actualAnnot, ANNOTATION_VALUES_TAG);
		Element expectedValues = findNamedChildElement(expectedAnnot, ANNOTATION_VALUES_TAG);
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
			if (nExpected.getNodeType() == Node.ELEMENT_NODE && ANNOTATION_VALUE_TAG.equals(nExpected.getNodeName())) {
				while (nActual != null &&
						(nActual.getNodeType() != Node.ELEMENT_NODE ||
						!ANNOTATION_VALUE_TAG.equals(nActual.getNodeName()))) {
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
			if (OPTIONAL_TAG.equals(attrName)) {
				// "optional" is an instruction to the comparer, not a model attribute
				continue;
			}
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
	 * If an expected element has the "optional" attribute, it is allowed to be missing from
	 * the actual contents.  It is always a mismatch if the actual contents include an element
	 * that is not in the expected contents, though.
	 *
	 * @param actual
	 *            must not be null
	 * @param expected
	 *            must not be null
	 * @return true if the contents are equivalent.
	 */
	private boolean compareDeclarationContents(DeclarationContents actual, DeclarationContents expected) {

		// Compare each collection at this level

		if (!optionalMatch(actual.typeDecls, expected.typeDecls)) {
			printProblem("Contents of <elements> nodes did not match: different sets of type-elements");
			printDifferences();
			return false;
		}
		if (!optionalMatch(actual.executableDecls, expected.executableDecls)) {
			printProblem("Contents of <elements> nodes did not match: different sets of executable-elements");
			printDifferences();
			return false;
		}
		if (!optionalMatch(actual.variableDecls, expected.variableDecls)) {
			printProblem("Contents of <elements> nodes did not match: different sets of variable-elements");
			printDifferences();
			return false;
		}

		// Recurse by comparing individual elements
		for (Map.Entry<String, Element> expectedEntry : expected.typeDecls.entrySet()) {
			String sname = expectedEntry.getKey();
			Element actualElement = actual.typeDecls.get(sname);
			if (actualElement != null && !compareDeclarations(actualElement, expectedEntry.getValue())) {
				return false;
			}
		}
		for (Map.Entry<String, Element> expectedEntry : expected.executableDecls.entrySet()) {
			String sname = expectedEntry.getKey();
			Element actualElement = actual.executableDecls.get(sname);
			if (actualElement != null && !compareDeclarations(actualElement, expectedEntry.getValue())) {
				return false;
			}
		}
		for (Map.Entry<String, Element> expectedEntry : expected.variableDecls.entrySet()) {
			String sname = expectedEntry.getKey();
			Element actualElement = actual.variableDecls.get(sname);
			if (actualElement != null && !compareDeclarations(actualElement, expectedEntry.getValue())) {
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
					+ expectedDecl.getAttribute(SNAME_TAG));
			printDifferences();
			return false;
		} else if (expectedContents.annotations != null) {
			// actualAnnots == null
			printProblem("Missing expected annotations within element: "
					+ actualDecl.getAttribute(SNAME_TAG));
			printDifferences();
			return false;
		}
		// both null at the same time is okay, not a mismatch

		// compare superclasses.  Ignore if reference does not specify a superclass.
		if (expectedContents.superclass != null) {
			if (actualContents.superclass == null) {
				printProblem("No superclass found for element: " + actualDecl.getAttribute(SNAME_TAG));
				printDifferences();
				return false;
			}
			if (!compareSuperclassNodes(actualContents.superclass, expectedContents.superclass)) {
				return false;
			}
		}

		// compare interface lists.  Ignore if reference does not specify interfaces.
		// TODO: javac fails to provide unresolved interfaces.  Here, we ignore interfaces altogether
		// if we're ignoring javac bugs, which means we also ignore the non-error cases.
		if (expectedContents.interfaces != null && !_ignoreJavacBugs) {
			if (actualContents.interfaces == null) {
				printProblem("No interfaces list found for element: " + actualDecl.getAttribute(SNAME_TAG));
				printDifferences();
				return false;
			}
			if (!compareInterfacesNodes(actualContents.interfaces, expectedContents.interfaces)) {
				return false;
			}
		}

		// compare the child elements
		if (!compareDeclarationContents(actualContents, expectedContents)) {
			return false;
		}

		return true;
	}

	/**
	 * Compare two interface lists, i.e., &lt;interfaces&gt; nodes.
	 * Each is expected to contain zero or more &lt;type-mirror&gt; nodes.
	 * The spec for {@link javax.lang.model.element.TypeElement#getInterfaces()}
	 * does not say anything about the order of the items returned, so here we
	 * load them into a Map<String, Element> keyed by the type's toString()
	 * output.  Note that toString() on a TypeMirror is not very well
	 * specified either, so this is not guaranteed to produce good results.
	 * @param actual the observed &lt;interfaces&gt; node, must be non-null.
	 * @param expected the reference &lt;interfaces&gt; node, must be non-null
	 * @return true if the nodes are equivalent.
	 */
	private boolean compareInterfacesNodes(Element actual, Element expected) {
		Map<String, Element> expectedTypes = new TreeMap<String, Element>();
		Map<String, Element> actualTypes = new TreeMap<String, Element>();
		if (!collectTypes(expected, expectedTypes)) {
			return false;
		}
		if (!collectTypes(actual, actualTypes)) {
			return false;
		}
		if (expectedTypes.size() != actualTypes.size()) {
			if (_ignoreJavacBugs) {
				// javac has a known bug where it does not correctly model
				// unresolved interface types.  Ideally we could still verify
				// the resolved ones but that seems like more work than it's worth.
				return true;
			}
			printProblem("Actual and expected interface lists have different sizes: expected = " +
					expectedTypes.size() + ", actual = " + actualTypes.size());
			printDifferences();
			return false;
		}
		Iterator<Entry<String, Element>> expectedIter = expectedTypes.entrySet().iterator();
		Iterator<Entry<String, Element>> actualIter = actualTypes.entrySet().iterator();
		// if we got this far, the two maps are the same size
		while (expectedIter.hasNext()) {
			Entry<String, Element> expectedEntry = expectedIter.next();
			Entry<String, Element> actualEntry = actualIter.next();
			if (!compareTypeMirrors(actualEntry.getValue(), expectedEntry.getValue())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compare two &lt;superclass&gt; nodes.  Each is expected to contain
	 * exactly one &lt;type-mirror&gt; node.
	 *
	 * @param actual the observed &lt;superclass&gt; node; must not be null
	 * @param expected the reference &lt;superclass&gt; node; must not be null
	 * @return true if the superclass types are equivalent
	 */
	private boolean compareSuperclassNodes(Element actual, Element expected) {
		Element expectedType = findNamedChildElement(expected, TYPE_MIRROR_TAG);
		if (expectedType == null) {
			// Syntax error in the reference model, i.e., problem in test code
			printProblem("Bug in reference model: a <superclass> node was missing its <type-mirror> element");
			printDifferences();
			return false;
		}
		Element actualType = findNamedChildElement(actual, TYPE_MIRROR_TAG);
		if (actualType == null) {
			// This probably indicates a problem in the XMLConverter class
			printProblem("Bug in test code: a <superclass> node was missing its <type-mirror> element in the XML model of the observed language model");
			printDifferences();
			return false;
		}
		return compareTypeMirrors(actualType, expectedType);
	}

	private boolean compareTypeMirrors(Element actual, Element expected) {
		String expectedKind = expected.getAttribute(KIND_TAG);
		if (expectedKind != null && expectedKind.length() > 0) {
			String actualKind = actual.getAttribute(KIND_TAG);
			if (!expectedKind.equals(actualKind)) {
				printProblem("Superclasses had different kind: expected " + expectedKind + " but found " + actualKind);
				printDifferences();
				return false;
			}
		}
		if (!TYPEKIND_ERROR.equals(expectedKind)) {
			String expectedToString = expected.getAttribute(TO_STRING_TAG);
			if (expectedToString != null && expectedToString.length() > 0) {
				String actualToString = actual.getAttribute(TO_STRING_TAG);
				if (!expectedToString.equals(actualToString)) {
					printProblem("Superclasses had different toString() output: expected " + expectedToString + " but found " + actualToString);
					printDifferences();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Given some non-null parent, find the first child element with a particular name
	 * @return the child, or null if one was not found
	 */
	private Element findNamedChildElement(Node parent, String name) {
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) {
				return (Element)n;
			}
		}
		return null;
	}

	/**
	 * Locate the outer &lt;model&gt; node. This node should always exist unless the model is
	 * completely empty.
	 *
	 * @return the root model node, or null if one could not be found.
	 */
	private Element findRootNode(Document doc) {
		return findNamedChildElement(doc, MODEL_TAG);
	}

	/**
	 * Compare actual and expected.  Ignore the presence of any elements in
	 * 'expected' that are absent from 'actual' iff the elements are tagged
	 * with the "optional" attribute.
	 * @return true if the collections match.
	 */
	private boolean optionalMatch(Map<String, Element> actual, Map<String, Element> expected) {
		// Does actual contain anything that is not in expected?
		Set<String> extraActuals = new HashSet<String>(actual.keySet());
		extraActuals.removeAll(expected.keySet());
		if (!extraActuals.isEmpty()) {
			return false;
		}

		// Does expected contain anything that is not in actual, that is not optional?
		Set<String> extraExpecteds = new HashSet<String>(expected.keySet());
		extraExpecteds.removeAll(actual.keySet());
		Iterator<String> iter = extraExpecteds.iterator();
		while (iter.hasNext()) {
			Element e = expected.get(iter.next());
			boolean optional = Boolean.parseBoolean(e.getAttribute(OPTIONAL_TAG));
			if (optional) {
				iter.remove();
			}
		}
		return extraExpecteds.isEmpty();
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
		if (_summary != null) {
			_summary.append(msg);
		}
		_out.println(msg);
	}
}
