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

set=zdb
#set=zdbholdings

iterations=2

d=`gdate +%Y-%m-%d`
while [ ${iterations} -gt 0 ]; do
  p1=`gdate -d "${d} - 1 days" +%Y-%m-%d`
  p2=`gdate -d "${d}" +%Y-%m-%d`
  d1=`gdate -d "${d} - 1 days" "+%Y%W"`
  d2=`gdate -d "${d}" +%Y%W`

  echo "d1=${d1} d2=${d2} p1=${p1} p2=${p2}"
  
  mkdir -p ${set}
  echo '
  {
    "uri" : [
        "http://services.dnb.de/oai/repository?verb=ListRecords&metadataPrefix=MARC21-xml&set='${set}'&from='${p1}'&until='${p2}'"
    ],
    "elements" : "/org/xbib/analyzer/marc/zdb/bib.json",
    "package" : "org.xbib.analyzer.marc.zdb.bib",
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
    "bib-index" : "zdb",
    "bib-type" : "zdb",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 1,
    "mock" : true,
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
    org.xbib.tools.feed.elasticsearch.zdb.bib.MarcBibOAI

  d=${p1}
  iterations=$((${iterations}-1))
done
