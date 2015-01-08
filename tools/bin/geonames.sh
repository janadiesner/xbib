#!/bin/bash

echo '
{
    "path" : "/Users/joerg/import/geonames",
    "pattern" : "allCountries.zip"
}
' | java -cp bin:lib/tools-1.0-SNAPSHOT-content.jar \
     org.xbib.tools.convert.geonames.GeonamesFromZIP
