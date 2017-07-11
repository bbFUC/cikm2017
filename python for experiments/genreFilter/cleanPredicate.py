#!usr/bin/python2
# coding=utf-8

import re

#mappingbased_objects_en.ttl
#instance_types_en.ttl
if __name__ == "__main__":
    writeFile1 = open('/home/cikm/3.8/field/IT_Clean_MusicGenre.ttl', 'a')
    # writeFile2 = open('/home/cikm/3.8/field/MBO_stylisticOriginAllGenre.ttl', 'a')

    try:
        sourceFile = open('/home/cikm/3.8/IT_MusicGenre.ttl', 'r')
    except Exception as e:
        print 'open file error', e

    pattern1 = re.compile('<.*> <http://www.w3.org')
    pattern2 = re.compile('<.*>')

    for line in sourceFile.readlines():
        a = re.search(pattern1, str(line))
        if a is not None:
            b = re.search(pattern2, str(a.group()))
            writeFile1.writelines(str(b.group())+'\n')

    writeFile1.close()
    # writeFile2.close()
    sourceFile.close()
