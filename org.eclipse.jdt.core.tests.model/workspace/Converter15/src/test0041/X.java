package test0041;

class PageFlowController {}

class T {
	T(String s) {}
}
/**
 * This is the default controller for a blank web application.
 */
@test0041.Controller
@test0041.ViewProperties(
        val = { 
            "<!-- This data is auto-generated. Hand-editing this section is not recommended. -->", 
            "<view-properties>", 
            "<pageflow-object id='action:begin.do'>", 
            "  <property value='60' name='x'/>", 
            "  <property value='80' name='y'/>", 
            "</pageflow-object>", 
            "<pageflow-object id='forward:path#index#index.jsp#@action:begin.do@'>", 
            "  <property value='96,130,130,164' name='elbowsX'/>", 
            "  <property value='72,72,72,72' name='elbowsY'/>", 
            "  <property value='East_1' name='fromPort'/>", 
            "  <property value='West_1' name='toPort'/>", 
            "  <property value='index' name='label'/>", 
            "</pageflow-object>", 
            "<pageflow-object id='page:index.jsp'>", 
            "  <property value='200' name='x'/>", 
            "  <property value='80' name='y'/>", 
            "</pageflow-object>", 
            "<pageflow-object id='page:error.jsp'>", 
            "  <property value='60' name='x'/>", 
            "  <property value='180' name='y'/>", 
            "</pageflow-object>", 
            "</view-properties>"
        }
    )
public class X 
    extends PageFlowController
{
    @test0041.Action(
        forwards={
           @test0041.Forward(name="success", path="index.jsp")
        }
    )
    protected T begin()
    {
        return new T("success");
    }
}