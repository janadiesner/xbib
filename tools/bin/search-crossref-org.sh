
year="2013"
rows="10000"

for page in `seq 1 250` ;
do
   mkdir -p "crossref/${year}/${rows}/"
   wget -O "crossref/${year}/${rows}/${page}.json" "http://search.crossref.org/dois?q=?&year=${year}&rows=${rows}&page=${page}"
done
