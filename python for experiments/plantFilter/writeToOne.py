# coding=utf-8


if __name__ == '__main__':
    file1 = open("/home/cikm/1510/field/MBO_PlantKingdom.ttl", 'r')
    file2 = open("/home/cikm/1510/field/MBO_PlantDivision.ttl", 'r')
    file3 = open("/home/cikm/1510/field/MBO_PlantClass.ttl", 'r')
    file4 = open("/home/cikm/1510/field/MBO_PlantOrder.ttl", 'r')
    file5 = open("/home/cikm/1510/field/MBO_PlantFamily.ttl", 'r')
    file6 = open("/home/cikm/1510/field/MBO_PlantGenus.ttl", 'r')
    file7 = open("/home/cikm/1510/field/MBO_PlantKKK.ttl", 'r')
    writeFile = open("/home/cikm/1510/field/MBO_All_Plant_Category.ttl", 'a')

    writeFile.write('source target\n')
    count = 0
    for a in file1:
        a = a.strip('\n')
        aList = a.split(' ')
        writeFile.write(aList[0][29:-1] + ' ' + aList[2][29:-1] + '\n')
        count = count + 1
    for a in file2:
        a = a.strip('\n')
        aList = a.split(' ')
        writeFile.write(aList[0][29:-1] + ' ' + aList[2][29:-1] + '\n')
        count = count + 1
    for a in file3:
        a = a.strip('\n')
        aList = a.split(' ')
        writeFile.write(aList[0][29:-1] + ' ' + aList[2][29:-1] + '\n')
        count = count + 1
    for a in file4:
        a = a.strip('\n')
        aList = a.split(' ')
        writeFile.write(aList[0][29:-1] + ' ' + aList[2][29:-1] + '\n')
        count = count + 1
    for a in file5:
        a = a.strip('\n')
        aList = a.split(' ')
        writeFile.write(aList[0][29:-1] + ' ' + aList[2][29:-1] + '\n')
        count = count + 1
    for a in file6:
        a = a.strip('\n')
        aList = a.split(' ')
        writeFile.write(aList[0][29:-1] + ' ' + aList[2][29:-1] + '\n')
        count = count + 1
    for a in file7:
        a = a.strip('\n')
        aList = a.split(' ')
        writeFile.write(aList[0][29:-1] + ' ' + aList[2][29:-1] + '\n')
        count = count + 1

    print count
    file1.close()
    file2.close()
    file3.close()
    file4.close()
    file5.close()
    file6.close()
    file7.close()
    writeFile.close()

