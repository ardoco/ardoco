#!/bin/bash
# Author: Dominik Fuchss
# Show query here: https://dbpedia.org/sparql/

# SELECT DISTINCT ?alabel
# WHERE {
#   ?a rdfs:label ?alabel .
#   ?a dct:subject ?sc .
#   ?sc skos:broader{0,1} <http://dbpedia.org/resource/Category:Software_design> .
#   optional {?sc rdfs:label ?label  } .
#   ?sc ?p ?o
#   filter (lang(?label) = "en")
#   filter ((lang(?o) = "en") || isURI(?o))
#   filter (lang(?alabel) = "en")
# }
#
#  SELECT DISTINCT ?alabel
# WHERE {
#   ?a rdfs:label ?alabel .
#   ?a dct:subject ?sc .
#   ?sc skos:broader{0,1} <http://dbpedia.org/resource/Category:Software_engineering> .
#   optional {?sc rdfs:label ?label  } .
#   ?sc ?p ?o
#   filter (lang(?label) = "en")
#   filter ((lang(?o) = "en") || isURI(?o))
#   filter (lang(?alabel) = "en")
# }

#wget -O common-cs-0.json "https://dbpedia.org/sparql/?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+DISTINCT+%3Falabel%0D%0AWHERE+%7B%0D%0A++%3Fa+rdfs%3Alabel+%3Falabel+.%0D%0A++%3Fa+dct%3Asubject+%3Fsc+.%0D%0A++%3Fsc+skos%3Abroader%7B0%2C0%7D+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3ASoftware_engineering%3E+.%0D%0A++optional+%7B%3Fsc+rdfs%3Alabel+%3Flabel++%7D+.%0D%0A++%3Fsc+%3Fp+%3Fo%0D%0A++filter+%28lang%28%3Flabel%29+%3D+%22en%22%29%0D%0A++filter+%28%28lang%28%3Fo%29+%3D+%22en%22%29+%7C%7C+isURI%28%3Fo%29%29%0D%0A++filter+%28lang%28%3Falabel%29+%3D+%22en%22%29%0D%0A%7D&format=application%2Fsparql-results%2Bjson&timeout=30000&signal_void=on&signal_unconnected=on"
#wget -O common-cs-1.json "https://dbpedia.org/sparql/?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+DISTINCT+%3Falabel%0D%0AWHERE+%7B%0D%0A++%3Fa+rdfs%3Alabel+%3Falabel+.%0D%0A++%3Fa+dct%3Asubject+%3Fsc+.%0D%0A++%3Fsc+skos%3Abroader%7B1%2C1%7D+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3ASoftware_engineering%3E+.%0D%0A++optional+%7B%3Fsc+rdfs%3Alabel+%3Flabel++%7D+.%0D%0A++%3Fsc+%3Fp+%3Fo%0D%0A++filter+%28lang%28%3Flabel%29+%3D+%22en%22%29%0D%0A++filter+%28%28lang%28%3Fo%29+%3D+%22en%22%29+%7C%7C+isURI%28%3Fo%29%29%0D%0A++filter+%28lang%28%3Falabel%29+%3D+%22en%22%29%0D%0A%7D&format=application%2Fsparql-results%2Bjson&timeout=30000&signal_void=on&signal_unconnected=on"
#wget -O common-cs-2.json "https://dbpedia.org/sparql/?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+DISTINCT+%3Falabel%0D%0AWHERE+%7B%0D%0A++%3Fa+rdfs%3Alabel+%3Falabel+.%0D%0A++%3Fa+dct%3Asubject+%3Fsc+.%0D%0A++%3Fsc+skos%3Abroader%7B2%2C2%7D+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3ASoftware_engineering%3E+.%0D%0A++optional+%7B%3Fsc+rdfs%3Alabel+%3Flabel++%7D+.%0D%0A++%3Fsc+%3Fp+%3Fo%0D%0A++filter+%28lang%28%3Flabel%29+%3D+%22en%22%29%0D%0A++filter+%28%28lang%28%3Fo%29+%3D+%22en%22%29+%7C%7C+isURI%28%3Fo%29%29%0D%0A++filter+%28lang%28%3Falabel%29+%3D+%22en%22%29%0D%0A%7D&format=application%2Fsparql-results%2Bjson&timeout=30000&signal_void=on&signal_unconnected=on"
#wget -O common-cs-3.json "https://dbpedia.org/sparql/?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+DISTINCT+%3Falabel%0D%0AWHERE+%7B%0D%0A++%3Fa+rdfs%3Alabel+%3Falabel+.%0D%0A++%3Fa+dct%3Asubject+%3Fsc+.%0D%0A++%3Fsc+skos%3Abroader%7B3%2C3%7D+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3ASoftware_engineering%3E+.%0D%0A++optional+%7B%3Fsc+rdfs%3Alabel+%3Flabel++%7D+.%0D%0A++%3Fsc+%3Fp+%3Fo%0D%0A++filter+%28lang%28%3Flabel%29+%3D+%22en%22%29%0D%0A++filter+%28%28lang%28%3Fo%29+%3D+%22en%22%29+%7C%7C+isURI%28%3Fo%29%29%0D%0A++filter+%28lang%28%3Falabel%29+%3D+%22en%22%29%0D%0A%7D&format=application%2Fsparql-results%2Bjson&timeout=30000&signal_void=on&signal_unconnected=on"

curl -o design.json "https://dbpedia.org/sparql/?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=+SELECT+DISTINCT+%3Falabel%0D%0A+WHERE+%7B%0D%0A+++%3Fa+rdfs%3Alabel+%3Falabel+.%0D%0A+++%3Fa+dct%3Asubject+%3Fsc+.%0D%0A+++%3Fsc+skos%3Abroader%7B0%2C1%7D+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3ASoftware_design%3E+.%0D%0A+++optional+%7B%3Fsc+rdfs%3Alabel+%3Flabel++%7D+.%0D%0A+++%3Fsc+%3Fp+%3Fo%0D%0A+++filter+%28lang%28%3Flabel%29+%3D+%22en%22%29%0D%0A+++filter+%28%28lang%28%3Fo%29+%3D+%22en%22%29+%7C%7C+isURI%28%3Fo%29%29%0D%0A+++filter+%28lang%28%3Falabel%29+%3D+%22en%22%29%0D%0A+%7D&format=application%2Fsparql-results%2Bjson&timeout=30000&signal_void=on&signal_unconnected=on"
curl -o engineering.json "https://dbpedia.org/sparql/?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=++SELECT+DISTINCT+%3Falabel%0D%0A+WHERE+%7B%0D%0A+++%3Fa+rdfs%3Alabel+%3Falabel+.%0D%0A+++%3Fa+dct%3Asubject+%3Fsc+.%0D%0A+++%3Fsc+skos%3Abroader%7B0%2C1%7D+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3ASoftware_engineering%3E+.%0D%0A+++optional+%7B%3Fsc+rdfs%3Alabel+%3Flabel++%7D+.%0D%0A+++%3Fsc+%3Fp+%3Fo%0D%0A+++filter+%28lang%28%3Flabel%29+%3D+%22en%22%29%0D%0A+++filter+%28%28lang%28%3Fo%29+%3D+%22en%22%29+%7C%7C+isURI%28%3Fo%29%29%0D%0A+++filter+%28lang%28%3Falabel%29+%3D+%22en%22%29%0D%0A+%7D&format=application%2Fsparql-results%2Bjson&timeout=30000&signal_void=on&signal_unconnected=on"