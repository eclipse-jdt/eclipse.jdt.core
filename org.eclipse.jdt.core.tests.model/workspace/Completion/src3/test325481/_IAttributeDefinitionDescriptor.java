package test325481;

@ConfigurationData(value= "configuration.attributes")
@_Path("attribute")
public interface _IAttributeDefinitionDescriptor {

	public static final String BUILT_ATTRIBUTE= "abc"; //$NON-NLS-1$
	public static final String WORK_ATTRIBUTE= "abc"; //$NON-NLS-1$
	public static final String RANKING_ATTRIBUTE= "abc"; //$NON-NLS-1$
	public static final String RANKING_ATTRIBUTE_V2= "abc"; //$NON-NLS-1$
	public static final String REFERENCE_ATTRIBUTE= "abc"; //$NON-NLS-1$
	
	public static final String ATTRIBUTE= "attribute"; //$NON-NLS-1$
	public static final String ADD_CUSTOM_ATTRIBUTES= "addCustomAttributes"; //$NON-NLS-1$
	
	@_Path("@id")
	String setId(String value);
	
	@_Path("@name")
	String setDisplayName(String value);

	@_Path("@implementation")
	String setImplementationName(String value);
	@_Path("@implementation")
	String setImplementationD(String value);
	@_Path("@implementation")
	String setImplementation2(String value);
	@_Path("@queryId")
	String getQueryId();

	@_Path("@queryId")
	String setQueryId(String value);

	@_Path("@readOnly")
	boolean isReadOnly();

	@_Path("@readOnly")
	boolean setReadOnly(boolean value);

	@_Path("@internal")
	boolean isInternal();
	
	@_Path("@internal")
	boolean setInternal(boolean value);
}
