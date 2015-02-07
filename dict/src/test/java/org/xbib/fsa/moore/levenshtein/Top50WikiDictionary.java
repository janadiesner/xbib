package org.xbib.fsa.moore.levenshtein;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class Top50WikiDictionary {
    
    private List<CharSequence> input = new ArrayList<CharSequence>();
    
    public Iterator<CharSequence> getWordsIterator() {
        try {
            return readTop50KWiki();
        } catch (Exception e) {
            return null;
        }
    }

    private Iterator<CharSequence> readTop50KWiki() throws Exception {
        URL resource = getClass().getResource("Top50KWiki.utf8");
        assert resource != null : "resource missing: Top50KWiki.utf8";
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.openStream(), "utf-8"));
        while ((line = br.readLine()) != null) {
            int tab = line.indexOf('|');
            assertTrue(tab > 0);
            String key = line.substring(0, tab);
            input.add(key);
        }
        br.close();
        return input.iterator();
    }
    
}
