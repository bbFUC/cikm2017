# coding=utf-8


if __name__ == '__main__':
    # writeFile = open("C:\\Users\\Rock\\Desktop\\genus1.txt", 'w')
    # royalty = open("C:\\Users\\Rock\\Desktop\\1.txt", 'r')
    # yago = open("C:\\Users\\Rock\\Desktop\\genus.ttl", 'r')
    writeFile = open('/home/cikm/1504/Diff1_Geo_Feature.ttl', 'w')
    file1 = open('/home/cikm/14/Geo_Feature.ttl', 'r')
    file2 = open('/home/cikm/1504/Geo_Feature.ttl', 'r')
    royaltyDict = {}
    for person in file1:
        person = person.strip('\n')
        royaltyDict[person] = 1

    count1 = 0
    count = 0
    for item in file2:
        item = item.strip('\n')
        count1 = count1 + 1
        if royaltyDict.get(item) == 1:
            writeFile.write(item + '\n')
            count = count + 1
    
    print count1
    print count
