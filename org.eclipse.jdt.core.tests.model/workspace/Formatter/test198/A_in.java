public String individualToString() {
	StringBuffer buffer = new StringBuffer("Exception flow context");
	int length = handledExceptions.length;
	for (int i = 0; i < length; i++) {
		buffer.append('[').append(handledExceptions[i].readableName());
		if (isReached[i]) {
			buffer.append("-reached");
		} else {if (isMasked[i])
				buffer.append("-masked");
			else buffer.append("-not reached");
	} buffer.append('-').append(initsOnExceptions[i].toString()).append(']');
} return buffer.toString();
}