package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.builder.IDevelopmentContext;
import org.eclipse.jdt.internal.core.builder.IPackage;
import org.eclipse.jdt.internal.core.builder.IState;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.builder.impl.*;
import org.eclipse.jdt.internal.core.search.indexing.*;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import javax.xml.parsers.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.eclipse.jdt.internal.core.builder.NotPresentException;

/**
 * The <code>JavaModelManager</code> manages instances of <code>IJavaModel</code>.
 * <code>IElementChangedListener</code>s register with the <code>JavaModelManager</code>,
 * and receive <code>ElementChangedEvent</code>s for all <code>IJavaModel</code>s.
 * <p>
 * The single instance of <code>JavaModelManager</code> is available from
 * the static method <code>JavaModelManager.getJavaModelManager()</code>.
 */
public class JavaModelManager implements IResourceChangeListener, ISaveParticipant { 	
	/**
	 * JavaCore options (don't use this field to add an new option,
	 * use JavaModelManager#addOption method.
	 */
	public static Hashtable fOptions;
	/**
	 * JavaCore options IDs
	 */
	private static ArrayList fOptionIDs;

	/**
	 * The singleton manager
	 */
	protected static JavaModelManager fgManager= null;

	/**
	 * Active Java Model Info
	 */
	protected JavaModelInfo fModelInfo= null;

	/**
	 * Turns delta firing on/off. By default it is on.
	 */
	protected boolean fFire= true;

	/**
	 * Queue of deltas created explicily by the Java Model that
	 * have yet to be fired.
	 */
	protected Vector fJavaModelDeltas= new Vector();

	/**
	 * Queue of deltas created as translations of ResourceDeltas that
	 * have yet to be fired.
	 */
	protected Vector fResourceDeltas= new Vector();

	/**
	 * Collection of listeners for Java element deltas
	 */
	protected Vector fElementChangedListeners= new Vector();

	/**
	 * Collection of projects that are in the process of being deleted.
	 * Project reside in this cache from the time the plugin receives
	 * the #deleting message until they resource delta is received
	 * claiming the project has been deleted. The java model will not allow
	 * a project that is being deleted to be opened - since this can leave
	 * files open, causing the delete to fail.
	 *
	 * fix for 1FW67PA
	 */
	protected Vector fProjectsBeingDeleted= new Vector();

	/**
	 * Used to convert <code>IResourceDelta</code>s into <code>IJavaElementDelta</code>s.
	 */
	protected DeltaProcessor fDeltaProcessor= new DeltaProcessor();

	public static boolean ENABLE_INDEXING= true;
	/**
	 * Local Java workspace properties file name (generated inside JavaCore plugin state location)
	 */
	private static final String WKS_PROP_FILENAME= "workspace.properties"/*nonNLS*/;

	/**
	 * Name of the handle id attribute in a Java marker
	 */
	private static final String ATT_HANDLE_ID= "org.eclipse.jdt.internal.core.JavaModelManager.handleId"/*nonNLS*/;

	/**
	 * Table from IProject to PerProjectInfo.
	 */
	protected Hashtable perProjectInfo = new Hashtable(5);

	static class PerProjectInfo {
		IProject project;
		IDevelopmentContext developmentContext = new JavaDevelopmentContextImpl();
		byte[] savedStateFingerprint;
		boolean triedRead = false;
		PerProjectInfo(IProject project) {
			this.project = project;
		}
		IState getLastBuiltState() {
			try {
				return developmentContext.getCurrentState();
			} catch (NotPresentException e) {
				return null;
			}
		}
		void setLastBuiltState(IState state) {
			developmentContext.setCurrentState(state);
		}
	};
	public final static boolean VERBOSE = false;

	/**
	 * Line separator to use throughout the JavaModel for any source edit operation
	 */
	public static String LINE_SEPARATOR = System.getProperty("line.separator"/*nonNLS*/);
	/**
	 * Constructs a new JavaModelManager
	 */
	private JavaModelManager() {
	}
	/**
	 * addElementChangedListener method comment.
	 */
	public void addElementChangedListener(IElementChangedListener listener) {
		if (fElementChangedListeners.indexOf(listener) < 0) {
			fElementChangedListeners.addElement(listener);
		}
	}
/*
 * Checks that the delta contains an added project. In this case,
 * removes it from the list of projects being deleted.
 */
public void checkProjectBeingAdded(IResourceDelta delta) {
	IResource resource = delta.getResource();
	switch (resource.getType()) {
		case IResource.ROOT :
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				this.checkProjectBeingAdded(children[i]);
			}
		case IResource.PROJECT :
			if (delta.getKind() == IResourceDelta.ADDED) {
				fProjectsBeingDeleted.remove(delta.getResource());
			}
	}
}
/**
 * Used when incrementally building, so as to force a partial refresh of the Java Model before it got a
 * chance to update by itself.
 */
