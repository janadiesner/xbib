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

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.util.Strings;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class Manifestation
        implements Comparable<Manifestation>, PipelineRequest {

    protected final Map<String, Object> map;

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private boolean forced;

    private String id;

    private String externalID;

    private String key;

    private String publisher;

    private String publisherPlace;

    private String language;

    private Integer firstDate;

    private Integer lastDate;

    private String description;

    private Map<String,Object> identifiers;

    private SetMultimap<String, Manifestation> relatedManifestations;

    private SetMultimap<String, Holding> relatedHoldings;

    private SetMultimap<Integer, Holding> relatedVolumes;

    private String title;

    private String corporate;

    private String meeting;

    private List<String> country;

    private String genre;

    private boolean isPeriodical;

    private boolean isDatabase;

    private boolean isPacket;

    private boolean isNewspaper;

    private boolean isWebsite;

    private boolean isInTimeline;

    private boolean isSubseries;

    private boolean isSupplement;

    private String printID;

    private String onlineID;

    private String printExternalID;

    private String onlineExternalID;

    private String contentType;

    private String mediaType;

    private String carrierType;

    private String timelineKey;

    private List<Map<String,Object>> links;

    private SetMultimap<String, String> relations;

    private SetMultimap<String, String> externalRelations;

    public Manifestation(Map<String, Object> map) {
        this.map = map;
        build();
    }

    private void build() {
        // we use DNB ID. ZDB ID collides with GND ID. Example: 21573803
        String s = getString("IdentifierDNB.identifierDNB");
        this.id = s != null ? s : "";
        s = getString("IdentifierZDB.identifierZDB");
        this.externalID = s != null ? s : "";

        this.isSubseries = getString("TitleStatement.titlePartName") != null
                || getString("TitleStatement.titlePartNumber") != null;

        makeCorporate();
        makeMeeting();
        makeTitle();

        this.publisher = getString("PublicationStatement.publisherName");
        this.publisherPlace = getPublisherPlace();
        this.language = getString("Language.value", "unknown");
        findCountry();
        Integer firstDate = getInteger("date1");
        this.firstDate = firstDate == null ? null : firstDate == 9999 ? null : firstDate;
        Integer lastDate = getInteger("date2");
        this.lastDate = lastDate == null ? null : lastDate == 9999 ? null : lastDate;
        findLinks();
        this.description = getString("DatesOfPublication.value");

        findSupplement();
        this.genre = getString("OtherCodes.genreSource");
        String resourceType = getString("typeOfContinuingResource");
        this.isPeriodical = "Periodical".equals(resourceType);
        this.isDatabase = "Updating database".equals(resourceType)
                || "ag".equals(genre);
        this.isPacket = "pt".equals(genre);
        this.isNewspaper = "Newspaper".equals(resourceType)
                || "fn".equals(genre) || "lp".equals(genre) || "ao".equals(genre)
                || "eo".equals(genre) || "up".equals(genre) || "zt".equals(genre) ;
        this.isWebsite = "Updating Web site".equals(resourceType);
        // "Monographic series"
        // "Updating loose-leaf"

        // first, compute content types
        computeContentTypes();
        // last, compute key
        this.key = computeKey();

        // ISSN, CODEN, ...
        this.identifiers = makeIdentifiers();

        // relations to other manifestations on ID basis
        makeRelations();

        // prepare the construction of relations to manifestations
        this.relatedManifestations = Multimaps.synchronizedSortedSetMultimap(TreeMultimap.create());
        // prepare the construction of relations to holdings
        this.relatedHoldings = TreeMultimap.create();
        // prepare holdings by date
        this.relatedVolumes = TreeMultimap.create();

        // unique identifier
        StringBuilder p = new StringBuilder();
        if (publisher != null) {
            p.append(publisher);
        }
        if (publisherPlace != null && !publisherPlace.isEmpty()) {
            p.append('-').append(publisherPlace);
        }
        /*this.unique = new PublishedJournal()
                .journalName(title)
                .publisherName(p.toString())
                .createIdentifier();*/

        this.timelineKey = makeTimelineKey();

    }

    private <T> T get(String key) {
        return this.<T>get(map, key.split("\\."));
    }

    private <T> T get(Map inner, String[] key) {
        if (inner == null) {
            return null;
        }
        Object o = inner.get(key[0]);
        if (o instanceof List) {
            o = ((List)o).get(0);
        }
        return (T) (o instanceof Map && key.length > 1 ?
                get((Map) o, Arrays.copyOfRange(key, 1, key.length)) : o);
    }

    private Object get(String key, Object defValue) {
        Object o = get(key);
        return (o != null) ? o : defValue;
    }

    public String getString(String key) {
        return get(key);
    }

    private String getString(String key, String defValue) {
        return (String) get(key, defValue);
    }

    private <T> T getAnyObject(String key) {
        return get(key);
    }

    private Integer getInteger(String key) {
        Object o = get(key);
        return o == null ? null : o instanceof Integer ? (Integer) o : Integer.parseInt(o.toString());
    }

    public Map map() {
        return map;
    }

    public Manifestation setForced(boolean forced) {
        this.forced = forced;
        return this;
    }

    public boolean getForced() {
        return forced;
    }

    public String id() {
        return id;
    }

    public String externalID() {
        return externalID;
    }

    public Manifestation contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String contentType() {
        return contentType;
    }

    public Manifestation mediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public String mediaType() {
        return mediaType;
    }

    public Manifestation carrierType(String carrierType) {
        this.carrierType = carrierType;
        return this;
    }

    public String carrierType() {
        return carrierType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    public String corporateName() {
        return corporate;
    }

    public String meetingName() {
        return meeting;
    }

    public String publisher() {
        return publisher;
    }

    public String publisherPlace() {
        return publisherPlace;
    }

    public String language() {
        return language;
    }

    public List<String> country() {
        return country;
    }

    public Integer firstDate() {
        return firstDate;
    }

    public Integer lastDate() {
        return lastDate;
    }

    public String description() {
        return description;
    }

    public String genre() {
        return genre;
    }

    public boolean isPeriodical() {
        return isPeriodical;
    }

    public boolean isSupplement() {
        return isSupplement;
    }

    public boolean isDatabase() {
        return isDatabase;
    }

    public boolean isNewspaper() {
        return isNewspaper;
    }

    public boolean isPacket() {
        return isPacket;
    }

    public boolean isWebsite() {
        return isWebsite;
    }

    public boolean isSubseries() {
        return isSubseries;
    }

    public boolean isInTimeline() {
        return isInTimeline;
    }

    public boolean isPrint() {
        return printID != null && id.equals(printID);
    }

    public boolean hasPrint() {
        return printID != null && id.equals(onlineID);
    }

    public boolean isOnline() {
        return onlineID != null && id.equals(onlineID);
    }

    public boolean hasOnline() {
        return onlineID != null && id.equals(printID);
    }

    public String getPrintID() {
        return printID;
    }

    public String getPrintExternalID() {
        return printExternalID;
    }

    public String getOnlineID() {
        return onlineID;
    }

    public String getOnlineExternalID() {
        return onlineExternalID;
    }

    public boolean hasIdentifiers() {
        return identifiers != null && !identifiers.isEmpty();
    }

    public Map<String,Object> getIdentifiers() {
        return identifiers;
    }

    public boolean hasLinks() {
        return links != null && !links.isEmpty();
    }

    public void setLinks( List<Map<String,Object>> links) {
        this.links = links;
    }

    public List<Map<String,Object>> getLinks() {
        return links;
    }

    public SetMultimap<String, String> getRelations() {
        return relations;
    }

    public SetMultimap<String, String> getExternalRelations() {
        return externalRelations;
    }

    public boolean hasCarrierRelations() {
        for (String key : relations.keys()) {
            if (carrierEditions.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private void findSupplement() {
        // recognize supplement
        this.isSupplement = "isSupplementOf".equals(getString("SupplementParentEntry.relation"));
        if (!isSupplement) {
            this.isSupplement = "isSupplementOf".equals(getString("SupplementSpecialEditionEntry.relation"));
        }
    }

    private void makeTitle() {
        // shorten title (series statement after '/' or ':')
        // but combine with corporate name, meeting name, and part specification
        StringBuilder sb = new StringBuilder();
        sb.append(clean(getString("TitleStatement.titleMain")));
        String titleRemainder = getString("TitleStatement.titleRemainder");
        if (titleRemainder != null) {
            sb.append(" ; ").append(titleRemainder);
        }
        // add part name / part number
        if (isSubseries) {
            String partName = clean(getString("TitleStatement.titlePartName"));
            if (!Strings.isNullOrEmpty(partName)) {
                sb.append(" ; ").append(partName);
            }
            String partNumber = clean(getString("TitleStatement.titlePartNumber"));
            if (!Strings.isNullOrEmpty(partNumber)) {
                sb.append(" ; ").append(partNumber);
            }
        }
        if (corporate != null) {
            sb.append(" / ").append(corporate);
        }
        if (meeting != null) {
            sb.append(" / ").append(meeting);
        }
        setTitle(sb.toString());
        // delete synthetic title words
        Map<String, Object> m = (Map<String, Object>) map.get("TitleStatement");
        if (m != null) {
            if ("[Elektronische Ressource]".equals(m.get("titleMedium"))) {
                m.remove("titleMedium");
            }
        }
    }

    private String clean(String title) {
        if (title == null) {
            return null;
        }
        int pos = title.indexOf('/');
        if (pos > 0) {
            title = title.substring(0, pos - 1);
        }
        pos = title.indexOf(':');
        if (pos > 0) {
            title = title.substring(0, pos - 1);
        }
        title = title.replaceAll("\\[.*?\\]","").trim();
        return title;
    }

    private void makeCorporate() {
        this.corporate = getString("CorporateName.corporateName");
    }

    private void makeMeeting() {
        this.meeting = getString("MeetingName.meetingName");
    }

    private String getPublisherPlace() {
        Object o = map.get("PublicationStatement");
        if (o == null) {
            return "";
        }
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        StringBuilder sb = new StringBuilder();
        List<Map<String,Object>> list = (List<Map<String,Object>>)o;
        for (Map<String,Object> m : list) {
            o = m.get("placeOfPublication");
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            List<String> l = (List<String>)o;
            for (String s : l) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(s);
            }
        }
        return sb.toString();
    }

    private void findLinks() {
        Object o = map.get("ElectronicLocationAndAccess");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            this.links = (List)o;
            return;
        }
        this.links = Collections.EMPTY_LIST;
    }

    private void computeContentTypes() {
        Object o = map.get("physicalDescriptionElectronicResource");
        if (o != null) {
            if (o instanceof List) {
                List l = (List) o;
                this.contentType = "text";
                this.mediaType = "computer";
                this.carrierType = l.iterator().next().toString();
                return;
            } else {
                this.contentType = "text";
                this.mediaType = "computer";
                this.carrierType = "online resource".equals(o.toString()) ? "online resource" : "computer disc";
                return;
            }
        }
        // before assuming unmediated text, check title strings for media phrases
        String[] phraseTitles = new String[]{
                getString("AdditionalPhysicalFormNote.value"),
                getString("OtherCodes.genre"),
                getString("TitleStatement.titleMedium"),
                getString("TitleStatement.titlePartName"),
                getString("Note.value")
        };
        for (String s : phraseTitles) {
            if (s != null) {
                for (String t : ER) {
                    if (s.contains(t)) {
                        this.contentType = "text";
                        this.mediaType = "computer";
                        this.carrierType = "online resource";
                        return;
                    }
                }
            }
        }
        // default
        this.contentType = "text";
        this.mediaType = "unmediated";
        this.carrierType = "volume";
    }

    private final static String[] ER = new String[]{
        "Elektronische Ressource"
    };

    private String computeKey() {
        StringBuilder sb = new StringBuilder();
        // precedence for text/unmediated/volume
        // contentType
        switch (contentType) {
            case "text": {
                sb.append("0");
                break;
            }
            default: { // non-text
                sb.append("1");
            }
        }
        // mediaType
        switch (mediaType) {
            case "unmediated": {
                sb.append("0");
                break;
            }
            default: { // microform, computer
                sb.append("1");
            }
        }
        // carrierType
        switch (carrierType) {
            case "volume": {
                sb.append("0");
                break;
            }
            default: { // online resource, computer disc, other
                sb.append("1");
                break;
            }
        }
        int delta;
        int d1;
        int d2 = 0;
        try {
            d1 = firstDate == null ? currentYear : firstDate;
            d2 = lastDate == null ? currentYear : lastDate;
            delta = d2 - d1;
        } catch (NumberFormatException e) {
            delta = 0;
        }
        String d2Str = Integer.toString(d2);
        String deltaStr = Integer.toString(delta);
        return d2Str.length() + d2Str + deltaStr.length() + deltaStr + sb.toString();
    }

    private void findCountry() {
        Object o = getAnyObject("publishingCountry.isoCountryCodesSource");
        if (o instanceof List) {
            this.country = (List<String>)o;
        } else if (o instanceof String) {
            List<String> l = newLinkedList();
            l.add((String)o);
            this.country = l;
        } else {
            List<String> l = newLinkedList();
            l.add("unknown");
            this.country = l;
        }
    }

    private Map<String,Object> makeIdentifiers() {
        Map<String,Object> m = newHashMap();
        // get and convert all ISSN
        Object o = map.get("IdentifierISSN");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            List<String> issns = newLinkedList();
            List<Map<String,Object>> l = (List<Map<String,Object>>)o;
            for (Map<String, Object> aL : l) {
                String s = (String) aL.get("value");
                if (s != null) {
                    issns.add(s.replaceAll("\\-", "").toLowerCase());
                }
            }
            m.put("issn", issns);
        }
        // get CODEN for better article matching
        o = map.get("IdentifierCODEN");
        if (o != null) {
            m.put("coden", o);
        }
        // TODO more identifiers?
        return m;
    }

    private final static Supplier<Set<String>> supplier = new Supplier<Set<String>>() {

        @Override
        public Set<String> get() {
            return newLinkedHashSet();
        }
    };

    /**
     * Iterate through relations. Check for DNB IDs and remember as internal IDs.
     * Check for ZDB IDs and remember as external IDs.
     */
    private void makeRelations() {
        Map<String, Collection<String>> relationMap = Maps.newTreeMap();
        this.relations = Multimaps.newSetMultimap(relationMap, supplier);
        Map<String, Collection<String>> externalRelationMap = Maps.newTreeMap();
        this.externalRelations = Multimaps.newSetMultimap(externalRelationMap, supplier);

        this.isInTimeline = false;
        boolean hasSuccessor = false;
        boolean hasPredecessor = false;
        boolean hasTransient = false;

        for (String rel : relationEntries) {
            Object o = map.get(rel);
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            for (Object obj : (List) o) {
                Map<String, Object> m = (Map<String, Object>) obj;
                Object relObj = m.get("relation");
                if (relObj == null) {
                    continue;
                }
                String key = relObj instanceof List ?
                        ((List)relObj).get(0).toString() : relObj.toString();
                // internal ID = DNB ID (used for identifying relationships between hits)
                // more than one DNB identifier is strange...
                Object internalObj = m.get("identifierDNB");
                // take only first entry from list
                String internal = internalObj == null ? null : internalObj instanceof List ?
                        ((List)internalObj).get(0).toString() : internalObj.toString();
                if (internal == null) {
                    continue;
                }
                this.relations.put(key, internal);

                // external ID = ZDB ID (used for external typed linking, internal linking may collide with GND ID)
                Object externalObj = m.get("identifierZDB");
                String external = externalObj == null ? null : externalObj instanceof List ?
                        ((List)externalObj).get(0).toString() : externalObj.toString();
                if (external == null) {
                    continue;
                }
                this.externalRelations.put(key, external);

                if ("succeededBy".equals(key)) {
                    hasSuccessor = true;
                } else if ("precededBy".equals(key)) {
                    hasPredecessor = true;
                } else if ("hasTransientEdition".equals(key)) {
                    hasTransient = true;
                } else if ("isTransientEditionOf".equals(key)) {
                    hasTransient = true;
                } else if ("hasOnlineEdition".equals(key)) {
                    this.printID = id;
                    this.printExternalID = externalID;
                    this.onlineID = internal;
                    this.onlineExternalID = external;
                } else if ("hasPrintEdition".equals(key)) {
                    this.onlineID = id;
                    this.onlineExternalID = externalID;
                    this.printID = internal;
                    this.printExternalID = external;
                }
            }
        }
        // this manifestation is in the time line iff it has a successor AND a predecessor,
        // but if there is a transient edition, the chance is there is no successor/predecessor
        // Example: ZDB ID 570215x
        this.isInTimeline = hasSuccessor && hasPredecessor && !hasTransient;
    }

    private final static Set<String> relationEntries = newHashSet(
            "PrecedingEntry",
            "SucceedingEntry",
            "OtherEditionEntry",
            "OtherRelationshipEntry",
            "SupplementSpecialIssueEntry",
            "SupplementParentEntry"
    );

    public static Set<String> relationEntries() {
        return relationEntries;
    }

    private final static Set<String> carrierEditions =  newHashSet(
            "hasPrintEdition",
            "hasOnlineEdition",
            "hasBrailleEdition",
            "hasCDEdition",
            "hasDVDEdition",
            "hasMicroformEdition",
            "hasDigitizedEdition"
    );

    public static Set<String> carrierEditions() {
        return carrierEditions;
    }

    public void addRelatedManifestation(String relation, Manifestation manifestation) {
        relatedManifestations.put(relation, manifestation);
    }

    public void addRelatedHolding(String relation, Holding holding) {
        relatedHoldings.put(relation, holding);
    }

    public void addVolume(Integer date, Holding holding) {
        relatedVolumes.put(date, holding);
    }

    public SetMultimap<String, Manifestation> getRelatedManifestations() {
        return relatedManifestations;
    }

    public SetMultimap<String, Holding> getVolumesByHolder() {
        return relatedHoldings;
    }

    public SetMultimap<Integer, Holding> getVolumesByDate() {
        return relatedVolumes;
    }

    public void build(XContentBuilder builder, Set<String> visited) throws IOException {
        if (visited != null) {
            if (visited.contains(externalID)) {
                return;
            }
            visited.add(externalID);
        }
        builder.startObject();
        builder.field("@id", externalID())
                .field("@type", "Manifestation")
                .field("key", getKey())
                .field("title", title());
        String s = corporateName();
        if (s != null) {
            builder.field("corporateName", s);
        }
        s = meetingName();
        if (s != null) {
            builder.field("meetingName", s);
        }
        builder.field("country", country())
                .field("language", language())
                .field("publishedat", publisherPlace())
                .field("publishedby", publisher())
                .field("firstdate", firstDate())
                .field("lastdate", lastDate())
                .field("contenttype", contentType())
                .field("mediatype", mediaType())
                .field("carriertype", carrierType());
        if (hasIdentifiers()) {
            builder.field("identifiers", getIdentifiers());
        }
        if (isSubseries()) {
            builder.field("subseries", isSubseries());
        }
        if (isSupplement()) {
            builder.field("supplement", isSupplement());
        }
        // add information for linking
        SetMultimap<String,String> map = getExternalRelations();
        if (map != null && !map.isEmpty()) {
            builder.startArray("relations");
            for (String rel : map.keySet()) {
                for (String id : map.get(rel)) {
                    builder.startObject()
                            .field("@id", id)
                            .field("@type", "Manifestation")
                            .field("@label", rel)
                            .endObject();
                }
            }
            builder.endArray();
        }
        if (hasLinks()) {
            builder.array("links", getLinks());
        }
        builder.endObject();
    }

    public Map<String,XContentBuilder> buildVolume(String identifier, String parentIdentifier,
                                                   Integer date, Set<Holding> holdings)
            throws IOException {
        Map<String,XContentBuilder> map = newLinkedHashMap();
        XContentBuilder builder = jsonBuilder();
        builder.startObject()
                .field("@id", parentIdentifier)
                .field("@type", "Volume");
        if (date != -1) {
            builder.field("date", date);
        }
        if (hasLinks()) {
            builder.field("links", getLinks());
        }
        SetMultimap<String,Holding> institutions = servicesPerInstitution(holdings);
        builder.field("institutioncount", institutions.size())
                .startArray("institution");
        List<XContentBuilder> instBuilders = newLinkedList();
        for (String institution : institutions.keySet()) {
            Set<Holding> holdingsPerInstitution = institutions.get(institution);
            XContentBuilder institutionBuilder = jsonBuilder();
            institutionBuilder.startObject()
                    .field("isil", institution)
                    .field("servicecount", holdingsPerInstitution.size())
                    .startArray("service");
            List<XContentBuilder> list = newLinkedList();
            for (Holding holding : holdingsPerInstitution) {
                XContentBuilder serviceBuilder = jsonBuilder();
                serviceBuilder.startObject()
                        .field("@id", holding.identifier())
                        .field("@type", "Service")
                        .startArray("@parent");
                for (Manifestation m : holding.getManifestations()) {
                    serviceBuilder.value(m.externalID());
                }
                serviceBuilder.endArray()
                        .field("mediatype", holding.mediaType())
                        .field("carriertype", holding.carrierType())
                        .field("organization", holding.getOrganization())
                        .field("isil", institution)
                        .field("serviceisil", holding.getServiceISIL())
                        .field("priority", holding.getPriority())
                        .fieldIfNotNull("type", holding.getServiceType())
                        .fieldIfNotNull("mode", holding.getServiceMode())
                        .fieldIfNotNull("distribution", holding.getServiceDistribution())
                        .fieldIfNotNull("comment", holding.getServiceComment())
                        .field("info", holding.getInfo())
                        .endObject();
                serviceBuilder.close();
                list.add(serviceBuilder);
                map.put(holding.identifier(), serviceBuilder);
            }
            institutionBuilder.copy(list);
            institutionBuilder.endArray().endObject();
            institutionBuilder.close();
            instBuilders.add(institutionBuilder);
        }
        builder.copy(instBuilders);
        builder.endArray().endObject();
        builder.close();
        map.put("", builder);
        return map;
    }

    private SetMultimap<String,Holding> servicesPerInstitution(Set<Holding> holdings) {
        SetMultimap<String,Holding> set = HashMultimap.create();
        for (Holding holding : holdings) {
            set.put(holding.getISIL(), holding);
        }
        return set;
    }

    public void buildHolding(XContentBuilder builder, String parentIdentifier, String isil, Set<Holding> holdings)
        throws IOException {
        if (holdings == null || holdings.isEmpty()) {
            return;
        }
        builder.startObject()
                .field("@id", externalID())
                .field("@type", "Holding")
                .field("isil", isil);
        if (hasLinks()) {
            builder.field("links", getLinks());
        }
        builder.field("servicecount", holdings.size())
                .startArray("service");
        for (Holding holding : holdings) {
            builder.startObject()
                    .field("@id", holding.identifier())
                    .field("@type", "Service")
                    .field("@parent", parentIdentifier)
                    .field("mediatype", holding.mediaType())
                    .field("carriertype", holding.carrierType())
                    .field("organization", holding.getOrganization())
                    .field("isil", holding.getServiceISIL() )
                    .field("priority", holding.getPriority())
                    .fieldIfNotNull("type", holding.getServiceType())
                    .fieldIfNotNull("mode", holding.getServiceMode())
                    .fieldIfNotNull("distribution", holding.getServiceDistribution())
                    .fieldIfNotNull("comment", holding.getServiceComment())
                    .field("info", holding.getInfo())
                    .endObject();
        }
        builder.endArray().endObject();
    }

    /**
     * Iterate through holdings and build a new list that contains
     * unique holdings.
     *
     * @param holdings the holdings
     * @return unique holdings
     */
    private Set<Holding> unique(Set<Holding> holdings) {
        Set<Holding> newHoldings = newTreeSet(Holding.getRoutingComparator());
        for (Holding holding : holdings) {
            if (holding instanceof License) {
                Holding other = holding.getSame(holdings);
                if (other != null) {
                   newHoldings.add(holding);
                }
            }
        }
        return newHoldings;
    }

    public String getKey() {
        return key;
    }

    public String toString() {
        return externalID;
    }

    @Override
    public boolean equals(Object other) {
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(Manifestation m) {
        return externalID.compareTo(m.externalID());
    }

    public static Comparator<Manifestation> getKeyComparator() {
        return new Comparator<Manifestation>() {
            @Override
            public int compare(Manifestation m1, Manifestation m2) {
                return m2.getKey().compareTo(m1.getKey());
            }
        };
    }

    private String makeTimelineKey() {
        Integer d1 = firstDate() == null ? currentYear : firstDate();
        Integer c1 = findCarrierTypeKey();
        return new StringBuilder()
                .append(country())
                .append(Integer.toString(d1))
                .append(Integer.toString(c1))
                .append(id())
                .toString();
    }

    public Integer findCarrierTypeKey() {
        switch (carrierType()) {
            case "online resource" : return 2;
            case "volume": return 1;
            case "computer disc" : return 4;
            case "computer tape cassette" : return 4;
            case "computer chip cartridge" : return 4;
            case "microform" : return 5;
            case "multicolored" : return 6;
            case "other" : return 6;
            default: throw new IllegalArgumentException("unknown carrier: " + carrierType() + " in " + externalID());
        }
    }

    public static Comparator<Manifestation> getTimeComparator() {
        return new Comparator<Manifestation>() {
            @Override
            public int compare(Manifestation m1, Manifestation m2) {
                return m1.timelineKey.compareTo(m2.timelineKey);
            }
        };
    }
}

