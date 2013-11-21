package org.xbib.standardnumber;

import java.net.URI;

/**
 * ORCID
 *
 * ORCID is comptabible to International Standard Name Identifier (ISNI)  ISO 2772
 *
 * Checksum is on accordance to ISO/IEC 7064:2003, MOD 11-2
 */
public class ORCID extends ISNI {

    public ORCID setValue(String value) {
        super.setValue(value);
        return this;
    }

    public ORCID checksum() {
        super.checksum();
        return this;
    }

    public ORCID parse() {
        super.parse();
        return this;
    }

    public ORCID verify() throws NumberFormatException {
        super.verify();
        return this;
    }

    public URI toURI() {
        return URI.create("http://orcid.org/" + getValue());
    }

}
