package org.xbib.template.handlebars.i237;

import org.junit.Test;
import org.xbib.template.handlebars.AbstractTest;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Issue237 extends AbstractTest {

    public enum Status {
        NEW, DONE, CLOSED
    }

    @Test
    public void workForEnumMap() throws IOException {
        Map<Status, Integer> statuses = new EnumMap<Status, Integer>(Status.class);
        statuses.put(Status.NEW, 10);
        statuses.put(Status.DONE, 20);
        statuses.put(Status.CLOSED, 3);
        shouldCompileTo("{{statuses.NEW}}", $("statuses", statuses), "10");
    }

    @Test
    public void wontWorkForNormalMap() throws IOException {
        Map<Status, Integer> statuses = new HashMap<Status, Integer>();
        statuses.put(Status.NEW, 10);
        statuses.put(Status.DONE, 20);
        statuses.put(Status.CLOSED, 3);
        shouldCompileTo("{{statuses.NEW}}", $("statuses", statuses), "");
    }
}
