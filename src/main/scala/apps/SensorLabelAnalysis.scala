package apps

import scala.collection.JavaConversions._
import scala.util.Random
import csv.CsvTrace

class SensorLabelAnalysis(csvFile: String, labelFile: String) {

  val csv = new CsvScalaAnalysis(csvFile)
  val scalaSet = csv.original
  val traces = scalaSet.set.traceData.toList

  val sensorLabels = new SensorLabels(labelFile)
  
  def createHistogram[A](mapping: List[A]): Map[A, Int] = {
    val distinct = mapping.distinct
    distinct.map(value => (value, mapping.count(f => f == value))).toMap
  }
  
  def getSupportData() =
    traces.map(trace => {
      val string = trace.stringConcat
      val token = sensorLabels.mapping(string)
      val origSplits = token.tokenStartsList

      val supports = csv.getSupport(token.tokenStartsList, trace)
      val actualSplitCounts = supports.supports.map(support => {
        val actualSplits = support.set.traceData.toList.map(csvTrace => sensorLabels.mapping(csvTrace.stringConcat).tokenStartsList)
        createHistogram(actualSplits)
      })
      println(string)
      println(origSplits)
      println(actualSplitCounts)
      (string, (origSplits, actualSplitCounts))
    })
    
  def classifyTraces() = {
    var remainingTraces = traces
    var traceTokenization = Map[CsvTrace, List[List[Int]]]()
    var iterations = 0
    var numViewed = 0
    
    while (!remainingTraces.isEmpty) {
      iterations += 1
      
      println("Remaining:" + remainingTraces.size + ":" + traceTokenization.size)

      val matches = traceTokenization.keys.toList.map(trace => {
        val splits = sensorLabels.mapping(trace.stringConcat).tokenStartsList 
        traceTokenization(trace).contains(splits)
      })
      
      printTokenizationQuality(traceTokenization)
      
      val randomTrace = remainingTraces.get(Random.nextInt(remainingTraces.size))
      val string = randomTrace.stringConcat
      val token = sensorLabels.mapping(string)
      val origSplits = token.tokenStartsList
      val supports = csv.getSupport(token.tokenStartsList, randomTrace)
      
      println("Iteration:" + iterations)
      println("Trace:" + string)
      println("Splits:" + origSplits)
      println("Supports:" + supports.supports.map(s => s.size))
            
      supports.supports.foreach(support => {
        support.set.traceData.toList.foreach(trace => {
          if (!traceTokenization.contains(trace)) {
            traceTokenization = traceTokenization + (trace -> List(origSplits))
            remainingTraces =  remainingTraces.diff(List(trace))
          } else {
            val currentTokenization = traceTokenization(trace)
            if (!currentTokenization.contains(origSplits)) {
              val newMapping = trace -> (currentTokenization :+ origSplits)
              traceTokenization = traceTokenization + newMapping
            }
          }  
        })
      })
    }
 
    printTokenizationQuality(traceTokenization)
    
    (iterations, traceTokenization)
  }
  
  def classifyTracesWithRemoval() = {
    var remainingTraces = traces
    var traceTokenization = Map[CsvTrace, List[List[Int]]]()
    var iterations = 0
    var numViewed = List[Set[Int]]()
    
    while (!remainingTraces.isEmpty) {
      iterations += 1
      
      println("Remaining:" + remainingTraces.size + ":" + traceTokenization.size)

      val matches = traceTokenization.keys.toList.map(trace => {
        val splits = sensorLabels.mapping(trace.stringConcat).tokenStartsList 
        traceTokenization(trace).contains(splits)
      })
      
      printTokenizationQuality(traceTokenization)
      
      val randomTrace = remainingTraces.get(Random.nextInt(remainingTraces.size))
      val string = randomTrace.stringConcat
      val token = sensorLabels.mapping(string)
      val origSplits = token.tokenStartsList
      
      val csvAnalysis = new CsvScalaAnalysis(remainingTraces)
      val supports = csvAnalysis.getSupport(token.tokenStartsList, randomTrace)
      
      println("Iteration:" + iterations)
      println("Trace:" + string)
      println("Splits:" + origSplits)
      println("Supports:" + supports.supports.map(s => s.size))
            
      supports.supports.foreach(support => {
        val subTraceCounts = csvAnalysis.splitToPartition(token.tokenStartsList).map(p => {
          val v = support.getValues(p);
          v.size
        })
        numViewed = numViewed :+ subTraceCounts
        
        support.set.traceData.toList.foreach(trace => {
          if (!traceTokenization.contains(trace)) {
            traceTokenization = traceTokenization + (trace -> List(origSplits))
            remainingTraces =  remainingTraces.diff(List(trace))
          } else {
            val currentTokenization = traceTokenization(trace)
            if (!currentTokenization.contains(origSplits)) {
              val newMapping = trace -> (currentTokenization :+ origSplits)
              traceTokenization = traceTokenization + newMapping
            }
          }  
        })
      })
    }
 
    printTokenizationQuality(traceTokenization)
    
    (iterations, numViewed, traceTokenization)
  }
  
