package org.eclipse.jdt.core.tests.builder;

import java.util.Vector;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IDebugRequestor;
import org.eclipse.jdt.internal.core.Util;

public class EfficiencyCompilerRequestor implements IDebugRequestor {
	private boolean isActive = false;
	
	private Vector compiledClasses = new Vector(10);
	
	public void acceptDebugResult(CompilationResult result){
		ClassFile[] classFiles = result.getClassFiles();
		Util.sort(classFiles, new Util.Comparer() {
			public int compare(Object a, Object b) {
				String aName = new String(((ClassFile)a).fileName());
				String bName = new String(((ClassFile)b).fileName());
				return aName.compareTo(bName);
			}
		});
		for (int i = 0; i < classFiles.length; i++) {
			String className = new String(classFiles[i].fileName());
			compiledClasses.addElement(className.replace('/', '.'));
		}
	}
	
	String[] getCompiledClasses(){
		return (String [])compiledClasses.toArray(new String[0]);
	}
	
	public void clearResult(){
		compiledClasses.clear();
	}
	
	public void reset() {
	}
	
	public void activate() {
		isActive = true;
	}
	
	public void deactivate() {
		isActive = false;
	}
	
	public boolean isActive() {
		return isActive;
	}
}