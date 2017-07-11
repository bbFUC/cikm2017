package de.tf.uni.freiburg.sparkrdf.sparql


import org.apache.spark.graphx.lib.ConnectedComponents
import scala.reflect.ClassTag
import scala.util.matching.Regex
import de.tf.uni.freiburg.sparkrdf.sparql.operator._
import collection.mutable.Buffer
import de.tf.uni.freiburg.sparkrdf.model.graph.GraphLoader
import de.tf.uni.freiburg.sparkrdf.model.rdf.triple.TriplePattern
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark._
import scala.collection.JavaConversions._
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.broadcast.Broadcast
import de.tf.uni.freiburg.sparkrdf.sparql.operator.bgp._
import de.tf.uni.freiburg.sparkrdf.parser.rdf._
import de.tf.uni.freiburg.sparkrdf.sparql.operator.result.ResultBuilder
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx._
import de.tf.uni.freiburg.sparkrdf.model.graph.node._
import de.tf.uni.freiburg.sparkrdf.model.rdf.executionresults.IntermediateResultsModel
import de.tf.uni.freiburg.sparkrdf.sparql.operator.result.util.SolutionMapping
import de.tf.uni.freiburg.sparkrdf.sparql.operator.order.Order
import de.tf.uni.freiburg.sparkrdf.sparql.operator.projection.Projection
import de.tf.uni.freiburg.sparkrdf.sparql.operator.optional.LeftJoin
import de.tf.uni.freiburg.sparkrdf.parser.query.expression.op.IExpression
import de.tf.uni.freiburg.sparkrdf.sparql.operator.filter.Filter
import com.hp.hpl.jena.shared.PrefixMapping
import de.tf.uni.freiburg.sparkrdf.constants.Const
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import java.util.Date
/*import it.unipd.dei._
import it.unipd.dei.graphx.diameter.DiameterApproximation*/

/**
 * Class that calls all spark methods, such that all method calls from java go through this class.
 *
 * @author Thorsten Berberich
 */
object SparkFacade extends BasicGraphPattern with ResultBuilder with Projection with LeftJoin with Filter with Order {

  /**
   * Spark context
   */
  private var context: SparkContext = null;

  /**
   * Creates a new Spark context
   */
  def createSparkContext() {
    val conf = new SparkConf()

    /*
     * See the configuration of Spark:
     * https://spark.apache.org/docs/1.2.0/configuration.html
     */

    // Use the Kryo serializer, because it is faster than Java serializing
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.kryo.registrator", "de.tf.uni.freiburg.sparkrdf.sparql.serialization.Registrator")
    conf.set("spark.core.connection.ack.wait.timeout", "5000");
    conf.set("spark.shuffle.consolidateFiles", "true");
    conf.set("spark.rdd.compress", "true");
    conf.set("spark.kryoserializer.buffer.max.mb", "512");

    if (Const.locale) {
      conf.setMaster("local")
    }

    if (Const.executorMem != null) {
      conf.set("spark.executor.memory", Const.executorMem)
    }

    if (Const.parallelism != null) {
      conf.set("spark.default.parallelism", Const.parallelism)
    }

    if (Const.memoryFraction != null) {
      conf.set("spark.storage.memoryFraction", Const.memoryFraction)
    }

    if (Const.jobName != null) {
      conf.setAppName(Const.jobName)
    } else {
      conf.setAppName("SPARQL for SPARK Graph: \"" + Const.inputFile + "\"")
    }

    context = new SparkContext(conf)

    Logger.getLogger("org").setLevel(Level.OFF) // 原本是WARN
    Logger.getLogger("akka").setLevel(Level.OFF) // 原本是WARN
  }

  /**
   * Loads the graph into the graph model, if it isn't loaded yet
   */
  def loadGraph() {
    val conf: Configuration = new Configuration();
    val fs: FileSystem = FileSystem.get(conf);

    if (!fs.exists(new Path(fs.getHomeDirectory().toString() +
      "/" + Const.inputFile + "_graph"))) {

      if (Const.countBasedLoading) {
        GraphLoader.loadGraphCountBased(Const.inputFile, context);
      } else {
        GraphLoader.loadGraphHashBased(Const.inputFile, context);
      }

    } else {
      GraphLoader.loadPreloadedGraph(Const.inputFile, context)
    }
  }

