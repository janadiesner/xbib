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
package org.xbib.tools.merge.zdb.entities;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.google.common.collect.Sets.newTreeSet;

public class TimeLine extends TreeSet<Manifestation> {

    private final static Logger logger = LoggerFactory.getLogger(TimeLine.class.getName());

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private Set<Integer> dates;

    private Integer firstDate;

    private Integer lastDate;

    private Set<Holding> holdings;

    private Set<License> licenses;

    private Map<Integer, Set<Holding>> holdingsByDate;

    private Map<Integer, Set<License>> licensesByDate;

    public TimeLine(Collection<Manifestation> manifestations) {
        super(Manifestation.getKeyComparator());
        addAll(manifestations);
        findExtremes();
    }

    public TimeLine(Collection<Manifestation> manifestations, Integer firstDate, Integer lastDate) {
        super(Manifestation.getKeyComparator());
        addAll(manifestations);
        this.firstDate = firstDate;
        this.lastDate = lastDate;
    }

    public Integer getFirstDate() {
        return firstDate;
    }

    public Integer getLastDate() {
        return lastDate;
    }

    private void findExtremes() {
        this.firstDate = Integer.MAX_VALUE;
        this.lastDate = Integer.MIN_VALUE;
        for (Manifestation m : this) {
            if (m.firstDate() == null) {
                continue;
            }
            if (m.firstDate() < this.firstDate) {
                this.firstDate = m.firstDate();
            }
            int d = m.lastDate() == null ? currentYear : m.lastDate();
            if (d > this.lastDate) {
                this.lastDate = d;
            }
            // we have to check all related manifestations for first/last dates
            for (Manifestation p : m.getRelatedManifestations().values()) {
                if (p.firstDate() != null && p.firstDate() < firstDate) {
                    this.firstDate = p.firstDate();
                }
                d = p.lastDate() == null ? currentYear : p.lastDate();
                if (d > this.lastDate) {
                    this.lastDate = d;
                }
            }
        }
        if (firstDate == Integer.MAX_VALUE) {
            // "Produkt-ISIL" z.B. 21543057
            firstDate = null;
            lastDate = null;
        } else if (lastDate == Integer.MIN_VALUE) {
            lastDate = currentYear;
        }
    }

