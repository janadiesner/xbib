/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.marc;

import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class MABMapperTest {

    @Test
    public void testMappedDE468() throws IOException, TransformerException, SAXException, ParserConfigurationException {
        InputStream in = getClass().getResource("/org/xbib/marc/aleph500-subfields.mrc").openStream();
        FileOutputStream out = new FileOutputStream("/var/tmp/DE-468-mapped.xml");
        Writer target = new OutputStreamWriter(out, "UTF-8");
        InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
        execute(source, target);
        target.flush();
        target.close();
    }

    private void execute(InputSource source, Writer w) throws IOException, SAXException,
            ParserConfigurationException, TransformerException {
        Iso2709Reader reader = new Iso2709Reader();
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.SUBFIELD_DELIMITER, "$$"); // will be automatically quoted before used as pattern

        // 902$ $ 9 -> 689$00$a0
        Map<String,Object> subfields = new HashMap();
        subfields.put("", "689$0{r}");
        subfields.put(" ", "689$0{r}$a");
        subfields.put("9", "689$0{r}$0");
        Map<String,Object> indicators = new HashMap();
        indicators.put(" ", subfields);
        Map<String,Object> fields = new HashMap();
        fields.put("902", indicators);
        reader.setFieldMap(fields);

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
    }
}
