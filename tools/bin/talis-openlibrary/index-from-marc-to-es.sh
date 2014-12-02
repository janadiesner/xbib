
java="java"

echo '
{
    "path" : "'${HOME}'/import/talis-openlibrary/",
    "pattern" : "*.mrc",
    "elements" : "/org/xbib/analyzer/marc/bib.json",
    "concurrency" : 1,
    "pipelines" : 16,
    "elasticsearch" : {
        "cluster" : "elasticsearch",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "index" : "marc",
    "type" : "title",
    "shards" : 1,
    "replica" : 0,
    "maxbulkactions" : 10000,
    "maxconcurrentbulkrequests" : 30,
    "maxtimewait" : "180s",
    "mock" : false,
    "detect" : true,
    "client" : "ingest",
    "direct" : true
}
' | ${java} \
     -cp $(pwd)/bin:$(pwd)/bin/\*:$(pwd)/lib/tools-1.0.0.Beta7-standalone.jar \
     -Dlog4j.configurationFile=$(pwd)/bin/log4j2.xml \
     org.xbib.tools.Runner org.xbib.tools.feed.elasticsearch.marc.FromMARC