  /**
   * Executes a basic graph pattern on the loaded graph
   */
  def executeBasicGraphPattern(triples: java.util.List[TriplePattern]): RDD[(VertexId, VertexInterface)] = {
    // Broadcast all Sparql triples to all workers
    val broadcastedTriples: Broadcast[java.util.List[TriplePattern]] = context.broadcast(triples)
    val result = matchBGP(broadcastedTriples, GraphLoader.getGraph, context)
    broadcastedTriples.unpersist(true)
    result
  }

  /**
   * Build the results out of the graph from the graph model
   */
  def buildResult(triples: java.util.List[TriplePattern], res: RDD[(VertexId, VertexInterface)]): RDD[SolutionMapping] = {
    // Broadcast all Sparql triples to all workers
    val broadcastedTriples: Broadcast[java.util.List[TriplePattern]] = context.broadcast(triples)
    val result = buildResult(broadcastedTriples, res)
    broadcastedTriples.unpersist(true)
    result
  }

  //求节点度最大值
  def max(a:(VertexId,Int),b:(VertexId,Int)):(VertexId,Int)={
    if (a._2>b._2) a
    else b
  }
  
/*  // clustering coefficient
  def clusteringCoefficient[VD:ClassTag,ED:ClassTag](g:Graph[VD,ED]) = {
    val numTriplets =
      g.aggregateMessages[Set[VertexId]](
      et => { et.sendToSrc(Set(et.dstId));
              et.sendToDst(Set(et.srcId)) },
      (a,b) => a ++ b) // #A
    .map(x => {val s = (x._2 - x._1).size; s*(s-1) / 2})
    .reduce(_ + _)
    
    println("***Number of numTriplets***")
    println(numTriplets)

    if (numTriplets == 0) 0.0 else
      g.triangleCount.vertices.map(_._2).reduce(_ + _) /
        numTriplets.toFloat
  }*/
  
  
  def mergeMaps(m1: Map[VertexId, Int], m2: Map[VertexId, Int])
    : Map[VertexId, Int] = {
    def minThatExists(k: VertexId): Int = {
      math.min(
        m1.getOrElse(k, Int.MaxValue),
        m2.getOrElse(k, Int.MaxValue))
    }
    
    (m1.keySet ++ m2.keySet).map {
      k => (k, minThatExists(k))
    }.toMap
  }
  
  def update(
      id: VertexId,
      state: Map[VertexId, Int],
      msg: Map[VertexId, Int]) = {
      mergeMaps(state, msg)
  }
  
  def checkIncrement(
      a: Map[VertexId, Int],
      b: Map[VertexId, Int],
      bid: VertexId) = {
    val aplus = a.map { case (v, d) => v -> (d + 1) }
    if (b != mergeMaps(aplus, b)) {
      Iterator((bid, aplus))
    } else {
      Iterator.empty
    }
  }
  
