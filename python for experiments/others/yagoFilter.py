# coding=utf-8


if __name__ == '__main__':
    writeFile = open('/home/cikm/1604/YagoRoyalty.ttl', 'w')
    royalty = open('/home/cikm/1604/field/IT_Clean_Royalty.ttl', 'r')
    yago = open('/home/cikm/1604/yago_types.ttl', 'r')
    royaltyDict = {}
    for person in royalty:
        person = person.strip('\n')
        royaltyDict[person] = 1

    for item in yago:
        item = item.strip('\n')
        itemList = item.split(' ')
        if royaltyDict.get(itemList[0]) == 1:
            print itemList[2]
            if itemList[2][31:38] == 'Wikicat':
                print itemList[2][31:38]
                writeFile.write(item + '\n')