public void closeAffectedElements(IResourceDelta delta) {
	fDeltaProcessor.closeAffectedElements(delta);
}

	/**
	 * Note that the project is now deleted.
	 *
	 * fix for 1FW67PA
	 */
	public void deleted(IProject project) {
		fProjectsBeingDeleted.removeElement(project);
	}
	/**
	 * Note that the project is about to be deleted.
	 *
	 * fix for 1FW67PA
	 */
	public void deleting(IProject project) {
		
		IndexManager indexManager= getIndexManager();
		if (indexManager != null) indexManager.deleting(project);
		
		if (!fProjectsBeingDeleted.contains(project)) {
			fProjectsBeingDeleted.addElement(project);
		}
	}
/**
 * @see ISaveParticipant
 */
public void doneSaving(ISaveContext context){
}
	/**
	 * Make sure the resource content is available locally
	 */
	public void ensureLocal(IResource resource) throws CoreException {

		// need to be tuned once having VCM support
		// resource.ensureLocal(IResource.DEPTH_ZERO, null);

		if (!resource.isLocal(IResource.DEPTH_ZERO) || !resource.exists()) { // project is always local but might not exist
			throw new CoreException(new JavaModelStatus(IJavaModelStatusConstants.NO_LOCAL_CONTENTS, resource.getFullPath()));
		}
	}
	/**
	 * Merges resource deltas and Java Model deltas, and fires the
	 * result, flushing all deltas. If the firing mode has been 
	 * turned off, this has no effect. 
	 */
	public void fire() {
		if (fFire) {
			Enumeration deltas= null;
			if (fJavaModelDeltas.isEmpty()) {
				deltas= fResourceDeltas.elements();
			} else {
				deltas= fJavaModelDeltas.elements();
			}
			try {
				while (deltas.hasMoreElements()) {
					IJavaElementDelta delta= (IJavaElementDelta) deltas.nextElement();
					ElementChangedEvent event= new ElementChangedEvent(delta);
					// Clone the listeners since they could remove themselves when told about the event 
					// (eg. a type hierarchy becomes invalid (and thus it removes itself) when the type is removed
					Vector listeners= (Vector) fElementChangedListeners.clone();
					for (int i= 0; i < listeners.size(); i++) {
						IElementChangedListener listener= (IElementChangedListener) listeners.elementAt(i);
						listener.elementChanged(event);
					}
				}
			} finally {
				// empty the queues
				flush();
			}
		}
	}
	/**
	 * Flushes all deltas without firing them.
	 */
	protected void flush() {
		fJavaModelDeltas= new Vector();
		fResourceDeltas= new Vector();
	}
	/**
	 * Returns the development context to use for the given project.
	 *
	 * @private for use by image builder only
	 */
	public IDevelopmentContext getDevelopmentContext(IProject project) {
		return getPerProjectInfo(project).developmentContext;
	}
	/** 
	 * Returns the set of elements which are out of synch with their buffers.
	 */
	protected Hashtable getElementsOutOfSynchWithBuffers() {
		if (fModelInfo == null) {
			return new Hashtable(1);
		} else {
			return fModelInfo.fElementsOutOfSynchWithBuffers;
		}
	}
	/**
	 * Returns the <code>IJavaElement</code> represented by the <code>String</code>
	 * memento.
	 * @see getHandleMemento()
	 */
	public IJavaElement getHandleFromMemento(String memento) throws JavaModelException {
		if (memento == null) {
			return null;
		}
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		if (workspace == null) {
			return null;
		}
		JavaModel model= (JavaModel) getJavaModel(workspace);
		if (memento.equals(""/*nonNLS*/)){ // workspace memento
			return model;
		}
		int modelEnd= memento.indexOf(JavaElement.JEM_JAVAPROJECT);
		if (modelEnd == -1) {
			return null;
		}
		boolean returnProject= false;
		int projectEnd= memento.indexOf(JavaElement.JEM_PACKAGEFRAGMENTROOT, modelEnd);
		if (projectEnd == -1) {
			projectEnd= memento.length();
			returnProject= true;
		}
		String projectName= memento.substring(modelEnd + 1, projectEnd);
		JavaProject proj= (JavaProject) model.getJavaProject(projectName);
		if (returnProject) {
			return proj;
		}
		int rootEnd= memento.indexOf(JavaElement.JEM_PACKAGEFRAGMENT, projectEnd + 1);
		if (rootEnd == -1) {
			return proj.getPackageFragmentRoot(new Path(Path.SEPARATOR + memento.substring(modelEnd + 1)));
		}
		String rootName= null;
		if (rootEnd == projectEnd - 1) {
			//default root
			rootName= IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH;
		} else {
			rootName= memento.substring(projectEnd + 1, rootEnd);
		}
		IPath rootPath= new Path(rootName);
		IPackageFragmentRoot root= null;
		if (rootPath.isAbsolute()) {
			root= proj.getPackageFragmentRoot(rootName);
		} else {
			root= proj.getPackageFragmentRoot(proj.getProject().getFullPath().append(rootName));
		}
		if (root == null)
			return null;

		int end= memento.indexOf(JavaElement.JEM_COMPILATIONUNIT, rootEnd);
		if (end == -1) {
			end= memento.indexOf(JavaElement.JEM_CLASSFILE, rootEnd);
			if (end == -1) {
				if (rootEnd + 1 == memento.length()) {
					return root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
				} else {
					return root.getPackageFragment(memento.substring(rootEnd + 1));
				}
			}
			//deal with class file and binary members
			return model.getHandleFromMementoForBinaryMembers(memento, root, rootEnd, end);
		}

		//deal with compilation units and source members
		return model.getHandleFromMementoForSourceMembers(memento, root, rootEnd, end);
	}
	public IndexManager getIndexManager() {
		return fDeltaProcessor.indexManager;
	}
	/**
	 *  Returns the info for the element.
	 */
	protected Object getInfo(IJavaElement element) {
		if (fModelInfo == null) {
			return null;
		}
		int elementType= ((JavaElement) element).fLEType;
		if (elementType == IJavaElement.JAVA_MODEL) {
			return fModelInfo;
		}
		if (((JavaElement) element).fLEType <= IJavaElement.CLASS_FILE) {
			return fModelInfo.fLRUCache.get(element);
		} else {
			return fModelInfo.fChildrenCache.get(element);
		}
	}
	/**
	 * Returns the handle to the active Java Model, or <code>null</code> if there
	 * is no active Java Model.
	 */
	public IJavaModel getJavaModel() {
		if (fModelInfo == null) {
			return null;
		} else {
			return fModelInfo.getJavaModel();
		}
	}
	/**
	 * Returns the JavaModel for the given workspace, creating
	 * it if it does not yet exist.
	 */
	public static JavaModel getJavaModel(IWorkspace workspace) {

		JavaModelInfo modelInfo= getJavaModelManager().fModelInfo;
		if (modelInfo != null) {
			// if the current java model corresponds to a different workspace,
			// try to close it
			if (!modelInfo.workspace.equals(workspace)) {
				try {
					modelInfo.fJavaModel.close();
				} catch (JavaModelException e) {
					Assert.isTrue(false, Util.bind("element.onlyOneJavaModel"/*nonNLS*/));
					return null;
				}
			}
		}
		if (modelInfo == null || modelInfo.workspace.equals(workspace)) {
			return new JavaModel(workspace);
		} else {
			Assert.isTrue(false, Util.bind("element.onlyOneJavaModel"/*nonNLS*/));
			return null;
		}

	}
	/**
	 * Returns the singleton JavaModelManager
	 */
	public static synchronized JavaModelManager getJavaModelManager() {
		if (fgManager == null) {
			fgManager= new JavaModelManager();
		}
		return fgManager;
	}
	/**
	 * Returns the last built state for the given project, or null if there is none.
	 * Deserializes the state if necessary.
	 *
	 * @private for use by image builder and evaluation support only
	 */
	public IState getLastBuiltState(IProject project, IProgressMonitor monitor) {
		PerProjectInfo info= getPerProjectInfo(project);
		IState state= info.getLastBuiltState();
		if (state == null && JavaBuilder.SAVE_ENABLED && !info.triedRead) {
			info.triedRead= true;
			try {
				if (monitor != null) monitor.subTask(Util.bind("build.readStateProgress"/*nonNLS*/, project.getName()));
				state= readState(info);
				info.setLastBuiltState(state);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return state;
	}
	/**
	 * Returns the last built state for the given project, or null if there is none.
	 * Deserializes the state if necessary.
	 *
	 */
	public INameEnvironment getNameEnvironment(IProject project) {
		StateImpl state= (StateImpl) getLastBuiltState(project, null);
		if (state == null)
			return null;
			
		BuilderEnvironment env = new BuilderEnvironment(new BatchImageBuilder(state));
		
		// Fix for 1GB7PUI: ITPJCORE:WINNT - evaluation from type in default package
		IPackage defaultPackage = state.getDevelopmentContext().getImage().getPackageHandle(PackageImpl.DEFAULT_PACKAGE_PREFIX + project.getName(), true);
		env.setDefaultPackage(defaultPackage);
		
		return env;
	}
	/**
	 * Returns the per-project info for the given project.
	 */
	private PerProjectInfo getPerProjectInfo(IProject project) {
		PerProjectInfo info= (PerProjectInfo) perProjectInfo.get(project);
		if (info == null) {
			info= new PerProjectInfo(project);
			perProjectInfo.put(project, info);
		}
		return info;
	}
	/**
	 * Returns the File to use for saving and restoring the last built state for the given project.
	 */
	private File getSerializationFile(IProject project) {
		if (!project.exists()) return null;
		IPluginDescriptor descr= JavaCore.getJavaCore().getDescriptor();
		IPath workingLocation= project.getPluginWorkingLocation(descr);
		return workingLocation.append("state.dat"/*nonNLS*/).toFile();
	}
	public String getVariablesAsXMLString() throws CoreException {

		Document document = new DocumentImpl();
		Element rootElement = document.createElement("variables"/*nonNLS*/);
		document.appendChild(rootElement);

		String[] variables = JavaCore.getClasspathVariableNames();
		
		for (int i= 0; i < variables.length; ++i) {
			String var = variables[i];
			IPath varPath = JavaCore.getClasspathVariable(var);
			Element varElement= document.createElement("variable"/*nonNLS*/);
			varElement.setAttribute("name"/*nonNLS*/, var);
			varElement.setAttribute("path"/*nonNLS*/, varPath.toString());			
			rootElement.appendChild(varElement);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		try {
			OutputFormat format = new OutputFormat();
			format.setIndenting(true);
			Serializer serializer = SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(writer, format);
			serializer.asDOMSerializer().serialize(document);
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
		return writer.toString();	
			
	}
	
	public String getOptionsAsXMLString() throws CoreException {

		Document document = new DocumentImpl();
		Element rootElement = document.createElement("options"/*nonNLS*/);
		document.appendChild(rootElement);

		String[] ids = JavaCore.getOptionIDs();
		
		for (int i= 0; i < ids.length; ++i) {
			ConfigurableOption option = (ConfigurableOption)fOptions.get(ids[i]);
			
			Element optionElement= document.createElement("option"/*nonNLS*/);
			optionElement.setAttribute("id"/*nonNLS*/, ids[i]);
			if(option.getPossibleValues() == ConfigurableOption.NoDiscreteValue){
				optionElement.setAttribute("value"/*nonNLS*/, option.getValue());	
			}
			else{
				optionElement.setAttribute("index"/*nonNLS*/, String.valueOf(option.getValueIndex()));	
			}
			rootElement.appendChild(optionElement);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		try {
			OutputFormat format = new OutputFormat();
			format.setIndenting(true);
			Serializer serializer = SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(writer, format);
			serializer.asDOMSerializer().serialize(document);
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
		return writer.toString();	
			
	}
/**
	 * Returns the open ZipFile at the given location. If the ZipFile
	 * does not yet exist, it is created, opened, and added to the cache
	 * of open ZipFiles. The location must be a absolute path.
	 *
	 * @exception CoreException If unable to create/open the ZipFile.
	 */
	public ZipFile getZipFile(IPath path) throws CoreException {
		if (fModelInfo == null) {
			return null;
		}

		String fileSystemPath= null;
		IWorkspaceRoot root = getJavaModel().getWorkspace().getRoot();
		IResource file = root.findMember(path);
		if (path.isAbsolute() && file != null) {
			if (file == null || file.getType() != IResource.FILE) {
				fileSystemPath= path.toOSString();
			} else {
				ensureLocal(file);
				fileSystemPath= file.getLocation().toOSString();
			}
		} else if (!path.isAbsolute()) {
			file= root.getFile(path);
			if (file == null || file.getType() != IResource.FILE) {
				throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("file.notFound"/*nonNLS*/), null));
			}
			ensureLocal(file);
			fileSystemPath= file.getLocation().toOSString();
		} else {
			fileSystemPath= path.toOSString();
		}

		try {
			return new ZipFile(fileSystemPath);
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("status.IOException"/*nonNLS*/), e));
		}
	}
	/**
	 * Returns true if the given project is being deleted, otherwise false.
	 *
	 * fix for 1FW67PA
	 */
	public boolean isBeingDeleted(IProject project) {
		return fProjectsBeingDeleted.contains(project);
	}
	/**
	 * Returns true if the firing is enabled
	 */
	public boolean isFiring() {
		return this.fFire;
	}
	public void loadVariables() throws CoreException {
		
		String xmlString = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(
								new QualifiedName(JavaCore.PLUGIN_ID, "variables"/*nonNLS*/));
		try {
			if (xmlString != null) readVariables(xmlString);
		} catch(IOException e){
			return;
		}
	}
	
	public void loadOptions() throws CoreException {	
		String xmlString = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(
								new QualifiedName(JavaCore.PLUGIN_ID, "options"/*nonNLS*/));
		try {
			if (xmlString != null) readOptions(xmlString);
		} catch(IOException e){
			return;
		}
	}
/**
 * Merged all awaiting deltas.
 */
public void mergeDeltas() {
	Enumeration deltas = null;
	if (fJavaModelDeltas.isEmpty()) {
		//deltas = fResourceDeltas.elements();
		return;
	} else {
		deltas = fJavaModelDeltas.elements();
	}	
	if (deltas != null) {
		JavaElementDelta rootDelta = new JavaElementDelta(getJavaModel());
		boolean insertedTree = false;
		while (deltas.hasMoreElements()) {
			IJavaElementDelta delta = (IJavaElementDelta)deltas.nextElement();
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int j = 0; j < children.length; j++) {
				JavaElementDelta projectDelta = (JavaElementDelta) children[j];
				rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
				insertedTree = true;
			}
		}
		if (insertedTree){
			if (fJavaModelDeltas.isEmpty()) {
				fResourceDeltas = new Vector(1);
				fResourceDeltas.addElement(rootDelta);
			} else {
				fJavaModelDeltas = new Vector(1);
				fJavaModelDeltas.addElement(rootDelta);
			}	
		}
		else {
			if (fJavaModelDeltas.isEmpty()) {
				fResourceDeltas = new Vector(0);
			} else {
				fJavaModelDeltas = new Vector(0);
			}	
		}
	}
}	
	/**
	 *  Returns the info for this element without
	 *  disturbing the cache ordering.
	 */
	protected Object peekAtInfo(IJavaElement element) {
		if (fModelInfo == null) {
			return null;
		}
		int type = ((JavaElement) element).fLEType;
		if (type == IJavaElement.JAVA_MODEL) {
			return fModelInfo;
		} else if (type <= IJavaElement.CLASS_FILE) {
			return fModelInfo.fLRUCache.peek(element);
		} else {
			return fModelInfo.fChildrenCache.get(element);
		}
	}
/**
 * @see ISaveParticipant
 */
public void prepareToSave(ISaveContext context) throws CoreException {
}
	protected void putInfo(IJavaElement element, Object info) {
		int elementType= ((JavaElement) element).fLEType;
		if (elementType == IJavaElement.JAVA_MODEL) {
			fModelInfo= (JavaModelInfo) info;
			return;
		}

		if (elementType <= IJavaElement.CLASS_FILE) {
			fModelInfo.fLRUCache.put(element, info);
		} else {
			fModelInfo.fChildrenCache.put(element, info);
		}
	}
	/**
	 * Reads the build state for the relevant project.
	 */
	protected IState readState(PerProjectInfo info) throws CoreException {
		File file= getSerializationFile(info.project);
		if (file == null || !file.exists())
			return null;
		try {
			DataInputStream in= new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			try {
				String pluginID= in.readUTF();
				if (!pluginID.equals(JavaCore.PLUGIN_ID))
					throw new IOException(Util.bind("build.wrongFileFormat"/*nonNLS*/));
				String kind= in.readUTF();
				if (!kind.equals("STATE"/*nonNLS*/))
					throw new IOException(Util.bind("build.wrongFileFormat"/*nonNLS*/));
				int version= in.readInt();
				if (version != 0x0001)
					throw new IOException(Util.bind("build.unhandledVersionFormat"/*nonNLS*/));
				boolean hasState= in.readBoolean();
				IState state= null;
				if (hasState) {
					state= info.developmentContext.restoreState(info.project, in);
				}
				return state;
			} finally {
				in.close();
			}
		} catch (Exception e) {
			//e.printStackTrace(); - silent failure
			//throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR, "Error reading last build state for project "+ info.project.getFullPath(), e));
		}
		return null;
	}
	public void readVariables(String xmlString) throws IOException {
		
		StringReader reader = new StringReader(xmlString);
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch(SAXException e) {
			return;
		} catch(ParserConfigurationException e){
			return;
		} finally {
			reader.close();
		}
		if (cpElement == null) return;
		if (!cpElement.getNodeName().equalsIgnoreCase("variables"/*nonNLS*/)) {
			return;
		}
		NodeList list= cpElement.getChildNodes();
		Vector variables = new Vector();
		int length= list.getLength();
		for (int i= 0; i < length; ++i) {
			Node node= list.item(i);
			short type= node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (element.getNodeName().equalsIgnoreCase("variable"/*nonNLS*/)) {
					String varName = element.getAttribute("name"/*nonNLS*/);
					String varPath = element.getAttribute("path"/*nonNLS*/);
					try {
						JavaCore.setClasspathVariable(varName, new Path(varPath), null);
					} catch(JavaModelException e){
					} catch(RuntimeException e){
					}
				}

			}
		}
	}
	
	public void readOptions(String xmlString) throws IOException {
		if(fOptions == null)
			JavaCore.resetOptions();
		
		StringReader reader = new StringReader(xmlString);
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch(SAXException e) {
			return;
		} catch(ParserConfigurationException e){
			return;
		} finally {
			reader.close();
		}
		if (cpElement == null) return;
		if (!cpElement.getNodeName().equalsIgnoreCase("options"/*nonNLS*/)) {
			return;
		}
		NodeList list= cpElement.getChildNodes();
		int length= list.getLength();
		for (int i= 0; i < length; ++i) {
			Node node= list.item(i);
			short type= node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (element.getNodeName().equalsIgnoreCase("option"/*nonNLS*/)) {
					String id = element.getAttribute("id"/*nonNLS*/);
					ConfigurableOption option = (ConfigurableOption) fOptions.get(id);
					
					String[] possible = option.getPossibleValues();
					if(possible == ConfigurableOption.NoDiscreteValue){
						String value = element.getAttribute("value"/*nonNLS*/);
						option.setValue(value);
					}
					else {
						try {
							int valueIndex = Integer.parseInt(element.getAttribute("index"/*nonNLS*/));
							option.setValueIndex(valueIndex);
						} catch(NumberFormatException e){
						}
					}
					
				}

			}
		}
	}
	/**
	 * Registers the given delta with this manager. This API is to be
	 * used to registerd deltas that are created explicitly by the Java
	 * Model. Deltas created as translations of <code>IResourceDeltas</code>
	 * are to be registered with <code>#registerResourceDelta</code>.
	 */
	protected void registerJavaModelDelta(IJavaElementDelta delta) {
		fJavaModelDeltas.addElement(delta);
	}
	/**
	 * Registers the given delta with this manager. This API is to be
	 * used to register deltas that are created as a side effect
	 * of an <code>IResourceDelta</code>. As <code>IResourceDelta</code>s
	 * are received by the Java Model, they are translated into 
	 * <code>IJavaElementDelta</code>s. This is where the translations
	 * are registered.
	 */
	public void registerResourceDelta(IJavaElementDelta delta) {
		fResourceDeltas.addElement(delta);
	}
	/**
	 * removeElementChangedListener method comment.
	 */
	public void removeElementChangedListener(IElementChangedListener listener) {
		fElementChangedListeners.removeElement(listener);
	}
	protected void removeInfo(IJavaElement element) {
		if (fModelInfo == null) {
			return;
		}
		if (((JavaElement) element).fLEType <= IJavaElement.CLASS_FILE) {
			fModelInfo.fLRUCache.remove(element);
		} else {
			fModelInfo.fChildrenCache.remove(element);
		}
	}
	void removePerProjectInfo(JavaProject javaProject) {
		IProject project = javaProject.getProject();
		PerProjectInfo info= (PerProjectInfo) perProjectInfo.get(project);
		if (info != null) {
			perProjectInfo.remove(project);
		}
	}
	/**
	 * Notifies this Java Model Manager that some resource changes have happened
	 * on the platform, and that the Java Model should update any required
	 * internal structures such that its elements remain consistent.
	 * Translates <code>IResourceDeltas</code> into <code>IJavaElementDeltas</code>.
	 *
	 * @see IResourceDelta
	 * @see IResource 
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		if (event.getSource() instanceof IWorkspace) {
			IResource resource = event.getResource();
			IResourceDelta delta = event.getDelta();
			switch(event.getType()){
				case IResourceChangeEvent.PRE_DELETE :
					try {
						if(resource.getType() == IResource.PROJECT 
							&& ((IProject) resource).hasNature(JavaCore.NATURE_ID)) {
							this.deleting((IProject)resource);
						}
					} catch(CoreException e){
					}
					break;
				case IResourceChangeEvent.PRE_AUTO_BUILD :
					if(delta != null) {
						this.checkProjectBeingAdded(delta);
						DeltaProcessor.checkProjectPropertyFileUpdate(delta, null); // will close project if affected by the property file change
					}
					break;
				case IResourceChangeEvent.POST_CHANGE :
					if (delta != null) {
						try {
							IJavaElementDelta[] translatedDeltas = fDeltaProcessor.processResourceDelta(delta);
							if (translatedDeltas.length > 0) {
								for (int i= 0; i < translatedDeltas.length; i++) {
									registerResourceDelta(translatedDeltas[i]);
								}
							}
							fire();
						} finally {
							// fix for 1FWIAEQ: ITPJCORE:ALL - CRITICAL - "projects being deleted" cache not cleaned up when solution deleted
							if (!fProjectsBeingDeleted.isEmpty()) {
								fProjectsBeingDeleted= new Vector(1);
							}
						}
					}				
					break;
			}
		}
	}
/**
 * @see ISaveParticipant
 */
