# coding=utf-8

import re


if __name__ == '__main__':
    writeFile = open('/home/cikm/1604/field/IT_Clean_Royalty.ttl', 'w')
    try:
        sourceFile = open('/home/cikm/1604/IT_Royalty.ttl', 'r')
    except Exception as e:
        print 'open file error', e

    pattern1 = re.compile('<.*> <http://www.w3.org')
    pattern2 = re.compile('<.*>')

    for line in sourceFile.readlines():
        a = re.search(pattern1, str(line))
        if a is not None:
            b = re.search(pattern2, str(a.group()))
            writeFile.writelines(str(b.group())+'\n')

    writeFile.close()
    sourceFile.close()
