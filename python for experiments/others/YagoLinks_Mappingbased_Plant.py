# coding=utf-8


if __name__ == '__main__':
    # writeFile = open("/home/cikm/3.9/yago_types.ttl", 'w')
    yago = open("/home/cikm/3.9/yago_links.nt", 'r')
    plant = open("/home/cikm/3.9/field/Clean_MBO_PlantKingdom.ttl", 'r')
    royaltyDict = {}
    for person in plant:
        person = person.strip('\n')
        royaltyDict[person] = 1

    count = 0
    for item in yago:
        item = item.strip('\n')
        itemList = item.split(' ')
        if royaltyDict.get(itemList[0][29:-1]) == 1:
            count = count + 1

    print '3.9: ', count