  def iterate(e: EdgeTriplet[Map[VertexId, Int], _]) = {
    checkIncrement(e.srcAttr, e.dstAttr, e.dstId) ++
    checkIncrement(e.dstAttr, e.srcAttr, e.srcId)
  }
  
  
  def getComponent[VD: ClassTag, ED: ClassTag](
    g: Graph[VD, ED], component: VertexId): Graph[VD, ED] = {
  val cc: Graph[VertexId, ED] = ConnectedComponents.run(g)
  // Join component ID to the original graph.
  val joined = g.outerJoinVertices(cc.vertices) {
    (vid, vd, cc) => (vd, cc)
  }
  // Filter by component ID.
  val filtered = joined.subgraph(vpred = {
    (vid, vdcc) => vdcc._2 == Some(component)
  })
  // Discard component IDs.
  filtered.mapVertices {
    (vid, vdcc) => vdcc._1
  }
}
  
  
  def abc() : Unit = {
    //求入度最大节点
    //val MaxInDegree = graph.inDegrees.reduce((a,b)=>max(a,b))
    //print ("*********************************max in Degree = "+MaxInDegree+"*******************")
    
    //输出所有节点信息
    //val allVerticesAttribute = graph.vertices.map(v=>v._2.asInstanceOf[VertexAttribute].getAttribute())
    //allVerticesAttribute.foreach(println(_)) 
    
    //用filter()来获得度最大节点
    //val theTop = graph.vertices.filter{ _._1 == MaxInDegree._1}
    //println("输出具有最大入度的点")
    //theTop.foreach(println(_))
    
    /*    println("***********输出入度最大的1000个点************")
    val length1 = fifteenVerticesInDegrees.length - 1
    for (i <- 0 to length1){
      val topFifteenInDegrees = graph.vertices.filter{ _._1 == fifteenVerticesInDegrees(i)._2 }.map(m=>m._2.asInstanceOf[VertexAttribute].getAttribute())
      topFifteenInDegrees.take(1).foreach(println(_))
    }
    */
    
    /*    println("***********输出出度最大的100个点************")
    val length2 = printHundredVerticesOutDegrees.length - 1
    for (i <- 0 to length2){
      val topFifteenOutDegrees = graph.vertices.filter{ _._1 == printHundredVerticesOutDegrees(i)._2 }.map(m=>m._2.asInstanceOf[VertexAttribute].getAttribute())
      topFifteenOutDegrees.take(1).foreach(println(_))
    }*/
    
    /*    println("***********输出度最大的100个点************")
    val length3 = fifteenVerticesDegrees.length - 1
    for (i <- 0 to length3){
      val topFifteenDegrees = graph.vertices.filter{ _._1 == fifteenVerticesDegrees(i)._2 }.map(m=>m._2.asInstanceOf[VertexAttribute].getAttribute())
      topFifteenDegrees.take(1).foreach(println(_))
    }*/
    
        //test5: find all the source nodes(inDegree=0) and the sink nodes(outDegree=0) 
    // source nodes--->因为graphx直接无视度为0的点所以这两个点的讨论没有意义
    /*println("************print out the lowest inDegrees************")
    val test1 = degree3.sortByKey(true, 1).take(10)
    test1.foreach(x=>println(x._2,x._1))*/
    //val inDegreeOneCount = degree3.filter{ _._1 == 1}
    //val allInDegreeIsZero = degree3.filter{ _._1 == 0}
    //val sourceNodeNumber = allInDegreeIsZero.count
    //println("************the number of the source nodes is: "+ sourceNodeNumber +"***************")
    //println("************the number of one inDegree nodes is: "+ inDegreeOneCount.count +"***************")
    //val allSourceNodes = graph.vertices.filter{ _._1 == allInDegreeIsZero}
    
    // sink nodes--->因为graphx直接无视度为0的点所以这两个点的讨论没有意义
    /*println("************print out the lowest outDegrees************")
    val test2 = degree4.sortByKey(true, 1).take(10)
    test2.foreach(x=>println(x._2,x._1))
    val outDegreeOneCount = degree4.filter{ _._1 == 1}
    //val allOutDegreeIsZero = degree4.filter{ _._1 == 0}
    //println("************the number of the sink nodes is: "+allOutDegreeIsZero.count+"***************")
    println("************the number of one outDegree nodes is: "+ outDegreeOneCount.count +"***************")
    //val allSinkNodes = graph.vertices.filter{ _._1 == allOutDegreeIsZero.id}
    val degreeOneCount = degree2.filter(_._1 == 1)
    val degreeTwoCount = degree2.filter(_._1 == 2)
    println("************the number of one Degree nodes is: "+ degreeOneCount.count +"***************")
    println("************the number of two Degrees nodes is: "+ degreeTwoCount.count +"***************")*/
    
    
    /*    //子图+正则测试
    val pattern = "^\".*"
    val literalRegex = pattern.r
    val subgraph1 = graph.subgraph(vpred=(id, attr)=>literalRegex.pattern.matcher(attr.asInstanceOf[VertexAttribute].getAttribute()).matches() == true)
    subgraph1.triplets.map(
      triplet => triplet.srcAttr.asInstanceOf[VertexAttribute].getAttribute() + "\n" + triplet.attr.getEdgeAttribute() + "\n" + triplet.dstAttr.asInstanceOf[VertexAttribute].getAttribute() + " ."
    ).collect.foreach(println(_))*/
    
    
    /*//子图测试
    val subgraph1 = graph.subgraph(vpred=(id, attr)=>id == -8139497927135256763L)
    subgraph1.triplets.map(
      triplet => triplet.srcAttr.asInstanceOf[VertexAttribute].getAttribute() + "\n" + triplet.attr + "\n" + triplet.dstAttr.asInstanceOf[VertexAttribute].getAttribute() + " ."
    ).collect.foreach(println(_))*/

     //println("---------------------------This is the divider-----------------------------------")
    
/*     //测试输出triplets
    val tripletStore = graph.triplets//.filter(triplet=>triplet.dstId == -8139497927135256763L)
    tripletStore.map(
      triplet => triplet.srcAttr.asInstanceOf[VertexAttribute].getAttribute() + "\n" + triplet.attr.getEdgeAttribute() + "\n" + triplet.dstAttr.asInstanceOf[VertexAttribute].getAttribute() + " ."
    ).collect.foreach(println(_))
     */
/*    //测试输出triplets
    val tripletStore = graph.triplets.filter(triplet=>triplet.dstId == -8139497927135256763L)
    tripletStore.map(
      triplet => triplet.srcAttr.asInstanceOf[VertexAttribute].getAttribute() + "\n" + triplet.attr.getEdgeAttribute() + "\n" + triplet.dstAttr.asInstanceOf[VertexAttribute].getAttribute() + " ."
    ).collect.foreach(println(_))*/
    
    
        //test7: pagerank算法
    //val now3 = new Date()
    //val time3 = now3.getTime
    //val pagerankGraph = subgraph1.pageRank(0.01)
    //val now4 = new Date()
    //val time4 = now4.getTime
    //println("***************It takes: "+(time4-time3)+" ms*********************")
    //pagerankGraph.vertices.filter{ _._2 > 1.00}.foreach(println(_))
    
    //val now3 = new Date()
    //val time3 = now3.getTime
    //val now4 = new Date()
    //val time4 = now4.getTime
    //println("***************It takes: "+(time4-time3)+" ms*********************")
    //pagerankGraph.vertices.filter{ _._2 > 1.00}.foreach(println(_))
    
    
    
    
    val graph = GraphLoader.getGraph   // 获取图
    
/*    // test11:print ids
    graph.triplets.map(c=>c.srcId + " 1.0 " + c.dstId).collect().foreach(println(_))*/
    
 /*   val a = graph.triplets
    val b = a.map(c=>c.srcId.toString() + " 1.0 " + c.dstId.toString())
    b.collect().foreach(println(_))*/
    
    
/*    type Distance = Double
    val graph1 = graph.mapEdges(e=>e.attr.getEdgeWeight.asInstanceOf[Distance])
    //val graph2 = graph1.mapVertices((id,v)=>v.asInstanceOf[Vert])
    val a = DiameterApproximation.run(graph1)
    println(a)
    //graph1.diameterApprox()
    //val graph1 = graph.mapEdges(Double)
*/    
    

    // test XI:选出最大联通分量构成的字子图

    val connectedComponent = graph.connectedComponents().vertices//.triplets
    //val countRes = connectedComponent.map(_.swap).groupByKey.map(_._2).collect
    val countRes = connectedComponent.map(_.swap).groupByKey.map(_._2).collect.sortBy(_.size)
    val countResLength = countRes.length
    //println("---------------show time---------------------")
    //countRes.foreach(println(_))

    val k = countRes(countResLength-1)
    val a = k.toArray
    a.foreach(println(_))
/*    val alength = a.length
    
    for (i <- 0 to alength - 1) {
      val tem = graph.triplets.filter(e=>e.srcId == a(i)).map(c=>c.srcId + " 1.0 " + c.dstId).collect
      tem.foreach(println(_))
    }
*/
    //a.foreach(println(_))
    //println(alength)
/*    val g1 = getComponent(graph, 5116360905243996601L)
    println(g1.vertices.count)
    val s = g1.triplets.map(c=>c.srcId.toString() + " 1.0 " + c.dstId.toString())*/
//    for (i <- 0 to alength - 1) {
//      
//    }
    
/*    for (i <- 0 to alength-1) {
      val subgraph1 = graph.subgraph(vpred=(id, attr)=>id == a(i))
    }*/
    
    //val g1 = getComponent(graph, -646501874963559973L)
    
    
  /*  // test X:average distance
    
    val fraction = 0.005
    val replacement = false
    // val seed = scala.util.Random.nextLong
    val sample = graph.vertices.map(v => v._1).
      sample(replacement, fraction, 2311L)
    val ids = sample.collect().toSet
    
    val mapGraph = graph.mapVertices((id,_) => {
      if (ids.contains(id)) {
        Map(id -> 0)
      } else {
        Map[VertexId, Int]()
      }
    })
    
    val mapGraph = graph.mapVertices((id,_) => {
      Map(id -> 0)
    })
    val start = Map[VertexId, Int]()
    val res = mapGraph.pregel(start)(update, iterate, mergeMaps)
    
    val paths = res.vertices.flatMap { case (id, m) =>
      m.map { case (k, v) =>
        if (id < k) {
          (id, k, v)
        } else {
          (k, id, v)
        }
      }
    }.distinct()
    paths.cache()
    
    println("-------------mean average distance--------------")
    println(paths.map(_._3).filter(_ > 0).stats())
    */
    
/*    val degree2=graph.degrees.map(a=>(a._2,a._1))          // 获取所有点的度
    val degree3=graph.inDegrees.map(a=>(a._2,a._1))        // 获取所有点的入度
    val degree4=graph.outDegrees.map(a=>(a._2,a._1))       // 获取所有点的出度
   
    val sortVerticesDegrees = degree2.sortByKey(true, 1)   // 排序度、入度、出度
    val sortVerticesInDegrees = degree3.sortByKey(true, 1)
    val sortVerticesOutDegrees = degree4.sortByKey(true, 1)
    
    // test0:输出排名前1000入度出度和度的点
    println("***********输出入度最大的1000个点************")
    val fifteenVerticesInDegrees = sortVerticesInDegrees.top(1000)
    val length1 = fifteenVerticesInDegrees.length - 1
    for (i <- 0 to length1){
      val topFifteenInDegrees = graph.vertices.filter{ _._1 == fifteenVerticesInDegrees(i)._2 }.map(m=>m._2.asInstanceOf[VertexAttribute].getAttribute())
      topFifteenInDegrees.take(1).foreach(print(_))
      print(", "+fifteenVerticesInDegrees(i)._1)
      println()
    }
    
    println("***********输出出度最大的1000个点************")
    val fifteenVerticesOutDegrees = sortVerticesOutDegrees.top(1000)
    val length2 = fifteenVerticesOutDegrees.length - 1
    for (i <- 0 to length2){
      val topFifteenOutDegrees = graph.vertices.filter{ _._1 == fifteenVerticesOutDegrees(i)._2 }.map(m=>m._2.asInstanceOf[VertexAttribute].getAttribute())
      topFifteenOutDegrees.take(1).foreach(print(_))
      print(", "+fifteenVerticesOutDegrees(i)._1)
      println()
    }
    
    println("***********输出度最大的1000个点************")
    val fifteenVerticesDegrees = sortVerticesDegrees.top(1000)
    val length3 = fifteenVerticesDegrees.length - 1
    for (i <- 0 to length3){
      val topFifteenDegrees = graph.vertices.filter{ _._1 == fifteenVerticesDegrees(i)._2 }.map(m=>m._2.asInstanceOf[VertexAttribute].getAttribute())
      topFifteenDegrees.take(1).foreach(print(_))
      print(", "+fifteenVerticesDegrees(i)._1)
      println()
    }*/
    
    
/*    //test1: 输出节点个数统计结果
    println("*************输出总节点个数:************")
    val allVertices = graph.vertices.count
    println(allVertices)*/
    
/*
    //test2: 输出入度最大的若干个点
    println("**********top 2500 inDegrees:************")
    //degree3.sortByKey(true, 1).top(15).foreach(x=>print(x._2,x._1))
    val thousandVerticesInDegrees = sortVerticesInDegrees.top(2500)
    //fifteenVerticesInDegrees.foreach(x=>println(x._2,x._1))
    val inDegreesLength = thousandVerticesInDegrees.length
    for (i <- 0 to inDegreesLength - 1) {
      println(thousandVerticesInDegrees(i)._1 + ",1")
      //fifteenVerticesInDegrees.foreach(println(x._1))
    }
    
    //输出入度为1-700点的数量
    println("输出入度为1-700点的数量")
    for (i <- 1 to 700) {
      val inDegreeCount = degree3.filter{ _._1 == i}
      println(i + ","+ inDegreeCount.count)
    }

    
    //test3: 输出出度最大的若干个点
    println("**********top 1000 OutDegrees:************")   
    val thousandVerticesOutDegrees = sortVerticesOutDegrees.top(1000)
    val outDegreesLength = thousandVerticesOutDegrees.length
    for (i <- 0 to outDegreesLength - 1) {
      println(thousandVerticesOutDegrees(i)._1 + ",1")
      //fifteenVerticesInDegrees.foreach(println(x._1))
    }
    
    //输出出度为1-100点的数量
    println("输出出度为1-100点的数量")
    for (i <- 1 to 100) {
      val outDegreeCount = degree4.filter{ _._1 == i}
      println(i + ","+ outDegreeCount.count)
    }

    
    //test4: 输出度最大的若干个点
    //交换键值对，这样可以直接对degrees进行.top().sortByKey()等的处理
    println("**********top 2500 degrees:***********\n")   
    val thousandVerticesDegrees = sortVerticesDegrees.top(2500)
    //thousandVerticesDegrees.foreach(println(_))
    val degreesLength = thousandVerticesDegrees.length
    for (i <- 0 to degreesLength - 1) {
      println(thousandVerticesDegrees(i)._1 + ",1")
      //fifteenVerticesDegrees.foreach(println(x._1))
    }
    
    //输出度为1-700点的数量
    println("输出度为1-700点的数量")
    for (i <- 1 to 700) {
      val degreeCount = degree2.filter{ _._1 == i}
      println(i + ","+ degreeCount.count)
    }*/
    
    
    
    //test5: triangle count
    //val edgePrint = edgeTest.map().collect.foreach(println(_))
/*    val edgeTest = Graph(graph.vertices, graph.edges.map(e=>
                   if (e.srcId<e.dstId) e else new Edge(e.dstId, e.srcId, e.attr))).partitionBy(PartitionStrategy.RandomVertexCut).
                     triangleCount().vertices.filter(_._2 > 0).map(a=>(a._2,a._1)).sortByKey(true, 1)
    val targetEdge = edgeTest.top(250)
    val lengthEdge = targetEdge.length - 1
    for (i <- 0 to lengthEdge) {
      val printEdge = graph.vertices.filter{ _._1 == targetEdge(i)._2 }.map(m=>m._2.asInstanceOf[VertexAttribute].getAttribute() + " " + targetEdge(i)._1)
      printEdge.take(1).foreach(println(_))
    }*/

    //val now2 = new Date()
    //val time2 = now2.getTime
    //println("***************It takes: "+(time2-time1)+" ms*********************")
    
/*    //test6: pagerank算法
   	println("***************The Top150 Page Rank Vertices******************")
    val pageRankGraph = graph.pageRank(0.0001).vertices
    val highestPageRankVertices = pageRankGraph.map(a=>(a._2,a._1)).sortByKey(true, 1).top(150)
    val highestPageRankVerticesLength = highestPageRankVertices.length
    for (i <- 0 to highestPageRankVerticesLength - 1) {
      val topPageRankVertice = graph.vertices.filter{ _._1 == highestPageRankVertices(i)._2 }.map(m=>m._2.asInstanceOf[VertexAttribute].getAttribute() + " " + highestPageRankVertices(i)._1)
      topPageRankVertice.take(1).foreach(println(_))
    }
    
    */
    
/*    //test7: 输出连通分量(connected component)graphx的输出格式是:一个连通分量中最小的id,然后输出这个连通分量的所有id
    //我们的目的是输出一个数据集中有多少连通分量来观测一个大致的结果
    println("***************The Number of Connected Components In This Dataset*******************")
    val connectedComponent = graph.connectedComponents().vertices
    //val countRes = connectedComponent.map(_.swap).groupByKey.map(_._2).collect
    val countRes = connectedComponent.map(_.swap).groupByKey.map(_._2).collect.sortBy(_.size)
    val countResLength = countRes.length
    println(countResLength)
    println("***************The Top100 Size of Connected Components*****************")
    for (i <- countResLength - 10 to countResLength - 1) {
      val countResSize = countRes(i).size
      println(countResSize)
    }
    println("---------------show time---------------------")
    //countRes.foreach(println(_))
    val k = countRes(countResLength-1)
    val a = k.toArray
    println(a.length)
    a.foreach(println(_))
    for (i <- 0 to a.length-1) {
      val subgraph1 = graph.subgraph(vpred=(id, attr)=>id == a(i))
    }
*/
    
/*    val cc = clusteringCoefficient(graph)
    println("*********Global Clustering Coefficient*********")
    println(cc)*/
    //test8: 求出global clustering coefficient
/*    println("***********global clustering coefficient***********")
    val numTriplets =
      graph.aggregateMessages[Set[VertexId]](
      et => { et.sendToSrc(Set(et.dstId));
              et.sendToDst(Set(et.srcId)) },
      (a,b) => a ++ b) // #A
    .map(x => {val s = (x._2 - x._1).size.toLong; s*(s-1) / 2})
    .reduce(_ + _)
    println("***Number of numTriplets***")
    println(numTriplets)
    println("***Number of Triangles***")
    val triangleNum = Graph(graph.vertices, graph.edges.map(e=>
                   if (e.srcId<e.dstId) e else new Edge(e.dstId, e.srcId, e.attr))).partitionBy(PartitionStrategy.RandomVertexCut).
                     triangleCount().vertices.map(_._2).reduce(_ + _)
    println(triangleNum)
    println("***Final Result***")
    if (numTriplets == 0)  println("0.0") else
      println(triangleNum / numTriplets.toFloat)*/
        
      
/*        //test8: 求出global clustering coefficient
    println("***********global clustering coefficient***********")
    val numTriplets =
      graph.aggregateMessages[Set[VertexId]](
      et => { et.sendToSrc(Set(et.dstId));
              et.sendToDst(Set(et.srcId)) },
      (a,b) => a ++ b) // #A
    .map(x => {val s = (x._2 - x._1).size; s*(s-1) / 2})
    .reduce(_ + _)
    println(numTriplets)
    if (numTriplets == 0)  println("0.0") else
      println(graph.triangleCount.vertices.map(_._2).reduce(_ + _))
      //  / numTriplets.toFloat
*/    
/*  
    //test9:平均度
    val numberOfVertice = graph.vertices.count.toDouble
    val sumOfDegree = graph.degrees.map(x=>x._2.toLong).reduce(_+_)
    val sumOfIndegree = graph.inDegrees.map(x=>x._2.toLong).reduce(_+_)
    val sumOfOutdegree = graph.outDegrees.map(x=>x._2.toLong).reduce(_+_)
    val meanDegree = sumOfDegree / numberOfVertice
    val meanIndegree = sumOfIndegree / numberOfVertice
    val meanOutdegree = sumOfOutdegree / numberOfVertice
    println("----------------Mean Degree--------------------")
    println(meanDegree)
    println("----------------Mean In-Degree--------------------")
    println(meanIndegree)
    println("----------------Mean Out-Degree--------------------")
    println(meanOutdegree)*/
    
    //println("-------------------The program is finished-------------------")
    
  }
  /**
   * Build the results out of the graph from the graph model
   */
  def projectResults(vars: java.util.Set[String], res: RDD[SolutionMapping]): RDD[SolutionMapping] = {
    // Broadcast all Sparql triples to all workers
    val broadcastVars: Broadcast[java.util.Set[String]] = context.broadcast(vars)
    val result = projection(broadcastVars, res)
    broadcastVars.unpersist(true)
    result
  }

