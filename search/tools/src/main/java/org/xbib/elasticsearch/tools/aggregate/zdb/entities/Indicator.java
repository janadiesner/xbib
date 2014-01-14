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

import org.xbib.util.Strings;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class Indicator extends License {

    private final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    public Indicator(Map<String, Object> m) {
        super(m);
    }

    @Override
    protected void build() {
        this.parent = getString("xbib:identifier");
        this.id = getString("dc:identifier");
        this.isil = getString("xbib:isil");
        this.dates = buildDateArray();
        this.info = buildInfo();
        this.findContentType();

    }

    protected void findContentType() {
        this.mediaType = "computer";
        this.carrierType = "online resource";
    }

    protected List<Integer> buildDateArray() {
        List<Integer> dates = newLinkedList();
        String firstDate = getString("xbib:firstDate");
        int first;
        int last;
        if (!Strings.isNullOrEmpty(firstDate)) {
            first = Integer.parseInt(firstDate);
            String lastDate = getString("xbib:lastDate");
            last = Strings.isNullOrEmpty(lastDate) ?
                    currentYear : Integer.parseInt(lastDate);
            if (first > 0 && last > 0) {
                for (int d = first; d <= last; d++) {
                    dates.add(d);
                }
            }
        }
        return dates;
    }

    protected Map<String, Object> buildInfo() {
        Map<String, Object> m = newLinkedHashMap();
        this.service = newLinkedHashMap();
        String s = getString("xbib:interlibraryloanCode");
        if (s != null) {
            switch(s) {
                // 4,5 mio
                case "kxn" : // 1.061.340
                {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "distribution-domestic-only";
                    break;
                }
                case "kxx" : // 1.376.538
                {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "distribution-unrestricted";
                    break;
                }
                case "kpn" : // 1.684.164
                {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "distribution-postal-domestic-only";
                    break;
                }
                case "kpx" : // 104.579
                {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "distribution-postal";
                    break;
                }
                case "exn" : // 172.778
                {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "distribution-electronic-domestic-only";
                    break;
                }
                case "exx" : // 116.673
                {
                    servicetype = "interlibrary";
                    servicemode = "copy";
                    servicedistribution = "distribution-electronic";
                    break;
                }
                default: {
                    servicetype = "interlibrary";
                    servicemode = "none";
                    servicedistribution = "none";
                    break;
                }
            }
            service.put("servicecomment", getString("xbib:comment"));
            service.put("servicetype", servicetype);
            service.put("servicemode", servicemode);
            service.put("servicedistribution", servicedistribution);
            m.put("service", service);
        }
        Map<String, Object> holdings = newHashMap();
        holdings.put("firstvolume", getString("xbib:firstVolume"));
        holdings.put("firstissue", getString("xbib:firstIssue"));
        holdings.put("firstdate", getString("xbib:firstDate"));
        holdings.put("lastvolume", getString("xbib:lastVolume"));
        holdings.put("lastissue", getString("xbib:lastIssue"));
        holdings.put("lastdate", getString("xbib:lastDate"));
        m.put("holdings", holdings);
        return m;
    }

    public String getRoutingKey() {
        return new StringBuilder().append(findRegionKey()).append(findCarrierTypeKey()).toString();
    }
}
