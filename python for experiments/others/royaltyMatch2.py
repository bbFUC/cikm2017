# coding=utf-8


if __name__ == '__main__':
    writeFile = open("/home/cikm/3.8/field/IT_Royalty_3.8&1604_Match.ttl", 'w')

    with open("/home/cikm/1604/field/IT_Clean_Royalty.ttl") as fa:
        for a in fa:
            flag = 0
            with open("/home/cikm/3.8/field/IT_Clean_Royalty.ttl") as fb:
                for b in fb:
                    if a == b:
                        flag = 1
                if flag == 0:
                    writeFile.writelines(str(a))

    writeFile.close()
