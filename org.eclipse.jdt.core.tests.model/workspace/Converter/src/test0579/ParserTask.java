package test0579;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * 
 */
public class ParserTask extends Task {
	public void doNothingMethod() {
		//
		int x = 0;
	}

	public void execute() throws BuildException {
		try {
			String line = null;
			StringBuffer buffer = new StringBuffer();

			/* Read in this source file */
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					"C:\\Projects5.1\\ParserTask\\src\\ParserTask.java"));

			/* Put source file contents into a StringBuffer */
			while ((line = bufferedReader.readLine()) != null) {
				buffer.append(line);
			}

			/* Parse the source code */
			CompilationUnit compUnit = AST.parseCompilationUnit(buffer
					.toString().toCharArray());

			System.out.println("\nPackage Declaration: ");
			PackageDeclaration packageDecl = compUnit.getPackage();
			System.out.println(packageDecl == null ? "(default)" : packageDecl
					.getName().toString());

			System.out.println("\nImports: ");
			List importList = compUnit.imports();
			Iterator itr = importList.iterator();
			while (itr.hasNext()) {
				ImportDeclaration importDecl = (ImportDeclaration) itr.next();

				System.out.println(importDecl.getName());
			}

			List typeList = compUnit.types();
			itr = typeList.iterator();
			while (itr.hasNext()) {
				TypeDeclaration typeDecl = (TypeDeclaration) itr.next();

				System.out.println("\nClass: " + typeDecl.getName());

				System.out.print("Superclass: ");
				Name superClassName = typeDecl.getSuperclass();

				System.out.println(superClassName == null ? "(none)"
						: superClassName.toString());

				MethodDeclaration[] methodDecls = typeDecl.getMethods();
				System.out.println("Methods: ");

				for (int i = 0; i < methodDecls.length; i++) {
					System.out.println("\t\t"
							+ methodDecls[i].getName().toString());
				}
			}

		} catch (Exception e) {
			throw new BuildException(e);
		}

	}

	void anotherDoNothingMethod() {
		int x = 0;
	}

}