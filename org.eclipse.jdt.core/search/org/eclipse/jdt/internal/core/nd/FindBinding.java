/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

/**
 * Look up bindings in BTree objects and IPDOMNode objects
 * @since 3.12
 */
public class FindBinding {
//	public static class DefaultBindingBTreeComparator implements IBTreeComparator {
//		protected final Database database;
//
//		public DefaultBindingBTreeComparator(PDOM pdom) {
//			this.database = pdom.getDB();
//		}
//
//		@Override
//		public int compare(long record1, long record2) throws IndexException {
//			IString nm1 = PDOMNamedNode.getDBName(this.database, record1);
//			IString nm2 = PDOMNamedNode.getDBName(this.database, record2);
//			int cmp= nm1.compareCompatibleWithIgnoreCase(nm2);
//			if (cmp == 0) {
//				long t1= PDOMBinding.getLocalToFileRec(this.database, record1);
//				long t2= PDOMBinding.getLocalToFileRec(this.database, record2);
//				if (t1 == t2) {
//					t1 = PDOMNode.getNodeType(this.database, record1);
//					t2 = PDOMNode.getNodeType(this.database, record2);
//					if (t1 == t2 && t1 == IIndexBindingConstants.ENUMERATOR) {
//						// Allow to insert multiple enumerators into the global index.
//						t1= record1;
//						t2= record2;
//					}
//				}
//				cmp= t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
//			}
//			return cmp;
//		}
//	}
//
//	public static class DefaultFindBindingVisitor implements IBTreeVisitor, IPDOMVisitor {
//		private final char[] fName;
//		private final int[] fConstants;
//		private final long fLocalToFile;
//		protected PDOMBinding fResult;
//		private PDOM pdom;
//
//		protected DefaultFindBindingVisitor(PDOM pdom, char[] name, int[] constants, long localToFile) {
//			this.pdom = pdom;
//			this.fName = name;
//			this.fConstants = constants;
//			this.fLocalToFile= localToFile;
//		}
//
//		// IBTreeVisitor
//		@Override
//		public int compare(long address) throws IndexException {
//			final Database db = this.pdom.getDB();
//			IString nm1 = PDOMNamedNode.getDBName(db, address);
//			int cmp= nm1.compareCompatibleWithIgnoreCase(this.fName);
//			if (cmp == 0) {
//				long t1= PDOMBinding.getLocalToFileRec(db, address);
//				long t2= this.fLocalToFile;
//				cmp= t1 < t2 ? -1 : (t1 > t2 ? 1 : 0);
//			}
//			return cmp;
//		}
//
//		// IBTreeVisitor
//		@Override
//		public boolean visit(long address) throws IndexException {
//			final PDOMNamedNode nnode = (PDOMNamedNode) PDOMNode.load(pdom, address);
//			if (nnode instanceof PDOMBinding) {
//				final PDOMBinding binding = (PDOMBinding) nnode;
//				if (matches(binding)) {
//					fResult= binding;
//					return false;
//				}
//			}
//			return true;
//		}
//
//		protected boolean matches(PDOMBinding nnode) throws IndexException {
//			if (nnode.hasName(fName)) {
//				int constant = nnode.getNodeType();
//				for (int c : fConstants) {
//					if (constant == c) {
//						return true;
//					}
//				}
//			}
//			return false;
//		}
//
//		public PDOMBinding getResult() {
//			return fResult;
//		}
//
//		@Override
//		public boolean visit(IPDOMNode node) throws IndexException {
//			if (node instanceof PDOMBinding) {
//				final PDOMBinding nnode = (PDOMBinding) node;
//				if (matches(nnode)) {
//					fResult= nnode;
//					throw new OperationCanceledException();
//				}
//			}
//			return false; /* do not visit children of node */
//		}
//
//		@Override
//		public void leave(IPDOMNode node) throws IndexException {
//		}
//	}
//
//	public static class NestedBindingsBTreeComparator extends DefaultBindingBTreeComparator {
//		public NestedBindingsBTreeComparator(PDOM linkage) {
//			super(linkage);
//		}
//
//		@Override
//		public int compare(long record1, long record2) throws IndexException {
//			int cmp= super.compare(record1, record2);	// compare names
//			if (cmp == 0) {								// any order will do.
//				if (record1 < record2) {
//					return -1;
//				} else if (record1 > record2) {
//					return 1;
//				}
//			}
//			return cmp;
//		}
//	}
//
//	public static class MacroBTreeComparator implements IBTreeComparator {
//		final private Database db;
//
//		public MacroBTreeComparator(Database database) {
//			db= database;
//		}
//		@Override
//		public int compare(long record1, long record2) throws IndexException {
//			return compare(PDOMNamedNode.getDBName(db, record1), PDOMNamedNode.getDBName(db, record2));	// compare names
//		}
//		private int compare(IString nameInDB, IString nameInDB2) throws IndexException {
//			return nameInDB.compareCompatibleWithIgnoreCase(nameInDB2);
//		}
//	}
//
//	public static PDOMBinding findBinding(BTree btree, final PDOM pdom, final char[] name,
//			final int[] constants, final long localToFileRec) throws IndexException {
//		final DefaultFindBindingVisitor visitor = new DefaultFindBindingVisitor(pdom, name, constants, localToFileRec);
//		btree.accept(visitor);
//		return visitor.getResult();
//	}
//
//	public static PDOMBinding findBinding(IPDOMNode node, final PDOM pdom, final char[] name, final int[] constants,
//			long localToFileRec) throws IndexException {
//		final DefaultFindBindingVisitor visitor = new DefaultFindBindingVisitor(pdom, name, constants, localToFileRec);
//		try {
//			node.accept(visitor);
//		} catch (OperationCanceledException e) {
//		}
//		return visitor.getResult();
//	}
}

