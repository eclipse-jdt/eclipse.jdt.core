/*
 * @(#)DeclarationFilter.java	1.2 04/07/19
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of
 *       its contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.mirror.util;


import java.util.ArrayList;
import java.util.Collection;

import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.Modifier;

import static com.sun.mirror.declaration.Modifier.*;


/**
 * A filter for selecting just the items of interest
 * from a collection of declarations.
 * The filter is said to <i>select</i> or to <i>match</i> those declarations.
 * Filters can be created in several ways:
 * by the static methods described below,
 * by negating or composing existing filters,
 * or by subclasses that implement arbitrary matching rules.
 *
 * <p> A subclass can create an arbitrary filter simply by implementing
 * the {@link #matches(Declaration)} method.
 *
 * <p> Examples.
 * <p> Selecting the <code>public</code> declarations from a collection:
 * <blockquote><pre>
 *     result = FILTER_PUBLIC.filter(decls);		</pre></blockquote>
 * Selecting class declarations (including enums):
 * <blockquote><pre>
 *     classFilter = DeclarationFilter.getFilter(ClassDeclaration.class);
 *     result = classFilter.filter(decls);		</pre></blockquote>
 * Selecting class declarations but excluding enums:
 * <blockquote><pre>
 *     enumFilter = DeclarationFilter.getFilter(EnumDeclaration.class);
 *     compoundFilter = classFilter.and(enumFilter.not());
 *     result = compoundFilter.filter(decls);		</pre></blockquote>
 * Selecting declarations named "Bob":
 * <blockquote><pre>
 *     nameFilter = new DeclarationFilter() {
 *                      public boolean matches(Declaration d) {
 *                          return d.getSimpleName().equals("Bob");
 *                      }
 *                  };
 *     result = nameFilter.filter(decls);		</pre></blockquote>
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.2 04/07/19
 * @since 1.5
 */

public class DeclarationFilter {

    // Predefined filters for convenience.

    /**
     * A filter that selects only <code>public</code> declarations.
     */
    public static final DeclarationFilter FILTER_PUBLIC =
	    new AccessFilter(PUBLIC);

    /**
     * A filter that selects only <code>protected</code> declarations.
     */
    public static final DeclarationFilter FILTER_PROTECTED =
	    new AccessFilter(PROTECTED);

    /**
     * A filter that selects only <code>public</code> or <code>protected</code>
     * declarations.
     */
    public static final DeclarationFilter FILTER_PUBLIC_OR_PROTECTED =
	    new AccessFilter(PUBLIC, PROTECTED);

    /**
     * A filter that selects only package-private (<i>default</i>)
     * declarations.
     */
    public static final DeclarationFilter FILTER_PACKAGE =
	    new AccessFilter();

    /**
     * A filter that selects only <code>private</code> declarations.
     */
    public static final DeclarationFilter FILTER_PRIVATE =
	    new AccessFilter(PRIVATE);


    /**
     * Constructs an identity filter:  one that selects all declarations.
     */
    public DeclarationFilter() {
    }



    // Methods to create a filter.

    /**
     * Returns a filter that selects declarations containing all of a
     * collection of modifiers.
     *
     * @param mods  the modifiers to match (non-null)
     * @return a filter that matches declarations containing <code>mods</code>
     */
    public static DeclarationFilter getFilter(
					     final Collection<Modifier> mods) {
	return new DeclarationFilter() {
	    @Override
		public boolean matches(Declaration d) {
		return d.getModifiers().containsAll(mods);
	    }
	};
    }

    /**
     * Returns a filter that selects declarations of a particular kind.
     * For example, there may be a filter that selects only class
     * declarations, or only fields.
     * The filter will select declarations of the specified kind,
     * and also any subtypes of that kind; for example, a field filter
     * will also select enum constants.
     *
     * @param kind  the kind of declarations to select
     * @return a filter that selects declarations of a particular kind
     */
    public static DeclarationFilter getFilter(
				     final Class<? extends Declaration> kind) {
	return new DeclarationFilter() {
	    @Override
		public boolean matches(Declaration d) {
		return kind.isInstance(d);
	    }
	};
    }