public void rollback(ISaveContext context){
}
	/**
	 * Runs a Java Model Operation
	 */
	public void runOperation(JavaModelOperation operation, IProgressMonitor monitor) throws JavaModelException {

		boolean wasFiring = isFiring();
		try {
			if (wasFiring) stopDeltas();
			if (operation.isReadOnly()) {
				operation.run(monitor);
			} else {
				// use IWorkspace.run(...) to ensure that a build will be done in autobuild mode
				this.getJavaModel().getWorkspace().run(operation, monitor);
			}
		} catch (CoreException ce) {
			if (ce instanceof JavaModelException) {
				throw (JavaModelException)ce;
			} else {
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e= ce.getStatus().getException();
					if (e instanceof JavaModelException) {
						throw (JavaModelException) e;
					}
				}
				throw new JavaModelException(ce);
			}
		} finally {
			// fire any registered deltas
			if (wasFiring){
				startDeltas();
				fire();
			}
		}
	}
	private void saveBuildState() throws CoreException {
		if (!JavaBuilder.SAVE_ENABLED)
			return;
		Vector vStats= null; // lazy initialized
		for (Enumeration enum= perProjectInfo.elements(); enum.hasMoreElements();) {
			PerProjectInfo info= (PerProjectInfo) enum.nextElement();
			try {
				saveStateIfNecessary(info);
			} catch (CoreException e) {
				if (vStats == null)
					vStats= new Vector();
				vStats.addElement(e.getStatus());
			}
		}
		if (vStats != null) {
			IStatus[] stats= new IStatus[vStats.size()];
			vStats.copyInto(stats);
			throw new CoreException(new MultiStatus(JavaCore.PLUGIN_ID, IStatus.ERROR, stats, Util.bind("build.cannotSaveStates"/*nonNLS*/), null));
		}
	}
	/**
	 * Saves the built state for the project.
	 */
	private void saveState(PerProjectInfo info) throws CoreException {

		if (VERBOSE) System.out.println(Util.bind("build.saveStateProgress"/*nonNLS*/, info.project.getName()));
		long t= System.currentTimeMillis();
		File file= getSerializationFile(info.project);
		if (file == null) return;
		try {
			DataOutputStream out= new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			try {
				out.writeUTF(JavaCore.PLUGIN_ID);
				out.writeUTF("STATE"/*nonNLS*/);
				out.writeInt(0x0001);
				IState state= info.getLastBuiltState();
				if (state == null) {
					out.writeBoolean(false);
				} else {
					out.writeBoolean(true);
					info.developmentContext.saveState(state, out);
				}
			} finally {
				out.close();
			}
		} catch (RuntimeException e) {
			try {
				file.delete();
			} catch(SecurityException se){
			}
			throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR, Util.bind("build.cannotSaveState"/*nonNLS*/, info.project.getName()), e));
		} catch (IOException e) {
			try {
				file.delete();
			} catch(SecurityException se){
			}
			throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR, Util.bind("build.cannotSaveState"/*nonNLS*/, info.project.getName()), e));
		}
		t= System.currentTimeMillis() - t;
		if (VERBOSE) System.out.println(Util.bind("build.saveStateComplete"/*nonNLS*/, String.valueOf(t)));
	}
	/**
	 * Saves the built state for the project if it has been changed since last save.
	 */
	private void saveStateIfNecessary(PerProjectInfo info) throws CoreException {
		IState state= info.getLastBuiltState();
		if (state == null) {
			saveState(info);
			info.savedStateFingerprint= null;
		} else {
			byte[] fingerprint= state.getFingerprint();
			if (Util.compare(fingerprint, info.savedStateFingerprint) != 0) {
				saveState(info);
				info.savedStateFingerprint= fingerprint;
			}
		}
	}
	public void saveVariables() throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(
			new QualifiedName(JavaCore.PLUGIN_ID, "variables"/*nonNLS*/), 
			getVariablesAsXMLString());
	}
	
	public void saveOptions() throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(
			new QualifiedName(JavaCore.PLUGIN_ID, "options"/*nonNLS*/), 
			getOptionsAsXMLString());
	}
