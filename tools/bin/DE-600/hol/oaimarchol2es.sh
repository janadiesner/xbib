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

#set=zdb
set="zdb:holdings"

iterations=2

d=`gdate --utc --iso-8601=seconds`
while [ ${iterations} -gt 0 ]; do
  p1=`gdate --utc -d "${d} -1 hour" +%Y-%m-%d'T'%H:%M:%S'Z'`
  p2=`gdate --utc -d "${d}" +%Y-%m-%d'T'%H:%M:%S'Z'`
  echo "d=${d} p1=${p1} p2=${p2}"
  
  echo '
  {
    "uri" : [
        "http://services.dnb.de/oai/repository?verb=ListRecords&metadataPrefix=MARC21-xml&set='${set}'&from='${p1}'&until='${p2}'"
    ],
    "elements" : "/org/xbib/analyzer/marc/zdb/hol.json",
    "package" : "org.xbib.analyzer.marc.zdb.hol",
    "concurrency" : 1,
    "pipelines" : 1,
    "identifier" : "DE-600",
    "collection" : "Zeitschriften",
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "hol-index" : "zdbholdings",
    "hol-type" : "zdbholdings",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 1,
    "mock" : false,
    "detect-unknown" : true,
    "client" : "ingest",
    "timewindow" : "yyyyMMddHH",
    "aliases" : true,
    "ignoreindexcreationerror" : true
  }
  ' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.zdb.hol.MarcHolOAI

  d=${p1}
  iterations=$((${iterations}-1))
done
