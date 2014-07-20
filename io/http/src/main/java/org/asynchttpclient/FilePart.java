package org.asynchttpclient;

import java.io.File;

/**
 * A file multipart part.
 */
public class FilePart implements Part {
    private String name;
    private File file;
    private String mimeType;
    private String charSet;

    public FilePart(String name, File file, String mimeType, String charSet) {
        this.name = name;
        this.file = file;
        this.mimeType = mimeType;
        this.charSet = charSet;
    }

    @Override
    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getCharSet() {
        return charSet;
    }
}