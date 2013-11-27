package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

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
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;

public class Cluster extends TreeSet<Manifestation> {

    private final static Logger logger = LoggerFactory.getLogger(Cluster.class.getName());

    public Cluster(Collection<Manifestation> c) {
        super(c);
    }

    public List<Set<Manifestation>> chronoStreams() {
        return chronoStreams(this);
    }

    public List<Set<Manifestation>> chronoStreams(TreeSet<Manifestation> set) {
        logger.info("input to chronostreaming {}", set);
        LinkedList<Manifestation> manifestations = newLinkedList(set);
        List<Set<Manifestation>> chronoStreams = newLinkedList();
        while (!manifestations.isEmpty()) {
            Manifestation manifestation = manifestations.removeFirst();
            logger.info("chronostreaming {}", manifestation.externalID());

            // chronological
            TreeSet<Manifestation> children = newTreeSet(chronoComparator);
            children.add(manifestation);
            makeChronoStream(manifestation, children);

            //chronoStreams.add(children);
            // by country
            List<Set<Manifestation>> countries = splitIntoCountrySegments(children);
            logger.info("after chronostreaming {} ---> {}: {}", manifestation, children, countries);
            chronoStreams.addAll(countries);

            // neighborhood
            makeNeighborStreams(manifestation, children);

            manifestations.removeAll(children);
        }
        // sort by first publication date
        Collections.sort(chronoStreams, chronoStreamComparator);
        return chronoStreams;
    }

    public Integer findFirstDate(Collection<Manifestation> manifestations) {
        // search for manifestatation of first publication date
        Manifestation m1 = manifestations.iterator().next();
        for (Manifestation m : manifestations) {
            if (m.firstDate() < m1.firstDate()) {
                m1 = m;
            }
        }
        return m1.firstDate();
    }

    public Integer findLastDate(Collection<Manifestation> manifestations) {
        // search for manifestatation of first publication date
        Manifestation m1 = manifestations.iterator().next();
        for (Manifestation m : manifestations) {
            if (m.lastDate() > m1.lastDate()) {
                m1 = m;
            }
        }
        return m1.lastDate();
    }

    private void makeChronoStream(Manifestation manifestation, TreeSet<Manifestation> result)  {
        Set<Manifestation> set = newTreeSet();
        set.addAll(manifestation.getRelatedManifestations().get("precededBy"));
        set.addAll(manifestation.getRelatedManifestations().get("succeededBy"));
        set.removeAll(result);
        result.addAll(set);
        for (Manifestation m : set) {
            makeChronoStream(m, result);
        }
    }

    private void makeNeighborStreams(Manifestation manifestation, TreeSet<Manifestation> result) {
        SetMultimap<String,Manifestation> neighbors =
                ImmutableSetMultimap.copyOf(manifestation.getRelatedManifestations());
        Set<String> keys = neighbors.keySet();
        for (String relation : keys) {
            // only "has" relations
            if (relation.startsWith("has")) {
                Set<Manifestation> relatedNeighbors = neighbors.get(relation);
                for (Manifestation neighbor : relatedNeighbors) {
                    TreeSet<Manifestation> children = newTreeSet(chronoComparator);
                    children.add(neighbor);
                    makeChronoStream(neighbor, children);
                    result.addAll(children);
                }
            }
        }
    }

    private List<Set<Manifestation>> splitIntoCountrySegments(TreeSet<Manifestation> manifestations) {
        List<Set<Manifestation>> countrySegments = newLinkedList();
        Set<Manifestation> countrySegment = newTreeSet(chronoComparator);
        Iterator<Manifestation> it = manifestations.iterator();
        Manifestation m = it.next();
        countrySegment.add(m);
        String country = m.country().toString();
        while (it.hasNext()) {
            m = it.next();
            if (!country.equals(m.country().toString())) {
                country = m.country().toString();
                countrySegments.add(countrySegment);
                countrySegment = newTreeSet(chronoComparator);
            }
            countrySegment.add(m);
        }
        countrySegments.add(countrySegment);
        return countrySegments;
    }

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private final static class ChronoComparator implements Comparator<Manifestation> {

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

            if ("31203".equals(m1.id()) || "31203".equals(m2.id())) {
                logger.info("compare {} {} {} {}", m1, s1, m2, s2);
            }

            return s2.compareTo(s1);
        }
    }

    private static Integer findCarrierTypeKey(Manifestation m) {
        switch (m.carrierType()) {
            case "volume": return 1;
            case "online resource" : return 2;
            case "computer disc" : return 3;
            case "microform" : return 4;
            case "other" : return 5;
            default: throw new IllegalArgumentException("unknown carrier: " + m.carrierType() + " in " + m.externalID());
        }
    }

    private final static ChronoComparator chronoComparator = new ChronoComparator();


    private final static class ChronoStreamComparator implements Comparator<Set<Manifestation>> {

        @Override
        public int compare(Set<Manifestation> set1, Set<Manifestation> set2) {
            if (set1 == set2) {
                return 0;
            }
            // search for manifestatation of first publication date
            Manifestation f1 = set1.iterator().next();
            for (Manifestation m : set1) {
                if (m.firstDate() < f1.firstDate()) {
                    f1 = m;
                }
            }
            Manifestation f2 = set2.iterator().next();
            for (Manifestation m : set2) {
                if (m.firstDate() < f2.firstDate()) {
                    f2 = m;
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

    private final static ChronoStreamComparator chronoStreamComparator = new ChronoStreamComparator();

    private final static class CarrierComparator implements Comparator<Manifestation> {
        @Override
        public int compare(Manifestation m1, Manifestation m2) {
            if (m1 == m2) {
                return 0;
            }
            Integer i1 = findCarrierTypeKey(m1);
            Integer i2 = findCarrierTypeKey(m2);
            return i1.compareTo(i2);
        }
    }

    private final static CarrierComparator carrierComparator = new CarrierComparator();

}
