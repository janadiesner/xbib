#!/bin/bash

iterations=14

d=`date +%Y`

while [ iterations -gt 0 ]; do
  p2=`date -d "${d}" +%Y`
  p1=`date -d "${d} -1 year" +%Y`
  d2=`date -d "${d}" +%Y`
  d1=`date -d "${d} -1 year" +%Y`

  echo "d1=${d1} d2=${d2}"
  
  mkdir -p bibdat/${d1}-${d2}/

  echo '
  {
    "input" : [
        "http://services.dnb.de/oai/repository?prefix=PicaPlus-xml&set=bib&from=${p1}&until=${p2}"
    ],
    "concurrency" : 1,
    "output" : "bibdat/${d1}-${d2}"
  }
  ' | /usr/java/jdk1.8.0/bin/java -cp bin:lib/tools-1.0.0.Beta2-content.jar \
     org.xbib.tools.feed.oai.DNB
     
  d=${p1}
  iterations=${iterations} - 1
done
