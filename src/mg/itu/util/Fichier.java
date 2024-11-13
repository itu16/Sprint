package mg.itu.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import jakarta.servlet.http.Part;

public class Fichier implements Part {
    protected Part parte;

    public Fichier() {

    }

    public Fichier(Part parte) {
        setPart(parte);
    }

    public void setPart(Part part) {
        this.parte = part;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.parte.getInputStream();
    }

    @Override
    public String getContentType() {
        return this.parte.getContentType();
    }

    @Override
    public String getName() {
        return this.parte.getName();
    }

    @Override
    public String getSubmittedFileName() {
        return this.parte.getSubmittedFileName();
    }

    @Override
    public long getSize() {
        return this.parte.getSize();
    }

    @Override
    public void write(String fileName) throws IOException {
        this.parte.write(fileName);
    }

    @Override
    public void delete() throws IOException {
        this.parte.delete();
    }

    @Override
    public String getHeader(String name) {
        return this.parte.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.parte.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.parte.getHeaderNames();
    }

}
