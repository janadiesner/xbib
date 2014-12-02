
package org.xbib.web.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.io.InputService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HebisDispatcherTest {

    private final static Logger logger = LogManager.getLogger(HebisDispatcherTest.class.getName());

    @Test
    public void testHebisDispatcher() throws IOException {
        String json = InputService.asString(getClass().getResourceAsStream("/14134238.json"), "UTF-8");

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

        //List<String> priority = Arrays.asList("DE-6", "DE-38", "DE-61", "DE-82", "DE-361", "DE-386", "DE-465", "DE-1010");

        DispatcherRequest dispatcherRequest = new DispatcherRequest()
                .setSource("zdb")
                .setIdentifier("14134238")
                .setYear(2012)
               // .setCompact(true)
                //.setBase("DE-384")
                .setBaseGroup("HEBIS")
                .setExpandGroups(true)
                .setGroupLimit(10)
                .setGroupMap(map);
                //.setGroupFilter(Arrays.asList("NRW"))
                //.setTypeFilter(Arrays.asList("interlibrary"))
                //.setModeFilter(Arrays.asList("copy", "copy-loan"));
                //.setInstitutionMarker("priority", priority);


        // just test the ordering and formatting

        Dispatcher dispatcher = new Dispatcher();
        logger.info("{}", dispatcher.execute(dispatcherRequest, json));
    }
}