/**
 * @see ISaveParticipant
 */
public void saving(ISaveContext context) throws CoreException {

	this.saveVariables();
	this.saveOptions();
	
	if (context.getKind() == ISaveContext.FULL_SAVE){
		this.saveBuildState();	// build state
	}
}
	/**
	 * Record the order in which to build the java projects (batch build). This order is based
	 * on the projects classpath settings.
	 */
	protected void setBuildOrder(String[] javaBuildOrder) throws JavaModelException {

		// optional behaviour
		// possible value of index 0 is Compute
		if (!JavaCore.getOptionPossibleValues(JavaCore.OPTION_ComputeBuildOrder)[0].equals(JavaCore.getOptionValue(JavaCore.OPTION_ComputeBuildOrder))) return;
		
		if (javaBuildOrder == null || javaBuildOrder.length <= 1) return;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		String[] wksBuildOrder = description.getBuildOrder();

		String[] newOrder;
		if (wksBuildOrder == null){
			newOrder = javaBuildOrder;
		} else {
			// remove projects which are already mentionned in java builder order
			int javaCount = javaBuildOrder.length;
			Hashtable newSet = new Hashtable(javaCount); // create a set for fast check
			for (int i = 0; i < javaCount; i++){
				newSet.put(javaBuildOrder[i], javaBuildOrder[i]);
			}
			int removed = 0;
			int oldCount = wksBuildOrder.length;
			for (int i = 0; i < oldCount; i++){
				if (newSet.containsKey(wksBuildOrder[i])){
					wksBuildOrder[i] = null;
					removed++;
				}
			}
			// add Java ones first
			newOrder = new String[oldCount - removed + javaCount];
			System.arraycopy(javaBuildOrder, 0, newOrder, 0, javaCount); // java projects are built first

			// copy previous items in their respective order
			int index = javaCount;
			for (int i = 0; i < oldCount; i++){
				if (wksBuildOrder[i] != null){
					newOrder[index++] = wksBuildOrder[i];
				}
			}
		}
		// commit the new build order out
		description.setBuildOrder(newOrder);
		try {
			workspace.setDescription(description);
		} catch(CoreException e){
			throw new JavaModelException(e);
		}
	}
	/**
	 * Sets the last built state for the given project, or null to reset it.
	 */
	public void setLastBuiltState(IProject project, IState state) {
		PerProjectInfo info= getPerProjectInfo(project);
		info.triedRead= true; // no point trying to re-read once using setter
		info.developmentContext.setCurrentState(state);
	}
	public void shutdown () {
		if (fDeltaProcessor.indexManager != null){ // no more indexing
			fDeltaProcessor.indexManager.shutdown();
			fDeltaProcessor.indexManager = null; 
		}
		try {
			IJavaModel model = this.getJavaModel();
			if (model != null) {
				model.close();
			}
		} catch (JavaModelException e) {
		}
	}
	/**
	 * Turns the firing mode to on. That is, deltas that are/have been
	 * registered will be fired.
	 */
	public void startDeltas() {
		fFire= true;
	}
	/**
	 * Turns the firing mode to off. That is, deltas that are/have been
	 * registered will not be fired until deltas are started again.
	 */
	public void stopDeltas() {
		fFire= false;
	}
	
	/**
 	* Answers a copy of the current set of options supported by the Java core.
 	* These options allow to configure the behavior of the underlying components.
 	*
 	* Changes on the set of options are not committed until invoking <code>JavaCore.setOptionValue</code>
 	*/
	public static ConfigurableOption[] getOptions(){
		if(fOptions == null)
			JavaCore.resetOptions();
		
		return (ConfigurableOption[])fOptions.values().toArray(new ConfigurableOption[0]);
	}
	
	/**
	 * Answers a set of option'IDs which are in option set of JavaCore
	 */
	public static String[] getOptionIDs(){
		if(fOptionIDs == null)
			return new String[0];
		return (String [])fOptionIDs.toArray(new String[0]);
	}
	
	public static void initializeOptions(){
		fOptions = new Hashtable(10);
		fOptionIDs = new ArrayList(10);
	}
	
	public static void addOption(ConfigurableOption option){
		String id = option.getID();
		fOptions.put(id,option);
		fOptionIDs.add(id);
	}
}
