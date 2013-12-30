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

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newTreeSet;

public class Cluster extends TreeSet<Manifestation> {

    public Cluster(Collection<Manifestation> c) {
        super(c);
    }

    public List<TimeLine> timeLines() {
        return timeLines(this);
    }

    public List<TimeLine> timeLines(TreeSet<Manifestation> set) {
        LinkedList<Manifestation> manifestations = newLinkedList(set);
        List<TimeLine> timeLines = newLinkedList();
        while (!manifestations.isEmpty()) {
            Manifestation manifestation = manifestations.removeFirst();
            TreeSet<Manifestation> children = newTreeSet(TIME_COMPARATOR);
            children.add(manifestation);
            // make a time line
            makeTimeLine(manifestation, children);
            // split time line by country
            List<TimeLine> countries = splitIntoCountrySegments(new TimeLine(children));
            timeLines.addAll(countries);
            // find neighborhood
            makeNeighborTimeLines(manifestation, children);
            manifestations.removeAll(children);
        }
        // sort by first publication date
        Collections.sort(timeLines, TIME_LINE_COMPARATOR);
        return timeLines;
    }

    private void makeTimeLine(Manifestation manifestation, TreeSet<Manifestation> result)  {
        Set<Manifestation> set = newTreeSet();
        set.addAll(manifestation.getRelatedManifestations().get("precededBy"));
        set.addAll(manifestation.getRelatedManifestations().get("succeededBy"));
        set.removeAll(result);
        result.addAll(set);
        for (Manifestation m : set) {
            makeTimeLine(m, result);
        }
    }

    private void makeNeighborTimeLines(Manifestation manifestation, TreeSet<Manifestation> result) {
        SetMultimap<String,Manifestation> neighbors =
                ImmutableSetMultimap.copyOf(manifestation.getRelatedManifestations());
        Set<String> keys = neighbors.keySet();
        for (String relation : keys) {
            // check all "has" relations and make time lines of them
            if (relation.startsWith("has")) {
                Set<Manifestation> relatedNeighbors = neighbors.get(relation);
                for (Manifestation neighbor : relatedNeighbors) {
                    TreeSet<Manifestation> children = newTreeSet(TIME_COMPARATOR);
                    children.add(neighbor);
                    makeTimeLine(neighbor, children);
                    result.addAll(children);
                }
            }
        }
    }

    private List<TimeLine> splitIntoCountrySegments(TimeLine manifestations) {
        List<TimeLine> countrySegments = newLinkedList();
        Set<Manifestation> countrySegment = newTreeSet(TIME_COMPARATOR);
        Iterator<Manifestation> it = manifestations.iterator();
        Manifestation m = it.next();
        countrySegment.add(m);
        String country = m.country().toString();
        while (it.hasNext()) {
            m = it.next();
            if (!country.equals(m.country().toString())) {
                country = m.country().toString();
                countrySegments.add(new TimeLine(countrySegment,
                        manifestations.getFirstDate(), manifestations.getLastDate()));
                countrySegment = newTreeSet(TIME_COMPARATOR);
            }
            countrySegment.add(m);
        }
        countrySegments.add(new TimeLine(countrySegment,
                manifestations.getFirstDate(), manifestations.getLastDate()));
        return countrySegments;
    }

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private final static class TimeComparator implements Comparator<Manifestation> {

        @Override
        public int compare(Manifestation m1, Manifestation m2) {
            if (m1 == m2) {
                return 0;
            }
            Integer d1 = m1.firstDate() == null ? currentYear : m1.firstDate();
            Integer c1 = findCarrierTypeKey(m1);

            Integer d2 = m2.firstDate() == null ? currentYear : m2.firstDate();
            Integer c2 = findCarrierTypeKey(m2);

            String s1 = new StringBuilder()
                    .append(m1.country())
                    .append(Integer.toString(d1))
                    .append(Integer.toString(c1))
                    .append(m1.id())
                    .toString();

            String s2 = new StringBuilder()
                    .append(m2.country())
                    .append(Integer.toString(d2))
                    .append(Integer.toString(c2))
                    .append(m2.id())
                    .toString();

            return s2.compareTo(s1);
        }
    }

    private static Integer findCarrierTypeKey(Manifestation m) {
        switch (m.carrierType()) {
            case "online resource" : return 2;
            case "volume": return 1;
            case "computer disc" : return 4;
            case "computer tape cassette" : return 4;
            case "computer chip cartridge" : return 4;
            case "microform" : return 5;
            case "multicolored" : return 6;
            case "other" : return 6;
            default: throw new IllegalArgumentException("unknown carrier: " + m.carrierType() + " in " + m.externalID());
        }
    }

    private final static TimeComparator TIME_COMPARATOR = new TimeComparator();

    private final static class TimeLineComparator implements Comparator<Set<Manifestation>> {

        @Override
        public int compare(Set<Manifestation> set1, Set<Manifestation> set2) {
            if (set1 == set2) {
                return 0;
            }
            Manifestation f1 = set1.iterator().next();
            if (f1 == null || f1.firstDate() == null) {
                return -1;
            }
            for (Manifestation m : set1) {
                if (m != null && m.firstDate() != null) {
                    if (m.firstDate() < f1.firstDate()) {
                        f1 = m;
                    }
                }
            }
            Manifestation f2 = set2.iterator().next();
            if (f2 == null || f2.firstDate() == null) {
                return 1;
            }
            for (Manifestation m : set2) {
                if (m != null && m.firstDate() != null) {
                    if (m.firstDate() < f2.firstDate()) {
                        f2 = m;
                    }
                }
            }

            Integer d1 = f1.firstDate() == null ? currentYear : f1.firstDate();
            Integer c1 = findCarrierTypeKey(f1);

            Integer d2 = f2.firstDate() == null ? currentYear : f2.firstDate();
            Integer c2 = findCarrierTypeKey(f2);

            String s1 = new StringBuilder()
                    .append(Integer.toString(d1))
                    .append(Integer.toString(c1))
                    .append(f1.id())
                    .toString();

            String s2 = new StringBuilder()
                    .append(Integer.toString(d2))
                    .append(Integer.toString(c2))
                    .append(f2.id())
                    .toString();

            return s1.compareTo(s2);
        }
    }

    private final static TimeLineComparator TIME_LINE_COMPARATOR = new TimeLineComparator();

}