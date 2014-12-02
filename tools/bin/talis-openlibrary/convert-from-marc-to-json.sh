#!/bin/bash

java="java"

echo '
{
    "path" : "'${HOME}'/import/talis-openlibrary/",
    "pattern" : "*.mrc",
    "encoding": "ISO-8859-1",
    "concurrency" : 1
}
' | ${java} \
     -cp $(pwd)/bin:$(pwd)/bin/\*:$(pwd)/lib/tools-1.0.0.Beta7-standalone.jar \
     -Dlog4j.configurationFile=$(pwd)/bin/log4j2.xml \
     org.xbib.tools.Runner org.xbib.tools.convert.marc.FromMARCToMarcJSON
