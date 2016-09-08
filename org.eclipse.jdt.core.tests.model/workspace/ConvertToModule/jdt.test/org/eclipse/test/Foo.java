package org.eclipse.test;

import java.net.ConnectException;
import java.sql.Connection;

public class Foo {
	Connection con = null;
	public void foo1() throws ConnectException {
	}
	public void foo2() throws java.rmi.ConnectException {
	}
}
