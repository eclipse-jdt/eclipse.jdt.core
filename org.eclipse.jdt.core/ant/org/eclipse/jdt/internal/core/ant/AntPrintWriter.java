package org.eclipse.jdt.internal.core.ant;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;

import org.apache.tools.ant.*;

public class AntPrintWriter extends PrintWriter {
	private Task task;
	private String currentLine;
	
	public AntPrintWriter(Task t){
		super(System.out);
		task = t;
		currentLine = ""; //$NON-NLS-1$
	}

    public void flush() {
		task.log(currentLine);
		currentLine = ""; //$NON-NLS-1$
    }

    public void close() {
    	flush();
    }

    public void write(int c) {
		currentLine += String.valueOf(c);
    }

    public void write(char buf[], int off, int len) {
		currentLine += new String(buf).substring(off,off+len);
    }

    public void write(char buf[]) {
		write(buf, 0, buf.length);
    }


    public void write(String s, int off, int len) {
		currentLine += s.substring(off,off+len);
    }

    public void write(String s) {
		write(s, 0, s.length());
    }

    public void print(boolean b) {
		write(b ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void print(char c) {
		write(String.valueOf(c));
    }

    public void print(int i) {
		write(String.valueOf(i));
    }

    public void print(long l) {
		write(String.valueOf(l));
    }

    public void print(float f) {
		write(String.valueOf(f));
    }

    public void print(double d) {
		write(String.valueOf(d));
    }

    public void print(char s[]) {
		write(s);
    }


    public void print(String s) {
		if (s == null) {
	 	   s = "null"; //$NON-NLS-1$
		}
		write(s);
    }

    public void print(Object obj) {
		write(String.valueOf(obj));
    }

    public void println() {
		flush();
    }

    public void println(boolean x) {
	    print(x);
	    println();
    }

    public void println(char x) {
	    print(x);
	    println();
    }

    public void println(int x) {
	    print(x);
	    println();
    }

    public void println(long x) {
	    print(x);
	    println();
    }

    public void println(float x) {
	    print(x);
	    println();
    }

    public void println(double x) {
	    print(x);
	    println();
    }

    public void println(char x[]) {
	    print(x);
	    println();
    }

    public void println(String x) {
	    print(x);
	    println();
    }

    public void println(Object x) {
	    print(x);
	    println();
    }
}

