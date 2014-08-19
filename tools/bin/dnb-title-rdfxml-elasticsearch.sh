
echo '
{
    "path" : "'${HOME}'/import/dnb/titel/20140611/",
    "pattern" : "DNBTitel.rdf.gz",
    "elasticsearch" : "es://localhost:9300?es.cluster.name=elasticsearch",
    "index" : "dnb",
    "type" : "title",
    "shards" : 3,
    "replica" : 0,
    "maxbulkactions" : 10000,
    "maxconcurrentbulkrequests" : 20,
    "client" : "ingest"
}
' | java \
    -cp $(pwd)/bin:$(pwd)/lib/tools-1.0.0.Beta3-standalone.jar \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.dnb.title.RdfXml
