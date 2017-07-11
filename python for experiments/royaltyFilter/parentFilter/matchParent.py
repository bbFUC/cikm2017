# coding=utf-8


if __name__ == '__main__':
    writeFile1 = open('/home/cikm/3.8/field/IF_MO&Fa_Match.ttl.ttl', 'w')
    writeFile2 = open('/home/cikm/3.8/field/IF_Fa&Mo_Match.ttl.ttl', 'w')

    with open("/home/cikm/3.8/field/IF_Clean_Mother.ttl") as fa:
        for a in fa:
            flag = 0
            with open("/home/cikm/3.8/field/IF_Clean_Father.ttl") as fb:
                for b in fb:
                    if a == b:
                        flag = 1
                        break
            if flag == 0:
                writeFile1.writelines(str(a))

    with open("/home/cikm/3.8/field/IF_Clean_Father.ttl") as fa:
        for a in fa:
            flag = 0
            with open("/home/cikm/3.8/field/IF_Clean_Mother.ttl") as fb:
                for b in fb:
                    if a == b:
                        flag = 1
                        break
            if flag == 0:
                writeFile2.writelines(str(a))

    writeFile1.close()
    writeFile2.close()
