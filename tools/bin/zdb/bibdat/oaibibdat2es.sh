#!/bin/bash

# cron?
tty -s
if [ "$?" -gt "0" ]
then
    cd $HOME/hbz-toolbox
    pwd=$(pwd)
    bin=${pwd}/bin
    lib=${pwd}/lib
else
    pwd="$( cd -P "$( dirname "$0" )" && pwd )"
    bin=${pwd}/../../../bin
    lib=${pwd}/../../../lib
fi

java="java"

set=bib

d=`gdate +%Y-%m-%d`
p1=`gdate -d "${d} -1 months" +"%Y-%m-%dT%H:%M:%SZ"`
p2=`gdate -d "${d}" +"%Y-%m-%dT%H:%M:%SZ"`

echo "p1=${p1} p2=${p2}"
  
echo '
  {
    "uri" : [
        "http://services.dnb.de/oai/repository?verb=ListRecords&metadataPrefix=PicaPlus-xml&set='${set}'&from='${p1}'&until='${p2}'"
    ],
    "count" : 120,
    "elements" : "/org/xbib/analyzer/pica/zdb/bibdat.json",
    "package" : "org.xbib.analyzer.pica.zdb.bibdat",
    "concurrency" : 1,
    "pipelines" : 1,
    "collection" : "Adressen",
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "bib-index" : "bibdat",
    "bib-type" : "bibdat",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 1,
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
    org.xbib.tools.feed.elasticsearch.zdb.bibdat.BibdatOAI
