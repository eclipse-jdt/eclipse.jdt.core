/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import java.io.*;
import java.net.*;
public class SocketHelper {
	int localPort = -1;
/**
 * Using the given port number to create a server, creates a connection to this server
 * and returns the local port of the client (after closing the connection so that
 * the local port is now available).
 * Returns -1 if the server could not be created.
 */
public int getAvailablePort(final int portNumber) {
	this.localPort = -1;
	Thread server = new Thread() {
		public void run() {
			try {
				new ServerSocket(portNumber).accept().close();
			} catch (IOException e) {
				// address in use
			}
		}
	};
	server.start();

	try {
		Socket socket = new Socket("127.0.0.1", portNumber);
		server.join(2000);
		this.localPort = socket.getLocalPort();
		socket.close();
	} catch (InterruptedException e) {
	} catch (IOException e) {
		// connection refused
	}
	
	return this.localPort;
}
}
