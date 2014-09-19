package org.xbib.marc;

import org.testng.annotations.Test;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;

import java.io.InputStream;

public class MarcXchange2KeyValueTest {

    private final Logger logger = LoggerFactory.getLogger(MarcXchange2KeyValueTest.class.getName());

    @Test
    public void testKeyValue() throws Exception {
        InputStream in = getClass().getResource("zdblokutf8.mrc").openStream();
        Iso2709Reader reader = new Iso2709Reader();
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(new KeyValueStreamAdapter<DataField, String>() {
                    @Override
                    public KeyValueStreamAdapter<DataField, String> begin() {
                        logger.debug("begin object");
                        return this;
                    }

                    @Override
                    public KeyValueStreamAdapter<DataField, String> keyValue(DataField fields, String value) {
                        logger.debug("begin");
                        for (Field f : fields) {
                            logger.debug("tag={} indicator={} subfield={} data={}",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data());
                        }
                        logger.debug("end");
                        return this;
                    }

                    @Override
                    public KeyValueStreamAdapter<DataField, String> end() {
                        logger.debug("end object");
                        return this;
                    }

                });
        reader.setMarcXchangeListener(kv);
        reader.parse(in);
        in.close();
    }
}
