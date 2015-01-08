#!/bin/bash

echo '
{
    "path" : "/Users/joerg/import/geonames",
    "pattern" : "allCountries.zip",
    "elasticsearch" : "es://localhost:9300?es.cluster.name=test",
    "index" : "geonames",
    "type" : "geonames",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 36
}
' | java -cp bin:lib/tools-1.0-SNAPSHOT-elasticsearch.jar \
     org.xbib.elasticsearch.tools.feed.geonames.GeonamesFromZIP
