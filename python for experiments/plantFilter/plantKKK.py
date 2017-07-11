# coding=utf-8


if __name__ == '__main__':
    writeFile = open("/home/cikm/1510/field/MBO_PlantKKK.ttl", 'w')
    mbo = open("/home/cikm/1510/mappingbased_objects_en.ttl", 'r')
    plant = open("/home/cikm/1510/field/Clean_MBO_PlantKingdom.ttl", 'r')
    plantDict = {}
    for item in plant:
        item = item.strip('\n')
        plantDict[item] = 1

    for item in mbo:
        item = item.strip('\n')
        itemList = item.split(' ')
        if itemList[1][29:36] == 'kingdom':
            if plantDict.get(itemList[2][29:-1]) == 1:
                writeFile.write(item + '\n')
