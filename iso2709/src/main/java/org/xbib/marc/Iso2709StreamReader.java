package org.xbib.marc;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Iso2709StreamReader implements XMLStreamReader {

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }

    @Override
    public int next() throws XMLStreamException {
        return 0;
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {

    }

    @Override
    public String getElementText() throws XMLStreamException {
        return null;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        return 0;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return false;
    }

    @Override
    public void close() throws XMLStreamException {

    }

    @Override
    public String getNamespaceURI(String prefix) {
        return null;
    }

    @Override
    public boolean isStartElement() {
        return false;
    }

    @Override
    public boolean isEndElement() {
        return false;
    }

    @Override
    public boolean isCharacters() {
        return false;
    }

    @Override
    public boolean isWhiteSpace() {
        return false;
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        return null;
    }

    @Override
    public int getAttributeCount() {
        return 0;
    }

    @Override
    public QName getAttributeName(int index) {
        return null;
    }

    @Override
    public String getAttributeNamespace(int index) {
        return null;
    }

    @Override
    public String getAttributeLocalName(int index) {
        return null;
    }

    @Override
    public String getAttributePrefix(int index) {
        return null;
    }

    @Override
    public String getAttributeType(int index) {
        return null;
    }

    @Override
    public String getAttributeValue(int index) {
        return null;
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        return false;
    }

    @Override
    public int getNamespaceCount() {
        return 0;
    }

    @Override
    public String getNamespacePrefix(int index) {
        return null;
    }

    @Override
    public String getNamespaceURI(int index) {
        return null;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return null;
    }

    @Override
    public int getEventType() {
        return 0;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public char[] getTextCharacters() {
        return new char[0];
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return 0;
    }

    @Override
    public int getTextStart() {
        return 0;
    }

    @Override
    public int getTextLength() {
        return 0;
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public boolean hasText() {
        return false;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public QName getName() {
        return null;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public boolean hasName() {
        return false;
    }

    @Override
    public String getNamespaceURI() {
        return null;
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean isStandalone() {
        return false;
    }

    @Override
    public boolean standaloneSet() {
        return false;
    }

    @Override
    public String getCharacterEncodingScheme() {
        return null;
    }

    @Override
    public String getPITarget() {
        return null;
    }

    @Override
    public String getPIData() {
        return null;
    }
}
