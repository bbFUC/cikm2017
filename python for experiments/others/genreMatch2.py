# coding=utf-8


if __name__ == '__main__':
    writeFile = open("/home/cikm/3.8/field/IT_MusicGenre_1604&3.8_Match.ttl", 'w')

    with open("/home/cikm/3.8/field/IT_Clean_MusicGenre.ttl") as fa:
        for a in fa:
            a = a.strip('\n')
            flag = 0
            with open("/home/cikm/1604/field/IT_Clean_MusicGenre.ttl") as fb:
                for b in fb:
                    b = b.strip('\n')
                    if a == b:
                        flag = 1
                        break
            if flag == 0:
                writeFile.writelines(str(a)+'\n')

    writeFile.close()