  /**
   * Filter the result
   */
  def filter(res: RDD[SolutionMapping], expr: java.util.Set[IExpression]): RDD[SolutionMapping] = {
    val broadcastExprs: Broadcast[java.util.Set[IExpression]] = context.broadcast(expr)
    val result = filterResult(res, broadcastExprs)
    broadcastExprs.unpersist(true)
    result
  }

  /**
   * Union two RDDs
   */
  def union(left: RDD[SolutionMapping], right: RDD[SolutionMapping]): RDD[SolutionMapping] = {
    if (left == null && right == null) {
      return null
    } else if (left != null && right == null) {
      return left
    } else if (left == null && right != null) {
      return right
    }

    val result = left.union(right).persist(Const.STORAGE_LEVEL)
    left.unpersist(true)
    right.unpersist(true);
    result
  }

  /**
   * Close the Spark context
   */
  def closeContext() {
    context.stop()
  }

  /**
   * Print the RDD to the console
   */
  def printRDD(rdd: RDD[SolutionMapping]) {
    if (rdd != null) {
      rdd.collect.foreach(println)
    }
  }

  /**
   * Count the RDD
   */
  def getRDDCount(rdd: RDD[SolutionMapping]): Long = {
    if (rdd != null) {
      rdd.count
    } else {
      return 0
    }

  }

