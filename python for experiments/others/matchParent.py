# coding=utf-8


if __name__ == '__main__':
    writeFile = open('C:\\Users\\Rock\\Desktop\\test\\IF_MO&Fa_Match.ttl', 'w')

    with open("C:\\Users\\Rock\\Desktop\\IF_Mother.ttl") as fa:
        for a in fa:
            flag = 0
            with open("C:\\Users\\Rock\\Desktop\\IF_Father.ttl") as fb:
                for b in fb:
                    if a == b:
                        flag = 1
                        break
            if flag == 0:
                writeFile.writelines(str(a))

    writeFile.close()
