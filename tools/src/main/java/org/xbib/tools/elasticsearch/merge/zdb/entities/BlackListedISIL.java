package org.xbib.tools.elasticsearch.merge.zdb.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class BlackListedISIL {

    public final static String DEFAULT_BLACKLIST_ISIL = "isil.blacklist";

    private Set<String> lookup = newHashSet();

    public void buildLookup(InputStream in) throws IOException {
        if (in == null) {
            return;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = r.readLine()) != null) {
            lookup.add(line);
        }
        r.close();
    }

    public Set<String> lookup() {
        return lookup;
    }
}
