package org.eclipse.jdt.core.tests.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wrapper around the external processes that are running a J9 VM
 * and a J9 Proxy.
 * This allows to kill these processes when we exit this vm.
 */
class J9VirtualMachine extends LocalVirtualMachine {
	private Process proxyProcess;
	private AbstractReader proxyConsoleReader;
	private String proxyOutputFile;
/**
 * Creates a new J9VirtualMachine from the Processes that runs this VM
 * and its J9 Proxy and with the given info.
 */
public J9VirtualMachine(Process vmProcess, int debugPort, String evalTargetPath, Process proxyProcess, String proxyOutputFile) {
	super(vmProcess, debugPort, evalTargetPath);
	this.proxyProcess = proxyProcess;
	this.proxyOutputFile = proxyOutputFile;
	
	// Start the Proxy console reader so that the proxy is not blocked on its stdout.
	if (this.proxyProcess != null) {
		if (this.proxyOutputFile == null) {
			this.proxyConsoleReader= 
				new NullConsoleReader(
					"J9 Proxy Console Reader", 
					this.proxyProcess.getInputStream());
		} else {
			this.proxyConsoleReader= 
				new ProxyConsoleReader(
					"J9 Proxy Console Reader", 
					this.proxyProcess.getInputStream(),
					this.proxyOutputFile);
		}
		this.proxyConsoleReader.start();
	}

}
private boolean isProxyRunning() {
	if (this.proxyProcess == null)
		return false;
	else {
		boolean hasExited;
		try {
			this.proxyProcess.exitValue();
			hasExited = true;
		} catch (IllegalThreadStateException e) {
			hasExited = false;
		}
		return !hasExited;
	}
}
/**
 * @see LocalVirtualMachine#shutDown
 */
public void shutDown()  throws TargetException {
	super.shutDown();
	if (this.proxyConsoleReader != null)
		this.proxyConsoleReader.stop();
	if ((this.proxyProcess != null) && isProxyRunning()) 
		this.proxyProcess.destroy();
}
/**
 * @see LocalVirtualMachine#shutDown
 */
public void waitForTermination() throws InterruptedException {
	super.waitForTermination();
	if (this.proxyProcess != null)
		this.proxyProcess.waitFor();
	if (this.proxyConsoleReader != null)
		this.proxyConsoleReader.stop();
}
}
