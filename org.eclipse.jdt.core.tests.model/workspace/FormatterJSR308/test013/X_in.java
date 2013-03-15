public @interface X { 
	public @Marker String value(); 
	@Marker String value2(); 
	@Marker public String value3(); 
}
@interface Marker {}
