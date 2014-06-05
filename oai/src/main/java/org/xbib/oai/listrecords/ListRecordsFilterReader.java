package org.xbib.oai.listrecords;

import org.xbib.oai.OAIConstants;
import org.xbib.oai.util.RecordHeader;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.oai.xml.MetadataHandler;
import org.xbib.util.DateUtil;
import org.xbib.xml.XMLFilterReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ListRecordsFilterReader extends XMLFilterReader {

    private final ListRecordsRequest request;

    private final ListRecordsResponse response;

    private StringBuilder content;

    private RecordHeader header;

    private ResumptionToken token;

    private boolean inMetadata;

    ListRecordsFilterReader(ListRecordsRequest request, ListRecordsResponse response) {
        super();
        this.request = request;
        this.response = response;
        this.content = new StringBuilder();
        this.inMetadata = false;
    }

    public ResumptionToken getResumptionToken() {
        return token;
    }

    public ListRecordsResponse getResponse() {
        return response;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        request.setResumptionToken(null);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localname, String qname, Attributes atts) throws SAXException {
        super.startElement(uri, localname, qname, atts);
        if (OAIConstants.NS_URI.equals(uri)) {
            if (localname.equals("header")) {
                header = new RecordHeader();
            } else if (localname.equals("error")) {
                response.setError(atts.getValue("code"));
            } else if (localname.equals("metadata")) {
                inMetadata = true;
                for (MetadataHandler mh : request.getHandlers()) {
                    mh.startDocument();
                }
            } else if (localname.equals("resumptionToken")) {
                try {
                    token = ResumptionToken.newToken(null);
                    String cursor = atts.getValue("cursor");
                    if (cursor != null) {
                        token.setCursor(Integer.parseInt(cursor));
                    }
                    String completeListSize = atts.getValue("completeListSize");
                    if (completeListSize != null) {
                        token.setCompleteListSize(Integer.parseInt(completeListSize));
                    }
                    if (!token.isComplete()) {
                        request.setResumptionToken(token);
                    }
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            }
            return;
        }
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.startElement(uri, localname, qname, atts);
            }
        }
    }

    @Override
    public void endElement(String nsURI, String localname, String qname) throws SAXException {
        super.endElement(nsURI, localname, qname);
        if (OAIConstants.NS_URI.equals(nsURI)) {
            if (localname.equals("header")) {
                for (MetadataHandler mh : request.getHandlers()) {
                    mh.setHeader(header);
                }
                header = new RecordHeader();
            } else if (localname.equals("metadata")) {
                for (MetadataHandler mh : request.getHandlers()) {
                    mh.endDocument();
                }
                inMetadata = false;
            } else if (localname.equals("responseDate")) {
                response.setDate(DateUtil.parseDateISO(content.toString()));
            } else if (localname.equals("resumptionToken")) {
                if (token != null && content != null && content.length() > 0) {
                    token.setValue(content.toString());
                    // feedback to request
                    request.setResumptionToken(token);
                } else {
                    // some servers send a null or an empty token as last token
                    token = null;
                    request.setResumptionToken(null);
                }
            } else if (localname.equals("identifier")) {
                if (header != null && content != null && content.length() > 0) {
                    String id = content.toString().trim();
                    header.setIdentifier(id);
                }
            } else if (localname.equals("datestamp")) {
                if (header != null && content != null && content.length() > 0) {
                    header.setDatestamp(DateUtil.parseDateISO(content.toString().trim()));
                }
            } else if (localname.equals("setSpec")) {
                if (header != null && content != null && content.length() > 0) {
                    header.setSetspec(content.toString().trim());
                }
            }
            content.setLength(0);
            return;
        }
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.endElement(nsURI, localname, qname);
            }
        }
        content.setLength(0);
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        super.characters(chars, start, length);
        content.append(new String(chars, start, length).trim());
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.characters(chars, start, length);
            }
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.startPrefixMapping(prefix, uri);
            }
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        super.endPrefixMapping(prefix);
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.endPrefixMapping(prefix);
            }
        }
    }

}