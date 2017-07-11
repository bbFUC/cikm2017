# coding=utf-8


if __name__ == '__main__':
    # writeFile = open("/home/cikm/3.9/yago_types.ttl", 'w')
    yago = open("/home/cikm/3.9/yago_types.nt", 'r')
    plant = open("/home/cikm/3.9/field/Clean_MBO_PlantKingdom.ttl", 'r')
    royaltyDict = {}
    for person in plant:
        person = person.strip('\n')
        royaltyDict[person] = 1

    count = 0
    for item in yago:
        item = item.strip('\n')
        itemList = item.split(' ')
        a = itemList[0][29:-1]
        if royaltyDict.get(a) == 1:
            if itemList[2][31:-1] == 'Plant100017222' or itemList[2][31:43] == 'WikicatPlant':
                count = count + 1
                del royaltyDict[a]

    print '3.9: ', count
