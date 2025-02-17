package org.eclipse.jdt.core.ant.taskdef;

import org.apache.tools.ant.Task;

public class CompileargTask extends Task {
	private String value;
	
	public CompileargTask(){
		this.value = new String(""); //$NON-NLS-1$
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
