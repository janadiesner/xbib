
echo '
{
    "path" : "'${HOME}'/import/dnb/gnd/20140710/",
    "pattern" : "GND.rdf.gz",
    "elasticsearch" : "es://localhost:9300?es.cluster.name=elasticsearch",
    "index" : "gnd",
    "type" : "gnd",
    "shards" : 3,
    "replica" : 0,
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 20,
    "client" : "ingest"
}
' | java \
    -cp $(pwd)/bin:$(pwd)/lib/tools-1.0.0.Beta3-standalone.jar \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.dnb.gnd.RdfXml
