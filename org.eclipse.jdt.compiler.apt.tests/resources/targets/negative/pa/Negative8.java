/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
package targets.negative.pa;

// Exploring the reporting of missing interfaces.
class Negative8a implements RemoteNegative8a {
}

interface Negative8b extends RemoteNegative8a {
}

class Negative8c<T> implements RemoteNegative8b<T> {
}

interface Negative8d<T> extends RemoteNegative8b<T> {
}

interface Negative8e extends Negative8f<T> {
}

interface Negative8f<T> {
}