  /**
   * Save the result to the HDFS file
   */
  def saveResultToFile(rdd: RDD[SolutionMapping]) {
    if (rdd != null) {
      rdd.saveAsTextFile(Const.outputFilePath);
    }
  }

  /**
   * Order the result
   */
  def order(result: RDD[SolutionMapping], variable: String, asc: Boolean): RDD[SolutionMapping] = {
    orderBy(result, variable, asc)
  }

  /**
   * Limit and offset the result
   */
  def limitOffset(result: RDD[SolutionMapping], limit: Int, offset: Int): RDD[SolutionMapping] = {
    if (result == null) {
      return null
    }

    /*
     * Correction if the result is smaller than the limit if the offset is 
     * taken away from the result
     */
    var copyLength = 0

    val resultCount = result.count
    if (resultCount <= limit + offset) {
      /*
       * Calculate how big the part is that is left when the offset is
       * subtracted
       */
      copyLength = (resultCount - offset).toInt
      if (copyLength < 0) {
        // Slice bigger than result
        return null
      }
    } else {
      copyLength = limit
    }

    val limited = result.take(limit + offset)
    val newOffset = new Array[SolutionMapping](copyLength)
    Array.copy(limited, offset, newOffset, 0, copyLength)
    result.unpersist(true)

    val resultRDD = context.parallelize(newOffset).persist(Const.STORAGE_LEVEL)
    resultRDD
  }

  /**
   * Limit the result
   */
  def limit(result: RDD[SolutionMapping], limit: Int): RDD[SolutionMapping] = {
    if (result == null) {
      return null
    }

    val limited = result.take(limit)
    result.unpersist(true)

    val resultRDD = context.parallelize(limited).persist(Const.STORAGE_LEVEL)
    resultRDD
  }

  /**
   * Delete double values in the result
   */
  def distinct(result: RDD[SolutionMapping]): RDD[SolutionMapping] = {
    if (result == null) {
      return null
    }

    result.distinct
  }

  /**
   * Execute a left join
   */
  def optional(joinVars: java.util.List[String], left: RDD[SolutionMapping], right: RDD[SolutionMapping]): RDD[SolutionMapping] = {
    val broadcastVars: Broadcast[java.util.List[String]] = context.broadcast(joinVars)
    val result = joinLeft(broadcastVars, left, right)
    broadcastVars.unpersist(true)
    result
  }

}