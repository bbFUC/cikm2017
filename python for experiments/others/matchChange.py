# coding=utf-8


if __name__ == '__main__':
    writeFile1 = open('C:\\Users\\Rock\\Desktop\\test\\AM_Match1604.ttl', 'w')
    writeFile2 = open('C:\\Users\\Rock\\Desktop\\test\\AM_Match3.8.ttl', 'w')

    with open("C:\\Users\\Rock\\Desktop\\wiki links\\1604\\1unziped\\amsterdammuseum_links.ttl") as fa:
        for a in fa:
            flag = 0
            with open("C:\\Users\\Rock\\Desktop\\wiki links\\3.8\\1unziped\\amsterdammuseum_links.nt") as fb:
                for b in fb:
                    if a == b:
                        flag = 1
                        break
            if flag == 0:
                writeFile1.writelines(str(a))

    with open("C:\\Users\\Rock\\Desktop\\wiki links\\3.8\\1unziped\\amsterdammuseum_links.nt") as fa:
        for a in fa:
            flag = 0
            with open("C:\\Users\\Rock\\Desktop\\wiki links\\1604\\1unziped\\amsterdammuseum_links.ttl") as fb:
                for b in fb:
                    if a == b:
                        flag = 1
                        break
            if flag == 0:
                writeFile2.writelines(str(a))

    writeFile1.close()
    writeFile2.close()
