#!/bin/bash

# Testdaten Bielefeld/Köln: HBZ_update_dump201250001.xml.gz
# Lieferung NRW  HBZ_update_dump201325001.gz



java="java"
#java="/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/java"
#java="/usr/java/jdk1.8.0/bin/java"
version="1.0.0.Beta3"

echo '
{
    "path" : "'${HOME}'/import/ezb/",
    "pattern" : "HBZ*.gz",
    "isChronologicallySorted" : true,
    "output" : "ezb.ttl"
}
' | ${java} \
    -cp $(pwd)/bin:$(pwd)/lib/tools-${version}-converter.jar \
    org.xbib.tools.Runner org.xbib.tools.convert.ezb.FromEZBXML
