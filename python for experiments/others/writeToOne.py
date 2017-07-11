# coding=utf-8


if __name__ == '__main__':
    file1 = open("C:\\Users\\Rock\\Desktop\\ppp\\Plant Category1604\\MBO_PlantKingdom.ttl", 'r')
    file2 = open("C:\\Users\\Rock\\Desktop\\ppp\\Plant Category1604\\MBO_PlantDivision.ttl", 'r')
    file3 = open("C:\\Users\\Rock\\Desktop\\ppp\\Plant Category1604\\MBO_PlantClass.ttl", 'r')
    file4 = open("C:\\Users\\Rock\\Desktop\\ppp\\Plant Category1604\\MBO_PlantOrder.ttl", 'r')
    file5 = open("C:\\Users\\Rock\\Desktop\\ppp\\Plant Category1604\\MBO_PlantFamily.ttl", 'r')
    file6 = open("C:\\Users\\Rock\\Desktop\\ppp\\Plant Category1604\\MBO_PlantGenus.ttl", 'r')
    file7 = open("C:\\Users\\Rock\\Desktop\\ppp\\Plant Category1604\\MBO_PlantKKK.ttl", 'r')
    writeFile = open("C:\\Users\\Rock\\Desktop\\ppp\\MBO_Plant_Category1604.ttl", 'a')

    for a in file1:
        writeFile.write(a)
    for a in file2:
        writeFile.write(a)
    for a in file3:
        writeFile.write(a)
    for a in file4:
        writeFile.write(a)
    for a in file5:
        writeFile.write(a)
    for a in file6:
        writeFile.write(a)
    for a in file7:
        writeFile.write(a)
