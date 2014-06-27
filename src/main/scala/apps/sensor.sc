package apps



object sensor {
  import apps.CsvScalaAnalysis
  import apps.SensorLabels
  import scala.collection.JavaConversions._
  import scala.util.Random
	
	val csv = new CsvScalaAnalysis("/Users/sbarman/Projects/trace-analysis/entanglement/SODA.csv")
                                                  //> csv  : apps.CsvScalaAnalysis = apps.CsvScalaAnalysis@2a265ea9
	val scalaSet = csv.original               //> scalaSet  : apps.CsvScalaSet = apps.CsvScalaSet@0
	val traces = scalaSet.set.traceData.toList//> traces  : List[csv.CsvTrace] = List(csv.CsvTrace@305fd85d, csv.CsvTrace@544f
                                                  //| e44c, csv.CsvTrace@3632be31, csv.CsvTrace@4206a205, csv.CsvTrace@64a294a6, c
                                                  //| sv.CsvTrace@1a052a00, csv.CsvTrace@12c8a2c0, csv.CsvTrace@150c158, csv.CsvTr
                                                  //| ace@17d677df, csv.CsvTrace@2f333739, csv.CsvTrace@14899482, csv.CsvTrace@47f
                                                  //| 6473, csv.CsvTrace@3234e239, csv.CsvTrace@6659c656, csv.CsvTrace@23ceabc1, c
                                                  //| sv.CsvTrace@490ab905, csv.CsvTrace@3dd3bcd, csv.CsvTrace@2f490758, csv.CsvTr
                                                  //| ace@1e965684, csv.CsvTrace@22927a81, csv.CsvTrace@1c6b6478, csv.CsvTrace@1a1
                                                  //| d6a08, csv.CsvTrace@58fdd99, csv.CsvTrace@63753b6d, csv.CsvTrace@6cc7b4de, c
                                                  //| sv.CsvTrace@25af5db5, csv.CsvTrace@5f71c76a, csv.CsvTrace@6ed3ef1, csv.CsvTr
                                                  //| ace@46238e3f, csv.CsvTrace@401e7803, csv.CsvTrace@47fd17e3, csv.CsvTrace@3d2
                                                  //| 4753a, csv.CsvTrace@5e3a8624, csv.CsvTrace@3d71d552, csv.CsvTrace@2a18f23c, 
                                                  //| csv.CsvTrace@edf4efb, csv.CsvTrace@233c0b17, csv.CsvTrace@d8355a8, csv.CsvTr
                                                  //| ace@2d8f65a4, csv.CsvTra
                                                  //| Output exceeds cutoff limit.
	
		val randomTrace = traces(Random.nextInt(traces.size))
                                                  //> randomTrace  : csv.CsvTrace = csv.CsvTrace@1a1d6a08
	  val string = randomTrace.stringConcat   //> string  : String = SODA1R682__AGN
	  val sensorLabels = new SensorLabels("/Users/sbarman/Projects/trace-analysis/entanglement/SODA-GROUND-TRUTH")
                                                  //> sensorLabels  : apps.SensorLabels = apps.SensorLabels@1cd629b3
	  val token = sensorLabels.mapping(string)//> token  : apps.SensorTokens = apps.SensorTokens@589da3f3
	  val origSplits = token.tokenStartsList  //> origSplits  : List[Int] = List(0, 3, 4, 5, 6, 11)
	  
	  val supports = csv.getSupport(token.tokenStartsList, randomTrace)
                                                  //> java.lang.UnsatisfiedLinkError: Could not load the library libminisat.dylib 
                                                  //| or any of its variants.
                                                  //| 	at kodkod.engine.satlab.NativeSolver.loadLibrary(NativeSolver.java:75)
                                                  //| 	at kodkod.engine.satlab.MiniSat.<clinit>(MiniSat.java:39)
                                                  //| 	at kodkod.engine.satlab.SATFactory$3.instance(SATFactory.java:75)
                                                  //| 	at kodkod.engine.fol2sat.Bool2CNFTranslator.translate(Bool2CNFTranslator
                                                  //| .java:67)
                                                  //| 	at kodkod.engine.fol2sat.Translator.toCNF(Translator.java:592)
                                                  //| 	at kodkod.engine.fol2sat.Translator.toBoolean(Translator.java:561)
                                                  //| 	at kodkod.engine.fol2sat.Translator.translate(Translator.java:412)
                                                  //| 	at kodkod.engine.fol2sat.Translator.translate(Translator.java:130)
                                                  //| 	at kodkod.engine.Solver$SolutionIterator.<init>(Solver.java:266)
                                                  //| 	at kodkod.engine.Solver.solveAll(Solver.java:179)
                                                  //| 	at entanglement.MaxBicliqueEnumerator$2.<init>(MaxBicliqueEnumerator.jav
                                                  //| a:194)
                                                  //| 	at entanglement.MaxBicliqueEnumerator.maxSupports(MaxBicliqueEnumerator.
                                                  //| java:193)
                                                  //| Output exceeds cutoff limit.
	  for (support <- supports.supports) {
	    val actualSplits = support.set.traceData.toList.map(csvTrace => sensorLabels.mapping(csvTrace.stringConcat).tokenStartsList)
	    val distinct = actualSplits.distinct
	    val distinctCount = distinct.map(split => (split, actualSplits.count(f => f == split))).toMap
	    println(distinctCount)
	  }
	  println(origSplits)
	  
  import apps.SensorLabelAnalysis
val s = new SensorLabelAnalysis("/Users/sbarman/Projects/trace-analysis/entanglement/SODA.csv",
"/Users/sbarman/Projects/trace-analysis/entanglement/SODA-GROUND-TRUTH")
s.classifyTraces()
}