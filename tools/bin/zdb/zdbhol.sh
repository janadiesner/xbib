
java="/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/java"
#java="/usr/java/jdk1.8.0/bin/java"

echo '
{
    "path" : "/Users/joerg/import/zdb",
    "pattern" : "1402*lok*mrc.gz",
    "elements" : "marc/zdb/hol",
    "detect" : true
}
' | ${java} \
    -cp $(pwd)/bin:$(pwd)/lib/tools-1.0.0.Beta2-feeder.jar \
    org.xbib.tools.Runner org.xbib.tools.convert.zdb.FromMARC
