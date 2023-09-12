# ProvImplantTir
Small internal application Android/java registering (on button press) GPS/GNSS position in a geojson/json file. 

The json file will be stored at \Internal storage memory\Android\data\com.provencale.provimplanttir\files\. So there is no need for internet access.
There is a mecanism to avoid registering a position before the accuracy is seemed acceptable.

# Example of volees.geojson
{'features': [{'geometry': {'coordinates': [2.878292, 42.696211, 30],
                            'type': 'Point'},
               'properties': {'name': 'test0101',
                              'nomVolee': 'test',
                              'numeroRangee': 1,
                              'numeroTrou': 1,
                              'timeUtc': 2023-09-12T13:15:47Z},
               'type': 'Feature'},
              {'geometry': {'coordinates': [2.87968, 42.696255, 30],
                            'type': 'Point'},
               'properties': {'name': 'test0102',
                              'nomVolee': 'test',
                              'numeroRangee': 1,
                              'numeroTrou': 2,
                              'timeUtc': 2023-09-12T13:16:10Z},
               'type': 'Feature'}],
 'type': 'FeatureCollection'}
 
"coordinates" are in WGS84 [longitude, latitude, elavation] according to geojson file format.
"name" is the concatenation of nomVolee and numeroRangee on two digits and numeroTrou on two digits.
"nomVolee" is a string of maximum 10 characters.
"numeroRangee" and "numeroTrou" are integers between 1 and 99.
"timeUtc" is the timestamp when the position was recorded in UTC time (ISO 8601 is accepted).