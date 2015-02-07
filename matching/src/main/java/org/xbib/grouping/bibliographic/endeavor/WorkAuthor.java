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
package org.xbib.grouping.bibliographic.endeavor;

import org.xbib.strings.encode.BaseformEncoder;
import org.xbib.strings.encode.EncoderException;
import org.xbib.strings.encode.WordBoundaryEntropyEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An identifiable endeavor for a work created by an author
 *
 */
public class WorkAuthor implements IdentifiableEndeavor {

    private StringBuilder workName;

    private StringBuilder authorName;

    private final WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();

    /* These work titles can not be work titles */
    private final static Set<String> blacklist = readResource("org/xbib/grouping/bibliographic/endeavor/work-blacklist.txt");

    public WorkAuthor() {
    }

    public WorkAuthor workName(CharSequence workName) {
        if (workName != null) {
            this.workName = new StringBuilder(workName);
        }
        return this;
    }

    public WorkAuthor authorName(CharSequence authorName) {
        if (authorName != null) {
            if (this.authorName == null) {
                this.authorName = new StringBuilder(authorName);
            } else {
                this.authorName.append(authorName);
            }
        }
        return this;
    }

    public String createIdentifier() {
        if (workName == null) {
            return null;
        }
        if (blacklisted(workName)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("wa");
        String wName = BaseformEncoder.normalizedFromUTF8(workName.toString())
                .replaceAll("aeiou", "");
        try {
            wName = encoder.encode(wName);
        } catch (EncoderException e) {
            // ignore
        }
        sb.append(wName);
        if (authorName != null) {
            String aName = BaseformEncoder.normalizedFromUTF8(authorName.toString())
                    .replaceAll("aeiou", "");
            try {
                aName = encoder.encode(aName);
            } catch (EncoderException e) {
                //ignore
            }
            sb.append(aName);
        }
        return sb.toString();
    }

    private final static Pattern p1 = Pattern.compile(".*Cover and Back matter.*", Pattern.CASE_INSENSITIVE);

    public Set<String> blacklist() {
        return blacklist;
    }

    protected boolean blacklisted(CharSequence work) {
        return blacklist.contains(work.toString()) || p1.matcher(work).matches();
    }

    private static Set<String> readResource(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        Set<String> set = new HashSet<String>();
        if (url != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                reader.lines().filter(line -> !line.startsWith("#")).forEach(set::add);
            } catch (IOException e) {
                // do nothing
            }
        }
        return set;
    }
}
