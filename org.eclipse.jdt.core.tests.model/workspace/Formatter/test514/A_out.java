public class X {
public void foo() {
	if (TextRequest.REQ_INSERT == request.getType()
			|| TextRequest.REQ_BACKSPACE == request.getType()
			|| TextRequest.REQ_DELETE == request.getType()
			|| TextRequest.REQ_REMOVE_RANGE == request.getType())
		return getHost();
}
}
