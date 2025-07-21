/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The element info for <code>JarPackageFragmentRoot</code>s.
 */
class JarPackageFragmentRootInfo extends PackageFragmentRootInfo {
    /**
     * contains .class file names, and non-Java resource names of a package
     */
    static final class PackageContent {
        private final List<String> javaClasses;
        private final List<String> resources;

        /**
         *
         */
        PackageContent(List<String> javaClasses, List<String> resources) {
            this.javaClasses = javaClasses;
            this.resources = resources;
        }

        PackageContent() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        public List<String> javaClasses() {
            return javaClasses;
        }

        public List<String> resources() {
            return resources;
        }

        @java.lang.Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PackageContent) obj;
            return java.util.Objects.equals(this.javaClasses, that.javaClasses) &&
                   java.util.Objects.equals(this.resources, that.resources);
        }

        @java.lang.Override
        public int hashCode() {
            return java.util.Objects.hash(javaClasses, resources);
        }

        @java.lang.Override
        public String toString() {
            return "PackageContent[" +
                   "javaClasses=" + javaClasses + ", " +
                   "resources=" + resources + ']';
        }

    }

	/**
	 * Cache for the the jar's entries names. A unmodifiable map from package name to PackageContent
	 */
	Map<List<String>, PackageContent> rawPackageInfo;
	Map<String, String> overriddenClasses;
}
