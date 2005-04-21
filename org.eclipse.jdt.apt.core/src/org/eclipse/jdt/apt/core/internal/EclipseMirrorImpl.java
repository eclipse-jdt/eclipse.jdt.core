/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;

public interface EclipseMirrorImpl
{
    public enum MirrorKind
    {
        CONSTRUCTOR{
            public boolean isType(){ return false; }
        },
        METHOD{
            public boolean isType(){ return false; }
        },
        ANNOTATION_ELEMENT{
            public boolean isType(){ return false; }
        },
        FIELD{
            public boolean isType(){ return false; }
        },
        ENUM_CONSTANT{
            public boolean isType(){ return false; }
        },
        ANNOTATION_VALUE{
            public boolean isType(){ return false; }
        },
        ANNOTATION_MIRROR{
            public boolean isType(){ return false; }
        },
        TYPE_ANNOTATION{
            public boolean isType(){ return true; }
        },
        TYPE_INTERFACE{
            public boolean isType(){ return true; }
        },
        TYPE_CLASS{
            public boolean isType(){ return true; }
        },
        TYPE_ENUM{
            public boolean isType(){ return true; }
        },
        TYPE_ARRAY{
            public boolean isType(){ return true; }
        },
        TYPE_WILDCARD{
            public boolean isType(){ return true; }
        },
        TYPE_VOID{
            public boolean isType(){ return true; }
        },
        TYPE_PRIMITIVE{
            public boolean isType(){ return true; }
        },
        TYPE_PARAMETER_VARIABLE{
            public boolean isType(){ return true; }
        },
        TYPE_ERROR{
            public boolean isType(){ return true; }
        },
        FORMAL_PARAMETER{
            public boolean isType(){ return false; }
        },
        PACKAGE{
            public boolean isType(){ return false; }
        };

        public abstract boolean isType();
    }

    public MirrorKind kind();
	
	/**
	 * @return the processor environment associated with the object.
	 * return null for primitive, void and error type. 
	 */
	public ProcessorEnvImpl getEnvironment();

} 
