package test325481;

@ConfigurationData(value= "configuration.attributes")
@Path("attribute")
public interface IAttributeDefinitionDescriptor {

	public static final String BUILT_ATTRIBUTE= "abc"; //$NON-NLS-1$
	public static final String WORK_ATTRIBUTE= "abc"; //$NON-NLS-1$
	public static final String RANKING_ATTRIBUTE= "abc"; //$NON-NLS-1$
	public static final String RANKING_ATTRIBUTE_V2= "abc"; //$NON-NLS-1$
	public static final String REFERENCE_ATTRIBUTE= "abc"; //$NON-NLS-1$
	
	public static final String ATTRIBUTE= "attribute"; //$NON-NLS-1$
	public static final String ADD_CUSTOM_ATTRIBUTES= "addCustomAttributes"; //$NON-NLS-1$
	
	@Path("@id")
	String setId(String value);
	
	@Path("@name")
	String setDisplayName(String value);

	@Path("@implementation")
	String setImplementationName(String value);
	@Path("@implementation")
	String setImplementationD(String value);
	@Path("@implementation")
	String setImplementation2(String value);
	@Path("@queryId")
	String getQueryId();

	@Path("@queryId")
	String setQueryId(String value);

	@Path("@readOnly")
	boolean isReadOnly();

	@Path("@readOnly")
	boolean setReadOnly(boolean value);

	@Path("@internal")
	boolean isInternal();
	
	@Path("@internal")
	boolean setInternal(boolean value);
}
