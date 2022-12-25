/*******************************************************************************
 * Copyright (c) 2006, 2021 IBM Corporation and others.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer - IBM Corporation - initial API and implementation
 *     Sonatype Inc. - ongoing development
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Objects;
import java.util.Properties;

/**
 * Creative copy&paste from org.eclipse.equinox.internal.launcher.Constants and
 * org.eclipse.equinox.launcher.Main.
 *
 * @author aniefer
 * @author igor
 *
 */
public class PlatformPropertiesUtils {
    public static final String INTERNAL_ARCH_I386 = "i386"; //$NON-NLS-1$
    public static final String INTERNAL_AMD64 = "amd64"; //$NON-NLS-1$
    public static final String INTERNAL_OS_SUNOS = "SunOS"; //$NON-NLS-1$
    public static final String INTERNAL_OS_LINUX = "Linux"; //$NON-NLS-1$
    public static final String INTERNAL_OS_MACOSX = "Mac OS"; //$NON-NLS-1$
    public static final String INTERNAL_OS_AIX = "AIX"; //$NON-NLS-1$
    public static final String INTERNAL_OS_HPUX = "HP-UX"; //$NON-NLS-1$
    public static final String INTERNAL_OS_QNX = "QNX"; //$NON-NLS-1$
    public static final String INTERNAL_OS_FREEBSD = "FreeBSD"; //$NON-NLS-1$

    public static final String ARCH_X86 = "x86";//$NON-NLS-1$
    public static final String ARCH_X86_64 = "x86_64";//$NON-NLS-1$
    public static final String ARCH_PPC = "ppc";//$NON-NLS-1$

    public final static String OSGI_WS = "osgi.ws"; //$NON-NLS-1$
    public final static String OSGI_OS = "osgi.os"; //$NON-NLS-1$
    public final static String OSGI_ARCH = "osgi.arch"; //$NON-NLS-1$
    public final static String OSGI_NL = "osgi.nl"; //$NON-NLS-1$

    /**
     * Constant string (value "win32") indicating the platform is running on a Window 32-bit
     * operating system (e.g., Windows 98, NT, 2000).
     */
    public static final String OS_WIN32 = "win32";//$NON-NLS-1$

    /**
     * Constant string (value "linux") indicating the platform is running on a Linux-based operating
     * system.
     */
    public static final String OS_LINUX = "linux";//$NON-NLS-1$

    /**
     * Constant string (value "aix") indicating the platform is running on an AIX-based operating
     * system.
     */
    public static final String OS_AIX = "aix";//$NON-NLS-1$

    /**
     * Constant string (value "solaris") indicating the platform is running on a Solaris-based
     * operating system.
     */
    public static final String OS_SOLARIS = "solaris";//$NON-NLS-1$

    /**
     * Constant string (value "hpux") indicating the platform is running on an HP/UX-based operating
     * system.
     */
    public static final String OS_HPUX = "hpux";//$NON-NLS-1$

    /**
     * Constant string (value "qnx") indicating the platform is running on a QNX-based operating
     * system.
     */
    public static final String OS_QNX = "qnx";//$NON-NLS-1$

    /**
     * Constant string (value "macosx") indicating the platform is running on a Mac OS X operating
     * system.
     */
    public static final String OS_MACOSX = "macosx";//$NON-NLS-1$

    /**
     * Constant string (value "freebsd") indicating the platform is running on a FreeBSD operating
     * system.
     */
    public static final String OS_FREEBSD = "freebsd";//$NON-NLS-1$

    /**
     * Constant string (value "unknown") indicating the platform is running on a machine running an
     * unknown operating system.
     */
    public static final String OS_UNKNOWN = "unknown";//$NON-NLS-1$

    /**
     * Constant string (value "win32") indicating the platform is running on a machine using the
     * Windows windowing system.
     */
    public static final String WS_WIN32 = "win32";//$NON-NLS-1$

    /**
     * Constant string (value "wpf") indicating the platform is running on a machine using the
     * Windows Presendation Foundation system.
     */
    public static final String WS_WPF = "wpf";//$NON-NLS-1$

