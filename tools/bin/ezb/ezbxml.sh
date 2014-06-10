#!/bin/bash

# Testdaten Bielefeld/Köln: HBZ_update_dump201250001.xml.gz
# Lieferung NRW  HBZ_update_dump201325001.gz

# Format-Dokumenation
# http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf

# alt:
#java \
#    -cp bin:lib/tools-1.0-SNAPSHOT-elasticsearch.jar \
#    org.xbib.elasticsearch.tools.ingest.EZB \
#    --elasticsearch "es://10.1.1.37:9300?es.cluster.name=joerg" \
#    --threads 1 \
#    --maxbulkactions 1000 \
#    --index "ezb" \
#    --type "licenses" \
#    --path "$HOME/import/ezb/" \
#    --pattern "HBZ_update_dump*.gz"

java="/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/java"
version="1.0.0.Beta3"
#java="/usr/java/jdk1.8.0/bin/java"

echo '
{
    "path" : "'${HOME}'/import/ezb/",
    "pattern" : "HBZ*.gz",
    "output" : "ezb.ttl"
}
' | ${java} \
    -cp $(pwd)/bin:$(pwd)/lib/tools-${version}-converter.jar \
    org.xbib.tools.Runner org.xbib.tools.convert.ezb.FromEZBXML
