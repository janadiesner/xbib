#!/bin/bash

# wget http://stedolan.github.io/jq/download/linux64/jq
# curl -s 0:9200/zdb/_search -d '
#  {"from":0,"size":100000,"query":{"match":{"ElectronicLocationAndAccess.nonpublicnote" : "EZB"}},"fields" : ["IdentifierZDB.identifierZDB"] }
#  ' | jq -c -M '[.hits.hits[] | .fields["IdentifierZDB.identifierZDB"][] ]'

#java \
#    -cp bin:lib/tools-1.0-SNAPSHOT-content.jar \
#    org.xbib.tools.harvest.EZB \
#    --input "${HOME}/ezb-zdb-ids.json" \
#    --url "http://rzbvm016.ur.de/flr/by_zdbid/" \
#    --output "ezbweb.ttl"

java="/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/java"
#java="/usr/java/jdk1.8.0/bin/java"

echo '
{
    "path" : "'${HOME}'/import/ezb/",
    "pattern" : "ezb-zdb-ids-20140220.json",
    "baseURL" : "http://rzbvm016.ur.de/flr/by_zdbid/",
    "concurrency" : 1,
    "elasticsearch" : "es://zephyros:19300?es.cluster.name=zbn-1.0",
    "index" : "ezbweb",
    "type" : "web",
    "shards": 3,
    "replica" : 0,
    "maxbulkactions" : 2000,
    "maxconcurrentbulkrequests" : 24
}
' | ${java} \
    -cp $(pwd)/bin:$(pwd)/lib/tools-1.0.0.Beta2-feeder.jar \
    org.xbib.tools.Runner org.xbib.tools.feed.elasticsearch.ezb.EZBWeb
