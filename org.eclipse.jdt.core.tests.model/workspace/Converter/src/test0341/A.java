package test0341;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Vector;

public class A {

	private Vector field;

	public void l()
		throws
			IOException,
			MalformedURLException,
			InterruptedIOException,
			UnsupportedEncodingException {
		if (field != null) {
			throw new IOException();
		} else if (field == null) {
			throw new MalformedURLException();
		} else if (field == null) {
			throw new InterruptedIOException();
		} else {
			throw new UnsupportedEncodingException();
		}
	}
}