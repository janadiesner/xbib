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
package org.xbib.tools.convert.geonames;

import org.xbib.io.InputService;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.tools.Converter;
import org.xbib.util.Strings;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Import geonames, UTF-8 tab-separated in ZIP
 */
public class GeonamesFromZIP extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(GeonamesFromZIP.class.getSimpleName());

    public static void main(String[] args) {
        try {
            new GeonamesFromZIP()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private GeonamesFromZIP() {
    }

    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new GeonamesFromZIP();
            }
        };
    }

    /**
     * geonameid         : integer id of record in geonames database
     name              : name of geographical point (utf8) varchar(200)
     asciiname         : name of geographical point in plain ascii characters, varchar(200)
     alternatenames    : alternatenames, comma separated varchar(5000)
     latitude          : latitude in decimal degrees (wgs84)
     longitude         : longitude in decimal degrees (wgs84)
     feature class     : see http://www.geonames.org/export/codes.html, char(1)
     feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
     country code      : ISO-3166 2-letter country code, 2 characters
     cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters
     admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
     admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
     admin3 code       : code for third level administrative division, varchar(20)
     admin4 code       : code for fourth level administrative division, varchar(20)
     population        : bigint (8 byte int)
     elevation         : in meters, integer
     dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
     timezone          : the timezone id (see file timeZone.txt) varchar(40)
     modification date : date of last modification in yyyy-MM-dd format
     * @param uri
     * @throws Exception
     */

    @Override
    public void process(URI uri) throws Exception {
        logger.info("start of processing {}", uri);
        InputStream in = InputService.getInputStream(uri);
        ZipInputStream zin = new ZipInputStream(in);
        for (ZipEntry zipEntry;(zipEntry = zin.getNextEntry()) != null;) {
            logger.info("reading zip entry {}", zipEntry.getName());
            Scanner sc = new Scanner(zin);
            while (sc.hasNextLine()) {
                int i = 0;
                String[] line = sc.nextLine().split("\t");
                String geonameid = line[i++];
                String name = line[i++];
                String asciiname = line[i++];
                String[] alternatenames = line[i++].split(",");
                Double latitude = Double.parseDouble(line[i++]);
                Double longitude = Double.parseDouble(line[i++]);
                String featureClass = line[i++];
                String featureCode = line[i++];
                String country = line[i++];
                String cc2 = line[i++];
                String admin1 = line[i++];
                String admin2 = line[i++];
                String admin3 = line[i++];
                String admin4 = line[i++];
                Long population = Long.parseLong(line[i++]);
                String elevationStr = line[i++];
                Integer elevation = Strings.isNullOrEmpty(elevationStr) ? 0 : Integer.parseInt(line[i++]);
                String dem = line[i++];
                String timezone = line[i++];
            }
        }
        zin.close();
        logger.info("end of processing {}", uri);
    }

}
