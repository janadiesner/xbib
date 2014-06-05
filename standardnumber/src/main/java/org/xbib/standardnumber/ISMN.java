package org.xbib.standardnumber;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 10957: International Standard Music Number (ISMN)
 *
 * Z39.50 BIB-1 Use Attribute 1092
 *
 * The International Standard Music Number (ISMN) is a thirteen-character alphanumeric identifier
 * for printed music developed by ISO. The original proposal for an ISMN was made by the
 * UK Branch of IAML (International Association of Music Libraries, Archives and Documentation
 * Centres).
 *
 * The original format comprised four elements: a distinguishing prefix M, a publisher ID,
 * an item ID and a check digit, typically looking like M-2306-7118-7.
 *
 * From 1 January 2008 the ISMN was defined as a thirteen digit identifier beginning 979-0 where
 * the zero replaced M in the old-style number. The resulting number is identical with its
 * EAN-13 number as encoded in the item's barcode.
 *
 * @see <a href="http://www.ismn-international.org/download/Web_ISMN%20Manual_2008-3.pdf">ISMN Manual 2008</a>
 */
public class ISMN implements Comparable<ISMN>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("[\\p{Digit}M\\-]{8,22}");

    private static final List<String> ranges = new ISMNRangeMessageConfigurator().getRanges();

    private String value;

    private String formatted;

    private boolean createWithChecksum;

    @Override
    public int compareTo(ISMN ismn) {
        return ismn != null ? normalizedValue().compareTo(ismn.normalizedValue()) : -1;
    }

    @Override
    public ISMN set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public ISMN checksum() {
        this.createWithChecksum = true;
        return this;
    }

    @Override
    public ISMN normalize() {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = value.substring(m.start(), m.end());
            this.value = (value.startsWith("979") ? "" : "979") + dehyphenate(value.replace('M', '0'));
        }
        return this;
    }

    @Override
    public ISMN verify() throws NumberFormatException {
        check();
        return this;
    }

    @Override
    public String normalizedValue() {
        return value;
    }

    @Override
    public String format() {
        if (formatted == null) {
            this.formatted = "979-0-" + fix(value.substring(4));
        }
        return formatted;
    }

    public GTIN toGTIN() throws NumberFormatException {
        return new GTIN().set(value).normalize().verify();
    }

    private void check() throws NumberFormatException {
        int l = createWithChecksum ? value.length() : value.length() - 1;
        int checksum = 0;
        int weight;
        int val;
        for (int i = 0; i < l; i++) {
            val = value.charAt(i) - '0';
            if (val < 0 || val > 9) {
                throw new NumberFormatException("not a digit: " + val );
            }
            weight = i % 2 == 0 ? 1 : 3;
            checksum += val * weight;
        }
        int chk = 10 - checksum % 10;
        if (createWithChecksum) {
            char ch = (char)('0' + chk);
            value = value + ch;
        }
        boolean valid = chk == value.charAt(l) - '0';
        if (!valid) {
            throw new NumberFormatException("invalid checksum: " + chk + " != " + value.charAt(l));
        }
    }

    private String hyphenate(String range, String ismn) {
        StringBuilder sb = new StringBuilder(ismn.substring(0, range.length()));
        sb.append('-');
        sb.append(ismn.substring(range.length(), ismn.length()-1));
        sb.append('-');
        sb.append(ismn.charAt(ismn.length() - 1));
        return sb.toString();
    }

    private String fix(String ismn) {
        if (ismn == null) {
            return null;
        }
        for (int i = 0; i < ranges.size(); i += 2) {
            if (isInRange(ismn, ranges.get(i), ranges.get(i + 1)) == 0) {
                return hyphenate(ranges.get(i), ismn);
            }
        }
        return ismn;
    }

    /**
     * Check if ISMN is within a given value range
     * @param ismn ISMN to check
     * @param begin lower
     * @param end  higher
     * @return -1 if too low, 1 if too high, 0 if range matches
     */
    private int isInRange(String ismn, String begin, String end) {
        String b = begin;
        int blen = b.length();
        int c = ismn.substring(0, blen).compareTo(b);
        if (c < 0) {
            return -1;
        }
        String e = end;
        int elen = e.length();
        c = e.compareTo(ismn.substring(0, elen));
        if (c < 0) {
            return 1;
        }
        return 0;
    }

    private String dehyphenate(String isbn) {
        StringBuilder sb = new StringBuilder(isbn);
        int i = sb.indexOf("-");
        while (i >= 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf("-");
        }
        return sb.toString();
    }

    private final static class ISMNRangeMessageConfigurator {

        private final Stack<StringBuilder> content;

        private final List<String> ranges;

        private String rangeBegin;

        private String rangeEnd;

        private boolean valid;

        public ISMNRangeMessageConfigurator() {
            content = new Stack<StringBuilder>();
            ranges = new ArrayList<String>();
            try {
                InputStream in = getClass().getResourceAsStream("/org/xbib/standardnumber/ISMNRangeMessage.xml");
                XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
                XMLEventReader xmlReader = xmlInputFactory.createXMLEventReader(in);
                while (xmlReader.hasNext()) {
                    processEvent(xmlReader.peek());
                    xmlReader.nextEvent();
                }
            } catch (XMLStreamException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        private void processEvent(XMLEvent e) {
            switch (e.getEventType()) {
                case XMLEvent.START_ELEMENT: {
                    StartElement element = e.asStartElement();
                    String name = element.getName().getLocalPart();
                    if ("RegistrationGroups".equals(name)) {
                        valid = true;
                    }
                    content.push(new StringBuilder());
                    break;
                }
                case XMLEvent.END_ELEMENT: {
                    EndElement element = e.asEndElement();
                    String name = element.getName().getLocalPart();
                    String v = content.pop().toString();
                    if (name.equals("Range")) {
                        int pos = v.indexOf('-');
                        if (pos > 0) {
                            rangeBegin = v.substring(0, pos);
                            rangeEnd = v.substring(pos + 1);
                        }

                    } else if (name.equals("Rule")) {
                        if (valid && rangeBegin != null && rangeEnd != null) {
                            ranges.add(rangeBegin);
                            ranges.add(rangeEnd);
                        }

                    }
                    break;
                }
                case XMLEvent.CHARACTERS: {
                    Characters c = (Characters) e;
                    if (!c.isIgnorableWhiteSpace()) {
                        String text = c.getData().trim();
                        if (text.length() > 0 && !content.empty()) {
                            content.peek().append(text);
                        }
                    }
                    break;
                }
            }
        }

        public List<String> getRanges() {
            return ranges;
        }
    }
}
