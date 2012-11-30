/**
 * based on RDFXMLWriter Copyright Aduna (http://www.aduna-software.com/) (c)
 * 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 *
 */
package org.xbib.rdf.io.rdfxml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import org.xbib.rdf.BlankNode;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.RDF;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;
import org.xbib.xml.XMLUtil;

public class RdfXmlWriter<S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>>
        implements RDF {

    private Writer writer;
    private boolean writingStarted;
    private boolean headerWritten;
    private S lastWrittenSubject;
    private NamespaceContext context;

    public void write(Resource resource, OutputStream out, NamespaceContext context) throws IOException {
        write(resource, new OutputStreamWriter(out, "UTF-8"), context);
    }

    public void write(Resource resource, Writer writer, NamespaceContext context) throws IOException {
        this.writer = writer;
        this.writingStarted = false;
        this.headerWritten = false;
        this.lastWrittenSubject = null;
        this.context = context == null ? SimpleNamespaceContext.getInstance() : context;
        // make RDF name spaces
        for (Map.Entry<String, String> entry : context.getNamespaces().entrySet()) {
            handleNamespace(entry.getKey(), entry.getValue());
        }
        startRDF();
        writeHeader();
        Iterator<Statement> it = resource.iterator();
        while (it.hasNext()) {
            Statement stmt = it.next();
            handleStatement(stmt);
        }
        endRDF();
    }

    private void startRDF() throws IOException {
        if (writingStarted) {
            throw new IOException("writing has already started");
        }
        writingStarted = true;
    }

    private void writeHeader() throws IOException {
        try {
            // This export format needs the RDF namespace to be defined, add a
            // prefix for it if there isn't one yet.
            setNamespace("rdf", NS_URI, false);
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeStartOfStartTag(NS_URI, "RDF");
            for (Map.Entry<String, String> entry : context.getNamespaces().entrySet()) {
                String prefix = entry.getKey();
                String name = entry.getValue();
                writeNewLine();
                writer.write("\t");
                writer.write("xmlns");
                if (prefix.length() > 0) {
                    writer.write(':');
                    writer.write(prefix);
                }
                writer.write("=\"");
                writer.write(escapeDoubleQuotedAttValue(name));
                writer.write("\"");
            }
            writer.write(">");
            writeNewLine();
        } finally {
            headerWritten = true;
        }
    }

    private void endRDF() throws IOException {
        if (!writingStarted) {
            throw new IOException("Document writing has not yet started");
        }
        try {
            if (!headerWritten) {
                writeHeader();
            }
            flushPendingStatements();
            writeNewLine();
            writeEndTag(NS_URI, "RDF");
            writer.flush();
        } finally {
            writingStarted = false;
            headerWritten = false;
        }
    }

    private void handleNamespace(String prefix, String name) {
        setNamespace(prefix, name, false);
    }

    private void setNamespace(String prefix, String name, boolean fixedPrefix) {
        if (headerWritten) {
            // Header containing namespace declarations has already been written
            return;
        }
        Map map = context.getNamespaces();
        if (!map.containsKey(name)) {
            // Namespace not yet mapped to a prefix, try to give it the specified
            // prefix
            boolean isLegalPrefix = prefix.length() == 0 || XMLUtil.isNCName(prefix);
            if (!isLegalPrefix || map.containsValue(prefix)) {
                // Specified prefix is not legal or the prefix is already in use,
                // generate a legal unique prefix
                if (fixedPrefix) {
                    if (isLegalPrefix) {
                        throw new IllegalArgumentException("Prefix is already in use: " + prefix);
                    } else {
                        throw new IllegalArgumentException("Prefix is not a valid XML namespace prefix: " + prefix);
                    }
                }
                if (prefix.length() == 0 || !isLegalPrefix) {
                    prefix = "ns";
                }
                int number = 1;
                while (map.containsValue(prefix + number)) {
                    number++;
                }
                prefix += number;
            }
            context.addNamespace(prefix, name);
        }
    }

    private void handleStatement(Statement<S, P, O> statement) throws IOException {
        if (!writingStarted) {
            throw new IOException("Document writing has not yet been started");
        }
        S subj = statement.getSubject();
        P pred = statement.getPredicate();
        O obj = statement.getObject();
        // Verify that an XML namespace-qualified name can be created for the
        // predicate
        String predString = pred.toString();
        int predSplitIdx = findURISplitIndex(predString);
        if (predSplitIdx == -1) {
            throw new IOException("Unable to create XML namespace-qualified name for predicate: "
                    + predString);
        }
        String predNamespace = predString.substring(0, predSplitIdx);
        String predLocalName = predString.substring(predSplitIdx);
        if (!headerWritten) {
            writeHeader();
        }
        // SUBJECT
        if (!subj.equals(lastWrittenSubject)) {
            flushPendingStatements();
            // Write new subject:
            writeNewLine();
            writeStartOfStartTag(NS_URI, "Description");
            if (subj.id().getScheme().equals(BlankNode.PREFIX)) {
                writeAttribute(NS_URI, "nodeID", subj.toString());
            } else {
                writeAttribute(NS_URI, "about", subj.toString());
            }
            writer.write(">");
            writeNewLine();
            lastWrittenSubject = subj;
        }
        // PREDICATE
        writer.write("\t");
        writeStartOfStartTag(predNamespace, predLocalName);
        // OBJECT
        if (obj instanceof Resource) {
            Resource objRes = (Resource) obj;
            if (objRes instanceof BlankNode) {
                BlankNode bNode = (BlankNode) objRes;
                writeAttribute(NS_URI, "nodeID", bNode.id().toString());
            } else {
                URI uri = objRes.id();
                writeAttribute(NS_URI, "resource", uri.toString());
            }
            writer.write("/>");
        } else if (obj instanceof Literal) {
            Literal objLit = (Literal) obj;
            // language attribute
            if (objLit.language() != null) {
                writeAttribute("xml:lang", objLit.language());
            }
            // datatype attribute
            boolean isXMLLiteral = false;
            URI datatype = objLit.type();
            if (datatype != null) {
                // Check if datatype is rdf:XMLLiteral
                isXMLLiteral = datatype.equals(RDF_XMLLITERAL);
                if (isXMLLiteral) {
                    writeAttribute(NS_URI, "parseType", "Literal");
                } else {
                    writeAttribute(NS_URI, "datatype", datatype.toString());
                }
            }
            writer.write(">");
            // label
            if (isXMLLiteral) {
                // Write XML literal as plain XML
                writer.write(objLit.toString());
            } else {
                writer.write(escapeCharacterData(objLit.toString()));
            }
            writeEndTag(predNamespace, predLocalName);
        }
        writeNewLine();
        // Don't write </rdf:Description> yet, maybe the next statement
        // has the same subject.
    }

    private void handleComment(String comment)
            throws IOException {
        if (!headerWritten) {
            writeHeader();
        }
        flushPendingStatements();
        writer.write("<!-- ");
        writer.write(comment);
        writer.write(" -->");
        writeNewLine();
    }

    private void flushPendingStatements() throws IOException {
        if (lastWrittenSubject != null) {
            // The last statement still has to be closed:
            writeEndTag(NS_URI, "Description");
            writeNewLine();
            lastWrittenSubject = null;
        }
    }

    private void writeStartOfStartTag(String namespace, String localName)
            throws IOException {
        String prefix = context.getPrefix(namespace);

        if (prefix == null) {
            writer.write("<");
            writer.write(localName);
            writer.write(" xmlns=\"");
            writer.write(escapeDoubleQuotedAttValue(namespace));
            writer.write("\"");
        } else if (prefix.length() == 0) {
            // default namespace
            writer.write("<");
            writer.write(localName);
        } else {
            writer.write("<");
            writer.write(prefix);
            writer.write(":");
            writer.write(localName);
        }
    }

    private void writeAttribute(String attName, String value) throws IOException {
        writer.write(" ");
        writer.write(attName);
        writer.write("=\"");
        writer.write(escapeDoubleQuotedAttValue(value));
        writer.write("\"");
    }

    private void writeAttribute(String namespace, String attName, String value)
            throws IOException {
        String prefix = context.getPrefix(namespace);
        if (prefix == null || prefix.length() == 0) {
            throw new IOException("No prefix has been declared for the namespace used in this attribute: "
                    + namespace);
        }
        writer.write(" ");
        writer.write(prefix);
        writer.write(":");
        writer.write(attName);
        writer.write("=\"");
        writer.write(escapeDoubleQuotedAttValue(value));
        writer.write("\"");
    }

    private void writeEndTag(String namespace, String localName) throws IOException {
        String prefix = context.getPrefix(namespace);
        if (prefix == null || prefix.length() == 0) {
            writer.write("</");
            writer.write(localName);
            writer.write(">");
        } else {
            writer.write("</");
            writer.write(prefix);
            writer.write(":");
            writer.write(localName);
            writer.write(">");
        }
    }

    private void writeNewLine() throws IOException {
        writer.write("\n");
    }

    /**
     * Escapes any special characters in the supplied text so that it can be
     * included as character data in an XML document. The characters that are
     * escaped are <tt>&amp;</tt>, <tt>&lt;</tt>, <tt>&gt;</tt> and <tt>carriage
     * return (\r)</tt>.
     */
    private String escapeCharacterData(String text) {
        text = gsub("&", "&amp;", text);
        text = gsub("<", "&lt;", text);
        text = gsub(">", "&gt;", text);
        text = gsub("\r", "&#xD;", text);
        return text;
    }

    /**
     * Escapes any special characters in the supplied value so that it can be
     * used as an double-quoted attribute value in an XML document. The
     * characters that are escaped are <tt>&amp;</tt>, <tt>&lt;</tt>,
     * <tt>&gt;</tt>, <tt>tab (\t)</tt>, <tt>carriage return (\r)</tt>, <tt>line
     * feed (\n)</tt> and <tt>"</tt>.
     */
    private String escapeDoubleQuotedAttValue(String value) {
        value = escapeAttValue(value);
        value = gsub("\"", "&quot;", value);
        return value;
    }

    private String escapeAttValue(String value) {
        value = gsub("&", "&amp;", value);
        value = gsub("<", "&lt;", value);
        value = gsub(">", "&gt;", value);
        value = gsub("\t", "&#x9;", value);
        value = gsub("\n", "&#xA;", value);
        value = gsub("\r", "&#xD;", value);
        return value;
    }

    /**
     * Substitute String "old" by String "new" in String "text" everywhere. This
     * is static util function that I could not place anywhere more appropriate.
     * The name of this function is from the good-old awk time.
     *
     * @param olds The String to be substituted.
     * @param news The String is the new content.
     * @param text The String in which the substitution is done.
     * @return The result String containing the substitutions; if no
     * substitutions were made, the result is 'text'.
     */
    private String gsub(String olds, String news, String text) {
        if (olds == null || olds.length() == 0) {
            // Nothing to substitute.
            return text;
        }
        if (text == null) {
            return null;
        }
        // Search for any occurences of 'olds'.
        int oldsIndex = text.indexOf(olds);
        if (oldsIndex == -1) {
            // Nothing to substitute.
            return text;
        }
        // We're going to do some substitutions.
        StringBuilder buf = new StringBuilder(text.length());
        int prevIndex = 0;
        while (oldsIndex >= 0) {
            // First, add the text between the previous and the current
            // occurence.
            buf.append(text.substring(prevIndex, oldsIndex));
            // Then add the substition pattern
            buf.append(news);
            // Remember the index for the next loop.
            prevIndex = oldsIndex + olds.length();
            // Search for the next occurence.
            oldsIndex = text.indexOf(olds, prevIndex);
        }
        // Add the part after the last occurence.
        buf.append(text.substring(prevIndex));
        return buf.toString();
    }

    /**
     * Tries to find a point in the supplied URI where this URI can be safely
     * split into a namespace part and a local name. According to the XML
     * specifications, a local name must start with a letter or underscore and
     * can be followed by zero or more 'NCName' characters.
     *
     * @param uri The URI to split.
     * @return The index of the first character of the local name, or
     * <tt>-1</tt> if the URI can not be split into a namespace and local name.
     */
    private int findURISplitIndex(String uri) {
        int uriLength = uri.length();
        // Search last character that is not an NCName character
        int i = uriLength - 1;
        while (i >= 0) {
            char c = uri.charAt(i);
            // Check for # and / characters explicitly as these
            // are used as the end of a namespace very frequently
            if (c == '#' || c == '/' || !XMLUtil.isNCNameChar(c)) {
                // Found it at index i
                break;
            }
            i--;
        }
        // Character after the just found non-NCName character could
        // be an NCName character, but not a letter or underscore.
        // Skip characters that are not letters or underscores.
        i++;
        while (i < uriLength) {
            char c = uri.charAt(i);
            if (c == '_' || XMLUtil.isLetter(c)) {
                break;
            }
            i++;
        }
        // Check that a legal split point has been found
        if (i == uriLength) {
            i = -1;
        }
        return i;
    }
}
