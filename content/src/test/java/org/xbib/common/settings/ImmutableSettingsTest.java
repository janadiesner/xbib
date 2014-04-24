package org.xbib.common.settings;

import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;

public class ImmutableSettingsTest {

    @Test
    public void testArray() {
        Settings settings = ImmutableSettings.settingsBuilder().putArray("input", Arrays.asList("a","b","c")).build();
        assertEquals("a", settings.getAsArray("input")[0]);
        assertEquals("b", settings.getAsArray("input")[1]);
        assertEquals("c", settings.getAsArray("input")[2]);
    }

}
