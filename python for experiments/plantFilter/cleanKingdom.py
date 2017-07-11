# coding=utf-8


if __name__ == '__main__':
    file = open('/home/cikm/1604/field/MBO_PlantKingdom.ttl', 'r')
    writeFile = open('/home/cikm/1604/field/Clean_MBO_PlantKingdom.ttl', 'w')

    for line in file:
        line = line.strip('\n')
        lineList = line.split(' ')
        writeFile.write(lineList[0][29:-1] + '\n')

    file.close()
    writeFile.close()