  def printTokenizationQuality(traceTokenization: Map[CsvTrace, List[List[Int]]]) {
    val matchesAny = traceTokenization.keys.toList.map(trace => {
      val splits = sensorLabels.mapping(trace.stringConcat).tokenStartsList
      traceTokenization(trace).contains(splits)
    })
    println("Matches:" + createHistogram(matchesAny))
    
    val matchesFirst = traceTokenization.keys.toList.map(trace => {
      val splits = sensorLabels.mapping(trace.stringConcat).tokenStartsList
      traceTokenization(trace)(0) == splits
    })
    println("Matches First:" + createHistogram(matchesFirst))
    

    val softMatches = traceTokenization.keys.toList.map(trace => {
      val string = trace.stringConcat
      val splits = sensorLabels.mapping(string).tokenStartsList
      
      val softTokenizations = traceTokenization(trace).map(t => t.map(index => string.indexWhere(p => p != '_', index)))
      softTokenizations.contains(splits)
    })
    println("Soft Matches:" + createHistogram(softMatches))
    
    val softMatchesFirst = traceTokenization.keys.toList.map(trace => {
      val string = trace.stringConcat
      val splits = sensorLabels.mapping(string).tokenStartsList
      
      val softTokenizations = traceTokenization(trace).map(t => t.map(index => string.indexWhere(p => p != '_', index)))
      softTokenizations(0) == splits
    })
    println("Soft Matches First:" + createHistogram(softMatchesFirst))
  }
  
  def getTokenizationQualityNumbers(traceTokenization: Map[CsvTrace, List[List[Int]]]) = {
    val matchesAny = traceTokenization.keys.toList.map(trace => {
      val splits = sensorLabels.mapping(trace.stringConcat).tokenStartsList
      traceTokenization(trace).contains(splits)
    })
    
    val matchesFirst = traceTokenization.keys.toList.map(trace => {
      val splits = sensorLabels.mapping(trace.stringConcat).tokenStartsList
      traceTokenization(trace)(0) == splits
    })

    val softMatches = traceTokenization.keys.toList.map(trace => {
      val string = trace.stringConcat
      val splits = sensorLabels.mapping(string).tokenStartsList
      
      val softTokenizations = traceTokenization(trace).map(t => t.map(index => string.indexWhere(p => p != '_', index)))
      softTokenizations.contains(splits)
    })
    
    val softMatchesFirst = traceTokenization.keys.toList.map(trace => {
      val string = trace.stringConcat
      val splits = sensorLabels.mapping(string).tokenStartsList
      
      val softTokenizations = traceTokenization(trace).map(t => t.map(index => string.indexWhere(p => p != '_', index)))
      softTokenizations(0) == splits
    })
    
    (createHistogram(matchesAny)(true), createHistogram(matchesFirst)(true), 
        createHistogram(softMatches)(true), createHistogram(softMatchesFirst)(true))
    
  }
}

object SensorLabelAnalysis {
  val s = new SensorLabelAnalysis("/Users/sbarman/Projects/trace-analysis/entanglement/SODA.csv", 
      "/Users/sbarman/Projects/trace-analysis/entanglement/SODA-GROUND-TRUTH")
}