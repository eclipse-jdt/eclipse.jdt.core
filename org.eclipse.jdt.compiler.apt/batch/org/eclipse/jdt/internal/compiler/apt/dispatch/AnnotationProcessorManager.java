package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.tools.JavaFileManager;

import org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;

public class AnnotationProcessorManager extends AbstractAnnotationProcessorManager {
	List<ICompilationUnit> addedUnits;
	JavaFileManager fileManager;
	
	public void configure(Main batchCompiler, String[] commandLineArguments) {
		if (batchCompiler instanceof EclipseCompiler) {
			this.fileManager = ((EclipseCompiler) batchCompiler).fileManager;
		} else {
			String encoding = (String) batchCompiler.options.get(CompilerOptions.OPTION_Encoding);
			Charset charset = encoding != null ? Charset.forName(encoding) : null;
			JavaFileManager manager = new EclipseFileManager(batchCompiler, batchCompiler.compilerLocale, charset);
			ArrayList<String> options = new ArrayList<String>();
			for (String argument : commandLineArguments) {
				options.add(argument);
			}
    		for (Iterator<String> iterator = options.iterator(); iterator.hasNext(); ) {
    			manager.handleOption(iterator.next(), iterator);
    		}
			this.fileManager = manager;
		}
	}

	private AnnotationProcessorManager() {
		this.addedUnits = new ArrayList<ICompilationUnit>();
	}

	public void processAnnotations(CompilationUnitDeclaration[] units, boolean isLastRound) {
		// do nothing
	}
	
	public void addNewUnit(ICompilationUnit unit) {
		this.addedUnits.add(unit);
	}

	public List<ICompilationUnit> getNewUnits() {
		return this.addedUnits;
	}
	
	public void reset() {
		this.addedUnits.clear();
	}

	public void setErr(PrintWriter err) {
		// do nothing
	}

	public void setProcessors(Object[] processors) {
		// do nothing
	}
	
	public void setOut(PrintWriter out) {
		// do nothing
	}
}
