#!/bin/bash

# cron?
tty -s
if [ "$?" -gt "0" ]
then
    cd $HOME/hbz-toolbox
    pwd=$(pwd)/bin
else
    pwd="$( cd -P "$( dirname "$0" )" && pwd )"
fi

bin=${pwd}/../../../bin
lib=${pwd}/../../../lib

java="java"

echo '
{
    "path" : "/Users/joerg/import/zdb",
    "pattern" : "1408*tit*mrc.gz",
    "elements" : "/org/xbib/analyzer/marc/zdb/bib.json",
    "package" : "org.xbib.analyzer.marc.zdb.bib",
    "concurrency" : 1,
    "pipelines" : 8,
    "identifier" : "DE-600",
    "collection" : "Zeitschriften",
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "bib-index" : "zdb",
    "bib-type" : "zdb",
    "maxbulkactions" : 2000,
    "maxconcurrentbulkrequests" : 8,
    "mock" : false,
    "detect" : true,
    "client" : "ingest",
    "timewindow" : "yyyyMMddHH",
    "aliases" : true,
    "ignoreindexcreationerror" : true
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.zdb.bib.MarcBib
