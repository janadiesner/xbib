#!/bin/bash

# sync data
dir="/Users/joerg/import/ezb/"
pushd ${dir}
wget --timestamping -r ftp://fize:gebida@rzblx5.uni-regensburg.de/
popd

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

echo '
{
    "path" : "'${dir}'rzblx5.uni-regensburg.de",
    "pattern" : "HBZ*.gz",
    "isChronologicallySorted" : true,
    "concurrency" : 4,
    "pipelines" : 8,
    "identifier" : "DE-606",
    "collection" : "Elektronische Zeitschriften",
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "index" : "ezbxml",
    "type" : "ezbxml",
    "maxbulkactions" : 10000,
    "maxconcurrentbulkrequests" : 8,
    "mock" : false,
    "client" : "ingest",
    "timewindow" : "yyyyMMddHH",
    "aliases" : true,
    "ignoreindexcreationerror" : true
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.ezb.EZBXML
