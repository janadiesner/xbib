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

bin=${pwd}/../../bin
lib=${pwd}/../../lib

java="java"

echo '
{
    "path" : "/Users/joerg/import/hbz/vk/2014113015/",
    "pattern" : "*.jsonl.gz",
    "buffersize" : "10m",
    "elements" : "/org/xbib/analyzer/mab/titel.json",
    "identifier" : "DE-605",
    "collection" : "hbz Verbundkatalog",
    "concurrency" : 1,
    "pipelines" : 4,
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "title-index" : "hbztitle",
    "title-type" : "title",
    "holdings-index" : "hbzholdings",
    "holdings-type" : "holdings",
    "timewindow" : "yyyyMMddHH",
    "maxbulkactions" : 5000,
    "maxconcurrentbulkrequests" : 5,
    "mock" : false,
    "detect" : true,
    "client" : "ingest",
    "aliases" : true,
    "ignoreindexcreationerror" : true
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.mab.MarcXchangeJSONLines
