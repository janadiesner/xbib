#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.convert.VIAFConverter \
    --path "${HOME}/import/viaf" \
    --pattern "viaf-20130115-clusters-rdf.xml.gz" \
    --translatePicaSortMarker "x-viaf" \
    --format "ntriples" \
    --output viaf.nt
