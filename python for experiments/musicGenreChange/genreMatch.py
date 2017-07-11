# coding=utf-8


if __name__ == '__main__':
    writeFile = open("/home/cikm/1604/field/MusicGenre_1510&1604_Match.ttl", 'w')

    with open("/home/cikm/1604/field/MBO_Clean_stylisticOriginAllGenre.ttl") as fa:
        for a in fa:
            flag = 0
            with open("/home/cikm/1510/field/MBO_Clean_stylisticOriginAllGenre.ttl") as fb:
                for b in fb:
                    if a == b:
                        flag = 1
                if flag == 0:
                    writeFile.writelines(str(a))

    writeFile.close()
