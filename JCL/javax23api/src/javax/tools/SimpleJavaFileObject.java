package javax.tools;

import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

public class SimpleJavaFileObject implements JavaFileObject {
    protected final URI uri;

    protected final Kind kind;

    protected SimpleJavaFileObject(URI uri, Kind kind) {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(kind);
        if (uri.getPath() == null)
            throw new IllegalArgumentException("URI must have a path: " + uri);
        this.uri = uri;
        this.kind = kind;
    }

    public URI toUri() {
        return uri;
    }

    public String getName() {
        return toUri().getPath();
    }

    public InputStream openInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        CharSequence charContent = getCharContent(ignoreEncodingErrors);
        if (charContent == null)
            throw new UnsupportedOperationException();
        if (charContent instanceof CharBuffer) {
            CharBuffer buffer = (CharBuffer)charContent;
            if (buffer.hasArray())
                return new CharArrayReader(buffer.array());
        }
        return new StringReader(charContent.toString());
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Writer openWriter() throws IOException {
        return new OutputStreamWriter(openOutputStream());
    }

    public long getLastModified() {
        return 0L;
    }

    public boolean delete() {
        return false;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isNameCompatible(String simpleName, Kind kind) {
        String baseName = simpleName + kind.extension;
        return kind.equals(getKind())
            && (baseName.equals(toUri().getPath())
                || toUri().getPath().endsWith("/" + baseName));
    }

    public NestingKind getNestingKind() { return null; }

    public Modifier getAccessLevel()  { return null; }

    @Override
    public String toString() {
        return getClass().getName() + "[" + toUri() + "]";
    }
}
