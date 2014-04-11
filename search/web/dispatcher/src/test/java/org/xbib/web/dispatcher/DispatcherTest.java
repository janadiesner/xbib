
package org.xbib.web.dispatcher;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;
import org.xbib.io.InputService;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DispatcherTest {

    private final Logger logger = LoggerFactory.getLogger(DispatcherTest.class.getName());

    @Test
    public void testDispatcher() throws IOException {
        String json = InputService.asString(getClass().getResourceAsStream("/test2.json"), "UTF-8");

        Map<String,String> map = new HashMap<String,String>();
        map.put("NRW", "HBZ");
        map.put("HAM", "VZG");
        map.put("NIE", "VZG");
        map.put("SAA", "VZG");
        map.put("THU", "VZG");
        map.put("BAW", "BSZ");
        map.put("SAX", "BSZ");
        map.put("BAY", "BVB");
        map.put("HES", "HEBIS");
        map.put("BER", "ZIB");

        Dispatcher dispatcher = new Dispatcher()
               // .setCompact(true)
                .setBase("DE-384")
                //.setBaseGroup("BAY")
                //.setGroupLimit(5)
                .setGroupMap(map);
                //.setGroupFilter(Arrays.asList("NRW"));
                //.setInstitutionMarker("pilot", Arrays.asList("DE-6", "DE-38", "DE-61", "DE-361", "DE-386", "DE-465", "DE-1010"))
                //.setTypeFilter(Arrays.asList("interlibrary"))
                //.setModeFilter(Arrays.asList("copy", "copy-loan"));

        // just test the ordering and formatting

        logger.info("{}", dispatcher.execute(json));

    }
}