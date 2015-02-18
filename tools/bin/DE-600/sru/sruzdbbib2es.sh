
echo '
{
    "uri" : "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve&query=alz%3D190171-0&recordSchema=MARC21plus-xml",
    "elements" : "marc/zdb/bib",
    "concurrency" : 1,
    "elasticsearch" :  "es://zephyros:19300?es.cluster.name=zbn-1.0",
    "index" : "zdbsru",
    "type" : "zdb",
    "maxbulkactions" : 1,
    "maxconcurrentbulkrequests" : 1,
    "detect" : true
}
' | java -cp bin:lib/tools-1.0.0.Beta2-elasticsearch.jar \
     org.xbib.tools.elasticsearch.feed.zdb.ZDBFromSRU

