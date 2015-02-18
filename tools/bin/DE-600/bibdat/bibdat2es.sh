
java="/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/java"
#java="/usr/java/jdk1.8.0/bin/java"

echo '
{
    "path" : "bib",
    "pattern" : "*.tar.gz",
    "concurrency" : 1,
    "elasticsearch" :  "es://localhost:9300?es.cluster.name=joerg",
    "index" : "bibdat",
    "type" : "zdb",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 36,
    "mock" : false,
    "detect" : true,
    "client" : "bulk"
}
' | ${java} \
    -cp $(pwd)/bin:$(pwd)/lib/tools-1.0.0.Beta3-feeder.jar \
    org.xbib.tools.Runner org.xbib.tools.feed.elasticsearch.zdb.bibdat.BibdatFromOAITar
