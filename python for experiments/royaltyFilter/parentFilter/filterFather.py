#!usr/bin/python2
# coding=utf-8

import re


if __name__ == "__main__":
    writeFile = open('/home/cikm/3.8/field/IF_Father.ttl', 'w')
    try:
        sourceFile = open('/home/cikm/3.8/infobox_objects_en.ttl', 'r')
    except Exception as e:
        print 'open file error', e

    pattern = re.compile('.*<http://dbpedia.org/ontology/father>.*')
    for line in sourceFile.readlines():
        a = re.match(pattern, str(line))
        if a is not None:
            writeFile.write(str(line))

    writeFile.close()
    sourceFile.close()
