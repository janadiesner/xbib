package org.xbib.xml.stream;

/**
 * Characters that represent line breaks and indentation. These are represented
 * as String-valued JavaBean properties.
 */
public interface Indentation {

    /**
     * Two spaces; the default indentation.
     */
    public static final String DEFAULT_INDENT = "  ";

    /**
     * Set the characters used for one level of indentation. The default is
     * {@link #DEFAULT_INDENT}. "\t" is a popular alternative.
     */
    void setIndent(String indent);

    /**
     * The characters used for one level of indentation.
     */
    String getIndent();

    /**
     * "\n"; the normalized representation of end-of-line in
     * <a href="http://www.w3.org/TR/xml11/#sec-line-ends">XML</a>.
     */
    public static final String NORMAL_END_OF_LINE = "\n";

}