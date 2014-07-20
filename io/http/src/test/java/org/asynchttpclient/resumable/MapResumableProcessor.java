package org.asynchttpclient.resumable;

import org.asynchttpclient.resumable.ResumableAsyncHandler.ResumableProcessor;

import java.util.HashMap;
import java.util.Map;

public class MapResumableProcessor
        implements ResumableProcessor {

    Map<String, Long> map = new HashMap<String, Long>();

    public void put(String key, long transferredBytes) {
        map.put(key, transferredBytes);
    }

    public void remove(String key) {
        map.remove(key);
    }

    public void save(Map<String, Long> map) {
    }

    public Map<String, Long> load() {
        return map;
    }
}