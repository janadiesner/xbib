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

import com.google.common.collect.SetMultimap;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;

public class Cluster extends TreeSet<Manifestation> {

    private final boolean isNewspaper;

    private final boolean isDatabase;

    private final boolean isWebsite;

    private Set<TimeLine> timeLines;

    public Cluster(Collection<Manifestation> c) {
        super(c);
        this.isNewspaper = c.stream().anyMatch(Manifestation::isNewspaper);
        this.isDatabase = c.stream().anyMatch(Manifestation::isDatabase);
        this.isWebsite = c.stream().anyMatch(Manifestation::isWebsite);
    }

    public boolean isNewspaper() {
        return isNewspaper;
    }

    public boolean isDatabase() {
        return isDatabase;
    }

    public boolean isWebsite() {
        return isWebsite;
    }

    public Set<TimeLine> timeLines() {
        if (this.timeLines == null) {
            this.timeLines = makeTimeLines();
        }
        return timeLines;
    }

    public Set<Manifestation> notInTimeLines() {
        if (timeLines == null) {
            return null;
        }
        // concatenate all timelines. Slow.
        List<Manifestation> list = newLinkedList();
        timeLines.forEach(list::addAll);
        Set<Manifestation> candidates = newHashSet(this);
        candidates.removeAll(list);
        return candidates;
    }

    private Set<TimeLine> makeTimeLines() {
        LinkedList<Manifestation> manifestations = newLinkedList(this);
        // sort by first publication date
        Set<TimeLine> timeLines = newTreeSet(TimeLine.getTimelineComparator());
        while (!manifestations.isEmpty()) {
            Manifestation manifestation = manifestations.removeFirst();
            Set<Manifestation> timeline = newTreeSet(Manifestation.getTimeComparator());
            timeline.add(manifestation);
            // make a time line
            makeTimeLine(manifestation, timeline);
            // add neighborhood to timeline
            addNeighborTimeLines(manifestation, timeline);
            // split time line by country marker (only print)
            List<TimeLine> countries = splitIntoCountrySegments(new TimeLine(timeline));
            timeLines.addAll(countries);
            manifestations.removeAll(timeline);
        }
        return timeLines;
    }

    private final static String[] timelineRelations = new String[] {
            "precededBy",
            "succeededBy",
            "hasPrintEdition",
            "hasOnlineEdition"
    };

    private void makeTimeLine(Manifestation manifestation, Set<Manifestation> result)  {
        SetMultimap<String,Manifestation> neighbors = manifestation.getRelatedManifestations();
        Set<Manifestation> set = newTreeSet();
        for (String relation : timelineRelations) {
            set.addAll(neighbors.get(relation));
        }
        set.removeAll(result);
        result.addAll(set);
        for (Manifestation m : set) {
            makeTimeLine(m, result);
        }
    }

    private void addNeighborTimeLines(Manifestation manifestation, Set<Manifestation> result) {
        SetMultimap<String,Manifestation> neighbors = manifestation.getRelatedManifestations();
        // check all outgoing "has..." relations and make time lines of them
        neighbors.keySet().stream().filter(relation -> relation.startsWith("has")).forEach(relation -> {
            Set<Manifestation> relatedNeighbors = neighbors.get(relation);
            for (Manifestation neighbor : relatedNeighbors) {
                TreeSet<Manifestation> children = newTreeSet(Manifestation.getTimeComparator());
                children.add(neighbor);
                makeTimeLine(neighbor, children);
                result.addAll(children);
            }
        });
    }

    private List<TimeLine> splitIntoCountrySegments(TimeLine manifestations) {
        List<TimeLine> countrySegments = newLinkedList();
        Set<Manifestation> countrySegment = newTreeSet(Manifestation.getTimeComparator());
        Iterator<Manifestation> it = manifestations.iterator();
        Manifestation m = it.next();
        countrySegment.add(m);
        String country = m.country().toString();
        while (it.hasNext()) {
            String carrier = m.carrierType();
            m = it.next();
            if (carrier.equals(m.carrierType()) && !country.equals(m.country().toString())) {
                // create new country segment
                country = m.country().toString();
                countrySegments.add(new TimeLine(countrySegment,
                        manifestations.getFirstDate(), manifestations.getLastDate()));
                countrySegment = newTreeSet(Manifestation.getTimeComparator());
            }
            countrySegment.add(m);
        }
        countrySegments.add(new TimeLine(countrySegment,
                manifestations.getFirstDate(), manifestations.getLastDate()));
        return countrySegments;
    }


    /*private final static class TimeLineComparator implements Comparator<Set<Manifestation>> {

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
            Integer c1 = f1.findCarrierTypeKey();
            String s1 = new StringBuilder()
                    .append(Integer.toString(d1))
                    .append(Integer.toString(c1))
                    .append(f1.id())
                    .toString();

            Integer d2 = f2.firstDate() == null ? currentYear : f2.firstDate();
            Integer c2 = f2.findCarrierTypeKey();
            String s2 = new StringBuilder()
                    .append(Integer.toString(d2))
                    .append(Integer.toString(c2))
                    .append(f2.id())
                    .toString();

            return s1.compareTo(s2);
        }
    }

    private final static TimeLineComparator TIMELINE_COMPARATOR = new TimeLineComparator();*/

}
