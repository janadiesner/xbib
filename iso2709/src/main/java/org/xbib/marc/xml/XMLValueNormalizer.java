package org.xbib.marc.xml;

import org.xbib.marc.ValueNormalizer;
import org.xbib.xml.XMLUtil;

import java.text.Normalizer;

public class XMLValueNormalizer implements ValueNormalizer {
    @Override
    public String normalize(String value) {
        return XMLUtil.clean(Normalizer.normalize(value, Normalizer.Form.NFC));
    }

}
