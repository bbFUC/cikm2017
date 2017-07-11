#!usr/bin/python2
# coding=utf-8

import re

#mappingbased_objects_en.ttl
#instance_types_en.ttl
if __name__ == "__main__":
    writeFile1 = open('/home/cikm/1604/field/MBO_stylisticOriginAllGenre.ttl', 'a')

    try:
        sourceFile = open('/home/cikm/1604/MBO_stylisticOrigin.ttl', 'r')
    except Exception as e:
        print 'open file error', e

    pattern1 = re.compile('<.*> <http://dbpedia.org/ontology')
    pattern2 = re.compile('<.*>')
    pattern3 = re.compile('stylisticOrigin> <.*>')

    for line in sourceFile.readlines():
        a = re.search(pattern3, str(line))
        if a is not None:
            b = re.search(pattern2, str(a.group()))
            writeFile1.writelines(str(b.group().strip('http://dbpedia.org/resource/'))+'\n')

    writeFile1.close()
    sourceFile.close()