    /**
     * Constant string (value "motif") indicating the platform is running on a machine using the
     * Motif windowing system.
     */
    public static final String WS_MOTIF = "motif";//$NON-NLS-1$

    /**
     * Constant string (value "gtk") indicating the platform is running on a machine using the GTK
     * windowing system.
     */
    public static final String WS_GTK = "gtk";//$NON-NLS-1$

    /**
     * Constant string (value "photon") indicating the platform is running on a machine using the
     * Photon windowing system.
     */
    public static final String WS_PHOTON = "photon";//$NON-NLS-1$

    /**
     * Constant string (value "carbon") indicating the platform is running on a machine using the
     * Carbon windowing system (Mac OS X).
     */
    public static final String WS_CARBON = "carbon";//$NON-NLS-1$

    /**
     * Constant string (value "cocoa") indicating the platform is running on a machine using the
     * Carbon windowing system (Mac OS X).
     */
    public static final String WS_COCOA = "cocoa";//$NON-NLS-1$

    /**
     * Constant string (value "unknown") indicating the platform is running on a machine running an
     * unknown windowing system.
     */
    public static final String WS_UNKNOWN = "unknown";//$NON-NLS-1$

    public static String getWS(Properties properties) {
        Objects.requireNonNull(properties);
        String ws = properties.getProperty(OSGI_WS);
        if (ws != null)
            return ws;
        String osName = getOS(properties);
        if (osName.equals(OS_WIN32))
            return WS_WIN32;
        if (osName.equals(OS_LINUX))
            return WS_GTK;
        if (osName.equals(OS_FREEBSD))
            return WS_GTK;
        if (osName.equals(OS_MACOSX)) {
            return WS_COCOA;
        }
        if (osName.equals(OS_HPUX))
            return WS_MOTIF;
        if (osName.equals(OS_AIX))
            return WS_MOTIF;
        if (osName.equals(OS_SOLARIS))
            return WS_GTK;
        if (osName.equals(OS_QNX))
            return WS_PHOTON;
        return WS_UNKNOWN;
    }

    public static String getOS(Properties properties) {
        Objects.requireNonNull(properties);
        String os = properties.getProperty(OSGI_OS);
        if (os != null)
            return os;
        String osName = System.getProperties().getProperty("os.name"); //$NON-NLS-1$
        if (osName.regionMatches(true, 0, OS_WIN32, 0, 3))
            return OS_WIN32;
        // EXCEPTION: All mappings of SunOS convert to Solaris
        if (osName.equalsIgnoreCase(INTERNAL_OS_SUNOS))
            return OS_SOLARIS;
        if (osName.equalsIgnoreCase(INTERNAL_OS_LINUX))
            return OS_LINUX;
        if (osName.equalsIgnoreCase(INTERNAL_OS_QNX))
            return OS_QNX;
        if (osName.equalsIgnoreCase(INTERNAL_OS_AIX))
            return OS_AIX;
        if (osName.equalsIgnoreCase(INTERNAL_OS_HPUX))
            return OS_HPUX;
        if (osName.equalsIgnoreCase(INTERNAL_OS_FREEBSD))
            return OS_FREEBSD;
        // os.name on Mac OS can be either Mac OS or Mac OS X
        if (osName.regionMatches(true, 0, INTERNAL_OS_MACOSX, 0, INTERNAL_OS_MACOSX.length()))
            return OS_MACOSX;
        return OS_UNKNOWN;
    }

    public static String getArch(Properties properties) {
        Objects.requireNonNull(properties);
        String arch = properties.getProperty(OSGI_ARCH);
        if (arch != null)
            return arch;
        String name = System.getProperties().getProperty("os.arch");//$NON-NLS-1$
        // Map i386 architecture to x86
        if (name.equalsIgnoreCase(INTERNAL_ARCH_I386))
            return ARCH_X86;
        // Map amd64 architecture to x86_64
        else if (name.equalsIgnoreCase(INTERNAL_AMD64))
            return ARCH_X86_64;

        return name;
    }

}
