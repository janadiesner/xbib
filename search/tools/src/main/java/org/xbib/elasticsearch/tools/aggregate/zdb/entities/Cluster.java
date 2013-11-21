package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
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

    public List<Set<Manifestation>> chronoStreams() {
        LinkedList<Manifestation> manifestations = newLinkedList(this);
        List<Set<Manifestation>> chronoStreams = newLinkedList();
        while (!manifestations.isEmpty()) {
            Manifestation manifestation = manifestations.removeFirst();

            // find other near editions, remove them from the chrono stream making
            manifestations.removeAll(manifestation.getRelatedManifestations().get("hasOnlineEdition"));
            manifestations.removeAll(manifestation.getRelatedManifestations().get("hasPrintEdition"));
            manifestations.removeAll(manifestation.getRelatedManifestations().get("hasPart"));
            manifestations.removeAll(manifestation.getRelatedManifestations().get("hasSupplement"));

            TreeSet<Manifestation> chrono = newTreeSet(chronoComparator);
            chrono.add(manifestation);
            makeChronoStream(manifestation, chrono);
            chronoStreams.add(chrono);
            manifestations.removeAll(chrono);
        }
        // sort by first publication date
        Collections.sort(chronoStreams, chronoStreamComparator);
        return chronoStreams;
    }

    private void makeChronoStream(Manifestation manifestation, TreeSet<Manifestation> chrono)  {
        Set<Manifestation> s1 = manifestation.getRelatedManifestations().get("precededBy");
        Set<Manifestation> s2 = manifestation.getRelatedManifestations().get("succeededBy");
        s1.addAll(s2);
        s1.removeAll(chrono);
        chrono.addAll(s1);
        for (Manifestation m : new LinkedList<Manifestation>(s1)) {
            makeChronoStream(m, chrono);
        }
    }

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private final static class ChronoComparator implements Comparator<Manifestation> {

        @Override
        public int compare(Manifestation m1, Manifestation m2) {
            if (m1 == m2) {
                return 0;
            }
            Integer d1 = m1.firstDate() == null ? currentYear : m1.firstDate();
            Integer e1 = m1.lastDate() == null ? currentYear : m1.lastDate();
            Integer c1 = "volume".equals(m1.carrierType()) ? 1 :
                    "online resource".equals(m1.carrierType()) ? 2 : 3;

            Integer d2 = m2.firstDate() == null ? currentYear : m2.firstDate();
            Integer e2 = m2.lastDate() == null ? currentYear : m2.lastDate();
            Integer c2 = "volume".equals(m2.carrierType()) ? 1 :
                    "online resource".equals(m2.carrierType()) ? 2 : 3;


            String s1 = new StringBuilder()
                    .append(m1.country())
                    .append(Integer.toString(e1))
                    .append(Integer.toString(d1))
                    .append(Integer.toString(c1))
                    .append(m1.id())
                    .toString();

            String s2 = new StringBuilder()
                    .append(m2.country())
                    .append(Integer.toString(e2))
                    .append(Integer.toString(d2))
                    .append(Integer.toString(c2))
                    .append(m2.id())
                    .toString();

            return s2.compareTo(s1);
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
            Integer c1 = "volume".equals(f1.carrierType()) ? 1 :
                    "online resource".equals(f1.carrierType()) ? 2 : 3;

            Integer d2 = f2.firstDate() == null ? currentYear : f2.firstDate();
            Integer c2 = "volume".equals(f2.carrierType()) ? 1 :
                    "online resource".equals(f2.carrierType()) ? 2 : 3;

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
            Integer i1 = "volume".equals(m1.carrierType()) ? 3 :
                    "online resource".equals(m1.carrierType()) ? 2 : 1;
            Integer i2 = "volume".equals(m2.carrierType()) ? 3 :
                    "online resource".equals(m2.carrierType()) ? 2 : 1;
            return i1.compareTo(i2);
        }
    }

    private final static CarrierComparator carrierComparator = new CarrierComparator();

}
