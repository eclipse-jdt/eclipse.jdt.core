package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.EventListener;

/**
 * An IBuildListener is a listener which is notified of builder-specific
 * information during a batch or incremental build.
 */
public interface IBuildListener extends EventListener {

	/**
	 * Notifification that builder information has changed.
	 */
	public void buildUpdate(BuildEvent event);
}
