# coding=utf-8


if __name__ == '__main__':
    writeFile = open("C:\\Users\\Rock\\Desktop\\links changing\\bricklink_links.1510&1604.txt", 'w')
    royalty = open("C:\\Users\\Rock\\Desktop\\wiki links\\1510\\unzip\\bricklink_links.nt", 'r')
    yago = open("C:\\Users\\Rock\\Desktop\\wiki links\\1604\\unzip\\bricklink_links.ttl", 'r')
    royaltyDict = {}
    for person in royalty:
        person = person.strip('\n')
        royaltyDict[person] = 1

    for item in yago:
        item = item.strip('\n')
        if royaltyDict.get(item) != 1:
            writeFile.write(item + '\n')
