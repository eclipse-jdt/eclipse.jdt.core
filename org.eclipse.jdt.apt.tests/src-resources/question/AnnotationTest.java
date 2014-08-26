package question;

@Deprecated
@RTVisibleAnno(anno=@SimpleAnnotation("test"), clazzes={})
@RTInvisibleAnno("question")
public interface AnnotationTest{

    @RTVisibleAnno(name = "Foundation",
                   boolValue   = false,
                   byteValue   = 0x10,
                   charValue   = 'c',
                   doubleValue = 99.0,
                   floatValue  = (float)9.0,
                   intValue    = 999,
                   longValue = 3333,
                   shortValue = 3,
                   colors ={ Color.RED, Color.BLUE },
                   anno = @SimpleAnnotation("core"),
                   simpleAnnos = {@SimpleAnnotation("org"),
              	                   @SimpleAnnotation("eclipse"),
        		                   @SimpleAnnotation("jdt") },
                   clazzes = {Object.class, String.class},
 		            clazz = Object.class)
     @RTInvisibleAnno("org.eclipse.jdt.core")
     @Deprecated
     public int field0 = 0;

    @Deprecated
    public int field1 = 1;

    @RTVisibleAnno(anno=@SimpleAnnotation("field"), clazzes={})
     @RTInvisibleAnno("2")
     public int field2 = 2;

    @RTInvisibleAnno("3")
    public int field3 = 3;

    @SimpleAnnotation("4")
    public int field4 = 4;

    @RTVisibleAnno(anno=@SimpleAnnotation("method0"), clazzes={})
    @RTInvisibleAnno("0")
    @Deprecated
     public int method0();

    @Deprecated
     public int method1();

    @RTVisibleAnno(anno=@SimpleAnnotation("method2"), clazzes={})
     @RTInvisibleAnno("2")
    public int method2();

   @RTInvisibleAnno("3")
   public int method3();

   @SimpleAnnotation("method4")
   public int method4();

   public int method5(int p0,
   				   @Deprecated
   				   int p1,
   				   @RTVisibleAnno(anno=@SimpleAnnotation("param2"), clazzes={})
   				   @RTInvisibleAnno("2")
   				   int p2);

   public int method6(int p0, int p1, int p2);

   @RTVisibleAnno(name = "I'm \"special\": \t\\\n",
		          charValue = '\'',
		          clazzes = {},
		          anno = @SimpleAnnotation(""))
   public int method7();
 }