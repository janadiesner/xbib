
package org.xbib.rule.io;

import java.io.IOException;
import java.io.Reader;

public interface RuleSource {

    String content() throws IOException;

    Reader reader() throws IOException;

    String filename();

    long lastModified();
}
