# coding=utf-8

import re


if __name__ == '__main__':
    writeFile1 = open('/home/cikm/3.8/field/MBO_stylisticOriginAllGenre.ttl', 'a')
    writeFile2 = open('/home/cikm/3.8/field/MBO_stylisticOriginAllGenre.ttl', 'a')

    try:
        sourceFile1 = open('/home/cikm/3.8/field/IF_Mother.ttl', 'r')
        sourceFile2 = open('/home/cikm/3.8/field/IF_Father.ttl', 'r')
    except Exception as e:
        print 'open file error', e

    pattern1 = re.compile('<.*> <http://dbpedia.org/property')
    pattern2 = re.compile('<.*>')

    for line in sourceFile1.readlines():
        a = re.search(pattern1, str(line))
        if a is not None:
            b = re.search(pattern2, str(a.group()))
            writeFile1.writelines(str(b.group())+'\n')

    for line in sourceFile2.readlines():
        a = re.search(pattern1, str(line))
        if a is not None:
            b = re.search(pattern2, str(a.group()))
            writeFile2.writelines(str(b.group())+'\n')

    writeFile1.close()
    writeFile2.close()
    sourceFile1.close()
    sourceFile2.close()
