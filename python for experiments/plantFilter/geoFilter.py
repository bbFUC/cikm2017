# coding=utf-8


if __name__ == "__main__":
    writeFile = open('/home/cikm/1504/Geo_Feature.ttl', 'w')
    try:
        sourceFile = open('/home/cikm/1504/mappingbased_objects_en.ttl', 'r')
    except Exception as e:
        print 'open file error', e

    for line in sourceFile:
        line = line.strip('\n')
        lineList = line.split(' ')
        if 'rdf-syntax-ns#type' in lineList[1]:
            if 'geo/wgs84_pos#SpatialThing' in lineList[2]:
                writeFile.write(str(lineList[0][29:-1])+'\n')

    writeFile.close()
    sourceFile.close()
