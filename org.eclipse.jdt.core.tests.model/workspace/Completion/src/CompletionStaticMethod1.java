public class CompletionStaticMethod1 extends TypeWithAMethodAndAStaticMethod {
	void bar(){
		new TypeWithAMethodAndAStaticMethod(){
			class Inner1 extends TypeWithAMethodAndAStaticMethod {
				void bar(){
					foo
				}
			}
		};
	}
	
}