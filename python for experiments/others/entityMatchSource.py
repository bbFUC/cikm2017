# coding=utf-8


if __name__ == '__main__':
    writeFile = open("C:\\Users\\Rock\\Desktop\\DestinationResult.ttl", 'w')

    with open("C:\\Users\\Rock\\Desktop\\1604&3.8_Difference.ttl") as fa:
        for a in fa:
            flag = 0
            with open("C:\\Users\\Rock\\Desktop\\Clean_MBO_stylisticOrigin3.8-1.ttl") as fb:
                for b in fb:
                    if a == b:
                        flag = 1
                if flag == 0:
                    writeFile.writelines(str(a))

    writeFile.close()
