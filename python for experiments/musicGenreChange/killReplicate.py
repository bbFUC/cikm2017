# coding=utf-8

ciku = open('/home/cikm/1604/field/MBO_stylisticOriginAllGenre.ttl', 'r')   # 打开需要去重文件
xieci = open('/home/cikm/1604/field/MBO_Clean_stylisticOriginAllGenre.ttl', 'w')   # 打开处理后存放的文件
cikus = ciku.readlines()
list2 = {}.fromkeys(cikus).keys()     # 列表去重方法，将列表数据当作字典的键写入字典，依据字典键不可重复的特性去重

for line in list2:
    xieci.writelines(line)

xieci.close()
