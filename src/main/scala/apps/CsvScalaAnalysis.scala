
package apps
import csv.CsvSet
import csv.CsvImport
import java.util.HashSet
import scala.collection.JavaConversions._
import csv.CsvTrace

class CsvScalaSet(val set: CsvSet) {
  def getName: String =
    set.getName()

  def size: Int =
    set.size()

  def idOrder: List[Integer] =
    set.idOrder().toList

  def getEntangledPartitions: List[List[Integer]] = {
    val partitions = set.getEntangledPartitions().toSet
    CsvScalaSet.convertPartitionSet(partitions.map(partition => partition.toSet))
  }

  def getValues(partition: Set[Integer]): Set[Map[Integer, String]] = {
    val values = set.getValues(partition).toSet
    values.map(value => value.toMap)
  }

  def getTraces: Set[Map[Integer, String]] = {
    val traces = set.getTraces().toSet
    traces.map(trace => trace.toMap)
  }

  def getTrace(mapping: Map[Integer, String]): CsvTrace = {
    set.getTrace(mapping)
  }

  def getSupports(subpartitioning: Set[Set[Integer]]): Set[CsvScalaSet] = {
    val converted = subpartitioning.map(partition => new java.util.HashSet[Integer](partition))
    val supports = set.getSupports(new java.util.HashSet[java.util.Set[Integer]](converted))
    supports.map(support => new CsvScalaSet(support)).toSet
  }
  
  
  override def equals(o: Any): Boolean = {
    o match {
      case s: CsvScalaSet => set.equals(s.set)
      case _ => false
    }
  }
  
  override def hashCode(): Int = {
    0
  }
}


object CsvScalaSet {
  def convertPartitionSet(partitions: Set[Set[Integer]]): List[List[Integer]] = {
    val partitionList = partitions.map(partition => partition.toList.sortWith((x,y) => x < y)).toList
    partitionList.sortWith((x,y) => x(0) < y(0))
  }
  
  def openGui(sets: List[CsvScalaSet]) = {
    val convertedSets = sets.map(set => set.set)
    CsvViewerGui.openGui(convertedSets)
  }
}

class CsvScalaAnalysis(traces: List[CsvTrace]) {
  def this(filePath: String) {
	this((new CsvImport(filePath)).traces.toList);  
  }

  val set = new CsvSet(new java.util.HashSet(traces));
  val original = new CsvScalaSet(set);
  val keys = original.idOrder;
      
  class Supports(val requested: List[List[Integer]], val supports: List[CsvScalaSet]) {
    val numSupports = supports.size
    val supportInfo = supports.map(support => new Support(support))
  }
  
  class Support(val support: CsvScalaSet) {
      val partitions = support.getEntangledPartitions
      val size = support.size;
      val crossProduct = partitions.map(partition => support.getValues(partition.toSet))
      val crossProductReadable = crossProduct.map(p => p.map(values => {
        val sortedKeys = values.keys.toList.sortWith((x, y) => x < y)
        sortedKeys.map(k => values(k)).mkString("")
      }))
      val crossProductSizes = crossProduct.map(values => values.size)
  }
  
  def splitToPartition(split: List[Int]): Set[Set[Integer]] = {
      val partitions = (0 :: split.toList).zip(split).foldRight(Set[Set[Integer]]())((bound, partial) =>
        partial + (bound._1 until bound._2).toList.map(k => k:Integer).toSet
      )
      partitions.filter(seq => !seq.isEmpty)
  }

  def subsets(L: Int, k: Int) =
    1 to L combinations k

  def getSplitInfo(numSplits: Int): List[Supports] = {
    val splits = subsets(this.keys.size, numSplits)
    val partitionings = splits.map(split => splitToPartition(split.toList))

    partitionings.toList.map(getSupport)
  }

  def getWindowInfo(minSize: Int, maxSize: Int): List[Supports] = {
    val splits = subsets(this.keys.size, 2)
    val windows = splits.filter(split => {
      val diff = split(1) - split(0)
      diff >= minSize && diff <= maxSize
    })

    val partitionings = windows.map(split => {
      (0 :: split.toList).zip(split).foldRight(Set[Set[Integer]]())((bound, partial) =>
        partial + (bound._1 until bound._2).toList.map(k => k: Integer).toSet)
    })

    partitionings.toList.map(getSupport)
  }
  
  def getSupport(partitioning: Set[Set[Integer]]): Supports = {
    new Supports(CsvScalaSet.convertPartitionSet(partitioning), original.getSupports(partitioning).toList)
  }
  
  def getSupport(partitioning: Set[Set[Integer]], goodTrace: CsvTrace): Supports = {
    val supports = getSupport(partitioning).supportInfo 
    new Supports(null, supports.filter(s => s.support.set.traceData.contains(goodTrace)).map(f => f.support))
  }
  
  def getSupport(split: List[Int]): Supports = {
    getSupport(splitToPartition(split)) 
  }
  
  def getSupport(split: List[Int], goodTrace: CsvTrace): Supports = {
    getSupport(splitToPartition(split), goodTrace) 
  }
  
  def findMinimalSupports(supports: Supports): List[Int] = {
    val original = this.original
    val originalSize = original.size
    val numSupports = supports.numSupports
    val s = supports.supports;
    
    val sorted = s.sortWith((x, y) => x.size > y.size)
    val minIndex = sorted.scanRight(0)((set, sum) => set.size + sum).indexWhere(sum => sum >= originalSize)
    
    for (i <-minIndex to numSupports) {
      val subsets = 0 until numSupports combinations i
      for (subsetIndices <- subsets) {
        val subsets = subsetIndices.map(index => s(index))
        val totalSize = subsets.map(s => s.size).sum
        println(subsetIndices + ":" + totalSize)
        
        if (totalSize >= originalSize) {
          val traces = subsets.flatMap(subset => subset.set.traceData.toSet).distinct

          if (traces.length >= originalSize) {
            println("found: " + traces.length)
            return subsetIndices.toList
          }
        }
      }
    }
    return List()
  }
}

object CsvScalaAnalysis {
  def main(args: Array[String]) {
    val csv = new CsvScalaAnalysis("SODA.csv");
  }
}