    /**
     * Returns a filter that selects those declarations selected
     * by both this filter and another.
     *
     * @param f  filter to be composed with this one
     * @return a filter that selects those declarations selected by
     *		both this filter and another
     */
    public DeclarationFilter and(DeclarationFilter f) {
	final DeclarationFilter f1 = this;
	final DeclarationFilter f2 = f;
	return new DeclarationFilter() {
	    @Override
		public boolean matches(Declaration d) {
		return f1.matches(d) && f2.matches(d);
	    }
	};
    }

    /**
     * Returns a filter that selects those declarations selected
     * by either this filter or another.
     *
     * @param f  filter to be composed with this one
     * @return a filter that selects those declarations selected by
     *		either this filter or another
     */
    public DeclarationFilter or(DeclarationFilter f) {
	final DeclarationFilter f1 = this;
	final DeclarationFilter f2 = f;
	return new DeclarationFilter() {
	    @Override
		public boolean matches(Declaration d) {
		return f1.matches(d) || f2.matches(d);
	    }
	};
    }

    /**
     * Returns a filter that selects those declarations not selected
     * by this filter.
     *
     * @return a filter that selects those declarations not selected
     * by this filter
     */
    public DeclarationFilter not() {
	return new DeclarationFilter() {
	    @Override
		public boolean matches(Declaration d) {
		return !DeclarationFilter.this.matches(d);
	    }
	};
    }



    // Methods to apply a filter.

    /**
     * Tests whether this filter matches a given declaration.
     * The default implementation always returns <code>true</code>;
     * subclasses should override this.
     *
     * @param decl  the declaration to match
     * @return <code>true</code> if this filter matches the given declaration
     */
    public boolean matches(Declaration decl) {
	return true;
    }

    /**
     * Returns the declarations matched by this filter.
     * The result is a collection of the same type as the argument;
     * the {@linkplain #filter(Collection, Class) two-parameter version}
     * of <code>filter</code> offers control over the result type.
     *
     * @param <D>    type of the declarations being filtered
     * @param decls  declarations being filtered
     * @return the declarations matched by this filter
     */
    public <D extends Declaration> Collection<D> filter(Collection<D> decls) {
	ArrayList<D> res = new ArrayList<>(decls.size());
	for (D d : decls) {
	    if (matches(d)) {
		res.add(d);
	    }
	}
	return res;
    }

    /**
     * Returns the declarations matched by this filter, with the result
     * being restricted to declarations of a given kind.
     * Similar to the simpler
     * {@linkplain #filter(Collection) single-parameter version}
     * of <code>filter</code>, but the result type is specified explicitly.
     *
     * @param <D>      type of the declarations being returned
     * @param decls    declarations being filtered
     * @param resType  type of the declarations being returned --
     *			the reflective view of <code>D</code>
     * @return the declarations matched by this filter, restricted to those
     *			of the specified type
     */
    public <D extends Declaration> Collection<D>
	    filter(Collection<? extends Declaration> decls, Class<D> resType) {
	ArrayList<D> res = new ArrayList<>(decls.size());
	for (Declaration d : decls) {
	    if (resType.isInstance(d) && matches(d)) {
		res.add(resType.cast(d));
	    }
	}
	return res;
    }



    /*
     * A filter based on access modifiers.
     */
    private static class AccessFilter extends DeclarationFilter {

	// The first access modifier to filter on, or null if we're looking
	// for declarations with no access modifiers.
	private Modifier mod1 = null;

	// The second access modifier to filter on, or null if none.
	private Modifier mod2 = null;

	// Returns a filter that matches declarations with no access
	// modifiers.
	AccessFilter() {
	}

	// Returns a filter that matches m.
	AccessFilter(Modifier m) {
	    mod1 = m;
	}

	// Returns a filter that matches either m1 or m2.
	AccessFilter(Modifier m1, Modifier m2) {
	    mod1 = m1;
	    mod2 = m2;
	}

	@Override
	public boolean matches(Declaration d) {
	    Collection<Modifier> mods = d.getModifiers();
	    if (mod1 == null) {	// looking for package private
		return !(mods.contains(PUBLIC) ||
			 mods.contains(PROTECTED) ||
			 mods.contains(PRIVATE));
	    }
	    return mods.contains(mod1) &&
		   (mod2 == null || mods.contains(mod2));
	}
    }
}
