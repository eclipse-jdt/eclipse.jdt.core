package net.sf.saxon.om;

public final class Navigator {

    /**
     * Determine if a string is all-whitespace
     *
     * @param content the string to be tested
     * @return true if the supplied string contains no non-whitespace
     *     characters
     */

    public final static boolean isWhite(CharSequence content) {
        for (int i=0; i<content.length();) {
            // all valid XML whitespace characters, and only whitespace characters, are <= 0x20
            if (content.charAt(i++) > 32) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the string value of an attribute of a given element, given the URI and
     * local part of the attribute name.
     * @return the attribute value, or null if the attribute is not present
     */

    public static String getAttributeValue(NodeInfo element, String uri, String localName) {
        int fingerprint = element.getNamePool().allocate("", uri, localName);
        return element.getAttributeValue(fingerprint);
    }

    /**
     * Get an absolute XPath expression that identifies a given node within its document
     *
     * @param node the node whose path is required
     * @return a path expression that can be used to retrieve the node
     */

    public static String getPath(NodeInfo node) {
        String pre;
        NodeInfo parent = node.getParent();
        // System.err.println("node = " + node + " parent = " + parent);
        // Handle parentless nodes of any kind
        if (parent==null) return "/";
        switch (node.getNodeKind()) {
            case Type.DOCUMENT:
                return "/";
            case Type.ELEMENT:
                pre = getPath(parent);
                return (pre.equals("/") ? "" : pre) +
                        "/" + node.getDisplayName() + "[" + getNumberSimple(node) + "]";
            case Type.ATTRIBUTE:
                return getPath(parent) + "/@" + node.getDisplayName();
            case Type.TEXT:
                pre = getPath(parent);
                return (pre.equals("/") ? "" : pre) +
                        "/text()[" + getNumberSimple(node) + "]";
            case Type.COMMENT:
                pre = getPath(parent);
                return (pre.equals("/") ? "" : pre) +
                    "/comment()[" + getNumberSimple(node) + "]";
            case Type.PROCESSING_INSTRUCTION:
                pre = getPath(parent);
                return (pre.equals("/") ? "" : pre) +
                    "/processing-instruction()[" + getNumberSimple(node) + "]";
            default:
                return "";
        }
    }

    /**
     * Get simple node number. This is defined as one plus the number of previous siblings of the
     * same node type and name. It is not accessible directly in XSL.
     *
     * @param node The node whose number is required
     * @param controller Used for remembering previous result, for
     *     performance
     * @exception XPathException if any error occurs
     * @return the node number, as defined above
     */

    public static int getNumberSimple(NodeInfo node, Controller controller) throws XPathException {

        //checkNumberable(node);

        int fingerprint = node.getFingerprint();
        NodeTest same;

        if (fingerprint==-1) {
            same = NodeKindTest.makeNodeKindTest(node.getNodeKind());
        } else {
            same = new NameTest(node);
        }

        SequenceIterator preceding = node.iterateAxis(Axis.PRECEDING_SIBLING, same);

        int i=1;
        while (true) {
            NodeInfo prev = (NodeInfo)preceding.next();
            if (prev == null) {
                break;
            }

            int memo = controller.getRememberedNumber(prev);
            if (memo>0) {
                memo += i;
                controller.setRememberedNumber(node, memo);
                return memo;
            }

            i++;
        }

        controller.setRememberedNumber(node, i);
        return i;
    }

    /**
     * Get simple node number. This is defined as one plus the number of previous siblings of the
     * same node type and name. It is not accessible directly in XSL. This version doesn't require
     * the controller, and therefore doesn't remember previous results. It is used only by getPath().
     *
     * @param node the node whose number is required
     * @return the node number, as defined above
     */

    private static int getNumberSimple(NodeInfo node) {

        int fingerprint = node.getFingerprint();
        NodeTest same;

        if (fingerprint==-1) {
            same = NodeKindTest.makeNodeKindTest(node.getNodeKind());
        } else {
            same = new NameTest(node);
        }

        AxisIterator preceding = node.iterateAxis(Axis.PRECEDING_SIBLING, same);

        int i=1;
        while (preceding.next() != null) {
            i++;
        }

        return i;
    }

    /**
     * Get node number (level="single"). If the current node matches the supplied pattern, the returned
     * number is one plus the number of previous siblings that match the pattern. Otherwise,
     * return the element number of the nearest ancestor that matches the supplied pattern.
     *
     * @param node the current node, the one whose node number is required
     * @param count Pattern that identifies which nodes should be
     *     counted. Default (null) is the element name if the current node is
     *      an element, or "node()" otherwise.
     * @param from Pattern that specifies where counting starts from.
     *     Default (null) is the root node. (This parameter does not seem
     *     useful but is included for the sake of XSLT conformance.)
     * @param controller the controller of the transformation, used if
     *     the patterns reference context values (e.g. variables)
     * @exception XPathException when any error occurs in processing
     * @return the node number established as follows: go to the nearest
     *     ancestor-or-self that matches the 'count' pattern and that is a
     *     descendant of the nearest ancestor that matches the 'from' pattern.
     *      Return one plus the nunber of preceding siblings of that ancestor
     *      that match the 'count' pattern. If there is no such ancestor,
     *     return 0.
     */

    public static int getNumberSingle(NodeInfo node, Pattern count,
                    Pattern from, Controller controller) throws XPathException {

//        checkNumberable(node);

        if (count==null && from==null) {
            return getNumberSimple(node, controller);
        }

        boolean knownToMatch = false;
        if (count==null) {
            if (node.getFingerprint()==-1) {	// unnamed node
                count = NodeKindTest.makeNodeKindTest(node.getNodeKind());
            } else {
                count = new NameTest(node);
            }
            knownToMatch = true;
        }

        NodeInfo target = node;
        while (!(knownToMatch || count.matches(target, controller))) {
            target = target.getParent();
            if (target==null) {
                return 0;
            }
            if (from!=null && from.matches(target, controller)) {
                return 0;
            }
        }

        // we've found the ancestor to count from

        SequenceIterator preceding =
            target.iterateAxis(Axis.PRECEDING_SIBLING, count.getNodeTest());
                        // pass the filter condition down to the axis enumeration where possible
        boolean alreadyChecked = (count instanceof NodeTest);
        int i = 1;
        while (true) {
            NodeInfo p = (NodeInfo)preceding.next();
            if (p == null) {
                return i;
            }
            if (alreadyChecked || count.matches(p, controller)) {
                i++;
            }
        }
    }

    /**
     * Get node number (level="any").
     * Return one plus the number of previous nodes in the
     * document that match the supplied pattern
     *
     * @exception XPathException
     * @param inst Identifies the xsl:number instruction; this is relevant
     *     when the function is memoised to support repeated use of the same
     *     instruction to number modulple nodes
     * @param node Identifies the xsl:number instruction; this is
     *     relevant when the function is memoised to support repeated use of
     *     the same instruction to number modulple nodes
     * @param count Pattern that identifies which nodes should be
     *     counted. Default (null) is the element name if the current node is
     *      an element, or "node()" otherwise.
     * @param from Pattern that specifies where counting starts from.
     *     Default (null) is the root node. Only nodes after the first (most
     *     recent) node that matches the 'from' pattern are counted.
     * @param controller The controller
     * @param hasVariablesInPatterns if the count or from patterns
     *     contain variables, then it's not safe to get the answer by adding
     *     one to the number of the most recent node that matches
     * @return one plus the number of nodes that precede the current node,
     *     that match the count pattern, and that follow the first node that
     *     matches the from pattern if specified.
     */

    public static int getNumberAny(Instruction inst, NodeInfo node, Pattern count,
                    Pattern from, Controller controller, boolean hasVariablesInPatterns) throws XPathException {

        NodeInfo memoNode = null;
        int memoNumber = 0;
        boolean memoise = (!hasVariablesInPatterns && count!=null);
        if (memoise) {
            Object[] memo = (Object[])controller.getUserData(inst, "xsl:number");
            if (memo != null) {
                memoNode = (NodeInfo)memo[0];
                memoNumber = ((Integer)memo[1]).intValue();
            }
        }

        int num = 0;
        if (count==null) {
            if (node.getFingerprint()==-1) {	// unnamed node
                count = NodeKindTest.makeNodeKindTest(node.getNodeKind());
            } else {
                count = new NameTest(node);
            }
            num = 1;
        } else if (count.matches(node, controller)) {
            num = 1;
        }

        // We use a special axis invented for the purpose: the union of the preceding and
        // ancestor axes, but in reverse document order

        // Pass part of the filtering down to the axis iterator if possible
        NodeTest filter;
        if (from==null) {
            filter = count.getNodeTest();
        } else if (from.getNodeKind()==Type.ELEMENT && count.getNodeKind()==Type.ELEMENT) {
            filter = NodeKindTest.ELEMENT;
        } else {
            filter = AnyNodeTest.getInstance();
        }

        SequenceIterator preceding =
            node.iterateAxis(Axis.PRECEDING_OR_ANCESTOR, filter);

        while (true) {
            NodeInfo prev = (NodeInfo)preceding.next();
            if (prev == null) {
                break;
            }
            if (from!=null && from.matches(prev, controller)) {
                return num;
            }
            if (count.matches(prev, controller)) {
                if (num==1 && memoNode!=null && prev.isSameNode(memoNode)) {
                    num = memoNumber + 1;
                    break;
                }
                num++;
            }
        }
        if (memoise) {
            Object[] memo = new Object[2];
            memo[0] = node;
            memo[1] = new Integer(num);
            controller.setUserData(inst, "xsl:number", memo);
        }
        return num;
    }

    /**
     * Get node number (level="multiple").
     * Return a vector giving the hierarchic position of this node. See the XSLT spec for details.
     *
     * @exception XPathException
     * @param node The node to be numbered
     * @param count Pattern that identifies which nodes (ancestors and
     *      their previous siblings) should be counted. Default (null) is the
     *      element name if the current node is an element, or "node()"
     *     otherwise.
     * @param from Pattern that specifies where counting starts from.
     *     Default (null) is the root node. Only nodes below the first (most
     *     recent) node that matches the 'from' pattern are counted.
     * @param controller The controller for the transformation
     * @return a vector containing for each ancestor-or-self that matches the
     *      count pattern and that is below the nearest node that matches the
     *      from pattern, an Integer which is one greater than the number of
     *     previous siblings that match the count pattern.
     */

    public static List getNumberMulti(NodeInfo node, Pattern count,
                    Pattern from, Controller controller) throws XPathException {

        //checkNumberable(node);

        ArrayList v = new ArrayList();

        if (count==null) {
            if (node.getFingerprint()==-1) {    // unnamed node
                count = NodeKindTest.makeNodeKindTest(node.getNodeKind());
            } else {
                count = new NameTest(node);
            }
        }

        NodeInfo curr = node;

        while(true) {
            if (count.matches(curr, controller)) {
                int num = getNumberSingle(curr, count, null, controller);
                v.add(0, new Long(num));
            }
            curr = curr.getParent();
            if (curr==null) break;
            if (from!=null && from.matches(curr, controller)) break;
        }

        return v;
    }

     /**
     * Generic (model-independent) implementation of deep copy algorithm for nodes.
     * This is available for use by any node implementations that choose to use it.
      * @param node The node to be copied
      * @param out The receiver to which events will be sent
      * @param namePool Namepool holding the name codes (used only to resolve namespace
      *          codes)
      * @param whichNamespaces Indicates which namespace nodes for an element should
      *          be copied
      * @param copyAnnotations Indicates whether type annotations should be copied
      * @throws TransformerException on any failure reported by the Receiver
     */

    public static void copy(NodeInfo node,
                            Receiver out,
                            NamePool namePool,
                            int whichNamespaces,
                            boolean copyAnnotations) throws TransformerException {

        switch (node.getNodeKind()) {
            case Type.DOCUMENT:
                AxisIterator children0 = node.iterateAxis(Axis.CHILD, new AnyNodeTest());
                while (true) {
                    NodeInfo child = (NodeInfo)children0.next();
                    if (child == null) {
                        return;
                    }
                    child.copy(out, whichNamespaces, copyAnnotations);
                }

            case Type.ELEMENT:
                out.startElement(node.getNameCode(), 0, 0);

                // output the namespaces

                if (whichNamespaces != NodeInfo.NO_NAMESPACES) {
                    node.outputNamespaceNodes(out, true);
                }

                // output the attributes

                AxisIterator attributes = node.iterateAxis(Axis.ATTRIBUTE, new AnyNodeTest());
                while (true) {
                    NodeInfo att = (NodeInfo)attributes.next();
                    if (att == null) {
                        break;
                    }
                    att.copy(out, whichNamespaces, copyAnnotations);
                }

                // output the children

                AxisIterator children = node.iterateAxis(Axis.CHILD, new AnyNodeTest());
                while (true) {
                    NodeInfo child = (NodeInfo)children.next();
                    if (child == null) {
                        break;
                    }
                    child.copy(out, whichNamespaces, copyAnnotations);
                }

                // finally the end tag

                out.endElement();
                return;

            case Type.ATTRIBUTE:
                out.attribute(node.getNameCode(), 0, node.getStringValue(), 0);
                return;

            case Type.TEXT:
                out.characters(node.getStringValue(), 0);
                return;

            case Type.COMMENT:
                out.comment(node.getStringValue(), 0);
                return;

            case Type.PROCESSING_INSTRUCTION:
                out.processingInstruction(node.getLocalPart(), node.getStringValue(), 0);
                return;

            case Type.NAMESPACE:
                out.namespace(namePool.allocateNamespaceCode(node.getLocalPart(), node.getStringValue()),0);
                return;

            default:

        }
    }

    /**
    * Generic (model-independent) method to determine the relative position of two
    * node in document order. The nodes must be in the same tree.
    * @param first The first node
    * @param second The second node, whose position is to be compared with the first node
    * @return -1 if this node precedes the other node, +1 if it follows the other
    * node, or 0 if they are the same node. (In this case, isSameNode() will always
    * return true, and the two nodes will produce the same result for generateId())
    */

    public static int compareOrder(SiblingCountingNode first, SiblingCountingNode second) {
        NodeInfo ow = second;

        // are they the same node?
        if (first.isSameNode(second)) {
            return 0;
        }
        // are they siblings (common case)
        if (first.getParent().isSameNode(second.getParent())) {
            return first.getSiblingPosition() - second.getSiblingPosition();
        }
        // find the depths of both nodes in the tree

        int depth1 = 0;
        int depth2 = 0;
        NodeInfo p1 = first;
        NodeInfo p2 = second;
        while (p1 != null) {
            depth1++;
            p1 = p1.getParent();
        }
        while (p2 != null) {
            depth2++;
            p2 = p2.getParent();
        }
        // move up one branch of the tree so we have two nodes on the same level

        p1 = first;
        while (depth1>depth2) {
            p1 = p1.getParent();
            if (p1.isSameNode(second)) {
                return +1;
            }
            depth1--;
        }

        p2 = ow;
        while (depth2>depth1) {
            p2 = p2.getParent();
            if (p2.isSameNode(first)) {
                return -1;
            }
            depth2--;
        }

        // now move up both branches in sync until we find a common parent
        while (true) {
            NodeInfo par1 = p1.getParent();
            NodeInfo par2 = p2.getParent();
            if (par1==null || par2==null) {
                throw new NullPointerException("DOM tree compare - internal error");
            }
            if (par1.isSameNode(par2)) {
                return ((SiblingCountingNode)p1).getSiblingPosition() -
                        ((SiblingCountingNode)p2).getSiblingPosition();
            }
            p1 = par1;
            p2 = par2;
        }
    }

    /**
    * Get a character string that uniquely identifies this node and that collates nodes
    * into document order
    * @return a string. The string is always interned so keys can be compared using "==".
    */

    public static String getSequentialKey(SiblingCountingNode node) {
        // TODO: this was designed so it could be used for sorting nodes into document
        // order, but is not currently used that way.
        StringBuffer key = new StringBuffer();
        while(!(node instanceof DocumentInfo)) {
            key.insert(0, alphaKey(node.getSiblingPosition()));
            node = (SiblingCountingNode)node.getParent();
        }
        key.insert(0, "w" + node.getDocumentNumber());
        return key.toString().intern();
    }

    /**
    * Construct an alphabetic key from an positive integer; the key collates in the same sequence
    * as the integer
    * @param value The positive integer key value (negative values are treated as zero).
    */

    public static String alphaKey(int value) {
        if (value<1) return "a";
        if (value<10) return "b" + value;
        if (value<100) return "c" + value;
        if (value<1000) return "d" + value;
        if (value<10000) return "e" + value;
        if (value<100000) return "f" + value;
        if (value<1000000) return "g" + value;
        if (value<10000000) return "h" + value;
        if (value<100000000) return "i" + value;
        if (value<1000000000) return "j" + value;
        return "k" + value;
    }


    ///////////////////////////////////////////////////////////////////////////////
    // Helper classes to support axis iteration
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * AxisFilter is an iterator that applies a NodeTest filter to
     * the nodes returned by an underlying AxisIterator.
     */

    public static class AxisFilter extends AxisIteratorImpl {
        private int last = -1;

        /**
         * Construct a AxisFilter
         * @param base the underlying iterator that returns all the nodes on
         * a required axis. This must not be an atomizing iterator!
         * @param test a NodeTest that is applied to each node returned by the
         * underlying AxisIterator; only those nodes that pass the NodeTest are
         * returned by the AxisFilter
         */

        public AxisFilter(AxisIterator base, NodeTest test) {
            this.base = base;
            this.nodeTest = test;
            position = 0;
        }

    	public Item next() {
            while (true) {
    	        current = base.next();
                if (current == null) {
                    return null;
                }
                NodeInfo n = (NodeInfo)current;
    	        if (nodeTest.matches(n.getNodeKind(),
                                     n.getFingerprint(),
                                     n.getTypeAnnotation())) {
    	            position++;
                    return current;
    	        }
    	    }
        }

    	public int getLastPosition() {

    	    // To find out how many nodes there are in the axis, we
    	    // make a copy of the original node enumeration, and run through
    	    // the whole thing again, counting how many nodes match the filter.

    	    if (last>=0) {
    	        return last;
    	    }
    	    last = 0;
            AxisIterator b = (AxisIterator)base.getAnother();
            while (true) {
                NodeInfo n = (NodeInfo)b.next();
                if (n == null) {
                    return last;
                }
                if (nodeTest.matches(n.getNodeKind(),
                                     n.getFingerprint(),
                                     n.getTypeAnnotation())) {
                    last++;
                }
            }
    	}

    	public SequenceIterator getAnother() {
    	    return new AxisFilter((AxisIterator)base.getAnother(), nodeTest);
    	}
	}
}
