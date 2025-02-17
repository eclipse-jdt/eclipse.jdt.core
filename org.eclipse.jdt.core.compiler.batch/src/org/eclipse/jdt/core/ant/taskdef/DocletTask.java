package org.eclipse.jdt.core.ant.taskdef;

import java.util.ArrayList;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

public class DocletTask extends Task {

	public class DocletParam extends Task{
		private String paramName;
		private String paramValue;

		public DocletParam(){
			this.paramName = ""; //$NON-NLS-1$
			this.paramValue = ""; //$NON-NLS-1$
		}

		public void setName(String name){
			this.paramName = name;
		}

		public void setValue(String value){
			this.paramValue = value;
		}

		public String getValue(){
			return this.paramValue;
		}

		public String getName(){
			return this.paramName;
		}
	}

	private final ArrayList<DocletParam> docletParams;
	private String docletName;
	private Path docletPath;

	public DocletTask(){
		this.docletParams = new ArrayList<>();
		this.docletName = ""; //$NON-NLS-1$
		this.docletPath = null;
	}

	public void setName(String name){
		this.docletName = name;
	}

	public void setPath(Path path){
		this.docletPath = path;
	}

	public DocletParam createParam(){
		DocletParam param = new DocletParam();
		this.docletParams.add(param);
		return param;
	}

	public String getName(){
		return this.docletName;
	}

	public Path getPath(){
		return this.docletPath;
	}

	public ArrayList<DocletParam> getParams(){
		return this.docletParams;
	}

}
