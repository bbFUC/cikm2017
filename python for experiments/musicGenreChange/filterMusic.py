#!usr/bin/python2
# coding=utf-8

import re

#mappingbased_objects_en.ttl
#instance_types_en.ttl
if __name__ == "__main__":
    writeFile = open('/home/cikm/1604/field/MBO_stylisticOrigin.ttl', 'w')
    try:
        sourceFile = open('/home/cikm/1604/mappingbased_objects_en.ttl', 'r')
    except Exception as e:
        print 'open file error', e

    pattern = re.compile('.*<http://dbpedia.org/ontology/stylisticOrigin>.*')
    for line in sourceFile.readlines():
        a = re.match(pattern, str(line))
        if a is not None:
            writeFile.write(str(line))

    writeFile.close()
    sourceFile.close()
