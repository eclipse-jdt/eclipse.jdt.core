package javax.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Objects;

public class ForwardingFileObject<F extends FileObject> implements FileObject {

    protected final F fileObject;

    protected ForwardingFileObject(F fileObject) {
        this.fileObject = Objects.requireNonNull(fileObject);
    }

    @Override
    public URI toUri() {
        return fileObject.toUri();
    }

    @Override
    public String getName() {
        return fileObject.getName();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return fileObject.openInputStream();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return fileObject.openOutputStream();
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return fileObject.openReader(ignoreEncodingErrors);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return fileObject.getCharContent(ignoreEncodingErrors);
    }

    @Override
    public Writer openWriter() throws IOException {
        return fileObject.openWriter();
    }

    @Override
    public long getLastModified() {
        return fileObject.getLastModified();
    }

    @Override
    public boolean delete() {
        return fileObject.delete();
    }
}
