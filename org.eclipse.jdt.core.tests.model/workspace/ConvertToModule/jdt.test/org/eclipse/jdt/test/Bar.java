package org.eclipse.jdt.test;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

public class Bar extends Image {

	@Override
	public Graphics getGraphics() {
		return null;
	}

	@Override
	public int getHeight(ImageObserver arg0) {
		return 0;
	}

	@Override
	public Object getProperty(String arg0, ImageObserver arg1) {
		return null;
	}

	@Override
	public ImageProducer getSource() {
		return null;
	}

	@Override
	public int getWidth(ImageObserver arg0) {
		return 0;
	}
	
}
