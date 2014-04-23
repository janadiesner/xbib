package org.xbib.io.archive.elasticsearch.bulk;


import org.xbib.util.URIUtil;

import java.io.File;
import java.nio.charset.Charset;

public class Coordinate {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    public static String encodeName(String[] components) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < components.length; i++) {
            if (i > 0) {
                sb.append(File.separator);
            }
            sb.append(URIUtil.encode(components[i], UTF8));
        }
        return sb.toString();
    }

    public static String[] decodeName(String component) {
        String[] components = component.split(File.separator);
        for (int i = 0; i < components.length; i++) {
            components[i] = URIUtil.decode(components[i], UTF8);
        }
        return components;
    }
}
