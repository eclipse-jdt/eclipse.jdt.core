package org.eclipse.jdt.core.ant.taskdef;

import java.io.File;
import java.net.URL;

/**
 * Represents a link triplet (href, whether link is offline,
 * location of the package list if off line)
 */
public class LinkArgument {
    private String href;
    private boolean offline = false;
    private File packagelistLoc;
    private URL packagelistURL;
    private boolean resolveLink = false;

    /** Constructor for LinkArguement */
    public LinkArgument() {
        //empty
    }

    /**
     * Set the href attribute.
     * @param hr a <code>String</code> value
     */
    public void setHref(String hr) {
        this.href = hr;
    }

    /**
     * Get the href attribute.
     * @return the href attribute.
     */
    public String getHref() {
        return this.href;
    }

    /**
     * Set the packetlist location attribute.
     * @param src a <code>File</code> value
     */
    public void setPackagelistLoc(File src) {
        this.packagelistLoc = src;
    }

    /**
     * Get the packetList location attribute.
     * @return the packetList location attribute.
     */
    public File getPackagelistLoc() {
        return this.packagelistLoc;
    }

    /**
     * Set the packetlist location attribute.
     * @param src an <code>URL</code> value
     */
    public void setPackagelistURL(URL src) {
        this.packagelistURL = src;
    }

    /**
     * Get the packetList location attribute.
     * @return the packetList location attribute.
     */
    public URL getPackagelistURL() {
        return this.packagelistURL;
    }

    /**
     * Set the offline attribute.
     * @param offline a <code>boolean</code> value
     */
    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    /**
     * Get the linkOffline attribute.
     * @return the linkOffline attribute.
     */
    public boolean isLinkOffline() {
        return this.offline;
    }

    /**
     * Sets whether Ant should resolve the link attribute relative
     * to the current basedir.
     * @param resolve a <code>boolean</code> value
     */
    public void setResolveLink(boolean resolve) {
        this.resolveLink = resolve;
    }

    /**
     * should Ant resolve the link attribute relative to the
     * current basedir?
     * @return the resolveLink attribute.
     */
    public boolean shouldResolveLink() {
        return this.resolveLink;
    }

}