    public void setHoldings(Set<Holding> holdings) {
        this.holdings = holdings;
        holdingsByDate = newTreeMap();
        for (Holding holding : holdings) {
            if (holding.isDeleted()) {
                continue;
            }
            List<Integer> dates = null;
            // check for generated dates
            Object o = holding.map().get("dates");
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
                }
                dates = newLinkedList();
                dates.addAll((List<Integer>)o);
            } else {
                // let's parse dates
                o = holding.map().get("FormattedEnumerationAndChronology");
                if (o != null) {
                    if (!(o instanceof List)) {
                        o = Arrays.asList(o);
                    }
                    dates = parseDates((List<Map<String, Object>>) o);
                } else {
                    o = holding.map().get("NormalizedHolding");
                    if (o != null) {
                        if (!(o instanceof List)) {
                            o = Arrays.asList(o);
                        }
                        dates = parseDates((List<Map<String, Object>>) o);
                    }
                }
            }
            if (dates == null || dates.isEmpty()) {
                continue;
            }
            Collection<Integer> invalid = newLinkedList();
            for (Integer date : dates) {
                if (((firstDate != null) && (date < firstDate)) ||
                     (lastDate != null && (date > lastDate))) {
                    invalid.add(date);
                    continue;
                }
                // there might be more holdings than one per date
                Set<Holding> set = holdingsByDate.get(date);
                if (set == null) {
                    set = newHashSet();
                }
                set.add(holding);
                holdingsByDate.put(date, set);
            }
            if (!invalid.isEmpty()) {
                logger.debug("dates {} in holdings for {} out of range {}-{}",
                        invalid, holding.parent(), firstDate, lastDate);
            }
        }
    }

    private List<Integer> parseDates(List<Map<String, Object>> groups) {
        List<Integer> begin = newLinkedList();
        List<Integer> end = newLinkedList();
        List<String> beginvolume = newLinkedList();
        List<String> endvolume = newLinkedList();
        List<Boolean> open = newLinkedList();
        for (Map<String, Object> m : groups) {
            Object o = m.get("movingwall");
            if (o != null) {
                logger.debug("movingwall detected: {}", o);
            }
            o = m.get("date");
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            for (String content : (List<String>) o) {
                if (content == null) {
                    continue;
                }
                EnumerationAndChronology.parse(content, begin, end, beginvolume, endvolume, open);
            }
        }
        List<Integer> dates = newLinkedList();
        for (int i = 0; i < begin.size(); i++) {
            if (open.get(i)) {
                end.set(i, currentYear);
            }
            if (begin.get(i) != null && end.get(i) != null) {
                for (int d = begin.get(i); d <= end.get(i); d++) {
                    dates.add(d);
                }
            } else if (begin.get(i) != null && begin.get(i) > 0) {
                dates.add(begin.get(i));
            }
        }
        return dates;
    }

    public void setLicensesAndIndicators(Set<License> licenses) {
        this.licenses = licenses;
        licensesByDate = newTreeMap();
        if (licenses == null) {
            return;
        }
        for (License license : licenses) {
            if (license.isDeleted()) {
                continue;
            }
            Collection<Integer> invalid = newHashSet();
            for (Integer date : license.dates()) {
                if (((firstDate != null) && (date < firstDate)) ||
                        (lastDate != null && (date > lastDate))) {
                    invalid.add(date);
                    continue;
                }
                Set<License> l = licensesByDate.get(date);
                if (l == null) {
                    l = newHashSet();
                }
                l.add(license);
                licensesByDate.put(date, l);
            }
            if (!invalid.isEmpty()) {
                logger.debug("dates {} in license for {} out of range {}-{}",
                        invalid, license.parent(), firstDate, lastDate);
            }
        }
    }

    /**
     * Iterate through all dates and make the available services.
     * Concatenate services of same institution.
     *
     */
    public void makeServices() {
        makeDates();
        for (Integer date : dates) {
            Map<String,List<Holding>> services = newHashMap();
            Set<Holding> holdings = holdingsByDate.get(date);
            if (holdings != null) {
                for (Holding holding : holdings) {
                    String isil = holding.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    List<Holding> list = services.get(isil);
                    if (list == null) {
                        list = newLinkedList();
                        services.put(isil, list);
                    }
                    list.add(holding);
                    for (Manifestation parent : holding.getManifestations()) {
                        parent.addVolume(date, holding);
                        // copy print holding over to online manifestation if available
                        Set<Manifestation> online = parent.getRelatedManifestations().get("hasOnlineEdition");
                        if (online != null) {
                            // almost sure we have only one online manifestation...
                            for (Manifestation m : ImmutableSet.copyOf(online)) {
                                m.addVolume(date, holding);
                            }
                        }
                    }
                }
            }
            Set<License> licenses = licensesByDate.get(date);
            if (licenses != null) {
                for (License license : licenses) {
                    String isil = license.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    List<Holding> list = services.get(isil);
                    if (list == null) {
                        list = newLinkedList();
                        services.put(isil, list);
                    }
                    list.add(license);
                    for (Manifestation parent : license.getManifestations()) {
                        parent.addVolume(date, license);
                        // copy online license over to print manifestation if available
                        Set<Manifestation> print = parent.getRelatedManifestations().get("hasPrintEdition");
                        if (print != null) {
                            for (Manifestation m : ImmutableSet.copyOf(print)) {
                                m.addVolume(date, license);
                            }
                        }
                    }
                }
            }
        }
    }

    public void build(String type, XContentBuilder builder) throws IOException {
        makeDates();
        Set<String> isils = makeISILs();
        Set<String> titles = makeTitles();
        Set<String> countries = makeCountries();
        Set<String> languages = makeLanguages();
        List<String> ids = collectIDs();
        builder.startObject()
                .field("@id", ids)
                .field("@type", type)
                .field("title", titles)
                .field("firstDate", firstDate)
                .field("lastDate", lastDate)
                .field("countryCount", countries.size())
                .field("country", countries)
                .field("languageCount", languages.size())
                .field("language", languages)
                .field("isilCount", isils.size())
                .field("isil", isils)
                .field("volumeCount", dates.size())
                .field("volume", dates)
                .field("timelineCount", size())
                .startArray("timeline");
        Set<String> visited = newHashSet();
        for (Manifestation m : this) {
            m.build(builder, visited);
        }
        builder.endArray();
        builder.field("manifestationCount", visited.size());
        builder.endObject();
    }

    private void makeDates() {
        if (dates != null) {
            return;
        }
        this.dates = newTreeSet();
        if (holdingsByDate != null) {
            dates.addAll(holdingsByDate.keySet());
        }
        if (licensesByDate != null) {
            dates.addAll(licensesByDate.keySet());
        }
    }

    private Set<String> makeTitles() {
        Set<String> set = newLinkedHashSet();
        for (Manifestation m : this) {
            set.add(m.title());
        }
        return set;
    }

    private Set<String> makeISILs() {
        Set<String> isils = newHashSet();
        if (holdings != null) {
            for (Holding holding : holdings) {
                if (holding.getISIL() != null) {
                    isils.add(holding.getISIL());
                }
            }
        }
        if (licenses != null) {
            for (License license : licenses) {
                if (license.getISIL() != null) {
                    isils.add(license.getISIL());
                }
            }
        }
        return isils;
    }

    private Set<String> makeCountries() {
        Set<String> set = newHashSet();
        for (Manifestation m : this) {
            set.addAll(m.country());
        }
        return set;
    }

    private Set<String> makeLanguages() {
        Set<String> set = newHashSet();
        for (Manifestation m : this) {
            set.add(m.language());
        }
        return set;
    }

    private List<String> collectIDs() {
        return this.stream().map(Manifestation::externalID).collect(Collectors.toList());
    }

}
