public class C {
	Object pipeIn;
	
  public String foo() {
    if (pipeIn == null)
      getReader();                                // spawn parsing thread
    return "ok";
  }
	
  public String bar() {
    if (pipeIn == null)
      getReader();                                // spawn parsing thread
    return "ok";
  }
  void getReader() {}
}
