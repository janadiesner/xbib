
echo '
{
    "uri" : "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve&query=zdbid:%s&recordSchema=MARC21plus-xml&startRecord=1&maximumRecords=1000",
    "numbers" : "notindexed.txt",
    "concurrency" : 1,
    "output" : "sru-zdb.tar.gz",
    "detect" : true
}
' | java -cp bin:lib/tools-1.0.0.Beta2-elasticsearch.jar \
     org.xbib.tools.zdb.ZDBFromSRU
