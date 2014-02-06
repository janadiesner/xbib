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
package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;

public class TimeLine extends TreeSet<Manifestation> {

    private final static Logger logger = LoggerFactory.getLogger(TimeLine.class.getSimpleName());

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private Set<Integer> dates;

    private Integer firstDate;

    private Integer lastDate;

    private Set<Holding> holdings;

    private Set<License> licenses;

    private Set<Indicator> indicators;

    private Map<Integer, Set<Holding>> holdingsByDate;

    private Map<Integer, Set<License>> licensesByDate;

    private Map<Integer, Set<Indicator>> indicatorsByDate;

    private Map<Integer,Map<String,List<Holding>>> servicesByDate;

    public TimeLine(Collection<Manifestation> manifestations) {
        super(manifestations);
        findExtremes();
    }

    public TimeLine(Collection<Manifestation> manifestations, Integer firstDate, Integer lastDate) {
        super(manifestations);
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
        if (lastDate == Integer.MIN_VALUE) {
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
                if (date < firstDate || date > lastDate) {
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
                logger.warn("dates {} in holdings for {} out of range {}-{}",
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
                logger.warn("movingwall detected: {}", o);
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

    public void setLicenses(Set<License> licenses) {
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
                if (date < firstDate || date > lastDate) {
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
                logger.warn("dates {} in license for {} out of range {}-{}",
                        invalid, license.parent(), firstDate, lastDate);
            }
        }
    }

    public void setIndicators(Set<Indicator> indicators) {
        this.indicators = indicators;
        indicatorsByDate = newTreeMap();
        if (indicators == null) {
            return;
        }
        for (Indicator indicator : indicators) {
            if (indicator.isDeleted()) {
                continue;
            }
            Collection<Integer> invalid = newLinkedList();
            for (Integer date : indicator.dates()) {
                if (date < firstDate || date > lastDate) {
                    invalid.add(date);
                    continue;
                }
                Set<Indicator> l = indicatorsByDate.get(date);
                if (l == null) {
                    l = newHashSet();
                }
                l.add(indicator);
                indicatorsByDate.put(date, l);
            }
            if (!invalid.isEmpty()) {
                logger.warn("dates {} in indicators for {} out of range {}-{}",
                        invalid, indicator.parent(), firstDate, lastDate);
            }
        }
    }

    public void makeDates() {
        this.dates = newTreeSet();
        dates.addAll(holdingsByDate.keySet());
        dates.addAll(licensesByDate.keySet());
        dates.addAll(indicatorsByDate.keySet());
    }

    /**
     * Iterate through all dates and get the available services.
     *
     * Overlay services if required
     *
     */
    public void makeServices() {
        if (dates == null) {
            return;
        }
        servicesByDate = newTreeMap();
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
                    holding.getManifestation().addEvidenceByDate(date, holding);
                    Set<Manifestation> online = holding.getManifestation().getRelatedManifestations().get("hasOnlineEdition");
                    if (online != null) {
                        for (Manifestation o : online) {
                            o.addEvidenceByDate(date, holding);
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
                    license.getManifestation().addEvidenceByDate(date, license);
                    Set<Manifestation> print = license.getManifestation().getRelatedManifestations().get("hasPrintEdition");
                    if (print != null) {
                        for (Manifestation p : print) {
                            p.addEvidenceByDate(date, license);
                        }
                    }
                }
            }
            Set<Indicator> indicators = indicatorsByDate.get(date);
            if (indicators != null) {
                for (Indicator indicator : indicators) {
                    String isil = indicator.getISIL();
                    if (isil == null) {
                        continue;
                    }
                    List<Holding> list = services.get(isil);
                    if (list == null) {
                        list = newLinkedList();
                        services.put(isil, list);
                    }
                    list.add(indicator);
                    indicator.getManifestation().addEvidenceByDate(date, indicator);
                    Set<Manifestation> print = indicator.getManifestation().getRelatedManifestations().get("hasPrintEdition");
                    if (print != null) {
                        for (Manifestation p : print) {
                            p.addEvidenceByDate(date, indicator);
                        }
                    }
                }
            }
            servicesByDate.put(date, services);
        }
    }

    public Map<Integer,Map<String,List<Holding>>> getServicesByDate() {
        return servicesByDate;
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
        if (indicators != null) {
            for (Indicator indicator : indicators) {
                if (indicator.getISIL() != null) {
                    isils.add(indicator.getISIL());
                }
            }
        }
        return isils;
    }

    public void build(XContentBuilder builder) throws IOException {
        makeDates();
        Set<String> isils = makeISILs();
        builder.startObject()
                .field("id", first().externalID())
                .field("title", first().title())
                .field("firstDate", firstDate)
                .field("lastDate", lastDate)
                .field("country", first().country())
                .field("isilCount", isils.size())
                .field("hasISIL", isils);
        builder.field("timelineSize", this.size())
                .startArray("timeline");
        for (Manifestation m : this) {
            m.buildGroup(builder);
        }
        builder.endArray();
        builder.endObject();
    }

}
