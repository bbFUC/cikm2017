# coding=utf-8


if __name__ == '__main__':
    writeFile = open("C:\\Users\\Rock\\Desktop\\DestinationResult_Clean.ttl", 'w')

    b = '123'
    count = 0
    with open("C:\\Users\\Rock\\Desktop\\DestinationResult.ttl") as fa:
        for a in fa:
            a = a.strip('\n')
            if b != a:
                b = a
                writeFile.writelines(str(a)+'\n')

    writeFile.close()
