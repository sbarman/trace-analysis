package apps



object sensor {
  import apps.CsvScalaAnalysis
  import apps.SensorLabels
  import scala.collection.JavaConversions._
  import scala.util.Random
	
	val csv = new CsvScalaAnalysis("/Users/sbarman/Projects/trace-analysis/entanglement/SODA.csv")
	val scalaSet = csv.original
	val traces = scalaSet.set.traceData.toList
	
		val randomTrace = traces(Random.nextInt(traces.size))
	  val string = randomTrace.stringConcat
	  val sensorLabels = new SensorLabels("/Users/sbarman/Projects/trace-analysis/entanglement/SODA-GROUND-TRUTH")
	  val token = sensorLabels.mapping(string)
	  val origSplits = token.tokenStartsList
	  
	  val supports = csv.getSupport(token.tokenStartsList, randomTrace)
	  for (support <- supports.supports) {
	    val actualSplits = support.set.traceData.toList.map(csvTrace => sensorLabels.mapping(csvTrace.stringConcat).tokenStartsList)
	    val distinct = actualSplits.distinct
	    val distinctCount = distinct.map(split => (split, actualSplits.count(f => f == split))).toMap
	    println(distinctCount)
	  }
	  println(origSplits)
	  
  import apps.SensorLabelAnalysis
val s = new SensorLabelAnalysis("/Users/sbarman/Projects/modular-oracles/code/trace-analysis/entanglement/SODA.csv",
"/Users/sbarman/Projects/modular-oracles/code/trace-analysis/entanglement/SODA-GROUND-TRUTH")
s.classifyTraces()
}