package org.eclipse.jdt.core.jdom;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * An <code>IDOMMember</code> defines functionality common to nodes which
 * can be members of types.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IDOMType
 * @see IDOMMethod
 * @see IDOMField
 * @see IDOMInitializer
 */
public interface IDOMMember extends IDOMNode {
/**
 * Returns the comment associated with this member (including comment delimiters).
 *
 * @return the comment, or <code>null</code> if this member has no associated
 *   comment
 */
public String getComment();
/**
 * Returns the flags for this member. The flags can be examined using the
 * <code>Flags</code> class.
 *
 * @return the flags
 * @see org.eclipse.jdt.core.Flags
 */
public int getFlags();
/**
 * Sets the comment associated with this member. The comment will appear
 * before the member in the source. The comment must be properly formatted, including
 * delimiters. A <code>null</code> comment indicates no comment. This member's
 * deprecated flag is automatically set to reflect the deprecated tag in the
 * comment.
 *
 * @param comment the comment, including comment delimiters, or 
 *   <code>null</code> indicating this member should have no associated comment
 * @see #setFlags
 */
public void setComment(String comment);
/**
 * Sets the flags for this member. The flags can be examined using the
 * <code>Flags</code> class. The deprecated flag passed in is ignored.
 *
 * @param flags the flags
 * @see org.eclipse.jdt.core.Flags
 */
public void setFlags(int flags);
}
