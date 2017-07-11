# coding=utf-8


if __name__ == '__main__':
    writeFile = open('/home/cikm/1604/field/MBO_Roy&Suc_Match.ttl', 'w')

    with open("/home/cikm/1604/field/IT_Clean_Royalty.ttl") as fa:
        for a in fa.readlines():
            a = a.strip('\n')
            with open("/home/cikm/1604/field/MBO_Successor.ttl") as fb:
                for b in fb.readlines():
                    if a in b:
                        writeFile.writelines(str(b))

    writeFile.close()
