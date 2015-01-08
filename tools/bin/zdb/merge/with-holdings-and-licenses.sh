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
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "bib-index" : "zdb",
    "hol-index" : "zdbholdings",
    "xml-license-index" : "ezbxml",
    "web-license-index" : "ezbweb",
    "volume-index" : "hbztitle",
    "volume-hol-index" : "hbzholdings",
    "index" : "xbib2",
    "maxBulkActions" : 1000,
    "maxConcurrentBulkRequests" : 4,
    "maxWait" : "60s",
    "scrollSize" : 10,
    "scrollTimeout" : 7200000,
    "concurrency" : 4,
    "client" : "ingest",
    "mock" : false
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.merge.zdb.licenseinfo.WithHoldingsAndLicenses

# Beispiele für "identifier"
# Nature (online) 14134238
# Nature (print) 1207143
# Il nuovo cimento (online) 15019664
# ¬The¬ European physical journal 14590669, 14590682, 14590694, 14590712
# Nature 1207143
# Bibliotheksdienst Beiheft 5018961
# not-indexed 21706372
# preceding: 1901710
# online/print in timeline: 15015051 10631951
# skipped? 5450548
# repariert? 22725787
# online/print 24016421 / 14713056
# E aber nicht EZB 624502-x 26839271
