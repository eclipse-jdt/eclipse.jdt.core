package org.eclipse.jdt.core.tests.eval;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.runtime.StandardVMLauncher;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.runtime.TargetInterface;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.eval.EvaluationContext;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

public class DebugEvaluationSetup extends EvaluationSetup {
public DebugEvaluationSetup(junit.framework.Test test) {
	super(test);
}
protected void initTest(Object test, VirtualMachine jdiVM) {
	if (test instanceof DebugEvaluationTest) {
		DebugEvaluationTest evalTest = (DebugEvaluationTest)test;
		evalTest.jdiVM = jdiVM;
		return;
	}
	if (test instanceof TestSuite) {
		TestSuite evaluationTestClassSuite = (TestSuite) test;
		Enumeration evaluationTestClassTests = evaluationTestClassSuite.tests();
		while (evaluationTestClassTests.hasMoreElements()) {
			initTest(evaluationTestClassTests.nextElement(), jdiVM);
		}
		return;
	}
		
}
protected void setUp() {
	// Launch VM in evaluation mode
	int debugPort = Util.nextAvailablePortNumber();
	int evalPort = Util.nextAvailablePortNumber();
	StandardVMLauncher launcher;
	try {
		launcher = new StandardVMLauncher();
		launcher.setVMArguments(new String[] {"-verify"});
		launcher.setVMPath(this.jrePath);
		launcher.setEvalPort(evalPort);
		launcher.setEvalTargetPath(this.evalDirectory);
		launcher.setDebugPort(debugPort);
		this.launchedVM = launcher.launch();
	} catch (TargetException e) {
		throw new Error(e.getMessage());
	}

	// Thread that read the stout of the VM so that the VM doesn't block
	try {
		startReader("VM's stdout reader", this.launchedVM.getInputStream());
	} catch (TargetException e) {
	}

	// Thread that read the sterr of the VM so that the VM doesn't block
	try {
		startReader("VM's sterr reader", this.launchedVM.getErrorStream());
	} catch (TargetException e) {
	}

	// Start JDI connection (try 10 times)
	VirtualMachine vm = null;
	for (int i = 0; i < 10; i++) {
		try {
			VirtualMachineManager manager = org.eclipse.jdi.Bootstrap.virtualMachineManager();
			List connectors = manager.attachingConnectors();
			if (connectors.size() == 0)
				break;
			AttachingConnector connector = (AttachingConnector)connectors.get(0);
			Map args = connector.defaultArguments();
			((Connector.Argument)args.get("port")).setValue(String.valueOf(debugPort));
			((Connector.Argument)args.get("hostname")).setValue(launcher.getTargetAddress());
			vm = connector.attach(args);
			break;
		} catch (IllegalConnectorArgumentsException e) {
		} catch (IOException e) {
			System.out.println("Got exception: " + e.getMessage());
			try {
				System.out.println("Could not contact the VM at " + launcher.getTargetAddress() + ":" + debugPort + ". Retrying...");
				Thread.sleep(100);
			} catch (InterruptedException e2) {
			}
		}
	}
	if (vm == null) {
		if (this.launchedVM != null) {
			// If the VM is not running, output error stream
			try {
				if (!this.launchedVM.isRunning()) {
					InputStream in = this.launchedVM.getErrorStream();
					int read;
					do {
						read= in.read();
						if (read != -1)
							System.out.print((char) read);
					} while (read != -1);
				}
			} catch (TargetException e) {
			} catch (IOException e) {
			}

			// Shut it down
			try {
				target.disconnect(); // Close the socket first so that the OS resource has a chance to be freed. 
				int retry = 0;
				while (launchedVM.isRunning() && (++retry < 20)) {
					try {
						Thread.sleep(retry * 100);
					} catch (InterruptedException e) {
					}
				}
				if (launchedVM.isRunning()) {
					launchedVM.shutDown();
				}
			} catch (TargetException e) {
			}
		}
		throw new Error("Could not contact the VM");
	}

	// Create context
	this.context = new EvaluationContext();

	// Create target
	this.target = new TargetInterface();
	this.target.connect("localhost", evalPort, 10000);

	// Create name environment
	INameEnvironment env = new FileSystem(new String[] {this.jrePath + "\\lib\\rt.jar"}, new String[0], null);

	// Init wrapped suite
	initTest(fTest, this.context, this.target, this.launchedVM, env);
	initTest(fTest, vm);
}
}
