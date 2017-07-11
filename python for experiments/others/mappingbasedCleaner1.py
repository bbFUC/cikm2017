# coding=utf-8


if __name__ == '__main__':
    writeFile = open('/home/cikm/1604/Clean_mappingbased_objects_en.ttl', 'w')
    mappingbased = open('/home/cikm/1604/mappingbased_objects_en.ttl', 'r')

    # writeFile.write('source ' + 'target\n')
    for item in mappingbased:
        item = item.strip('\n')
        itemList = item.split(' ')
        itemList[0] = itemList[0][29:-1]
        itemList[2] = itemList[2][29:-1]
        writeFile.write('<' + itemList[0] + '> <a> <' + itemList[2] + '> .\n')
